package valmuri.di

import scala.annotation.StaticAnnotation

// Framework service auto-discovery
case class ValmuriService(name: String = "") extends StaticAnnotation
case class ValmuriController(path: String = "") extends StaticAnnotation
case class ValmuriRepository() extends StaticAnnotation
case class ValmuriMiddleware(order: Int = 0) extends StaticAnnotation

// Configuration binding
case class ConfigValue(path: String) extends StaticAnnotation
case class ConfigSection(prefix: String) extends StaticAnnotation
