package db

import models._
import components._

import slick.driver.ExtendedProfile

class ChatRoomDal(override  val profile: ExtendedProfile)
  extends ChatRoomComponent
  with UserComponent
  with MessageComponent
  with ChatRoomMembershipComponent 
  with Profile{

  import profile.simple._

  def createChatRooms(implicit session: Session): Unit={
    ChatRooms.ddl.create
  }


  def updateChatRoom(chatroom:ChatRoom)(implicit session: Session)={
    (for{ 
      c <-ChatRooms if c.id === chatroom.id
    }yield c.name ~ c.updatedDate).update(chatroom.name, System.currentTimeMillis)
  }

  def findByChatRoomId(chatroomId: String)(implicit session: Session)={
    (for{
      c <- ChatRooms if c.id === chatroomId
    } yield c).list
  }

  def insertChatRoom(chatroom:ChatRoom)(implicit session: Session)={
    ChatRooms.insert(
      chatroom.copy(
        createdDate=Some(System.currentTimeMillis),
        updatedDate=Some(System.currentTimeMillis)
      )
    )
  }

  def addUserToChatRoom(chatroomMembership:ChatRoomMembership)(implicit session: Session)={
    ChatRoomMemberships.forInsert.insert(
      chatroomMembership.copy(
        createdDate = Some(System.currentTimeMillis),
        updatedDate = Some(System.currentTimeMillis)
    ))
  }

  def getUsersInChatRoom(chatroomId:String)(implicit session: Session)={
    for{
      c <- ChatRooms if c.id === chatroomId
      u <- c.members
    }yield u
  }

  def deleteChatRoom(chatroomId:String)(implicit session: Session)={
    val i = ChatRoomMemberships.where(cm => 
      cm.chatRoomId === chatroomId).
    delete

    val j = Messages.where(m =>
      m.chatRoomId === chatroomId).
    delete

    val k = ChatRooms.where(c=>
      c.id === chatroomId
    ).delete
    (i, j, k)
  }
}
