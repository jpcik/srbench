package es.upm.fi.oeg.siq.voc
import es.upm.fi.oeg.morph.voc.Vocabulary

object Wgs84 extends Vocabulary{
  override val prefix="http://www.w3.org/2003/01/geo/wgs84_pos#"
  val Point=resource("Point")
  val alt=property("alt")
  val lat=property("lat")
  val long=property("long")
}


object OwlTime extends Vocabulary{
  override val prefix="http://www.w3.org/2006/time#"
  val Instant=resource("Instant")
  val inXsdDataTime=property("inXSDDateTime")
}

object XMLSchema extends Vocabulary{
  override val prefix="http://www.w3.org/2001/XMLSchema#"
  val dateTime=resource("dateTime")
}