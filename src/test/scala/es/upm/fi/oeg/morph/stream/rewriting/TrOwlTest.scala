package es.upm.fi.oeg.morph.stream.rewriting

import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import org.slf4j.LoggerFactory
import org.junit.Before
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import scala.io.Source
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl
import org.semanticweb.owlapi.model.IRI
import collection.JavaConversions._
import uk.ac.manchester.cs.owl.owlapi.OWLIndividualRelationshipAxiomImpl
import org.semanticweb.owlapi.model.OWLPropertyExpression
import scala.collection.JavaConversions
import org.semanticweb.owlapi.model.OWLAxiom
import es.upm.fi.oeg.siq.tools.ParameterUtils
import eu.planetdata.srbench.feed.LsdOwlAxiomFeed
import eu.planetdata.srbench.feed.OWLVocab
import eu.planetdata.srbench.feed.OWLVocab

class TrOwlTest extends JUnitSuite with ShouldMatchersForJUnit {
  private val logger= LoggerFactory.getLogger(this.getClass)
  val props = ParameterUtils.load(getClass.getClassLoader.getResourceAsStream("config/srbench.properties"))
  val manager = OWLManager.createOWLOntologyManager    
  val df = manager.getOWLDataFactory
  
  @Before def setUpBeforeClass() {      
    println("finish init")
  }
  

  @Test def addAxioms{
    
    val rl2 = new eu.trowl.owlapi3.rel.tms.reasoner.dl.RELReasonerFactory  
    val str=getClass.getResourceAsStream("/ontologies/sensordemo.owl")
    val ontology = manager.loadOntologyFromOntologyDocument(str) 
    val reasoner:eu.trowl.owlapi3.rel.tms.reasoner.dl.RELReasoner = rl2.createReasoner(ontology)
    val adds=manager.loadOntologyFromOntologyDocument(getClass.getResourceAsStream("/ontologies/add1.ttl"))
    val feed=new LsdOwlAxiomFeed(props,reasoner)
    
    feed.streamAxioms    
    Thread.sleep(20000)
    feed.sys.shutdown  
    logger.info("processed axioms: "+feed.countAll)
    reasoner.getIndividuals(feed.oeg_TemperatureObservation).getFlattened().foreach{n=>
      //logger.info("ind:"+n.asOWLNamedIndividual.getIRI)
    }
    
  }
}