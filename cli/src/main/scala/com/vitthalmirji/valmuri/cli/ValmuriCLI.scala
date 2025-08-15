package com.vitthalmirji.valmuri.cli

import java.nio.file.{ Files, Paths }

object ValmuriCLI {
  def main(args: Array[String]): Unit =
    args.toList match {
      case "new" :: name :: Nil =>
        generateProject(name, "basic")

      case "new" :: name :: "--template" :: template :: Nil =>
        generateProject(name, template)

      case "dev" :: Nil =>
        runDevServer()

      case "help" :: Nil =>
        printHelp()

      case _ =>
        println("Unknown command. Use 'valmuri help' for usage.")
    }

  def generateProject(name: String, template: String): Unit = {
    val projectDir = Paths.get(name)
    Files.createDirectories(projectDir)

    template match {
      case "blog"      => generateBlogProject(projectDir, name)
      case "api"       => generateApiProject(projectDir, name)
      case "portfolio" => generatePortfolioProject(projectDir, name)
      case _           => generateBasicProject(projectDir, name)
    }

    println(s"✅ Created $name project")
    println(s"📁 cd $name")
    println(s"🚀 sbt run")
  }

  private def generateBlogProject(projectDir: java.nio.file.Path, name: String): Unit = {
    // Create directory structure
    Files.createDirectories(projectDir.resolve("src/main/scala"))
    Files.createDirectories(projectDir.resolve("src/main/resources/static/css"))
    Files.createDirectories(projectDir.resolve("src/main/resources/posts"))
    Files.createDirectories(projectDir.resolve("src/main/resources/templates"))

    // Generate main application
    val appContent = s"""
package ${name.toLowerCase}

import com.vitthalmirji.valmuri._

object ${name.capitalize}App extends VApplication {
  
  override def routes(): List[VRoute] = List(
    VRoute("/", _ => VResult.success(renderHomePage())),
    VRoute("/blog", _ => VResult.success(renderBlogIndex())),
    VRoute("/about", _ => VResult.success(renderAboutPage()))
  ) ++ generateBlogRoutes()
  
  private def renderHomePage(): String = {
    '''<!DOCTYPE html>
    <html>
    <head>
        <title>$name - Personal Blog</title>
        <link rel="stylesheet" href="/static/css/style.css">
    </head>
    <body>
        <nav>
            <a href="/">Home</a>
            <a href="/blog">Blog</a>
            <a href="/about">About</a>
        </nav>
        <main>
            <h1>Welcome to $name</h1>
            <p>A personal blog built with Valmuri framework.</p>
            <a href="/blog" class="cta">Read Blog Posts</a>
        </main>
    </body>
    </html>'''
  }
  
  private def renderBlogIndex(): String = {
    val posts = loadMarkdownPosts()
    val postsHtml = posts.map(post => 
      s'''<article>
         <h2><a href="/blog/$${post.slug}">${post.title}</a></h2>
         <p>${post.excerpt}</p>
         <time>${post.date}</time>
       </article>'''
    ).mkString("\\n")
    
    s'''<!DOCTYPE html>
    <html>
    <head><title>Blog - $name</title></head>
    <body>
        <h1>Blog Posts</h1>
        <div class="posts">$$postsHtml</div>
    </body>
    </html>'''
  }
  
  private def generateBlogRoutes(): List[VRoute] = {
    loadMarkdownPosts().map(post => 
      VRoute(s"/blog/$${post.slug}", _ => VResult.success(renderPost(post)))
    )
  }
  
  case class BlogPost(title: String, content: String, slug: String, date: String, excerpt: String)
  
  private def loadMarkdownPosts(): List[BlogPost] = {
    import java.nio.file.{Files, Paths}
    import scala.jdk.CollectionConverters._
    
    val postsDir = Paths.get("src/main/resources/posts")
    if (Files.exists(postsDir)) {
      Files.list(postsDir).iterator().asScala
        .filter(_.toString.endsWith(".md"))
        .map(parseMarkdownPost)
        .toList
        .sortBy(_.date).reverse
    } else {
      List.empty
    }
  }
  
  private def parseMarkdownPost(path: java.nio.file.Path): BlogPost = {
    val content = Files.readString(path)
    val lines = content.split("\\n")
    val title = lines.find(_.startsWith("# ")).map(_.substring(2)).getOrElse("Untitled")
    val slug = path.getFileName.toString.replace(".md", "")
    val date = java.time.LocalDate.now().toString
    val excerpt = lines.drop(1).find(_.trim.nonEmpty).getOrElse("")
    
    BlogPost(title, markdownToHtml(content), slug, date, excerpt)
  }
  
  private def markdownToHtml(markdown: String): String = {
    markdown
      .replaceAll("^# (.*)", "<h1>$$1</h1>")
      .replaceAll("^## (.*)", "<h2>$$1</h2>")
      .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$$1</strong>")
      .replaceAll("\\*(.*?)\\*", "<em>$$1</em>")
      .replaceAll("\\n", "<br>")
  }
  
  private def renderPost(post: BlogPost): String = {
    s'''<!DOCTYPE html>
    <html>
    <head><title>${post.title} - $name</title></head>
    <body>
        <nav><a href="/blog">← Back to Blog</a></nav>
        <article>
            <h1>${post.title}</h1>
            <time>${post.date}</time>
            <div class="content">${post.content}</div>
        </article>
    </body>
    </html>'''
  }
  
  def main(args: Array[String]): Unit = {
    start() match {
      case VResult.Success(_) => 
        println("✅ Blog running at http://localhost:8080")
        Thread.currentThread().join()
      case VResult.Failure(error) => 
        println(s"❌ Failed to start: $${error.message}")
    }
  }
}
    """.stripMargin.replace("'''", "\"\"\"")

    Files.write(projectDir.resolve("src/main/scala/Main.scala"), appContent.getBytes)

    // Generate build.sbt
    val buildContent = s"""
name := "$name"
version := "0.1.0"
scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  "com.vitthalmirji" %% "valmuri-core" % "0.1.0"
)

mainClass := Some("${name.toLowerCase}.${name.capitalize}App")
    """.stripMargin

    Files.write(projectDir.resolve("build.sbt"), buildContent.getBytes)

    // Generate sample blog post
    val postContent = """# Welcome to My Blog

This is my first blog post using **Valmuri framework**.

## What is Valmuri?

Valmuri brings the productivity of Django and Rails to **Scala** with:

- Type safety
- Functional programming
- Zero configuration
- 30-minute deployment

## Getting Started

Creating a blog with Valmuri is simple:


valmuri new my-blog --template blog
cd my-blog
sbt run


*Happy blogging with Valmuri!*
"""

    Files.write(projectDir.resolve("src/main/resources/posts/welcome.md"), postContent.getBytes)

    // Generate CSS
    val cssContent = """
body {
font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
line-height: 1.6;
max-width: 800px;
margin: 0 auto;
padding: 20px;
color: #333;
}

nav {
margin-bottom: 30px;
padding-bottom: 20px;
border-bottom: 1px solid #eee;
}

nav a {
margin-right: 20px;
text-decoration: none;
color: #007cba;
}

.cta {
display: inline-block;
background: #007cba;
color: white;
padding: 10px 20px;
text-decoration: none;
border-radius: 4px;
margin-top: 20px;
}

article {
margin-bottom: 30px;
padding-bottom: 20px;
border-bottom: 1px solid #eee;
}

article h2 a {
text-decoration: none;
color: #333;
}

time {
color: #666;
font-size: 0.9em;
}
"""

    Files.write(projectDir.resolve("src/main/resources/static/css/style.css"), cssContent.getBytes)

    // Generate deployment script
    val deployScript = s"""#!/bin/bash
set -e

echo "🚀 Deploying $name to production..."

# Build the application
echo "🔨 Building..."
sbt assembly

# Create Dockerfile
cat > Dockerfile << 'EOF'
FROM openjdk:11-jre-slim
COPY target/scala-2.13/$name-assembly-0.1.0.jar /app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app.jar"]
EOF

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t $name .

# Deploy based on available platform
if command -v heroku &> /dev/null; then
echo "🚀 Deploying to Heroku..."
heroku create $name-$$(date +%s) 2>/dev/null || true
heroku container:push web -a $name-$$(date +%s)
heroku container:release web -a $name-$$(date +%s)
echo "✅ Deployed!"
elif command -v railway &> /dev/null; then
echo "🚀 Deploying to Railway..."
railway up
echo "✅ Deployed!"
else
echo "🐳 Docker image ready: $name"
echo "🚀 Run with: docker run -p 8080:8080 $name"
fi
"""

    Files.write(projectDir.resolve("deploy.sh"), deployScript.getBytes)
    projectDir.resolve("deploy.sh").toFile.setExecutable(true)
  }

  private def generateBasicProject(projectDir: java.nio.file.Path, name: String): Unit = {
    // Similar structure but simpler app
    Files.createDirectories(projectDir.resolve("src/main/scala"))

    val appContent = s"""
import com.vitthalmirji.valmuri._

object ${name.capitalize}App extends VApplication {
def routes() = List(
VRoute("/", _ => VResult.success("Welcome to $name!")),
VRoute("/api/hello", _ => VResult.success('''{"message": "Hello from Valmuri!"}'''))
)

def main(args: Array[String]): Unit = {
start()
Thread.currentThread().join()
}
}
""".stripMargin.replace("'''", "\"\"\"")

    Files.write(projectDir.resolve("src/main/scala/Main.scala"), appContent.getBytes)

    val buildContent = s"""
name := "$name"
version := "0.1.0"
scalaVersion := "2.13.16"
libraryDependencies += "com.vitthalmirji" %% "valmuri-core" % "0.1.0"
""".stripMargin

    Files.write(projectDir.resolve("build.sbt"), buildContent.getBytes)
  }

  private def runDevServer(): Unit = {
    println("🔥 Starting development server with hot reload...")
    import scala.sys.process._
    "sbt ~run".!
  }

  private def printHelp(): Unit =
    println("""
Valmuri CLI v0.1.0 - Full-stack Scala web framework

Usage:
valmuri new <name>                    Create new project
valmuri new <name> --template <type>  Create with template
valmuri dev                           Start dev server
valmuri help                          Show this help

Templates:
basic      Simple web application (default)
blog       Personal blog with markdown
api        REST API server
portfolio  Portfolio/personal site

Examples:
valmuri new my-blog --template blog
valmuri new my-api --template api

After creating:
cd my-blog
sbt run                    # Start development server
./deploy.sh               # Deploy to production
""")
}
