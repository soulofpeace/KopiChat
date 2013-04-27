package models

import play.api._
import play.api.mvc._

import controllers._

case class ChatRoom(
  id:String,
  name:String,
  creatorId:String,
  createdDate:Option[Long],
  updatedDate:Option[Long]
){
  def inviteUrl:String = routes.ChatRoomController.joinChatRoom(id).url
  def enterUrl:String =  routes.ChatRoomController.show(id).url
}

