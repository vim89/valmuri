#!/bin/bash
set -e

PROJECT_NAME=${1:-"test-project"}
TEMPLATE=${2:-"basic"}

echo "🚀 Creating Valmuri project: $PROJECT_NAME"

# Create minimal project structure
mkdir -p "$PROJECT_NAME/src/main/scala"

# Create build.sbt
cat > "$PROJECT_NAME/build.sbt" << EOF
name := "$PROJECT_NAME"
version := "0.1.0"
scalaVersion := "2.13.16"
libraryDependencies += "com.vitthalmirji" %% "valmuri-core" % "0.1.0"
EOF

# Create Main.scala
cat > "$PROJECT_NAME/src/main/scala/Main.scala" << 'EOF'
import com.vitthalmirji.valmuri._

object Main extends VApplication {
  def routes() = List(
    VRoute("/", _ => VResult.success("Hello from Valmuri!"))
  )

  def main(args: Array[String]): Unit = {
    start()
    Thread.currentThread().join()
  }
}
EOF

# Create Dockerfile
cat > "$PROJECT_NAME/Dockerfile" << EOF
FROM openjdk:11-jre-slim
COPY target/scala-2.13/*.jar /app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app.jar"]
EOF

echo "✅ Project created successfully"
exit 0
