package valmuri.cli.commands

import java.nio.file._
import java.nio.file.StandardOpenOption._

object GenerateCommand {

  def controller(name: String): Unit = {
    val className = name.capitalize + "Controller"
    val fileName  = s"${className}.scala"
    val content   = generateControllerContent(className, name.toLowerCase)

    writeFile(Paths.get(s"app/controllers/$fileName"), content)
    // writeFile(Paths.get(s"app/routes/${className}Routes.scala"), generateRoutesContent(className, name.toLowerCase))
    // writeFile(Paths.get(s"test/controllers/${className}Test.scala"), generateControllerTestContent(className))

    println(s"✅ Generated controller: $className")
    println(s"   - app/controllers/$fileName")
    println(s"   - app/routes/${className}Routes.scala")
    println(s"   - test/controllers/${className}Test.scala")
  }

  def model(name: String, fields: List[String]): Unit = {
    val className = name.capitalize
    val tableName = name.toLowerCase + "s"
    // val content = generateModelContent(className, tableName, parseFields(fields))

    // writeFile(Paths.get(s"app/models/$className.scala"), content)
    // generateMigration(s"Create$className", tableName, parseFields(fields))
    // writeFile(Paths.get(s"test/models/${className}Test.scala"), generateModelTestContent(className))

    println(s"✅ Generated model: $className")
    println(s"   - app/models/$className.scala")
    println(s"   - db/migrations/{timestamp}_Create$className.scala")
    println(s"   - test/models/${className}Test.scala")
  }

  private def generateControllerContent(className: String, resourceName: String): String = {
    ""
//    s"""package app.controllers
//       |
//       |import zio._
//       |import zio.http._
//       |import valmuri.http.ValmuriRoutes
//       |
//       |class $className extends ValmuriRoutes {
//       |
//       |  val routes = Routes(
//       |    Method.GET / "$resourceName" -> handler(index),
//       |    Method.GET / "$resourceName" / int("id") -> handler { (id: Int, req: Request) => show(id) },
//       |    Method.POST / "$resourceName" -> handler { (req: Request) => create(req) },
//       |    Method.PUT / "$resourceName" / int("id") -> handler { (id: Int, req: Request) => update(id, req) },
//       |    Method.DELETE / "$resourceName" / int("id") -> handler { (id: Int, req: Request) => delete(id) }
//       |  )
//       |
//       |  private def index: UIO[Response] =
//       |    ZIO.succeed(Response.json("[]"))
//       |
//       |  private def show(id: Int): UIO[Response] =
//       |    ZIO.succeed(Response.json(s"""{"id": }"""))
//                                                        |
//                                                        |  private def create(req: Request): UIO[Response] =
//                                                        |    ZIO.succeed(Response.json("""{"created": true}""").withStatus(Status.Created))
//                                                                                                             |
//                                                                                                             |  private def update(id: Int, req: Request): UIO[Response] =
//                                                                                                             |    ZIO.succeed(Response.json(s"""{"id": $$id, "updated": true}"""))
//                                                                                                                                                                               |
//                                                                                                                                                                               |  private def delete(id: Int): UIO[Response] =
//                                                                                                                                                                               |    ZIO.succeed(Response.json(s"""{"id": $$id, "deleted": true}"""))
//                                                                                                                                                                                                                                                 |}
//                                                                                                                                                                                                                                                 |""".stripMargin
  }

  private def writeFile(path: Path, content: String): Unit = {
    Files.createDirectories(path.getParent)
    Files.write(path, content.getBytes("UTF-8"), CREATE, TRUNCATE_EXISTING)
  }
}
