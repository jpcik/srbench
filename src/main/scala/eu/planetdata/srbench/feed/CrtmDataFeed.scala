package eu.planetdata.srbench.feed

import es.upm.fi.oeg.morph.esper.EsperProxy
import java.util.Properties
import scala.compat.Platform
import scala.concurrent.duration._
import es.upm.fi.oeg.morph.esper.Event
import org.slf4j.LoggerFactory
import java.util.Date
import es.upm.fi.oeg.morph.db.Rdb
import org.joda.time.DateTime
import java.sql.Timestamp
import collection.JavaConversions._

class CrtmDataFeed(props:Properties, proxy:EsperProxy) extends LsdDataFeed(props,proxy){
  private val logger = LoggerFactory.getLogger(this.getClass)
  override protected val dbattributes=Array("serie","line","to_char(validationtime AT TIME ZONE INTERVAL '4:00' HOUR TO MINUTE, 'YYYY\"_\"MM\"_\"DD\"_\"HH\"_\"MI\"_\"SS')","validationtime","stop","validationresult","operator")

  override def schedule{
    val eng=proxy.engine
    val datas:Iterator[(String,Seq[EsperEvent])]=new DataIterator
    lazy val init =Platform.currentTime
    import proxy.system.dispatcher
    proxy.system.scheduler.schedule(0 seconds, rate milliseconds){
      val dnext=datas.next
      if (dnext!=null){
        logger.trace((Platform.currentTime-init)+" sendingo "+rate+"-"+dnext._1)
      
        val data:Seq[EsperEvent]=dnext._2
        data.foreach{d=>
          eng ! Event("crtm_observations",d)          
        }
      }
    }      
  }

  
  override def getData(date:Date)={
    val db=new Rdb(props)
    val dateStr=dateFormat.format(date)
    val dateEnd=new DateTime(date).plusMinutes(window).toDate
    val datefin=dateFormat.format(dateEnd)
    val init=Platform.currentTime
    logger.debug("Date: "+dateStr)
    //'2004-08-08 06:00:00+02'
    //val (res1,con1)=db.query("select samplingtime AS observationTime,st.stationid AS stationId,airtemperature AS temperature,relativehumidity AS relativeHumidity,precipitation AS precipitation,st.code AS code from observation obs, station st where st.stationid=obs.stationid",Array("samplingtime"))
//res1.foreach(println)
    val query="select "+projatts+" from temporal obs "//where  "+conditions //samplingtime>='"+dateStr+"' and samplingtime<'"+datefin+"' and "+conditions,
    logger.debug("sent query: "+query)
    val (res,con)=db.query(query,attributes)
    val grouped=res.groupBy(a=>a(0)).map{v=>      
      val key=dateFormat.format(v._1.asInstanceOf[Timestamp])
      (key,v._2.map(dt=>attributes.zip(dt).toMap[String,Any]).toSeq  )
    }    
    con.close
    val sorted=grouped.toSeq.sortBy(a=>a._1)
    logger.debug("elapsed "+(Platform.currentTime-init))        
    //grouped.toSeq.reverse
    sorted
  }  
  
}