package models

import play.api.Play.current
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._

case class User(
  id: ObjectId = new ObjectId,
  username: String,
  deviceId: String,
  deviceType: String
)

object User extends UserDao with UserJson

trait UserDao extends ModelCompanion[User, ObjectId] with UserJson {
  def collection = mongoCollection("users")
  val dao = new SalatDAO[User, ObjectId](collection) {}
  def findOneByUsername(username: String): Option[User] = dao.findOne(MongoDBObject("username" -> username))

  // Indexes
  collection.ensureIndex(DBObject("username" -> 1), "username", unique = true)
}

/**
 * Trait used to convert to and from json
 */
trait UserJson {

  implicit val userJsonWrite = new Writes[User] {
    def writes(u: User): JsValue = {
      Json.obj(
        "id" -> u.id,
        "username" -> u.username,
        "deviceId" -> u.deviceId,
        "deviceType" -> u.deviceType
      )
    }
  }

  implicit val userJsonRead = (
    (__ \ 'id).read[ObjectId] ~
    (__ \ 'username).read[String] ~
    (__ \ 'deviceId).read[String] ~
    (__ \ 'deviceType).read[String]
  )(User.apply _)
}
