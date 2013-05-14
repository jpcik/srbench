package eu.planetdata.srbench.data.lsd

import scala.slick.driver.PostgresDriver.simple._
import java.sql.Timestamp

object Observations extends Table[(Timestamp,Int,Double,Double,Double,Double)]("observation") {
  def samplingTime = column[Timestamp]("samplingtime", O.PrimaryKey)
  def stationId =    column[Int]("stationid", O.PrimaryKey) 
  def airTemperature   = column[Double]("airtemperature")
  def relativeHumidity = column[Double]("relativehumidity")
  def precipitation    = column[Double]("precipitation")
  def airPressure      = column[Double]("pressure")
  def windSpeed        = column[Double]("windspeed")
  def * = samplingTime ~ stationId ~ airTemperature ~ relativeHumidity ~ precipitation ~ airPressure
}