package hello

import com.vitthalmirji.valmuri.core.VApplication
import com.vitthalmirji.valmuri.error.VResult
import com.vitthalmirji.valmuri.http.VRoute

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
