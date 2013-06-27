package es.upm.fi.oeg.morph.stream.rewriting

import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import org.slf4j.LoggerFactory
import es.upm.fi.oeg.morph.esper.EsperServer
import es.upm.fi.oeg.siq.tools.ParameterUtils
import es.upm.fi.oeg.morph.stream.evaluate.QueryEvaluator
import java.net.URI
import org.junit.Before
import org.junit.Test
import es.upm.fi.oeg.morph.stream.evaluate.StreamReceiver
import es.upm.fi.oeg.siq.sparql.SparqlResults
import es.upm.fi.oeg.morph.stream.evaluate.EvaluatorUtils
import eu.planetdata.srbench.feed.LsdDataFeed
import es.upm.fi.oeg.morph.esper.EsperProxy
import org.junit.After

class RewritingExecutionTest extends JUnitSuite with ShouldMatchersForJUnit {
  private val logger= LoggerFactory.getLogger(this.getClass)
  lazy val esper=new EsperServer
  val props = ParameterUtils.load(getClass.getClassLoader.getResourceAsStream("config/srbench.properties"))
  val eval = new QueryEvaluator(props,esper.system)
  val proxy=new EsperProxy(esper.system)
  val feed=new LsdDataFeed(props,proxy)

  private def ssn(q:String)=ParameterUtils.loadQuery("queries/ssn/"+q)
  private val srbenchR2rml=new URI("mappings/srbench.ttl")
  private val ssnR2rml=new URI("mappings/ssn.ttl")
  
  @Before def setUpBeforeClass() {
    esper.startup()
    
    println("finish init")
  }
  

  @Test def filterUriDiff{
    val res= new ResultsReceiver(0,0) 
    val qid=eval.listenToQuery(ssn("q9.sparql"),ssnR2rml,res)
    feed.schedule
    Thread.sleep(20000)
    logger.info("obs processed "+feed.countAll)
  }

  @After def finish{
    esper.system.shutdown
  }
}

class ResultsReceiver (start:Long,rate:Long) extends StreamReceiver{
  private val logger=LoggerFactory.getLogger(this.getClass)
  
  override def receiveData(s:SparqlResults){    
   // logger.info(EvaluatorUtils.serialize(s))
  }
  
}