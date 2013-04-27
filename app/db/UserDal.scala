package db

import models._
import components._

import slick.driver.ExtendedProfile

class UserDal(override val profile: ExtendedProfile) 
  extends UserComponent
  with ChatRoomComponent
  with ChatRoomMembershipComponent
  with Profile {

  import profile.simple._

  def createRobot(implicit session: Session)={
    val robot = User("robot", "robot", "robot", "robot", "robot", "0000-01-01", "M", "kopichat.heroku.com", "kopichat.heroku.com", None,
      None)
    createOrUpdateUser(robot)
  }

  def userByIdQuery(userId:String) = for{
      u <- Users if u.id === userId
    } yield u

  def createUsers(implicit session: Session): Unit = {
    Users.ddl.create //helper method to create all tables
  }

  def findByUserId(userId:String)(implicit session: Session)={
    userByIdQuery(userId).list
  }

  def insertUser(user:User)(implicit session: Session)={
    Users.insert(user.copy(createdDate=Some(System.currentTimeMillis), updatedDate=Some(System.currentTimeMillis)))
  }

  def updateUser(user:User)(implicit session: Session)={
    userByIdQuery(user.id).update(user.copy(updatedDate=Some(System.currentTimeMillis)))
  }

  def createOrUpdateUser(user:User)(implicit session:Session):User={
    findByUserId(user.id).map(
      user => {
        updateUser(user)
        user
      }
    ).headOption.getOrElse{
      insertUser(user)
      user
    }
  }

  def getChatRoomsOfUser(userId:String)(implicit session: Session)= (for{
    u <- Users if u.id === userId
    c <- u.chatRooms
  } yield c).list

  def getCreatedChatRoomsOfUser(userId:String)(implicit session: Session)= (for{
    u <- Users if u.id === userId
    c <- u.createdChatRooms
  } yield c).list

  def joinChatRoom(chatroomMembership:ChatRoomMembership)(implicit session: Session)={
    ChatRoomMemberships.forInsert.insert(
      chatroomMembership.copy(
        createdDate = Some(System.currentTimeMillis),
        updatedDate = Some(System.currentTimeMillis)
    ))
  }

  def isOwnerOf(chatRoomId:String, userId:String)(implicit session: Session)={
    Query(ChatRooms.where( c => 
      c.id === chatRoomId && c.creatorId === userId).exists
    ).first
  }
}
