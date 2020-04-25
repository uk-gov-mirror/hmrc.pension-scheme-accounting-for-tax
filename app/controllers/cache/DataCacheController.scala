/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.cache

import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import play.api.Configuration
import play.api.Logger
import repository.DataCacheRepository
import repository.model.SessionData
import repository.model.SessionData._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.AuthorisedFunctions
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.UnauthorizedException
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataCacheController @Inject()(
                                     config: Configuration,
                                     repo: DataCacheRepository,
                                     val authConnector: AuthConnector,
                                     cc: ControllerComponents
                                   ) extends BackendController(cc) with AuthorisedFunctions {

  import DataCacheController._

  def save: Action[AnyContent] = Action.async {
    implicit request =>
      getIdWithName { case (sessionId, id, _) =>
        request.body.asJson.map {
          jsValue =>

            repo.save(id, jsValue, sessionId)
              .map(_ => Created)
        } getOrElse Future.successful(BadRequest)
      }
  }

  def setSessionData(lock:Boolean): Action[AnyContent] = Action.async {
    implicit request =>
      getIdWithName { case (sessionId, id, name) =>
        request.body.asJson.map {
          jsValue => {
            (request.headers.get("version"), request.headers.get("accessMode")) match {
              case (Some(version), Some(accessMode)) =>
                repo.setSessionData(id, if (lock) Some(name) else None, jsValue, sessionId, version.toInt, accessMode).map(_ => Created)
              case _ => Future.successful(BadRequest("Version and/or access mode not present in request header"))
            }
          }
        } getOrElse Future.successful(BadRequest)
      }
  }

  def lockedBy: Action[AnyContent] = Action.async {
    implicit request =>
      getIdWithName { case (sessionId, id, _) =>
        repo.lockedBy(sessionId, id).map { response =>
          Logger.debug(message = s"DataCacheController.lockedBy: Response for request Id $id is $response")
          response match {
            case None => NotFound
            case Some(name) => Ok(Json.toJson(name))
          }
        }
      }
  }

  def getSessionData: Action[AnyContent] = Action.async {
    implicit request =>
      getIdWithName { case (sessionId, id, _) =>
        repo.getSessionData(sessionId, id).map { response =>
          Logger.debug(message = s"DataCacheController.getSessionData: Response for request Id $id is $response")
          response match {
            case None => NotFound
            case Some(sd) => Ok(Json.toJson(sd))
          }
        }
      }
  }

  def get: Action[AnyContent] = Action.async {
    implicit request =>
      getIdWithName { (sessionId, id, _) =>
        repo.get(id, sessionId).map { response =>
          Logger.debug(message = s"DataCacheController.get: Response for request Id $id is $response")
          response.map {
            Ok(_)
          } getOrElse NotFound
        }
      }
  }

  def remove: Action[AnyContent] = Action.async {
    implicit request =>
      getIdWithName { (sessionId, id, _) =>
        repo.remove(id, sessionId).map(_ => Ok)
      }
  }



  private def getIdWithName(block: (String, String, String) => Future[Result])
                           (implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Result] = {
    authorised(Enrolment("HMRC-PODS-ORG")).retrieve(Retrievals.name) {
      case Some(name) =>
        val id = request.headers.get("id").getOrElse(throw MissingHeadersException)
        val sessionId = request.headers.get("X-Session-ID").getOrElse(throw MissingHeadersException)
        block(sessionId, id, s"${name.name.getOrElse("")} ${name.lastName.getOrElse("")}".trim)
      case _ => Future.failed(CredNameNotFoundFromAuth())
    }
  }
}

object DataCacheController {

  case object MissingHeadersException extends BadRequestException("Missing id(pstr and startDate) or Session Id from headers")

  case class CredNameNotFoundFromAuth(msg: String = "Not Authorised - Unable to retrieve credentials - name")
    extends UnauthorizedException(msg)

}
