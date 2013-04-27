package models

case class ChatRoomMembership(
  id:Option[Long],
  chatRoomId:String,
  userId:String,
  createdDate:Option[Long],
  updatedDate:Option[Long]

)
