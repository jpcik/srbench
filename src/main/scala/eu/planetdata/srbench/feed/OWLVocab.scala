package eu.planetdata.srbench.feed

import org.semanticweb.owlapi.model.OWLDataFactory
import org.semanticweb.owlapi.model.IRI
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl

trait OWLVocab {
  val df=new OWLDataFactoryImpl
   val ssn="http://purl.oclc.org/NET/ssnx/ssn#"
   val oeg="http://oeg-upm.net/onto/sensordemo/"
   val rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   val meteo="http://meteo.us/"
   val qu="http://purl.oclc.org/NET/ssnx/qu/qu#"
   val unit="http://purl.oclc.org/NET/ssnx/qu/unit#" 
   val cfproperty="http://purl.oclc.org/NET/ssnx/cf/cf-property#" 
  
   val ssn_Sensor=df.getOWLClass(IRI.create(ssn+"Sensor")) 
   val ssn_Observation=df.getOWLClass(IRI.create(ssn+"Observation")) 
   val oeg_TemperatureObservation=df.getOWLClass(IRI.create(oeg+"TemperatureObservation")) 
   val oeg_HumidityObservation=df.getOWLClass(IRI.create(oeg+"HumidityObservation")) 
   val oeg_WindSpeedObservation=df.getOWLClass(IRI.create(oeg+"WindSpeedObservation")) 
   val qu_numericalValue=df.getOWLDataProperty(IRI.create(qu+"numericalValue")) 
   val qu_unit=df.getOWLObjectProperty(IRI.create(qu+"unit")) 
   val ssn_observedBy=df.getOWLObjectProperty(IRI.create(ssn+"observedBy")) 
   val ssn_observedProperty=df.getOWLObjectProperty(IRI.create(ssn+"observedProperty")) 
   val ssn_featureOfInterest=df.getOWLObjectProperty(IRI.create(ssn+"featureOfInterest")) 
   val ssn_observationResult=df.getOWLObjectProperty(IRI.create(ssn+"observationResult")) 
   val ssn_hasValue=df.getOWLObjectProperty(IRI.create(ssn+"hasValue")) 
   val unit_degreeCelsius=ind(unit+"degreeCelsius")
   val cfproperty_windspeed=ind(cfproperty+"wind_speed")
   val cfproperty_airtemperature=ind(cfproperty+"air_temperature")
   val cfproperty_relativehumidity=ind(cfproperty+"relative_humidity")
  
   def ind(uri:String)=  
     df.getOWLNamedIndividual(IRI.create(uri))

}