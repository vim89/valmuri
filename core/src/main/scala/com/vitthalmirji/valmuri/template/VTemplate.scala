package com.vitthalmirji.valmuri.template

import com.vitthalmirji.valmuri.VResult

import scala.io.Source
import scala.util.Try

object VTemplate {

  def render(templateName: String, variables: Map[String, Any] = Map.empty): VResult[String] = {
    VResult.fromTry(Try {
      val templatePath = s"src/main/resources/templates/$templateName"
      val template = Source.fromFile(templatePath).mkString

      variables.foldLeft(template) { case (tmpl, (key, value)) =>
        tmpl.replace(s"{{$key}}", value.toString)
      }
    })
  }

  def renderMarkdown(content: String): String = {
    // Simple markdown rendering (basic implementation)
    content
      .replaceAll("^# (.*)", "<h1>$1</h1>")
      .replaceAll("^## (.*)", "<h2>$1</h2>")
      .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
      .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
      .replaceAll("\\n", "<br>")
  }
}
