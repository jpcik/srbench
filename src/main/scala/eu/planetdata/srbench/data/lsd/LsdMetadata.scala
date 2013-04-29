package eu.planetdata.srbench.data.lsd
import java.io.File
import java.io.FilenameFilter
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import scala.io.Source
import java.io.FileInputStream
import es.upm.fi.oeg.morph.voc.RDF
import collection.JavaConversions._
import com.hp.hpl.jena.rdf.model.Statement
import com.hp.hpl.jena.rdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import es.upm.fi.oeg.morph.relational.JDBCRelationalModel
import es.upm.fi.oeg.siq.tools.ParameterUtils._
import es.upm.fi.oeg.morph.db.Rdb
import com.hp.hpl.jena.rdf.model.Literal
import com.hp.hpl.jena.datatypes.DatatypeFormatException
import es.upm.fi.oeg.siq.voc.Wgs84
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import es.upm.fi.oeg.siq.voc.XMLSchema
import scala.language.implicitConversions

trait RdfNavigate{
  case class ExtendedResource(r:Resource){
    def lit(prop:Property)=r.getProperty(prop).getLiteral
    def res(prop:Property)=r.getProperty(prop).getResource
    def typedSpecial(prop:Property)={
      val lit=r.getProperty(prop).getLiteral
      if (lit.getDatatype==null){
        val sp=lit.getString.split("\\^\\^")
        if (sp.last.equals(XMLSchema.dateTime.getURI))  sp.head.replaceFirst("-NA","")
        else lit.getString
      }
      else typed(prop)
    }
    def typed(prop:Property)={
      val lit=r.getProperty(prop).getLiteral
      try lit.getValue
      catch{case e:DatatypeFormatException=>null}
    }
  }
  implicit def state2String(s:Statement):String=s.getLiteral.getString
  implicit def res2extres(s:Resource):ExtendedResource=ExtendedResource(s)
  implicit def lit2str(s:Literal):Object=s.getValue  
}

object LsdMetadata extends RdfNavigate{
    
  def main(atgs:Array[String]){
    val metadataDir=new File("/media/Add/data/lsd/metadta/")
    val props=load(new File("conf/srbench.properties"))
    val db=new Rdb(props) 
    
    metadataDir.listFiles.filter(f=>f.getName.endsWith(".n3")).foreach{f=>
      val model=ModelFactory.createDefaultModel
      val s=Source.fromFile(f)
      model.read(new FileInputStream(f),null,"N3")
      val sys=model.listSubjectsWithProperty(RDF.a,OmOwl.System)
      sys.foreach{s=>        
        println(s.getLocalName)
        val loc=s.res(OmOwl.processLocation)        
        db.insert("station",Array(Array(s.typed(OmOwl.id),loc.typed(Wgs84.alt),
                                  loc.typed(Wgs84.lat),loc.typed(Wgs84.long))))
      }
      s.close
    }  
      
  }
} 