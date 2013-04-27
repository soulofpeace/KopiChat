package components

import models.Message
import db.Profile


trait MessageComponent{
  this: Profile with UserComponent with ChatRoomComponent =>
    import profile.simple._

  object Messages extends Table[Message]("messages"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def text = column[String]("text")
    def userId = column[String]("user_id")
    def chatRoomId = column[String]("chat_room_id")
    def createdDate = column[Long]("created_date")
    def updatedDate = column[Long]("updated_date")
    def user = foreignKey("fk_user_id", userId, Users)(_.id)
    def chatRoom = foreignKey("fk_chatroom", chatRoomId, ChatRooms)(_.id)


    def *  = id.? ~ text ~ userId ~ chatRoomId ~ createdDate.? ~ updatedDate.? <>
    (Message, Message.unapply  _)
    def forInsert = text ~ userId ~ chatRoomId ~ createdDate.? ~ updatedDate.?  <>
         ({ (t, u, c, cd, ud) => Message (None, t, u, c, cd, ud) }, { m:Message => Some((m.text, m.userId, m.chatRoomId, m.createdDate, m.updatedDate))})

  }
}
