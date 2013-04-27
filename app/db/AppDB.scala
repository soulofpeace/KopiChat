package db

import play.api.Play.current

import slick.session.Session

object AppDB extends DBeable {

  lazy val database = getDb

  lazy val userDal = new UserDal(driver)

  lazy val messageDal = new MessageDal(driver)

  lazy val chatroomDal = new ChatRoomDal(driver)

  lazy val chatRoomMembershipDal = new ChatRoomMembershipDal(driver)

}
