package eu.planetdata.srbench.data.lsd
import es.upm.fi.oeg.morph.voc.Vocabulary

object OmOwl extends Vocabulary{
  override val prefix="http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#"
  val System=resource("System")
  val Observation=resource("Observation")
  val MeasureData=resource("MeasureData")
  val id=property("ID")
  val parameter=property("parameter")
  val processLocation=property("processLocation")
  val observedProperty=property("observedProperty")
  val procedure=property("procedure")
  val result=property("result")
  val samplingTime=property("samplingTime")
  val floatValue=property("floatValue")
  val uom=property("uom")
}

object Weather extends Vocabulary{
  override val prefix="http://knoesis.wright.edu/ssw/ont/weather.owl#"
  val winddirection=resource("_WindDirection") 
  val windspeed=resource("_WindSpeed")
  val windgust=resource("_WindGust")
  val peakwindspeed=resource("_PeakWindSpeed")
  val peakwinddirection=resource("_PeakWindDirection")
  val precipitationaccumulated=resource("_PrecipitationAccumulated")
  val precipitation=resource("_Precipitation")
  val relativehumidity=resource("_RelativeHumidity")
  val airtemperature=resource("_AirTemperature")
  val dewpoint=resource("_DewPoint")
  val pressure=resource("_Pressure")
  val precipitationsmoothed=resource("_PrecipitationSmoothed")
  val soiltemperature=resource("_SoilTemperature")
  val visibility=resource("_Visibility")
  val watertemperature=resource("_WaterTemperature")
  
  val milesPerHour=resource("milesPerHour")
  val centimeters=resource("centimeters")
  val fahrenheit=resource("fahrenheit")
  val percent=resource("percent")
  val degrees=resource("degrees")
  val inches=resource("inches")
  
  
}


 