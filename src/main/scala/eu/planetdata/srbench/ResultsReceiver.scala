package eu.planetdata.srbench

import es.upm.fi.oeg.morph.stream.evaluate.StreamReceiver
import org.slf4j.LoggerFactory
import scala.collection.mutable.ArrayBuffer
import java.io.OutputStream
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json._
import es.upm.fi.oeg.siq.sparql.SparqlResults
import play.api.libs.json.JsNull

class ResultsReceiver (start:Long,rate:Long) extends StreamReceiver{
  private val logger=LoggerFactory.getLogger(this.getClass)
  private val allResults=new ArrayBuffer[(Long,SparqlResults)]()
  private val rounding=(rate).toDouble
  private var stTime=0L
  
  def initTime{
    stTime=System.nanoTime//Platform.currentTime
  }
  
  override def receiveData(s:SparqlResults){    
    //Utils.print(logger, Platform.currentTime-start,System.out, s)
    val orig=(System.nanoTime-stTime)/1000000
    logger.debug("got at: "+orig)
    val timed:Long = (Math.round(orig/rounding)*rounding).toLong
    //logger.info("times: "+orig+" "+timed)
    allResults.+= ((timed,s))
  }
  
  def serializeAll(o:OutputStream)=allResults.foreach{p=>
    IOUtils.print(p._1,o,p._2)
  }

  def jsonize(o:OutputStream)={
    val tt:Seq[JsValue]=allResults.map(r=>IOUtils.json(r._1,r._2)).toSeq.filter(_!=JsNull)
    
    val js=Json.toJson(Map("relations"->toJson(tt)))
    o.write(Json.stringify(js).getBytes)
  }

}
