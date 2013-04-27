package db

import slick.session.Database
import play.api.db.DB
import play.api.Application
import slick.driver.ExtendedProfile



trait DBeable {

  val SLICK_DRIVER = "slick.db.driver"

  val DEFAULT_SLICK_DRIVER = "scala.slick.driver.PostgresDriver"

  def driver(implicit app:Application)={
    val driverClass = app.configuration.getString(SLICK_DRIVER).getOrElse(DEFAULT_SLICK_DRIVER)
    singleton[ExtendedProfile](driverClass)
  }

  def getDb(implicit app : Application) = {
    Database.forDataSource(DB.getDataSource())
  }

  private def singleton[T](name : String)(implicit man: Manifest[T]) : T =
    Class.forName(name + "$").getField("MODULE$").get(man.runtimeClass).asInstanceOf[T]

}
