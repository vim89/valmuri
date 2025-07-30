package com.vitthalmirji.valmuri.error

case class ServiceError(message: String) extends Exception(message)
