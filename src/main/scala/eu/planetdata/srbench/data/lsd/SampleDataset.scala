package eu.planetdata.srbench.data.lsd

import es.upm.fi.oeg.morph.r2rml.R2rmlReader
import es.upm.fi.oeg.morph.relational.RelationalModel
import es.upm.fi.oeg.morph.relational.JDBCRelationalModel
import es.upm.fi.oeg.morph.execute.RdfGenerator
import java.util.Properties
import es.upm.fi.oeg.siq.tools.ParameterUtils._
import java.io.File
import es.upm.fi.oeg.morph.voc.RDFFormat
import collection.JavaConversions._
import java.io.FileOutputStream

class SampleDataset {
  def generate={
    
    val props = load(new File("conf/srbench.properties"))
    val relat:RelationalModel=new JDBCRelationalModel(props)
    val reader=R2rmlReader("mappings/lsd.ttl")
    val ds=new RdfGenerator(reader,relat).generate
    
    ds.listNames.toArray.sorted.zipWithIndex.foreach{name=>
      val fos=new FileOutputStream("data_"+name._2+".ttl")
      val model=ds.getNamedModel(name._1)

      model.setNsPrefix("om-owl", "http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#")
      model.setNsPrefix("weather","http://knoesis.wright.edu/ssw/ont/weather.owl#")
      model.setNsPrefix("sens-obs","http://knoesis.wright.edu/ssw/")
      model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#")
      model.write(fos,RDFFormat.TTL)
      fos.close
    }
    //ds.getDefaultModel.write(fos,RDFFormat.TTL)
  }
}

object SampleDataset{
  def main(args:Array[String]){
    new SampleDataset().generate
  }
}