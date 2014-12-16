/*******************************************************************************
 * Copyright (c) 2014 Christian Garbers.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Simplified BSD License
 * which accompanies this distribution
 * 
 * Contributors:
 *     Christian Garbers - initial API and implementation
 ******************************************************************************/
package eoceneServices

import java.sql.Connection
import play.api.Logger
import securesocial.core._
import securesocial.core.providers.{ UsernamePasswordProvider, MailToken }
import scala.concurrent.Future
import securesocial.core.services.{ UserService, SaveMode }
import anorm._
import play.api.db.DB
import play.api.Play.current


class eoceneUserService extends UserService[EoceneUser]{

  /**
	* Finds a SocialUser that maches the specified id
	*
	* @param providerId the provider id
	* @param userId the user id
	* @return an optional profile
	*/
	def find(providerId: String, userId: String): Future[Option[BasicProfile]] =
	{
	    Future.successful(findProfile(providerId, userId))
	}
	
	def findProfile(providerId: String, userId: String): Option[BasicProfile] = {
	  DB.withConnection("chars") { implicit c =>
	    val querry = FIND_USER_BY_EMAIL_AND_ID .onParams(providerId, userId)()
	    if (querry.size==0) return None
	    val row = querry.head
	    Some(BasicProfile(providerId, row[String]("userId"), 
	        row[Option[String]]("firstName"),
	        row[Option[String]]("lastName"),
	        row[Option[String]]("fullName"),
	        row[Option[String]]("email"),
	        row[Option[String]]("AvatarUrl"),
	        AuthenticationMethod(row[String]("AuthenticationMethod")),
	        getOAuth1WithRow(row),getOAuth2WithRow(row),getPasswordInfoWithRow(row)
	        ))
	    }
	}
	
		
	/**
		* Finds a profile by email and provider
		*
		* @param email - the user email
		* @param providerId - the provider id
		* @return an optional profile
		*/
	  def findByEmailAndProvider(email: String, providerId: String): 
		  Future[Option[BasicProfile]] = {
		    DB.withConnection("chars") { implicit c =>
		    val querry = FIND_USER_BY_EMAIL_AND_PROVIDER.onParams(providerId, email)()
		    if (querry.size==0) return Future.successful(None)
		    val row = querry.head
		    Future.successful(Some(BasicProfile(providerId, row[String]("userId"), 
		        row[Option[String]]("firstName"),
		        row[Option[String]]("lastName"),
		        row[Option[String]]("fullName"),
		        row[Option[String]]("email"),
		        row[Option[String]]("AvatarUrl"),
		        AuthenticationMethod(row[String]("AuthenticationMethod")),
		        getOAuth1WithRow(row),getOAuth2WithRow(row),getPasswordInfoWithRow(row)
		        )))    
		    }
	  }
	  

	  

		/**
		* Saves a profile. This method gets called when a user logs in, registers or changes his password.
		* This is your chance to save the user information in your backing store.
		*
		* @param profile the user profile
		* @param mode a mode that tells you why the save method was called
		*/
	  def save(profile: BasicProfile, mode: SaveMode): Future[EoceneUser] = {
			DB.withConnection("chars") { implicit c =>
			  	val maybeProfile = findProfile(profile.providerId, 
			  								   profile.userId) 
			  	maybeProfile match{
		        case None => persistProfile(profile)        
		        		  	 Future.successful(EoceneUser(profile, 
		        		  			   			 List(profile)))         
		        case _ => Future.successful(EoceneUser(profile, List(profile)))		            
		        }			
			}	   
	  	}
	  
	  def persistProfile(profile: BasicProfile)(implicit c:Connection ) = {
		    PERSIST_NEW_USER.onParams(profile.providerId, profile.userId,
		    		profile.firstName,profile.lastName,profile.fullName,
		    		profile.email,profile.avatarUrl,profile.authMethod.method)
		    		.execute()
            if(!(profile.oAuth1Info==None)) persistoAuth1(profile)
            if(!(profile.oAuth2Info==None)) persistoAuth2(profile)
	  	}
	  
	  def persistoAuth1(profile: BasicProfile) (implicit c:Connection ) = {
		 	PERSIST_OAUTH1 .onParams(profile.oAuth1Info.get.token,
		 							 profile.oAuth1Info.get.secret).execute
        	PERSIST_USERS_OAUTH1.onParams(profile.userId , GET_OAUTH1_ID .
        							 onParams(profile.oAuth1Info.get.token,
        		  				     profile.oAuth1Info.get.secret)().
        		  				     head[Int]("id")).execute	
	  	}
	  
	  def persistoAuth2(profile: BasicProfile) (implicit c:Connection ) = {
          PERSIST_OAUTH2 .onParams(profile.oAuth2Info.get.accessToken,
        		  				   profile.oAuth2Info.get.tokenType,
        		  				   profile.oAuth2Info.get.expiresIn,
        		  				   profile.oAuth2Info.get.refreshToken)
        		  				   .execute
          PERSIST_USERS_OAUTH2.onParams(profile.userId , GET_OAUTH2_ID .
              onParams(profile.oAuth2Info.get.accessToken)().
        		  				   head[Int]("id")).execute	    
	  	}
	  
	  def getOAuth1WithRow(row:anorm.Row) : Option[OAuth1Info] = {
	    if (row[Option[String]]("token")==None) return None
	    Some(OAuth1Info(row[String]("token"),row[String]("secret")))   
	  }
  
	  def getOAuth2WithRow(row:anorm.Row) : Option[OAuth2Info] = {
	    if (row[Option[String]]("accessToken")==None) return None
	    Some(OAuth2Info(row[String]("accessToken"),
	        row[Option[String]]("tokenType"),
	        row[Option[Int]]("expiresIn"),
	        row[Option[String]]("tokenType")))   
	  }
	  
	  def getPasswordInfoWithRow(row:anorm.Row):Option[PasswordInfo] = {
	    if (row[Option[String]]("hasher")==None) return None
	    Some(PasswordInfo(row[String]("hasher"),
	        row[String]("password"),
	        row[Option[String]]("salt")))   
	  }
	
	  /**
	* Links the current user to another profile
	*
	* @param current The current user instance
	* @param to the profile that needs to be linked to
	*/
	def link(current: EoceneUser, to: BasicProfile): Future[EoceneUser] ={
	  Future.successful(current)
	}

	/**
	* Returns an optional PasswordInfo instance for a given user
	*
	* @param user a user instance
	* @return returns an optional PasswordInfo
	*/
	def passwordInfoFor(user: EoceneUser): Future[Option[PasswordInfo]] = {
	  Future.successful(None)
	}
	/**
	* Updates the PasswordInfo for a given user
	*
	* @param user a user instance
	* @param info the password info
	* @return
	*/
	def updatePasswordInfo(user: EoceneUser, info: PasswordInfo): Future[Option[BasicProfile]]={
	  Future.successful(None)	  
	}
	/**
	* Saves a mail token. This is needed for users that
	* are creating an account in the system or trying to reset a password
	*
	* Note: If you do not plan to use the UsernamePassword provider just provide en empty
	* implementation
	*
	* @param token The token to save
	*/
	def saveToken(token: MailToken): Future[MailToken] = Future.successful(token)
	/**
	* Finds a token
	*
	* Note: If you do not plan to use the UsernamePassword provider just provide en empty
	* implementation
	*
	* @param token the token id
	* @return
	*/
	def findToken(token: String): Future[Option[MailToken]] = Future.successful(None)
	/**
	* Deletes a token
	*
	* Note: If you do not plan to use the UsernamePassword provider just provide en empty
	* implementation
	*
	* @param uuid the token id
	*/
	def deleteToken(uuid: String): Future[Option[MailToken]] =  Future.successful(None)
	/**
	* Deletes all expired tokens
	*
	* Note: If you do not plan to use the UsernamePassword provider just provide en empty
	* implementation
	*
	*/
	def deleteExpiredTokens() = {}

  
  
  val FIND_USER_BY_EMAIL_AND_PROVIDER = SQL("""
      SELECT * FROM Users LEFT JOIN users_oauth1 ON
      users_oauth1.id_user = Users.userId LEFT JOIN OAuth1Info ON
      OAuth1Info.id = users_oauth1.id_oauth1 LEFT JOIN users_oauth2 ON
      users_oauth2.id_user = Users.userId LEFT JOIN OAuth2Info ON
      OAuth2Info.id = users_oauth2.id_oauth2 LEFT JOIN users_passwords ON
      users_passwords.id_user = Users.userId LEFT JOIN PasswordInfo ON
      PasswordInfo.id = users_passwords.id_password 
      WHERE providerId={pid} AND email={uid}
      """)
  
  val FIND_USER_BY_EMAIL_AND_ID = SQL("""
      SELECT * FROM Users LEFT JOIN users_oauth1 ON
      users_oauth1.id_user = Users.userId LEFT JOIN OAuth1Info ON
      OAuth1Info.id = users_oauth1.id_oauth1 LEFT JOIN users_oauth2 ON
      users_oauth2.id_user = Users.userId LEFT JOIN OAuth2Info ON
      OAuth2Info.id = users_oauth2.id_oauth2 LEFT JOIN users_passwords ON
      users_passwords.id_user = Users.userId LEFT JOIN PasswordInfo ON
      PasswordInfo.id = users_passwords.id_password 
      WHERE providerId={pid} AND Users.userId={uid}
      """)
  val FIND_USER_BY_EMAIL = SQL("""
      SELECT * FROM Users LEFT JOIN users_oauth1 ON
      users_oauth1.id_user = Users.userId LEFT JOIN OAuth1Info ON
      OAuth1Info.id = users_oauth1.id_oauth1 LEFT JOIN users_oauth2 ON
      users_oauth2.id_user = Users.userId LEFT JOIN OAuth2Info ON
      OAuth2Info.id = users_oauth2.id_oauth2 LEFT JOIN users_passwords ON
      users_passwords.id_user = Users.userId LEFT JOIN PasswordInfo ON
      PasswordInfo.id = users_passwords.id_password 
      WHERE Users.email={email}
      """)
  val PERSIST_NEW_USER = SQL("""
      INSERT INTO Users VALUES
      ({pid},{uid},{firstName},{lastName},{fullName},{email},{avatarUrl},
      {AuthMeth})
      """)
  val PERSIST_OAUTH1 = SQL("""
      INSERT INTO OAuth1Info VALUES
      (Null,{token},{secret})
      """)
  val PERSIST_USERS_OAUTH1 = SQL("""
      INSERT INTO users_oauth1 VALUES
      ({id_user},{id_oauth1})
      """)
  val GET_OAUTH1_ID = SQL("""
      SELECT * FROM OAuth1Info WHERE token={token} AND secret={secret}
	  """)
  val PERSIST_OAUTH2 = SQL("""
      INSERT INTO OAuth2Info VALUES
      (Null, {accessToken},{tokenType},{expires},{refreshToken})
      """)
  val PERSIST_USERS_OAUTH2 = SQL("""
      INSERT INTO users_oauth2 VALUES
      ({id_user},{id_oauth1})
      """)
  val GET_OAUTH2_ID = SQL("""
      SELECT * FROM OAuth2Info WHERE accessToken={accessToken} 
	  """)


}
object eoceneUserService{
  	/**
	* Checks whether a userid is allowed to modify a character
	*
*
	* @param userId
	* @param charId
	* @return allowed
	*/
	def userAllowdOnChar(userId:String, charId:Int) = {
		UsersChars.filter(entry=> entry._1 .equals(userId) && 
									  entry._2 ==charId).size>0
	}
  
  	/**
	* Adds acces rights to a char
	*
*
	* @param userId
	* @param charId
	* @return allowed
	*/
	def addUserToChar(userId:String, charId:Int) = {
		UsersChars = (userId, charId)::UsersChars 
	}
	
	def getAllUsersChars() ={
	  DB.withConnection("chars") { implicit c =>
	    SQL("""SELECT * FROM chars_users""")().
	    map(row=>(row[String]("id_user"),row[Int]("id_char"))).toList
	  }
	}
	
	def updateUsersChars() ={
		UsersChars  = getAllUsersChars()
	}
  
  	var UsersChars:List[(String,Int)] = getAllUsersChars()
  	
}

case class EoceneUser(main: BasicProfile, identities: List[BasicProfile])
