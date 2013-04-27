package components

import models.ChatRoomMembership

import db.Profile

trait ChatRoomMembershipComponent{
  this: Profile with UserComponent with ChatRoomComponent =>

  import profile.simple._

  object ChatRoomMemberships extends Table[ChatRoomMembership]("chatroom_memberships"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def chatRoomId = column[String]("chat_room_id")
    def userId = column[String]("user_id")
    def createdDate = column[Long]("created_date")
    def updatedDate = column[Long]("updated_date")

    def chatRoom = foreignKey("fk_chatroom", chatRoomId, ChatRooms)(_.id)
    def user = foreignKey("fk_user_id", userId, Users)(_.id)

    def * = id.? ~ chatRoomId ~ userId ~ createdDate.? ~ updatedDate.? <>
      (ChatRoomMembership, ChatRoomMembership.unapply _)

    def forInsert = chatRoomId ~ userId ~ createdDate.? ~ updatedDate.? <>
      ({(c, u, cd, ud) => ChatRoomMembership(None, c, u , cd, ud)}, {cm:ChatRoomMembership => Some((cm.chatRoomId, cm.userId, cm.createdDate, cm.updatedDate))})
}
  }
