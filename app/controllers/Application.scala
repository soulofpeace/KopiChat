package controllers

import db._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

import json.JsonUtil.chatRoomWrite

import slick.session.Session

object Application 
  extends Controller 
  with Secured{

  def index = Action {
    implicit request => {
      Ok(views.html.index())
    }
  }

  def logout = Action {
    Redirect(routes.Application.index).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
 }

  def admin = IsAuthenticated(BodyParsers.parse.anyContent){
    user => request => {
      Async{
        scala.concurrent.Future{
          AppDB.database.withSession{ implicit session:Session => 
            val chatrooms = AppDB.userDal.getCreatedChatRoomsOfUser(user.id)
            val chatroomsJson = Json.stringify(Json.toJson(chatrooms))
            Ok(views.html.admin("admin", user, chatroomsJson))
          }
        }
      }
    }
  }


  def chatrooms = IsAuthenticated(BodyParsers.parse.anyContent){
    user => request => {
      Async{
        scala.concurrent.Future{
          AppDB.database.withSession{ implicit session:Session => {
            val chatrooms = AppDB.userDal.getChatRoomsOfUser(user.id)
            println(chatrooms)
            Ok(views.html.chatrooms("chatrooms", user, chatrooms))
    }}}}}}
}


