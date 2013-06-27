package eu.planetdata.srbench.feed

import java.util.Properties
import scala.compat.Platform
import concurrent.duration._
import org.slf4j.LoggerFactory
import org.semanticweb.owlapi.model.OWLDataFactory
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAxiom
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import es.upm.fi.oeg.morph.esper.EsperProxy
import eu.trowl.owlapi3.rel.tms.reasoner.dl.RELReasoner
import scala.collection.JavaConversions
import collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLClassExpression


class LsdOwlAxiomFeed(props:Properties,reasoner:RELReasoner) extends LsdDataFeed(props,null) with OWLVocab{
  private val logger = LoggerFactory.getLogger(this.getClass)
  val sys=ActorSystem("LsdSystem",ConfigFactory.load.getConfig("espereng"))
  //var countAll=0
  def streamAxioms{
    
    val datas:Iterator[(String,Seq[EsperEvent])]=new DataIterator
    val oldAxioms=new ArrayBuffer[OWLAxiom]()
    lazy val init =Platform.currentTime
    import sys.dispatcher
    //sys.scheduler.schedule(0 seconds, rate milliseconds){
    sys.scheduler.scheduleOnce(0 seconds){
    var cut=false
    while (!cut){
      val dnext=datas.next
      if (dnext!=null){
        logger.trace((Platform.currentTime-init)+" sending "+rate+"-"+dnext._1)
      
        val data:Seq[EsperEvent]=dnext._2
        val axiomsset=data.map{d=>
          generateObservations(d)                   
        }.flatten.toSet        
        val axioms=JavaConversions.setAsJavaSet[OWLAxiom](axiomsset)
        countAll+=axioms.size
        logger.debug("axioms"+axioms.size)
        
        reasoner.clean(setAsJavaSet(oldAxioms.toSet))
        reasoner.add(axioms)
        oldAxioms.clear
        oldAxioms.++=(axiomsset)
        
        reasoner.reclassify
       
      }
      Thread.sleep(rate)
    }
    }
    
    sys.scheduler.schedule(0 seconds, 1000 milliseconds){
           logger.debug("exec query")
           //val some=df.getowl
           //.getOWLObjectSomeValuesFrom(ssn_observedBy,ssn_Sensor)
           

           val rest=//df.getOWLObjectProperty(ssn_observedProperty.getIRI)
           //.getOWLObjectSomeValuesFrom(ssn_observedBy,ssn_Sensor)
           reasoner.getInstances( ssn_Observation,false).getFlattened()
           rest.foreach{n=>
            logger.info("ind:"+n.asOWLNamedIndividual.getIRI)
           }
      
    }      

    
  }
       
  private def generateObservation(code:String,timestamp:Any,value:Double,typeString:String,
      obsprop:OWLNamedIndividual,obstype:OWLClassExpression,sensor:OWLNamedIndividual):Set[OWLAxiom]={    
    val obs=ind("%s%s/%s/observation/%s" format(meteo,code,typeString,timestamp))
    val obsres=ind("%s%s/%s/observationresult/%s" format(meteo,code,typeString,timestamp))
    val obsval=ind("%s%s/%s/observationvalue/%s" format(meteo,code,typeString,timestamp))
     Set(
        df.getOWLClassAssertionAxiom(obstype, obs),
        df.getOWLObjectPropertyAssertionAxiom(ssn_observationResult, obs, obsres),
        df.getOWLObjectPropertyAssertionAxiom(ssn_observedBy, obs, sensor),
        df.getOWLObjectPropertyAssertionAxiom(ssn_hasValue, obsres, obsval),
        df.getOWLObjectPropertyAssertionAxiom(ssn_observedProperty, obs, obsprop),
        df.getOWLObjectPropertyAssertionAxiom(qu_unit, obsval, unit_degreeCelsius),
        df.getOWLDataPropertyAssertionAxiom(qu_numericalValue, obsval, df.getOWLLiteral(value))
    )     
  }
  
  private def generateObservations(data:EsperEvent):Set[OWLAxiom]={
    val timestamp=data("timeformat")
    val code=data("code").toString
    val temp=data("temperature").asInstanceOf[Double]
    val humid=data("relativeHumidity").asInstanceOf[Double]
    val wsp=data("windSpeed").asInstanceOf[Double]

    val sensor=ind("%ssensor/id/%s_sens" format(meteo,code))    
    generateObservation(code,timestamp,temp,"temperature",
        cfproperty_airtemperature,oeg_TemperatureObservation,sensor) ++
    generateObservation(code,timestamp,humid,"humidity",
        cfproperty_relativehumidity,oeg_HumidityObservation,sensor) ++
    generateObservation(code,timestamp,wsp,"windspeed",
        cfproperty_windspeed,oeg_WindSpeedObservation,sensor)
  } 
}