package com.vitthalmirji.valmuri.template

import com.vitthalmirji.valmuri.{VResult, VRoute}

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

case class BlogPost(title: String, content: String, date: String, slug: String)

object VBlog {

  def loadPosts(postsDir: String = "src/main/resources/posts"): List[BlogPost] = {
    val postsPath = Paths.get(postsDir)

    if (Files.exists(postsPath)) {
      Files.list(postsPath).iterator().asScala
        .filter(_.toString.endsWith(".md"))
        .map(loadPost)
        .toList
        .sortBy(_.date).reverse
    } else {
      List.empty
    }
  }

  def loadPost(postPath: java.nio.file.Path): BlogPost = {
    val content = Files.readString(postPath)
    val lines = content.split("\n")

    val title = lines.find(_.startsWith("# ")).map(_.substring(2)).getOrElse("Untitled")
    val slug = postPath.getFileName.toString.replace(".md", "")
    val date = java.time.LocalDate.now().toString

    BlogPost(title, VTemplate.renderMarkdown(content), date, slug)
  }

  def generateRoutes(): List[VRoute] = {
    val posts = loadPosts()

    List(
      VRoute("/blog", _ => {
        val postsHtml = posts.map(post =>
          s"""<article>
             |  <h2><a href="/blog/${post.slug}">${post.title}</a></h2>
             |  <time>${post.date}</time>
             |</article>""".stripMargin
        ).mkString("\n")

        VResult.success(VTemplate.render("blog/index.html", Map("posts" -> postsHtml)).getOrElse(postsHtml))
      })
    ) ++ posts.map(post =>
      VRoute(s"/blog/${post.slug}", _ =>
        VResult.success(VTemplate.render("blog/post.html", Map(
          "title" -> post.title,
          "content" -> post.content,
          "date" -> post.date
        )).getOrElse(s"<h1>${post.title}</h1>${post.content}"))
      )
    )
  }
}
