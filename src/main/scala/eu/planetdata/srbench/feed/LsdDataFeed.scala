package eu.planetdata.srbench.feed
import es.upm.fi.oeg.morph.db.Rdb
import java.util.Properties
import es.upm.fi.oeg.morph.esper.EsperProxy
import akka.util.duration._
import es.upm.fi.oeg.morph.esper.Event
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Queue
import org.scala_tools.time.Imports.DateTime
import org.scala_tools.time.DateImplicits
import java.sql.Timestamp
import scala.compat.Platform

class LsdDataFeed(props:Properties,val proxy:EsperProxy) {
  private val interval=5
  private val window=30
  private val dateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZ")
  private val attributes=Array("observationTime","stationId","temperature","relativeHumidity")
  private val dbattributes=Array("samplingtime","stationid","airtemperature","relativehumidity")
  val projatts=dbattributes.zip(attributes).map(a=>a._1+" AS "+a._2).mkString(",")

  def getData(date:Date)={
    val db=new Rdb(props)
    val dateStr=dateFormat.format(date)
    val cal = Calendar.getInstance
    cal.setTime(date)
    cal.add(Calendar.MINUTE, window)    
    val datefin=dateFormat.format(cal.getTime)
    val init=Platform.currentTime
    println("ate: "+dateStr)
    //'2004-08-08 06:00:00+02'
    val (res,con)=db.query("select "+projatts+" from observation where samplingtime>='"+dateStr+"' and samplingtime<'"+datefin+"'",
    		attributes)
    val grouped=res.groupBy(a=>a(0)).map{v=>
      val key=dateFormat.format(v._1.asInstanceOf[Timestamp])
      (key,v._2.map(dt=>attributes.zip(dt).toMap[String,Any]).toSeq  )}
    con.close
    val sorted=grouped.toSeq.sortBy(a=>a._1)
    println("elapsed "+(Platform.currentTime-init))        
    //grouped.toSeq.reverse
    sorted
  }
  
  class DataIterator extends Iterator[(String,Seq[EsperEvent])]{
    val start=dateFormat.parse("2004-08-08 06:00:00+0200")
    val cache=new Queue[(String,Seq[EsperEvent])]
    cache++=getData(start)
    println("keys init "+cache.map(i=>i._1).mkString)
    
    override def hasNext= !cache.isEmpty
    override def next={            
      val head=cache.dequeue
      if (cache.size<=1 && !proxy.system.isTerminated)
        try proxy.system.scheduler.scheduleOnce(0 seconds){
          val lastDate=new DateTime(dateFormat.parse(cache.last._1))
          val newDate=lastDate.plusMinutes(interval)
          println("last date "+newDate)
          val gedata=getData(newDate.toDate)
          println("keys just before "+gedata.map(i=>i._1).mkString)

          cache++=gedata
          println("keys now "+cache.map(i=>i._1).mkString)

        }
        catch {case e:Exception=>null}
      head
    }
  }
  
  type EsperEvent=Map[String,Any]
  
  def schedule{
    val eng=proxy.engine
    val datas:Iterator[(String,Seq[EsperEvent])]=new DataIterator
    proxy.system.scheduler.schedule(0 seconds, 5 seconds){
      val data:Seq[EsperEvent]=datas.next()._2
      data.foreach{d=>
        eng ! Event("lsd_observations",d)
      }
    }      
  }
 
}
