package actors

import akka.actor._
import scala.concurrent.duration._

import play.api.libs.json._
import play.api.Play.current
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._

import akka.util.Timeout
import akka.pattern.ask

import slick.session.Session

import db._
import models._

object ChatRoomRegistry {


  lazy val default = Akka.system.actorOf(Props[ChatRoomRegistry], "chatRoomRegistry")

  def joinChatRoom(chatroomId:String,
    userId:String):scala.concurrent.Future[(Iteratee[JsValue,_],Enumerator[JsValue])]={
    AppDB.database.withSession{ implicit session : Session =>{
      (for{
           chatroom <- AppDB.chatroomDal.findByChatRoomId(chatroomId).headOption
           user <- AppDB.userDal.findByUserId(userId).headOption
      }yield joinChatRoom(chatroom, user)).getOrElse(
        scala.concurrent.Future{
          fail("unauthorized")
        }
      )
    }}
  }

  def joinChatRoom(chatroom:ChatRoom, user:User)={
    implicit val timeout = Timeout(1.second)

    getOrCreateChatroom(chatroom.id).flatMap { chatroomActorRef:ActorRef =>
      (chatroomActorRef ? Join(user)).map {
        case Connected(enumerator) => {
            //Create an Iteratee to consume the feed
            val iteratee = Iteratee.foreach[JsValue] { event =>
              chatroomActorRef ! Talk(user, (event \ "text").as[String])
              }.mapDone { _ =>
                default ! Quit(user)
            }
            (iteratee,enumerator)
        }
        case CannotConnect(error) => {
          fail(error)
        }
      }
    }
  }

  private def getOrCreateChatroom(chatroomId:String)={
    implicit val timeout = Timeout(1.second)
    (default ? CreateChatRoom(chatroomId)).mapTo[ActorRef]
  }

  private def fail(error:String="")={
    val iteratee = Done[JsValue,Unit]((),Input.EOF)

    //Send an error and close the socket
    val enumerator = Enumerator[JsValue](JsObject(Seq("error" ->
      JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))

    (iteratee,enumerator)
  }
}

class ChatRoomRegistry extends Actor{
  def receive={
    case CreateChatRoom(chatroomId) =>{
      sender!context.child(chatroomId).getOrElse(context.actorOf(Props[ChatRoomActor], chatroomId))
    }
  }
}
