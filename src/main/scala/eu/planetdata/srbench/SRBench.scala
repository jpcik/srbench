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
    //val actor=proxy.system.actorOf(Props(new StreamRec),"acting")
    //val query="SELECT DISTINCT observationTime AS observationTime,temperature AS value,stationId AS stationId,NULL AS sensor FROM lsd_observations.win:time(1.0 second) AS rel0"
    //proxy.engine ! ListenQuery(query,actor)

    val o= new FileOutputStream("result.out")
    
    val rec=new CustomReceiver(Platform.currentTime)    
    eval.listenToQuery(srbench("q1.sparql"),srbenchR2rml,rec)
    //val key=eval.registerQuery(srbench("q5.sparql"), srbenchR2rml)
    val init=Platform.currentTime
    feed.schedule
    Thread.sleep(30000)
    /*
    (1 to 30).foreach{i=>
      Thread.sleep(1000)
      Utils.print(logger, Platform.currentTime-init,o, eval.pull(key))
    }*/
    val end=Platform.currentTime
    esper.system.shutdown
    //rec.serializeAll(o)
    rec.jsonize(o)
    o.close
    logger.debug("elapsed "+ (end-init))
  }  
}

object Utils{
  
  def jsonValue(v:String,value:RDFNode):JsValue=value match{
    case lit:Literal=>toJson(Map("type"->toJson("literal"),
                                  "datatype"->toJson(lit.getDatatypeURI),
                                  "value"->toJson(lit.getString)))
    case res:Resource=>toJson(Map("type"->toJson("uri"),
                                  "value"->toJson(res.getURI)))
  }
  
  def json(time:Long,s:SparqlResults):JsValue={    
    val timed=Math.round(time/500)*500
    val vars=s.getResultSet.getResultVars    
    val dat:Iterator[Map[String,JsValue]]=s.getResultSet.map{r=>
      val binding=vars.map(v=>(v->jsonValue(v,r.get(v)))).toMap
      Map("timestamp"->toJson(timed),
          "binding"->toJson(binding))        
    }
  
    toJson(
      Map("head"->toJson(Map("vars"->vars.map(v=>toJson(v)))),
          "timestamp"->toJson(timed),          
          "results"->toJson(Map("bindings"->toJson(dat.toSeq))))           
    )
  }
  
  def print(l:Logger,time:Long,o:OutputStream,s:SparqlResults):Unit={
    val timed=Math.round(time/500)*500
    l.debug("time %s " format time)
    //o.write(("time: %s\r" format timed).getBytes )
    //ResultSetFormatter.outputAsCSV(o,s.getResultSet)
    print(s,timed,o)
    
  }
  
  def print(s:SparqlResults,time:Long,o:OutputStream):Unit={
    //json(time,s)
    
    val vars=s.getResultSet.getResultVars
    if (!s.getResultSet.hasNext)
      o.write(("<>:["+time+"]\r").getBytes)
    s.getResultSet.foreach{rs=>
      o.write(("<"+vars.map(v=>v+"="+rs.get(v)).mkString(" ")+" >:["+time+"]\r").getBytes)
    }
  }
}

class CustomReceiver(start:Long) extends StreamReceiver{
  private val logger=LoggerFactory.getLogger(this.getClass)
  private val allResults=new ArrayBuffer[(Long,SparqlResults)]()
 
  override def receiveData(s:SparqlResults){
    logger.debug("got at: "+(Platform.currentTime-start))
    //Utils.print(logger, Platform.currentTime-start,System.out, s)
    allResults.+= ((Platform.currentTime-start,s))
  }
  
  def serializeAll(o:OutputStream)=allResults.foreach{p=>
    Utils.print(logger, p._1,o,p._2)
  }

  def jsonize(o:OutputStream)={
    val tt:Seq[JsValue]=allResults.map(r=>Utils.json(r._1,r._2)).toSeq
    
    val js=Json.toJson(Map("results"->toJson(tt)))
    o.write(Json.stringify(js).getBytes)
  }

}
