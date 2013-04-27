package json

import models._

import java.util.Date

import play.api.libs.functional.syntax._
import play.api.libs.json._

object JsonUtil{
  implicit val userReads= (
    (__ \"id").read[String] and 
    (__ \"email").read[String] and
    (__ \"name").read[String] and
    (__ \"family_name").read[String] and
    (__ \"given_name").read[String] and
    (__ \"birthday").read[String] and
    (__ \"gender").read[String] and
    (__ \"link").read[String] and
    (__ \"picture").read[String] and
    (__ \"createdDate").readNullable[Long] and
    (__ \"updatedDate").readNullable[Long]
  )(User)

  implicit val chatRoomWrite = new Writes[ChatRoom] {
    def writes(c: ChatRoom): JsValue = {
      Json.obj(
        "id" -> c.id,
        "name" -> c.name,
        "creatorId" -> c.creatorId,
        "createdDate"-> c.createdDate,
        "updatedDate"-> c.updatedDate,
        "inviteUrl" -> c.inviteUrl
        )
    }
  }

  implicit val chatRoomRead = (
    (__ \"id").read[String] and
    (__ \"name").read[String] and
    (__ \"creatorId").read[String] and
    (__ \"createdDate").readNullable[Long] and
    (__ \"updatedDate").readNullable[Long]
  )(ChatRoom)

  implicit val chatRoomFormat = Format(chatRoomRead, chatRoomWrite)

}
