package hello

import com.vitthalmirji.valmuri.{ VApplication, VRoute }
import com.vitthalmirji.valmuri.VResult

object HelloApp extends VApplication {

  override def routes(): List[VRoute] = List(
    VRoute("/", _ => VResult.success("🎉 Hello from Valmuri!")),
    VRoute("/api/hello", _ => VResult.success("""{"message": "Hello World!", "framework": "Valmuri"}"""))
  )
}

object Main {
  def main(args: Array[String]): Unit =
    println(HelloApp.start())
}
