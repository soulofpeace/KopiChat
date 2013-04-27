package models

case class Message(
  id:Option[Long],
  text: String,
  userId:String,
  chatRoomId:String,
  createdDate:Option[Long],
  updatedDate:Option[Long]
)
