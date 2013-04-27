package db

import models._
import components._

import scala.math._

import slick.driver.ExtendedProfile

class MessageDal(override val profile: ExtendedProfile)
  extends MessageComponent
  with UserComponent
  with ChatRoomComponent
  with ChatRoomMembershipComponent
  with Profile{

  import profile.simple._

  def createMessages(implicit session: Session): Unit ={
    Messages.ddl.create
  }

  def getMessages(chatroomId:String, offset:Int, limit:Int)(implicit session: Session)={
    Messages.
      where(_.chatRoomId === chatroomId).
      sortBy(c => {
          if(offset>0){
            c.createdDate.asc
          }
          else{
            c.createdDate.desc
          }
        }).
      drop(abs(offset)).take(limit) 
  }

  def addMessages(message:Message)(implicit session: Session)={
    Messages.forInsert.insert(
      message.copy(
        createdDate=Some(System.currentTimeMillis),
        updatedDate=Some(System.currentTimeMillis)
      ))
  }

}
