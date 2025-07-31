package com.vitthalmirji.valmuri

import com.vitthalmirji.valmuri.error.FrameworkError

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * Functional Result type - Like Either but more expressive
 */
sealed trait VResult[+A] {

  // Functor: map over successful values
  def map[B](f: A => B): VResult[B] = this match {
    case VResult.Success(value) => VResult.Success(f(value))
    case failure: VResult.Failure => failure
  }

  // Monad: flatMap for chaining computations
  def flatMap[B](f: A => VResult[B]): VResult[B] = this match {
    case VResult.Success(value) => f(value)
    case failure: VResult.Failure => failure
  }

  // Error recovery with partial function
  def recoverWith[B >: A](pf: PartialFunction[FrameworkError, VResult[B]]): VResult[B] = this match {
    case failure @ VResult.Failure(error) =>
      if (pf.isDefinedAt(error)) pf(error) else failure
    case success => success
  }

  // Extract value or throw
  def get: A = this match {
    case VResult.Success(value) => value
    case VResult.Failure(error) => throw new RuntimeException(error.message)
  }

  // Extract value or default
  def getOrElse[B >: A](default: => B): B = this match {
    case VResult.Success(value) => value
    case VResult.Failure(_) => default
  }

  // Transform errors
  def mapError(f: FrameworkError => FrameworkError): VResult[A] = this match {
    case success: VResult.Success[A] => success
    case VResult.Failure(error) => VResult.Failure(f(error))
  }

  // Check if successful
  def isSuccess: Boolean = this match {
    case _: VResult.Success[_] => true
    case _: VResult.Failure => false
  }

  def isFailure: Boolean = !isSuccess
}

object VResult {
  final case class Success[+A](value: A) extends VResult[A]
  final case class Failure(error: FrameworkError) extends VResult[Nothing]

  // Smart constructors
  def success[A](value: A): VResult[A] = Success(value)
  def failure[A](error: FrameworkError): VResult[A] = Failure(error)

  // From Try
  def fromTry[A](t: Try[A]): VResult[A] = t match {
    case scala.util.Success(value) => success(value)
    case scala.util.Failure(ex) => failure(FrameworkError.UnexpectedError(ex.getMessage))
  }

  // From Future (for async operations) - Fixed timeout handling
  def fromFuture[A](future: Future[A], timeout: Duration = Duration.Inf): VResult[A] = {
    scala.util.Try {
      scala.concurrent.Await.result(future, timeout)
    } match {
      case scala.util.Success(value) => success(value)
      case scala.util.Failure(ex) => failure(FrameworkError.UnexpectedError(ex.getMessage))
    }
  }

  // From Option
  def fromOption[A](opt: Option[A], error: => FrameworkError): VResult[A] =
    opt.fold(failure[A](error))(success)

  // Traverse a list of results - Fixed for better performance
  def sequence[A](results: List[VResult[A]]): VResult[List[A]] = {
    results.foldRight(success(List.empty[A])) { (result, acc) =>
      for {
        value <- result
        list <- acc
      } yield value :: list
    }
  }
}
