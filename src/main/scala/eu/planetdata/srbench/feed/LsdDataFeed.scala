package eu.planetdata.srbench.feed
import es.upm.fi.oeg.morph.db.Rdb
import java.util.Properties
import es.upm.fi.oeg.morph.esper.EsperProxy
import concurrent.duration._
import es.upm.fi.oeg.morph.esper.Event
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Queue
import java.sql.Timestamp
import scala.compat.Platform
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import scala.language.postfixOps

class LsdDataFeed(props:Properties,val proxy:EsperProxy) {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val rate=props.getProperty("feed.rate").toLong
  private val interval=5
  private val window=30
  private val dateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZ")
  private val startTime=dateFormat.parse(props.getProperty("feed.starttime"))
  private val attributes=props.getProperty("feed.attnames").split(',')
  private val dbattributes=Array("samplingtime","st.stationid","airtemperature","relativehumidity","precipitation","to_char(samplingtime, 'YYYY_MM_DD_HH12_MI_SS')","st.code")
  val projatts=dbattributes.zip(attributes).map(a=>a._1+" AS "+a._2).mkString(",")
  private val conditions=props.getProperty("feed.conditions").split(',').mkString(" and ")
  
  def getData(date:Date)={
    val db=new Rdb(props)
    val dateStr=dateFormat.format(date)
    val dateEnd=new DateTime(date).plusMinutes(window).toDate
    val datefin=dateFormat.format(dateEnd)
    val init=Platform.currentTime
    logger.debug("Date: "+dateStr)
    //'2004-08-08 06:00:00+02'
    val (res,con)=db.query("select "+projatts+" from observation obs, station st where st.stationid=obs.stationid and samplingtime>='"+dateStr+"' and samplingtime<'"+datefin+"' and "+conditions,
    		attributes)
    val grouped=res.groupBy(a=>a(0)).map{v=>
      val key=dateFormat.format(v._1.asInstanceOf[Timestamp])
      (key,v._2.map(dt=>attributes.zip(dt).toMap[String,Any]).toSeq  )}
    con.close
    val sorted=grouped.toSeq.sortBy(a=>a._1)
    logger.debug("elapsed "+(Platform.currentTime-init))        
    //grouped.toSeq.reverse
    sorted
  }
  
  class DataIterator extends Iterator[(String,Seq[EsperEvent])]{
    val start=startTime
    val cache=new Queue[(String,Seq[EsperEvent])]
    cache++=getData(start)
    //println("keys init "+cache.map(i=>i._1).mkString)
    
    override def hasNext= !cache.isEmpty
    override def next={            
      val head=try cache.dequeue
      catch {case e:NoSuchElementException=>null}
      if (cache.size<=1 && !proxy.system.isTerminated && !cache.isEmpty ){
        logger.debug("Load the cache")
        import proxy.system.dispatcher
        try proxy.system.scheduler.scheduleOnce(0 seconds){          
          val lastDate=new DateTime(dateFormat.parse(cache.last._1))
          val newDate=lastDate.plusMinutes(interval)
          //println("last date "+newDate)
          val gedata=getData(newDate.toDate)
          //println("keys just before "+gedata.map(i=>i._1).mkString)

          cache++=gedata
          //println("keys now "+cache.map(i=>i._1).mkString)

        }
        catch {case e:Exception=>null}
      }
      head
    }
  }
  
  type EsperEvent=Map[String,Any]
  
  def schedule{
    val eng=proxy.engine
    val datas:Iterator[(String,Seq[EsperEvent])]=new DataIterator
    lazy val init =Platform.currentTime
    import proxy.system.dispatcher
    proxy.system.scheduler.schedule(0 seconds, rate milliseconds){
      val dnext=datas.next
      if (dnext!=null){
        logger.trace((Platform.currentTime-init)+" sending "+dnext._1)
      
        val data:Seq[EsperEvent]=dnext._2
        data.foreach{d=>
          eng ! Event("lsd_observations",d)
        }
      }
    }      
  }
 
}
