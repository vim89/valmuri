import com.vitthalmirji.valmuri.core.VApplication
import com.vitthalmirji.valmuri.error.VResult
import com.vitthalmirji.valmuri.http.VRoute
import hello.PersonalSite
import munit.FunSuite

import java.net.URI
import java.net.http.{ HttpClient, HttpRequest, HttpResponse }

class ValmuriIntegrationTest extends FunSuite {

  val rootPath: String = System.getProperty("project.root")

  test("Personal site should start and serve content") {
    // Start the personal site example
    val server = new Thread(() => PersonalSite.main(Array.empty))
    server.setDaemon(true)
    server.start()

    // Wait for startup
    Thread.sleep(3000)

    val client = HttpClient.newHttpClient()

    // Test home page
    val homeRequest = HttpRequest
      .newBuilder()
      .uri(URI.create("http://localhost:8080/"))
      .build()
    val homeResponse = client.send(homeRequest, HttpResponse.BodyHandlers.ofString())

    if (homeResponse.statusCode() != 200) {
      // Retry after brief wait in case the server isn't fully up
      Thread.sleep(2000)
      val retry = client.send(homeRequest, HttpResponse.BodyHandlers.ofString())
      assertEquals(retry.statusCode(), 200, s"Home page still not 200, got ${retry.statusCode()}")
    }

    assert(homeResponse.body().contains("Vitthal Mirji"))
    assert(homeResponse.body().contains("Staff Data Engineer"))

    // Test blog page
    val blogRequest = HttpRequest
      .newBuilder()
      .uri(URI.create("http://localhost:8080/blog"))
      .build()
    val blogResponse = client.send(blogRequest, HttpResponse.BodyHandlers.ofString())

    assertEquals(blogResponse.statusCode(), 200)
    assert(blogResponse.body().contains("Blog"))

    // Test health endpoint
    val healthRequest = HttpRequest
      .newBuilder()
      .uri(URI.create("http://localhost:8080/health"))
      .build()
    val healthResponse = client.send(healthRequest, HttpResponse.BodyHandlers.ofString())

    assertEquals(healthResponse.statusCode(), 200)
    assert(healthResponse.body().contains("UP"))
  }

  test("30-minute deployment script should work") {
    import scala.sys.process._

    // Run the deployment script in test mode
    val testDir = "/tmp/valmuri-test-" + System.currentTimeMillis()
    val deployScript = new java.io.File(s"$rootPath/scripts/deploy.sh")
    if (deployScript.canExecute) {
      val result = s"${deployScript.getPath} $testDir blog".!
      assertEquals(result, 0)
    } else {
      println("Skipping deploy.sh test – script not found or not executable at " + deployScript.getPath)
    }

    // Verify project structure was created
    import java.nio.file.{ Files, Paths }
    assert(Files.exists(Paths.get(s"$testDir/build.sbt")))
    assert(Files.exists(Paths.get(s"$testDir/src/main/scala/Main.scala")))
    assert(Files.exists(Paths.get(s"$testDir/Dockerfile")))
  }

  test("CLI should generate working projects") {
    import scala.sys.process._

    // Build CLI if not already built
    "sbt cli/assembly".!

    // Test project generation
    val testProject = "cli-test-" + System.currentTimeMillis()
    val cliJar = new java.io.File("../cli/target/scala-2.13/valmuri-cli.jar")
    if (cliJar.exists()) {
      val result = s"java -jar ${cliJar.getPath} new $testProject --template blog".!
      assertEquals(result, 0)

      // Verify the generated project compiles
      val compileResult = Process("sbt compile", new java.io.File(testProject)).!
      assertEquals(compileResult, 0)
    } else {
      println("Skipping CLI generation test – CLI jar not found at " + cliJar.getPath)
    }
  }

  test("Framework performance meets targets") {
    // Startup time test
    val startTime = System.currentTimeMillis()

    val app = new VApplication {
      override def routes() = List(
        VRoute("/", _ => VResult.success("OK"))
      )
    }

    app.start()
    val startupTime = System.currentTimeMillis() - startTime

    // Should start in under 1 second
    assert(startupTime < 1000, s"Startup time was ${startupTime}ms, should be < 1000ms")

    // Memory usage test
    val runtime = Runtime.getRuntime
    runtime.gc()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)

    // Should use less than 50MB
    assert(usedMemory < 50, s"Memory usage was ${usedMemory}MB, should be < 50MB")
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }
}
