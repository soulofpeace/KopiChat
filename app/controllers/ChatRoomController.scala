package controllers

import db._
import models._


import java.util.UUID

import slick.session.Session

import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.mvc._

import actors.ChatRoomRegistry
import json.JsonUtil.chatRoomRead
import json.JsonUtil.chatRoomWrite

object ChatRoomController
  extends Controller
  with Secured{

  def createChatRoom= IsAuthenticated(BodyParsers.parse.anyContent){
    user => implicit request => {
      val name = for{
        nameValues <- request.queryString.get("name")
        value <- nameValues.headOption
      } yield value
      if(name.isDefined){
        val newChatRoom = ChatRoom( 
          getNextChatRoomId,
          name.get,
          user.id,
          Some(System.currentTimeMillis),
          Some(System.currentTimeMillis)
        )
        Async{
          scala.concurrent.Future{
            AppDB.database.withTransaction{
              implicit session:Session =>{
                if (AppDB.chatroomDal.insertChatRoom(newChatRoom) > 0 ){
                  AppDB.userDal.joinChatRoom(
                    ChatRoomMembership(
                      None,
                      newChatRoom.id,
                      user.id,
                      None,
                      None
                    )
                  )
                  val message  = "invite user to this new url: "+ routes.ChatRoomController.joinChatRoom(newChatRoom.id).absoluteURL()
                  Ok(Json.toJson(
                      Json.obj(
                        "success" -> message,
                        "chatroom" -> Json.toJson(newChatRoom)
                      )
                    )
                  )
                }
                else{
                  Ok(Json.
                    toJson(
                      Map(
                        "error" ->
                        "create fail"
                      )
                    )
                  )
                }
              }
            }
          }
        }
      }
      else{
        Ok(Json.
          toJson(
            Map(
              "error" ->
              "chat room name required"
              )
            )
          )
      }
    }
  }

  def listChatRooms = IsAuthenticated(BodyParsers.parse.anyContent){
    user => implicit request => {
      Async{
        scala.concurrent.Future{
          AppDB.database.withSession{ implicit session:Session => 
            val chatrooms = AppDB.userDal.getCreatedChatRoomsOfUser(user.id)
            Ok(Json.toJson(chatrooms))
          }
        }
      }
    }
  }


  def show(chatRoomId:String) = IsMemberOfChatRoom(chatRoomId)(BodyParsers.parse.anyContent){
    user => chatroom => implicit request => {
      Ok(views.html.chat(user, chatroom))
    }
  }

  def chat(chatroomId:String, userId:String) = WebSocket.async[JsValue] { request  =>{
    if(userId == getLoggedInUserId(request).get && checkMemberShip(chatroomId, userId)){
      ChatRoomRegistry.joinChatRoom(chatroomId, userId)
    }
    else{
      scala.concurrent.Future{
        val iteratee = Done[JsValue,Unit]((),Input.EOF)

        //Send an error and close the socket
        val enumerator = Enumerator[JsValue](JsObject(Seq("error" ->
          JsString("Not Authorized")))).andThen(Enumerator.enumInput(Input.EOF))

        (iteratee,enumerator)
      }
    }
  }}


  def joinChatRoom(chatRoomId:String) = IsAuthenticated(BodyParsers.parse.anyContent){
    user => request => {
      Async{
        scala.concurrent.Future{
          AppDB.database.withSession{
            implicit session: Session => {
              val chatroomMembership = ChatRoomMembership(None, chatRoomId, user.id, None, None)
              if(!checkMemberShip(chatRoomId, user.id)){
                AppDB.chatroomDal.addUserToChatRoom(chatroomMembership)
              }
              Redirect(routes.ChatRoomController.show(chatRoomId))
            }
          }
        }
      }
    }
  }

  def leaveChatRoom(chatRoomId:String) = IsMemberOfChatRoom(chatRoomId)(BodyParsers.parse.anyContent){
    user => chatRoom => request => {
      Async{
        scala.concurrent.Future{
          AppDB.database.withSession{
            implicit session:Session => {
              Ok(
                Json.toJson(
                  Map(
                    "deleted" -> 
                    AppDB.chatRoomMembershipDal.removeChatRoomMembership(chatRoom.id, user.id).toString
                  )
                )
              )
            }
          }
        }
      }
    }
  }

  def updateChatRoom = IsOwnerOfChatRoom(BodyParsers.parse.json){
    user => chatRoom => request => {
      request.body.validate[ChatRoom].fold(
        valid = ( chatRoom => {
          AppDB.database.withSession{
            implicit session: Session => {
              AppDB.chatroomDal.updateChatRoom(chatRoom)
              Ok(
                Json.toJson(
                  Map(
                    "success" ->
                    "update successful"
                  )
                )
              )
            }
          }
        }),
        invalid = ( e => {
          Ok(
            Json.toJson(
              Map(
                "error" ->
                  e.toString
              )
            )
          )
        })
      )
    }
  }

  def deleteChatRoom = IsOwnerOfChatRoom(BodyParsers.parse.anyContent){
    user => chatRoom => request => {
      Async{
        scala.concurrent.Future{
          AppDB.database.withTransaction{
            implicit session:Session => {
              val result = AppDB.chatroomDal.deleteChatRoom(chatRoom.id)
              Ok(
                Json.toJson(
                  Map(
                    "members deleted" -> result._1,
                    "messages deleted" -> result._2,
                    "chatroom deleted" -> result._3
                  )
                )
              )
    }}}}}}



  private def getNextChatRoomId=UUID.randomUUID.toString.replace("-", "")

}
