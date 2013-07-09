package es.upm.fi.oeg.morph.stream.rewriting

import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.junit.JUnitSuite
import org.slf4j.LoggerFactory
import es.upm.fi.oeg.morph.esper.EsperServer
import es.upm.fi.oeg.siq.tools.ParameterUtils
import es.upm.fi.oeg.morph.stream.evaluate.QueryEvaluator
import es.upm.fi.oeg.morph.esper.EsperProxy
import java.net.URI
import org.junit.Before
import org.junit.Test
import org.junit.After
import eu.planetdata.srbench.feed.DataFeed
import es.upm.fi.oeg.morph.esper.RegisterQuery
import es.upm.fi.oeg.morph.esper.ListenQuery
import es.upm.fi.oeg.morph.stream.evaluate.StreamReceiver
import akka.actor.Actor
import akka.actor.Props

class CrtmStreamingTest extends JUnitSuite with ShouldMatchersForJUnit {
  private val logger= LoggerFactory.getLogger(this.getClass)
  lazy val esper=new EsperServer
  val props = ParameterUtils.load(getClass.getClassLoader.getResourceAsStream("config/srbench.properties"))
  val eval = new QueryEvaluator(props,esper.system)
  val proxy=new EsperProxy(esper.system)
  val feed=new DataFeed("crtm",proxy)

  private def ssn(q:String)=ParameterUtils.loadQuery("queries/ssn/"+q)
  private val ssnR2rml=new URI("mappings/ssn.ttl")
  
  @Before def setUpBeforeClass() {
    esper.startup()
    
    println("finish init")
  }
  

  @Test def filterUriDiff{
    //val res= new ResultsReceiver(0,0) 
    //val qid=eval.listenToQuery(ssn("q9.sparql"),ssnR2rml,res)
    feed.schedule
    val acrf=proxy.system.actorOf(Props(new CrtmReceiver()),"reci")
    proxy.engine ! RegisterQuery("insert into busylines select lineid,count(*) from crtm_observations.win:time(2 seconds) " +
    		"group by lineid having count(*) >30 output every 2 seconds")
    proxy.engine ! ListenQuery("select * from busylines.win:time(2 seconds) output every 2 seconds",acrf)
    Thread.sleep(20000)
    //logger.info("obs processed "+feed.countAll)
  }

  @After def finish{
    esper.system.shutdown
  }

  class CrtmReceiver extends Actor{
    def receive={
      case data:Array[Array[Object]]=>
          //logger.debug("Array intercepted")
          logger debug("size is "+data.size)
      case _=>
        logger.error("unknown")
    }
  }
}