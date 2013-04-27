package components

import models.User
import db.Profile

trait UserComponent{ 
  this: Profile 
  with ChatRoomComponent
  with ChatRoomMembershipComponent => 
  import profile.simple._

  object Users extends Table[User]("users"){
    def id = column[String]("id", O.PrimaryKey)
    def email = column[String]("email")
    def name = column[String]("name")
    def familyName = column[String]("family_name")
    def givenName = column[String]("given_name")
    def birthday = column[String]("birthday")
    def gender = column[String]("gender")
    def link = column[String]("link")
    def picture = column[String]("picture")
    def createdDate = column[Long]("created_date")
    def updatedDate = column[Long]("updated_date")

    def chatRooms = ChatRoomMemberships.filter(_.userId === id).flatMap(_.chatRoom)
    def createdChatRooms = ChatRooms.filter(_.creatorId === id)

    def * = id ~ email ~ name ~ familyName ~ givenName ~ birthday ~ gender ~ link ~ picture ~ createdDate.? ~ updatedDate.? <> (User, User.unapply _)
  }
}
