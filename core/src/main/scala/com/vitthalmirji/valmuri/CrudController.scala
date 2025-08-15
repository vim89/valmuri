package com.vitthalmirji.valmuri

import com.vitthalmirji.valmuri.encoder.{ JsonEncoder, ResponseEncoder }
import com.vitthalmirji.valmuri.error.FrameworkError

/**
 * CRUD Controller template with generics and type bounds
 */
abstract class CrudController[T: JsonEncoder, ID: ResponseEncoder] extends VController {

  // Abstract methods that must be implemented
  protected def findAll(): VResult[List[T]]

  protected def findById(id: ID): VResult[Option[T]]

  protected def create(entity: T): VResult[T]

  protected def update(id: ID, entity: T): VResult[T]

  protected def delete(id: ID): VResult[Boolean]

  // Standard CRUD routes with pattern matching
  protected def crudRoutes(basePath: String): List[VRoute] = List(
    VRoute.safe(s"$basePath", handleIndex),
    VRoute.safe(s"$basePath/:id", handleShow),
    VRoute.safe(s"$basePath", handleCreate),
    VRoute.safe(s"$basePath/:id", handleUpdate),
    VRoute.safe(s"$basePath/:id", handleDelete)
  )

  private def handleIndex: VRequest => VResult[String] = { req =>
    matchMethod(req)(get = findAll().flatMap(entities => json(entities)))
  }

  private def handleShow: VRequest => VResult[String] = { req =>
    matchMethod(req)(
      get = for {
        idStr     <- req.getRequiredParam("id")
        id        <- parseId(idStr)
        entityOpt <- findById(id)
        result <- entityOpt match {
          case Some(entity) => json(entity)
          case None         => notFound(s"Entity with id $id not found")
        }
      } yield result
    )
  }

  private def handleCreate: VRequest => VResult[String] = { req =>
    matchMethod(req)(
      post = for {
        body    <- req.body.fold(VResult.failure[String](FrameworkError.MissingParameter("body")))(VResult.success)
        entity  <- parseEntity(body)
        created <- create(entity)
        result  <- json(created)
      } yield result
    )
  }

  private def handleUpdate: VRequest => VResult[String] = { req =>
    matchMethod(req)(
      put = for {
        idStr   <- req.getRequiredParam("id")
        id      <- parseId(idStr)
        body    <- req.body.fold(VResult.failure[String](FrameworkError.MissingParameter("body")))(VResult.success)
        entity  <- parseEntity(body)
        updated <- update(id, entity)
        result  <- json(updated)
      } yield result
    )
  }

  private def handleDelete: VRequest => VResult[String] = { req =>
    matchMethod(req)(
      delete = for {
        idStr   <- req.getRequiredParam("id")
        id      <- parseId(idStr)
        deleted <- delete(id)
        result  <- if (deleted) ok("Deleted successfully") else notFound("Entity not found")
      } yield result
    )
  }

  // Abstract methods for parsing - to be implemented by concrete controllers
  protected def parseId(idStr: String): VResult[ID]

  protected def parseEntity(json: String): VResult[T]
}
