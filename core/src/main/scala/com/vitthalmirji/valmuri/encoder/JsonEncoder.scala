package com.vitthalmirji.valmuri.encoder

object JsonEncoder {
  // Basic JSON encoders
  implicit val stringJsonEncoder: JsonEncoder[String]   = (value: String) => s""""$value""""
  implicit val intJsonEncoder: JsonEncoder[Int]         = (value: Int) => value.toString
  implicit val booleanJsonEncoder: JsonEncoder[Boolean] = (value: Boolean) => value.toString

  // Case class JSON encoder (simplified - real implementation would use reflection)
  implicit def caseClassJsonEncoder[A <: Product]: JsonEncoder[A] = (value: A) => {
    val fields = value.productElementNames
      .zip(value.productIterator)
      .map { case (name, elem) => s""""$name": ${jsonValue(elem)}""" }
      .mkString(", ")
    s"{$fields}"
  }

  // List JSON encoder
  implicit def listJsonEncoder[A: JsonEncoder]: JsonEncoder[List[A]] = (list: List[A]) => {
    val encoder = implicitly[JsonEncoder[A]]
    "[" + list.map(encoder.toJson).mkString(", ") + "]"
  }

  // Option JSON encoder
  implicit def optionJsonEncoder[A: JsonEncoder]: JsonEncoder[Option[A]] = {
    case Some(value) => implicitly[JsonEncoder[A]].toJson(value)
    case None        => "null"
  }

  // Helper for encoding any value to JSON string
  private def jsonValue(value: Any): String = value match {
    case s: String  => s""""$s""""
    case i: Int     => i.toString
    case b: Boolean => b.toString
    case null       => "null"
    case other      => s""""${other.toString}""""
  }
}

/**
 * Type class for JSON encoding (simplified)
 */
trait JsonEncoder[A] {
  def toJson(value: A): String
}
