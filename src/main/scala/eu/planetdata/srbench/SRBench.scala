package eu.planetdata.srbench
import io.Source._
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
import es.upm.fi.oeg.morph.esper.Ping
import es.upm.fi.oeg.morph.esper.Event
import play.api.libs.json.JsNull
import eu.planetdata.srbench.data.lsd.Observations
import scala.slick.driver.PostgresDriver.simple._
import java.util.Properties
import eu.planetdata.srbench.data.lsd.LsdDB
import es.upm.fi.oeg.siq.tools.ParameterUtils

object SRBench {
  val logger = LoggerFactory.getLogger(this.getClass)  
  private def srbench(q:String)=ParameterUtils.loadQuery("queries/srbench/"+q)
  private val srbenchR2rml=new URI("mappings/srbench.ttl")

  import Database.threadLocalSession
 
/*
  def q(props:Properties)={
Database.forURL(url=props.getProperty("jdbc.source.url"), 
    user=props.getProperty("jdbc.source.user"),
    password=props.getProperty("jdbc.source.password"),
    prop=null,
    driver = props.getProperty("jdbc.driver")) withSession {
    logger.info("preinttt")
    val q2 = for {
  c <- Observations if c.airTemperature > 90d
  //s <- Suppliers if s.id === c.supID
} yield (c.samplingTime, c.stationId)
q2 foreach println
  }  }*/
  
  def main(args:Array[String]){
    //val props1=load(this.getClass.getClassLoader.getResourceAsStream("config/morph.properties"))
    //LsdDB.loadData(props1)
    
    val props=ParameterUtils.load(getClass.getResourceAsStream("/config/srbench.properties"))
      //load(new File("conf/srbench.properties"))
    val esper=new EsperServer           
    val eval = new QueryEvaluator("srbench")
    esper.startup    
    val proxy=new EsperProxy(esper.system)
    val feed=new LsdDataFeed(props,proxy)
    val rate = props.getProperty("feed.rate").toLong
    //val actor=proxy.system.actorOf(Props(new StreamRec),"acting")
    //val query="SELECT DISTINCT observationTime AS observationTime,temperature AS value,stationId AS stationId,NULL AS sensor FROM lsd_observations.win:time(1.0 second) AS rel0"
    //proxy.engine ! ListenQuery(query,actor)
    val query=props.getProperty("srbench.query")
    val pull=props.getProperty("srbench.pull").equals("true")
    val serialize=props.getProperty("srbench.serialize")
    val interval=props.getProperty("srbench.pull.interval").toInt
    val maxtime=props.getProperty("srbench.maxtime").toInt
    val o=new FileOutputStream("results/"+query+".json")    
    val rec=new ResultsReceiver(Platform.currentTime,rate)
    //val init=Platform.currentTime
/*
    (1 to 50).foreach{i=>
      proxy.engine ! Event("lsd_observations",null)
    }*/
    
    //q(props)

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
      eval.listenToQuery(srbench(query),srbenchR2rml,null)
      logger.info("nowwiiiiii")
      feed.schedule
      rec.initTime
      logger.info("nowwiiiiii")
      Thread.sleep(maxtime)
    }
      
    
    val end=Platform.currentTime
    esper.system.shutdown
    if (serialize.equals("json"))
      rec.jsonize(o)
    else
      rec.serializeAll(System.out)
    
    //rec.jsonize(o)
    o.close
    //logger.debug("elapsed "+ (end-init))
  }  
}


