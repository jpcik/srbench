package eu.planetdata.srbench.data.crtm

import java.io.File
import scala.io.Source
import es.upm.fi.oeg.siq.tools.ParameterUtils
import es.upm.fi.oeg.morph.db.Rdb
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import collection.JavaConversions._
import com.hp.hpl.jena.rdf.model.ResourceFactory
import scala.compat.Platform


class CrtmData {  
  def insertData(db:Rdb,f:String)={
      println("for file "+f)
      val m= ModelFactory.createDefaultModel
      m.read(f,null)
      val data=m.listSubjectsWithProperty(CrtmVocab.has_serie) map {s=>
        
        try Array[Object](s.getLocalName.substring(4),
            s.getProperty(CrtmVocab.has_serie).getObject.asLiteral.getString,
            s.getProperty(CrtmVocab.has_fechahora).getObject.asLiteral.getString,
            s.getProperty(CrtmVocab.has_fechaprimera).getObject.asLiteral.getString,
            s.getProperty(CrtmVocab.restriccion_titulo).getObject.asResource.getLocalName,
            s.getProperty(CrtmVocab.restriccion_perfil).getObject.asResource.getLocalName,
            s.getProperty(CrtmVocab.restriccion_operador).getObject.asResource.getLocalName,
            s.getProperty(CrtmVocab.has_linea).getObject.asLiteral.getString,
            s.getProperty(CrtmVocab.has_parada).getObject.asLiteral.getString,
            s.getProperty(CrtmVocab.restriccion_resultado).getObject.asResource.getLocalName)
        catch {case a:Exception=>throw new Exception("illegal s: "+s)} 
      }
      //data.foreach(p=>p.foreach(println))
      db.insert("temporal", data.toIterable)              
  } 
  
  
  def readFiles(dir:String)={ 
    val props=ParameterUtils.load(new File("conf/srbench.properties"))
    val db=new Rdb(props)
    val directory=new File(dir)
    if (!directory.isDirectory)
      throw new IllegalArgumentException("not a directory: "+dir)
    directory.list.filter(_.endsWith(".owl")).par.foreach {f=>
      insertData(db, dir+"/"+f)
    }
  }
}


object CrtmVocab{
  val ttp="http://purl.org/crtm/TTPDesfire#"
  def prop(localName:String)=ResourceFactory.createProperty(ttp, localName)
  val has_serie=prop("has_serie")
  val has_fechahora=prop("has_fechahora")
  val has_fechaprimera=prop("has_fechaprimera")
  val restriccion_titulo=prop("restriccion_titulo")
  val restriccion_perfil=prop("restriccion_perfil")
  val restriccion_operador=prop("restriccion_operador")
  val has_linea=prop("has_linea")
  val has_parada=prop("has_parada")
  val restriccion_resultado=prop("restriccion_resultado")
}

object CrtmData{
  def main(args:Array[String]):Unit={
    val crtm=new CrtmData
    val ari=(1 to 10).toArray
    val init=Platform.currentTime
    ari.par foreach{i=>
      println(i)
      Thread.sleep(3000)
    }
    println("time "+(Platform.currentTime-init))
    //crtm.readFiles("/media/Add/data/crtm/checkins/rdf/VAL_CRTM_UPM_2013_02_15_081303")
  }
}