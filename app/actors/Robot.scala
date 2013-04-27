package actors

import akka.actor._
import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.Play.current
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._

import akka.util.Timeout
import akka.pattern.ask

import models._


case class Robot(chatRoomActor:ActorRef){
   implicit val timeout = Timeout(1.second) // Make the robot join the room
   val robotUser =  User("robot", "", "Robot", "", "", "", "M", "", "", None, None)

   val loggerIteratee = Iteratee.foreach[JsValue](event => Logger("robot").info(event.toString))


   chatRoomActor ? (Join(robotUser)) map {
     case Connected(robotChannel) =>
       // Apply this Enumerator on the logger.
       robotChannel |>> loggerIteratee
   }

   // Make the robot talk every 30 seconds
   Akka.system.scheduler.schedule(
     60.seconds,
     60.seconds,
     chatRoomActor,
     Talk(robotUser, "I'm still alive")
   )
}

