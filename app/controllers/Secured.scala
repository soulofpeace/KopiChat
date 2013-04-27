package controllers

import db._
import models._

import play.api._
import play.api.mvc._
import play.api.mvc.BodyParsers._
import play.api.libs.json._

import slick.session.Session

/**
* Provide security features
*/
trait Secured {
  private def userId(request: RequestHeader) = request.session.get("userId")

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Google.authenticate)

  def IsAuthenticated[A](bp:BodyParser[A])(f: => User  => Request[A] => Result) = 
    Security.Authenticated(userId, onUnauthorized) { userId =>{
      val user = getUserById(userId)
      if(user.isDefined){
        Action(bp)(request => f(user.get)(request))
      }
      else{
        Action(bp)(request => onUnauthorized(request))
      }
    }
  }

  def IsMemberOfChatRoom[A](bp:BodyParser[A])(f: => User => ChatRoom  => Request[A] => Result):EssentialAction ={
    IsAuthenticated(bp){ user => request => {
      val chatRoomId = for{
        idValues <- request.queryString.get("chatRoomId")
        value <- idValues.headOption
      } yield value
      if(chatRoomId.isDefined){
        if (checkMemberShip(chatRoomId.get, user.id)){
          AppDB.database.withSession{ implicit session => 
            f(user)(AppDB.chatroomDal.findByChatRoomId(chatRoomId.get).head)(request)
          }
        }
        else{
          Results.Forbidden
        }
      }
      else{
        Results.Ok("chatroomId required")
      }

  }}}

  def IsMemberOfChatRoom[A](chatRoomId:String)(bp:BodyParser[A])(f: => User => ChatRoom  => Request[A] => Result):EssentialAction={
    IsAuthenticated(bp){ user => request => {
        if (checkMemberShip(chatRoomId, user.id)){
          AppDB.database.withSession{ implicit session => 
            f(user)(AppDB.chatroomDal.findByChatRoomId(chatRoomId).head)(request)
          }
        }
        else{
          Results.Forbidden
        }
      }
    }
  }

  def IsOwnerOfChatRoom[A](bp:BodyParser[A])(f: => User => ChatRoom  => Request[A] => Result) ={
    IsAuthenticated(bp){ user => request => {
      val chatRoomId = for{
        idValues <- request.queryString.get("chatRoomId")
        value <- idValues.headOption
      } yield value
      if(chatRoomId.isDefined){
        AppDB.database.withSession{
          implicit session : Session => {
            if(AppDB.userDal.isOwnerOf(chatRoomId.get, user.id)){
              f(user)(AppDB.chatroomDal.findByChatRoomId(chatRoomId.get).head)(request)
            }
            else{
              Results.Forbidden
            }
          }
        }
      }
      else{
        Results.Ok("chatroomId required")
      }
    }}
  }

   def checkMemberShip(chatRoomId:String, userId:String)={
    AppDB.database.withSession{
      implicit session : Session => {
        AppDB.chatRoomMembershipDal.isMemberOfChatRoom(chatRoomId, userId)
      }
    }
  }

   def isUserPresent(userId: String)={
     !getUserById(userId).isEmpty
   }

   def getLoggedInUserId(request: RequestHeader)={
     userId(request)
   }

   private def getUserById(userId:String)={
      AppDB.database.withSession{ implicit session :Session => {
        AppDB.userDal.findByUserId(userId).headOption
      }}
   }
}
