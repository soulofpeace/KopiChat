package controllers

import models.User
import db.AppDB
import json.JsonUtil.userReads

import play.Logger
import play.api._
import play.api.mvc._
import play.api.libs._
import play.api.libs.json._
import play.api.libs.ws.WS

import scala.concurrent._

import com.ning.http.client.RequestBuilder
import com.ning.http.client.FluentStringsMap


import slick.session.Session

object Google extends Controller{

  val AUTH_URL="https://accounts.google.com/o/oauth2/auth" 
  val TOKEN_URL="https://accounts.google.com/o/oauth2/token"
  val USER_INFO_URL ="https://www.googleapis.com/oauth2/v1/userinfo"
  val CLIENT_SECRET=Play.current.configuration.getString("google.client.secret").get
  val CLIENT_EMAIl=Play.current.configuration.getString("google.client.email").get
  val CLIENT_ID=Play.current.configuration.getString("google.client.id").get


  def authenticate= Action{ implicit request => {
    val authenticateRequestBuilder = new RequestBuilder
    val authenticateRequestUrl = authenticateRequestBuilder.setUrl(AUTH_URL).
      addQueryParameter("scope", "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile").
      addQueryParameter("state", "/profile").
      addQueryParameter("redirect_uri", routes.Google.callback.absoluteURL()).
      addQueryParameter("response_type", "code").
      addQueryParameter("client_id", CLIENT_ID).build.getRawUrl

    Redirect(authenticateRequestUrl)

  }}

  def callback=Action{
    implicit request => {
      Async{
          import play.api.libs.concurrent.Execution.Implicits._
          val code  = request.queryString.get("code").getOrElse(Seq()).headOption
          if(code.isDefined){
            for{
              tokenResponse <- exchangeToken(code.get)(request)
              userInfo <- getUserInfo(getToken(tokenResponse.json))
            } yield {
              userInfo.json.validate[User].fold(
                 valid = ( user => {
                    AppDB.database.withSession{
                      implicit session: Session => {
                        AppDB.userDal.createOrUpdateUser(user)
                      }
                    }
                    Redirect(routes.Application.admin()).withSession("userId" -> user.id)
                 }),
                 invalid = ( e => {
                    BadRequest("Invalid User")
                 })
             )}
          }
          else{
            future{
              Ok("Not Ok")
            }
          }
      }
    }
  }

  private def exchangeToken(code:String)(implicit request:Request[_])={
    WS.url(TOKEN_URL).
    withHeaders(("Content-Type", "application/x-www-form-urlencoded")).post(
      Map(
        "code"-> Seq(code),
        "client_id"-> Seq(CLIENT_ID),
        "client_secret"-> Seq(CLIENT_SECRET),
        "redirect_uri"-> Seq(routes.Google.callback.absoluteURL()),
        "grant_type"-> Seq("authorization_code")))
  }

  private def getUserInfo(token:String)={
      WS.url(USER_INFO_URL).withQueryString(("access_token", token)).
        withHeaders(("Authorization", "Bearer "+token)).get

  }

  private def getToken(jsonVal:JsValue)={
    (jsonVal\"access_token").as[String]
  }




}
