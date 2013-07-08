package models

case class User(id: String, deviceId: String, deviceType: String)

object User {
  def all(): List[Task] = Nil
  def create(id: String, deviceId: String, deviceType: String) {}
  def get(id: String) {}
}
