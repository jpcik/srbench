package eu.planetdata.srbench

import org.slf4j.LoggerFactory
import es.upm.fi.oeg.morph.esper.EsperServer
import es.upm.fi.oeg.siq.tools.ParameterUtils
import es.upm.fi.oeg.morph.stream.evaluate.QueryEvaluator
import es.upm.fi.oeg.morph.esper.EsperProxy
import eu.planetdata.srbench.feed.DataFeed
import java.net.URI
import es.upm.fi.oeg.morph.stream.evaluate.StreamReceiver
import es.upm.fi.oeg.siq.sparql.SparqlResults
import es.upm.fi.oeg.morph.stream.evaluate.EvaluatorUtils
import collection.JavaConversions._
import java.net.URL
import es.upm.fi.oeg.morph.stream.esper.EsperAdapter

object OntologyQueryAnswering {
  private val logger= LoggerFactory getLogger(this.getClass)
  lazy val esper=new EsperServer
  val props = ParameterUtils load(getClass.getClassLoader getResourceAsStream("config/srbench.properties"))
  val eval = new EsperAdapter(esper.system,"demokyrie")
  val proxy=new EsperProxy(esper.system)
  val feed=new DataFeed("lsd",proxy)
  val query=props("srbench.query")
  val globalpath=props("srbench.globalpath")
  val maxtime=props("srbench.maxtime") toLong
  val numberqueries=props("srbench.numberqueries") toInt
  
println("traaaaa"+numberqueries)
  println("fsdfsdf "+globalpath)
  private def ssn(q:String)=ParameterUtils.loadAsString(new URL(globalpath+"queries/ssn/"+q))
  private val ssnR2rml=new URI("mappings/ssn.ttl")
  
  def launchQueries:Unit={
    val res=new ResultsReceiver  //create a results receiver
    (1 to numberqueries).foreach{i=>
      eval listenToQuery(ssn(query),ssnR2rml,res) //listen to a query
      Thread.sleep(10)
    }
    feed.schedule                //start stream
    Thread sleep(maxtime) 
    logger.info("total input tuples: "+feed.groupsize)
    logger.info("total groups: "      +feed.totalgroups)
    logger.info("average data size: " +(feed.groupsize/feed.totalgroups))
    logger.info("total responses: " +res.res)
    logger.info("avg response size: " +res.size/res.res)
  }
    
  def main(args:Array[String]):Unit={
    esper.startup                 //start esper
    launchQueries
    esper.system.shutdown       //stop esper
  }
  
  
  class ResultsReceiver extends StreamReceiver{
    private val logger=LoggerFactory.getLogger(this.getClass)  
    var res=0L
    var size=0L
    override def receiveData(s:SparqlResults){    
      res+=1
      size+=s.getResultSet.size
      //println("size of "+s.getResultSet.size)
      //println(EvaluatorUtils.serialize(s))
    }
  }
}