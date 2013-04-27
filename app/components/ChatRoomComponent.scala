package components

import models.ChatRoom
import db.Profile


trait ChatRoomComponent{
  this: Profile with UserComponent with ChatRoomMembershipComponent=>
    import profile.simple._

  object ChatRooms extends Table[ChatRoom]("chatrooms"){
    def id = column[String]("id", O.PrimaryKey)
    def name = column[String]("name")
    def creatorId = column[String]("creator_id")
    def createdDate = column[Long]("created_date")
    def updatedDate = column[Long]("updated_date")
    def creator = foreignKey("fk_creator", creatorId, Users)(_.id)
    def members =  ChatRoomMemberships.filter(_.chatRoomId === id).flatMap(_.user)

    def  * = id ~ name ~ creatorId  ~ createdDate.? ~ updatedDate.? <>
    (ChatRoom, ChatRoom.unapply _)
  }


}
