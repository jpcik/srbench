package eu.planetdata.srbench
import es.upm.fi.oeg.siq.tools.ParameterUtils._
import java.io.File
import eu.planetdata.srbench.feed.LsdDataFeed
import es.upm.fi.oeg.morph.esper.EsperServer
import es.upm.fi.oeg.morph.esper.EsperProxy
import es.upm.fi.oeg.morph.stream.evaluate.QueryEvaluator
import java.net.URI
import es.upm.fi.oeg.morph.stream.evaluate.StreamReceiver


class CustomRec extends StreamReceiver{
  override def receiveData(s:String){
    println("getting this "+s)
  }
}

object SRBench {
  private def srbench(q:String)=loadQuery("queries/srbench/"+q)
  private val srbenchR2rml=new URI("mappings/srbench.ttl")

  def main(args:Array[String]){
    val props=load(new File("conf/srbench.properties"))
    val esper=new EsperServer           
    val eval = new QueryEvaluator(props,esper.system)

    esper.startup
    val feed=new LsdDataFeed(props,new EsperProxy(esper.system))
    feed.schedule
    val rec=new CustomRec
    eval.listenToQuery(srbench("join-pattern-matching.sparql"),srbenchR2rml,rec)
    
    
    Thread.sleep(20000)
    esper.system.shutdown
    //esper.shutdown
  }

}
