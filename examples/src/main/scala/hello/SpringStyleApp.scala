package hello

import com.vitthalmirji.valmuri.{ VApplication, VController, VResult, VRoute }

/**
 * Spring Boot style - everything auto-configured!
 */
object SpringStyleApp extends VApplication {

  override def routes(): List[VRoute] = List(
    VRoute("/", _ => VResult.success(s"🎉 Hello from ${getConfig.appName}!")),
    VRoute("/config", _ => VResult.success(s"""{"port": ${getConfig.serverPort}}"""))
  )
}

trait UserService {
  def getUsers: String
}

class UserServiceImpl extends UserService {
  def getUsers: String = """[{"id": 1, "name": "Spring Style User"}]"""
}

class UserController(userService: UserService) extends VController {
  def routes(): List[VRoute] = List(
    VRoute("/api/users", _ => VResult.success(userService.getUsers))
  )
}

object SpringMain {
  def main(args: Array[String]): Unit =
    println(SpringStyleApp.start())
}
