package eu.planetdata.srbench
import es.upm.fi.oeg.siq.tools.ParameterUtils._
import java.io.File
import eu.planetdata.srbench.feed.LsdDataFeed
import es.upm.fi.oeg.morph.esper.EsperServer
import es.upm.fi.oeg.morph.esper.EsperProxy
import es.upm.fi.oeg.morph.stream.evaluate.QueryEvaluator
import java.net.URI
import es.upm.fi.oeg.morph.stream.evaluate.StreamReceiver
import scala.compat.Platform
import org.slf4j.LoggerFactory
import akka.pattern.{ ask, pipe }
import ch.qos.logback.core.util.StatusPrinter
import ch.qos.logback.classic.LoggerContext
import es.upm.fi.oeg.morph.esper.ListenQuery
import akka.actor.Props
import akka.actor.Actor
import collection.JavaConversions._
import com.hp.hpl.jena.sparql.engine.binding.BindingMap
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.sparql.core.Var
import com.hp.hpl.jena.sparql.engine.QueryIterator
import com.hp.hpl.jena.sparql.engine.binding.Binding
import com.hp.hpl.jena.sparql.engine.ResultSetStream
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.sparql.serializer.SerializationContext
import com.hp.hpl.jena.query.ResultSetFormatter
import com.hp.hpl.jena.sparql.resultset.SPARQLResult
import com.hp.hpl.jena.sparql.resultset.JSONOutputResultSet
import com.hp.hpl.jena.sparql.resultset.JSONOutput
import es.upm.fi.oeg.siq.sparql.SparqlResults
import es.upm.fi.oeg.morph.stream.evaluate.EvaluatorUtils
import scala.collection.mutable.ArrayBuffer
import org.slf4j.Logger
import java.io.OutputStream
import java.io.FileOutputStream
import play.api.libs.json.Json
import play.api.libs.json.Json._
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.Literal
import com.hp.hpl.jena.rdf.model.Resource
import play.api.libs.json.JsValue

object SRBench {
  val logger = LoggerFactory.getLogger(this.getClass)  
  private def srbench(q:String)=loadQuery("queries/srbench/"+q)
  private val srbenchR2rml=new URI("mappings/srbench.ttl")

  def main(args:Array[String]){    
    val props=load(new File("conf/srbench.properties"))
    val esper=new EsperServer           
    val eval = new QueryEvaluator(props,esper.system)
    esper.startup    
    val proxy=new EsperProxy(esper.system)
    val feed=new LsdDataFeed(props,proxy)
    val rate = props.getProperty("feed.rate").toLong
    //val actor=proxy.system.actorOf(Props(new StreamRec),"acting")
    //val query="SELECT DISTINCT observationTime AS observationTime,temperature AS value,stationId AS stationId,NULL AS sensor FROM lsd_observations.win:time(1.0 second) AS rel0"
    //proxy.engine ! ListenQuery(query,actor)
    val query=props.getProperty("srbench.query")
    val pull=props.getProperty("srbench.pull").equals("true")
    val interval=props.getProperty("srbench.pull.interval").toInt
    val maxtime=props.getProperty("srbench.maxtime").toInt
    val o=new FileOutputStream("result.out")    
    val rec=new CustomReceiver(Platform.currentTime,rate)
    //val init=Platform.currentTime

    
    if (pull){
      logger.info("pulling")
      val key=eval.registerQuery(srbench(query), srbenchR2rml)
      feed.schedule
      rec.initTime
      (1 to maxtime/interval).foreach{i=>
        Thread.sleep(interval)
        rec.receiveData(eval.pull(key))     
      }      
    }
    else{
      logger.info("pushing")
      eval.listenToQuery(srbench(query),srbenchR2rml,rec)
      feed.schedule
      rec.initTime
      Thread.sleep(maxtime)
    }
      
    
    val end=Platform.currentTime
    esper.system.shutdown
    rec.serializeAll(System.out)
    
    //rec.jsonize(o)
    o.close
    //logger.debug("elapsed "+ (end-init))
  }  
}


class CustomReceiver(start:Long,rate:Long) extends StreamReceiver{
  private val logger=LoggerFactory.getLogger(this.getClass)
  private val allResults=new ArrayBuffer[(Long,SparqlResults)]()
  private val rounding=(rate).toDouble
  var stTime=0L
  
  def initTime{
    stTime=Platform.currentTime
  }
  
  override def receiveData(s:SparqlResults){
    logger.debug("got at: "+(Platform.currentTime-stTime))
    //Utils.print(logger, Platform.currentTime-start,System.out, s)
    val orig=Platform.currentTime-stTime
    val timed:Long = (Math.round(orig/rounding)*rounding).toLong
    //logger.info("times: "+orig+" "+timed)
    allResults.+= ((timed,s))
  }
  
  def serializeAll(o:OutputStream)=allResults.foreach{p=>
    IOUtils.print(p._1,o,p._2)
  }

  def jsonize(o:OutputStream)={
    val tt:Seq[JsValue]=allResults.map(r=>IOUtils.json(r._1,r._2)).toSeq
    
    val js=Json.toJson(Map("results"->toJson(tt)))
    o.write(Json.stringify(js).getBytes)
  }

}
