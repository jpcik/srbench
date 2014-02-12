package eu.planetdata.srbench.feed

import java.text.SimpleDateFormat
import java.sql.Timestamp
import java.util.Date
import scala.compat.Platform
import scala.concurrent.duration._
import collection.mutable.Queue
import collection.JavaConversions._
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import es.upm.fi.oeg.morph.db.Rdb
import es.upm.fi.oeg.morph.esper.EsperProxy
import es.upm.fi.oeg.morph.esper.Event
import org.joda.time.DateTime


class DataFeed(key:String,proxy:EsperProxy) {
  val conf=ConfigFactory.load getConfig(key)  
  private val logger = LoggerFactory.getLogger(this.getClass)
  
  protected val dateFormat=new SimpleDateFormat(conf.getString("feed.dateformat"))
  val rating=conf.getLong("feed.rate")
  val grouping=conf.getLong("feed.grouping")*1000
  val interval=conf.getInt("feed.extractinterval")
  val startTime=dateFormat.parse(conf.getString("feed.starttime"))
  val attributes=conf.getStringList("feed.attnames").toSeq.toArray
  val dbattributes=conf.getStringList("feed.dbattributes").toSeq.toArray
  val streamname=conf.getString("feed.streamname")
  val querytemplate=conf.getString("feed.querytemplate")
  lazy val projatts=dbattributes.zip(attributes).map(a=>a._1+" AS "+a._2).mkString(",")
  var datain,groupsize,totalgroups=0L
  
  type EsperEvent=Map[String,Any]
  
  def schedule{
    val eng=proxy.engine
    val datas:Iterator[(String,Seq[EsperEvent])]=new CrtmDataIterator
    lazy val init =Platform.currentTime
    import proxy.system.dispatcher
    proxy.system.scheduler.schedule(0 seconds, rating milliseconds){
      val dnext=datas.next
      if (dnext!=null){
        if (logger.isTraceEnabled)
          logger.trace((Platform.currentTime-init)+" sendingo "+rating+"-"+dnext._1)
      
        val data:Seq[EsperEvent]=dnext._2
        totalgroups+=1
        groupsize+=data.size
        data.foreach{d=>
          //d.put("internalTime",Platform.currentTime)
          eng ! Event(streamname,d+("sendingTime"->Platform.currentTime))               
        }
      }
    }      
  }

  
  def getData(date:Date)={    
    val db=new Rdb(key)
    val dateStr=dateFormat.format(date)
    val dateEnd=new DateTime(date).plusMinutes(interval).toDate
    val datefin=dateFormat.format(dateEnd)
    val init=Platform.currentTime
    logger.debug("Date: "+dateStr)
    //'2004-08-08 06:00:00+02'
    //val (res1,con1)=db.query("select samplingtime AS observationTime,st.stationid AS stationId,airtemperature AS temperature,relativehumidity AS relativeHumidity,precipitation AS precipitation,st.code AS code from observation obs, station st where st.stationid=obs.stationid",Array("samplingtime"))
//res1.foreach(println)
    val query=querytemplate.replace("%projatts%", projatts).replace("%dateStr%", dateStr).replace("%datefin%", datefin)
    logger.debug("sent query: "+query)
    val (res,con)=db.query(query,attributes)
    logger.debug("query done")

    def divide(t:Object,ini:Long,lapse:Long)={
      (t.asInstanceOf[Timestamp].getTime-ini)/lapse      
    }
    
    val grouped=res.groupBy(a=>divide(a(0),date.getTime,grouping)).map{v=>      
      val key=v._1.toString//dateFormat.format(v._1.asInstanceOf[Timestamp])         
      (key,v._2.map(dt=>attributes.zip(dt).toMap[String,Any]).toSeq  )
    }    
    //logger.debug("grouping done")
    con.close

    
    val sorted=grouped.toSeq.sortBy(a=>a._1.toInt)
    logger.debug("elapsed "+(Platform.currentTime-init))        
    //grouped.toSeq.reverse
    sorted
    //grouped.toSeq
  }  
  
  
  class CrtmDataIterator extends Iterator[(String,Seq[EsperEvent])]{
    val start=startTime
    val cache=new Queue[(String,Seq[EsperEvent])]
    val initial=getData(start)
    cache++=initial
    var lastDate=new DateTime(start)
    //println("keys init "+cache.map(i=>i._1).mkString)
    
    override def hasNext= !cache.isEmpty
    override def next={            
      val head=try cache.dequeue
      catch {case e:NoSuchElementException=>null}
      if (cache.size<=3 && !proxy.system.isTerminated && !cache.isEmpty ){
        logger.debug("Load the cache")
        import proxy.system.dispatcher
        try proxy.system.scheduler.scheduleOnce(0 seconds){          
          val newDate=lastDate.plusMinutes(interval)
          val gedata=initial//getData(newDate.toDate)
          lastDate=newDate
          cache++=gedata

        }
        catch {case e:Exception=>null}
      }
      head
    }
  }

}