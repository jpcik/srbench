package eu.planetdata.srbench.data.lsd
import java.io.File
import es.upm.fi.oeg.siq.tools.ParameterUtils._
import es.upm.fi.oeg.morph.db.Rdb
import com.hp.hpl.jena.rdf.model.ModelFactory
import scala.io.Source
import java.io.FileInputStream
import es.upm.fi.oeg.morph.voc.RDF
import collection.JavaConversions._
import es.upm.fi.oeg.siq.voc.OwlTime
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import Weather._

object LsdObservations extends RdfNavigate{
  val obsMapping=Map(windspeed->milesPerHour,relativehumidity->percent,
      airtemperature->fahrenheit,precipitationaccumulated->centimeters,
      dewpoint->fahrenheit,precipitation->centimeters,
      winddirection->degrees,windgust->milesPerHour,
      pressure->inches,precipitationsmoothed->centimeters,
      soiltemperature->fahrenheit,visibility->centimeters,
      peakwindspeed->milesPerHour,watertemperature->fahrenheit,
      peakwinddirection->degrees)
  
  def main(atgs:Array[String]){
    val subset="charley"
    val obsDir=new File("/media/Add/data/lsd/observations/"+subset)
    val props=load(new File("conf/srbench.properties"))
    val db=new Rdb(props) 
    
    obsDir.listFiles.filter(f=>f.getName.endsWith(".n3")).drop(48180).take(5000).foreach{f=>
      val code=f.getName.split('_').head
      println(code)
      val stationids= db.queryFirst("SELECT stationid FROM station WHERE code='"+code+"'",Array("stationid"))
      //if (stationids==null) {
      //  db.insert("station",Array(code,null,null,null))
      //}
      if (true){
      val stationid=stationids.head
      val model=ModelFactory.createDefaultModel
      val s=Source.fromFile(f)
      model.read(new FileInputStream(f),null,"N3")
      
      val sys=model.listSubjectsWithProperty(RDF.a,OwlTime.Instant)
      
      val toinsert=sys.map{s=>        
        println(s.getLocalName)
        val time=s.typedSpecial(OwlTime.inXsdDataTime)
        val obs=model.listSubjectsWithProperty(OmOwl.samplingTime,s)        
        val obsval=obs.map{o=>          
          val measure=o.getProperty(OmOwl.result).getResource
          val measClass= measure.getProperty(RDF.a).getResource
          val obsProp=o.res(OmOwl.observedProperty)
          if (measClass==OmOwl.MeasureData){
            if (measure.res(OmOwl.uom)!=obsMapping(obsProp))
              throw new Exception("bad unit"+measure.res(OmOwl.uom))
            (obsProp.getLocalName, 
             measure.typed(OmOwl.floatValue))
          }
          else (obsProp.getLocalName,null) 
        }.toMap
        val props=Array(windspeed,relativehumidity,airtemperature,precipitation,dewpoint,
            winddirection,windgust,precipitationaccumulated,pressure,precipitationsmoothed,
            soiltemperature,visibility,peakwindspeed,watertemperature,peakwinddirection)
        val propsArr=props.map{p=>obsval.getOrElse(p.getLocalName,null)}
        Array(time,stationid)++propsArr
      }.toStream
      if (!toinsert.isEmpty)
        db.insert("observation",toinsert)
      s.close
      model.close
    }  
    }
  }

}