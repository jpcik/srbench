package eu.planetdata.srbench

import com.hp.hpl.jena.rdf.model.RDFNode
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import com.hp.hpl.jena.rdf.model.Literal
import es.upm.fi.oeg.siq.sparql.SparqlResults
import org.slf4j.Logger
import java.io.OutputStream
import com.hp.hpl.jena.rdf.model.Resource
import collection.JavaConversions._

object IOUtils {

  def jsonValue(v:String,value:RDFNode):JsValue=value match{
    case lit:Literal=>toJson(Map("type"->toJson("literal"),
                                  "datatype"->toJson(lit.getDatatypeURI),
                                  "value"->toJson(lit.getString)))
    case res:Resource=>toJson(Map("type"->toJson("uri"),
                                  "value"->toJson(res.getURI)))
  }
  
  def json(time:Long,s:SparqlResults):JsValue={       
    val vars=s.getResultSet.getResultVars    
    val dat:Iterator[Map[String,JsValue]]=s.getResultSet.map{r=>
      val binding=vars.map(v=>(v->jsonValue(v,r.get(v)))).toMap
      Map("timestamp"->toJson(time),
          "binding"->toJson(binding))        
    }
  
    toJson(
      Map("head"->toJson(Map("vars"->vars.map(v=>toJson(v)))),
          "timestamp"->toJson(time),          
          "results"->toJson(Map("bindings"->toJson(dat.toSeq))))           
    )
  }
  
  def print(time:Long,o:OutputStream,s:SparqlResults):Unit={
    //val timed=Math.round(time/500)*500
    //l.debug("time %s " format time)
    //o.write(("time: %s\r" format timed).getBytes )
    //ResultSetFormatter.outputAsCSV(o,s.getResultSet)
    print(s,time,o)
    
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