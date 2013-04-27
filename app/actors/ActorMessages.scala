package actors

import play.api.libs.iteratee._
import play.api.libs.json._

import models._

case class Join(user: User)
case class Quit(user: User)
case class Talk(user: User, text: String)
case class NotifyJoin(user: User)

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)
case class CreateChatRoom(chatroomId: String)
