package com.vitthalmirji.valmuri.encoder

object ResponseEncoder {
  // Implicit instances for common types
  implicit val stringEncoder: ResponseEncoder[String] = (value: String) => value
  implicit val intEncoder: ResponseEncoder[Int] = (value: Int) => value.toString
  implicit val booleanEncoder: ResponseEncoder[Boolean] = (value: Boolean) => value.toString

  // Generic encoder for case classes (simple toString)
  implicit def caseClassEncoder[A <: Product]: ResponseEncoder[A] =
    (value: A) => s"${value.productPrefix}(${value.productIterator.mkString(", ")})"

  // List encoder
  implicit def listEncoder[A: ResponseEncoder]: ResponseEncoder[List[A]] = (list: List[A]) => {
    val encoder = implicitly[ResponseEncoder[A]]
    "[" + list.map(encoder.encode).mkString(", ") + "]"
  }

  // Option encoder
  implicit def optionEncoder[A: ResponseEncoder]: ResponseEncoder[Option[A]] = {
    case Some(value) => implicitly[ResponseEncoder[A]].encode(value)
    case None => "null"
  }
}

/**
 * Type class for response encoding
 */
trait ResponseEncoder[A] {
  def encode(value: A): String
}
