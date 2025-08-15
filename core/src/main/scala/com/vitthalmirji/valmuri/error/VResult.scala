package com.vitthalmirji.valmuri.error

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

/**
 * Monadic result type for error handling
 */
sealed trait VResult[+A] { self =>

  def map[B](f: A => B): VResult[B] = self match {
    case VResult.Success(value)   => VResult.Success(f(value))
    case err @ VResult.Failure(_) => err
  }

  def flatMap[B](f: A => VResult[B]): VResult[B] = self match {
    case VResult.Success(value)   => f(value)
    case err @ VResult.Failure(_) => err
  }

  def recover[B >: A](f: PartialFunction[ValmuriError, B]): VResult[B] = self match {
    case VResult.Success(value)                         => VResult.Success(value)
    case VResult.Failure(error) if f.isDefinedAt(error) => VResult.Success(f(error))
    case err @ VResult.Failure(_)                       => err
  }

  def recoverWith[B >: A](f: PartialFunction[ValmuriError, VResult[B]]): VResult[B] = self match {
    case VResult.Success(value)                         => VResult.Success(value)
    case VResult.Failure(error) if f.isDefinedAt(error) => f(error)
    case err @ VResult.Failure(_)                       => err
  }

  def getOrElse[B >: A](default: => B): B = self match {
    case VResult.Success(value) => value
    case VResult.Failure(_)     => default
  }

  def orElse[B >: A](alternative: => VResult[B]): VResult[B] = self match {
    case success @ VResult.Success(_) => success
    case VResult.Failure(_)           => alternative
  }

  def fold[B](onFailure: ValmuriError => B)(onSuccess: A => B): B = self match {
    case VResult.Success(value) => onSuccess(value)
    case VResult.Failure(error) => onFailure(error)
  }

  def isSuccess: Boolean = self.isInstanceOf[VResult.Success[_]]
  def isFailure: Boolean = !isSuccess

  def toOption: Option[A] = self match {
    case VResult.Success(value) => Some(value)
    case VResult.Failure(_)     => None
  }

  def toEither: Either[ValmuriError, A] = self match {
    case VResult.Success(value) => Right(value)
    case VResult.Failure(error) => Left(error)
  }

  def toTry: Try[A] = self match {
    case VResult.Success(value) => Success(value)
    case VResult.Failure(error) => Failure(new RuntimeException(error.message))
  }
}

object VResult {
  final case class Success[+A](value: A)        extends VResult[A]
  final case class Failure(error: ValmuriError) extends VResult[Nothing]

  // Constructors
  def success[A](value: A): VResult[A]            = Success(value)
  def failure[A](error: ValmuriError): VResult[A] = Failure(error)

  // From other types
  def fromTry[A](t: Try[A]): VResult[A] = t match {
    case scala.util.Success(value) => Success(value)
    case scala.util.Failure(ex)    => Failure(ValmuriError.UnexpectedError(ex.getMessage))
  }

  def fromOption[A](opt: Option[A], error: => ValmuriError): VResult[A] =
    opt.fold[VResult[A]](Failure(error))(Success(_))

  def fromEither[A](either: Either[ValmuriError, A]): VResult[A] = either match {
    case Right(value) => Success(value)
    case Left(error)  => Failure(error)
  }

  def fromFuture[A](future: Future[A], timeout: Duration = Duration.Inf)(implicit ec: ExecutionContext): VResult[A] = {
    println(s"Using context ${ec.toString}")
    fromTry(Try(Await.result(future, timeout)))
  }

  // Sequence operations
  def sequence[A](results: List[VResult[A]]): VResult[List[A]] =
    results.foldRight[VResult[List[A]]](Success(Nil)) { (result, acc) =>
      for {
        value <- result
        list  <- acc
      } yield value :: list
    }

  def traverse[A, B](list: List[A])(f: A => VResult[B]): VResult[List[B]] =
    sequence(list.map(f))
}
