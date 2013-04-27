package models

case class User(
  id:String,
  email:String,
  name:String,
  familyName:String,
  givenName:String,
  birthday:String,
  gender:String,
  link:String,
  picture:String,
  createdDate:Option[Long],
  updatedDate:Option[Long]
)


