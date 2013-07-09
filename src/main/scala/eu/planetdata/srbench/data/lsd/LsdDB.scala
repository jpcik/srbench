package eu.planetdata.srbench.data.lsd

import es.upm.fi.oeg.siq.tools.ParameterUtils
import java.net.URL
import es.upm.fi.oeg.morph.db.Rdb
import java.util.Properties
import io.Source._

object LsdDB {
  def loadData(props:Properties)={
    println("yahoooooooo")
    //val state=fromInputStream(getClass.getResourceAsStream("/db/create.sql")).mkString
    val db=new Rdb("lsd")
    val tt=db.query("select count(*) as numb from observation ", Array("numb"))
    tt._1.foreach(a=>println(a.mkString))
    //db.batch(state)
  }
}