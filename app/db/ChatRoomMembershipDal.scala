package db

import models._
import components._

import slick.driver.ExtendedProfile

class ChatRoomMembershipDal(override val profile:ExtendedProfile)
  extends ChatRoomMembershipComponent
  with UserComponent
  with ChatRoomComponent
  with Profile{

  import profile.simple._

  def createChatRoomMembership(implicit session: Session): Unit ={
    ChatRoomMemberships.ddl.create
  }

  def isMemberOfChatRoom(chatRoomId: String, userId:String)(implicit session: Session) ={
    Query(ChatRoomMemberships.where( cm => 
      cm.chatRoomId === chatRoomId && cm.userId === userId).exists
    ).first
  }

  def removeChatRoomMembership(chatRoomId: String, userId:String)(implicit session: Session)={
    ChatRoomMemberships.where( cm => {
      cm.chatRoomId === chatRoomId && cm.userId === userId 
    }).delete
  }
}

