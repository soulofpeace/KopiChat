package actors

import models._
import db._

import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.actor._
import akka.util.Timeout

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._


import slick.session.Session



class ChatRoomActor extends Actor {

  var activeMembers = Set.empty[User]
  val robot = new Robot(self)
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]

  def receive = {
    case Join(user) => {
      activeMembers = activeMembers + user
      sender ! Connected(chatEnumerator)
      self ! NotifyJoin(user)
    }

    case NotifyJoin(user) => {
      notifyAll("join", user, "has entered the room")
    }
    case Talk(user, text) => {
      notifyAll("talk", user, text)
    }
    case Quit(user) => {
      activeMembers = activeMembers - user
      notifyAll("quit", user, "has left the room")
    }
  }

  private def notifyAll(kind: String, user: User, text: String) {
    saveMsgToDB(user, text)
    val msg = JsObject(
      Seq(
        "kind" -> JsString(kind),
        "user" -> JsString(user.name),
        "message" -> JsString(text),
        "activeMembers" -> JsArray(
          activeMembers.toList.map(m => JsString(m.name))
        )
      )
    )
    chatChannel.push(msg)
  }

  private def saveMsgToDB(user:User, text:String)={
    AppDB.database.withSession{implicit session : Session => {
      val message = Message(None, text, user.id, self.path.name, None, None)
      AppDB.messageDal.addMessages(message)
    }}
  }
}

