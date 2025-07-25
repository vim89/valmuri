package valmuri.routing

import scala.util.matching.Regex

sealed trait PathSegment {
  def matches(segment: String): Option[Map[String, String]]
}

case object Root extends PathSegment {
  def matches(segment: String): Option[Map[String, String]] =
    if (segment.isEmpty || segment == "/") Some(Map.empty) else None
}

case class Literal(value: String) extends PathSegment {
  def matches(segment: String): Option[Map[String, String]] =
    if (segment == value) Some(Map.empty) else None
}

case class StringParam(name: String) extends PathSegment {
  def matches(segment: String): Option[Map[String, String]] =
    if (segment.nonEmpty) Some(Map(name -> segment)) else None
}

case class IntParam(name: String) extends PathSegment {
  def matches(segment: String): Option[Map[String, String]] =
    try {
      segment.toInt
      Some(Map(name -> segment))
    } catch {
      case _: NumberFormatException => None
    }
}

case class PathPattern(segments: List[PathSegment]) {
  def matches(path: String): Option[Map[String, String]] = {
    val pathSegments = path.split("/").filter(_.nonEmpty).toList

    if (path == "/" && segments == List(Root)) {
      return Some(Map.empty)
    }

    if (pathSegments.length != segments.length) {
      return None
    }

    val matches = segments.zip(pathSegments).map { case (segment, pathPart) =>
      segment.matches(pathPart)
    }

    if (matches.forall(_.isDefined)) {
      Some(matches.flatten.flatten.toMap)
    } else {
      None
    }
  }
}
