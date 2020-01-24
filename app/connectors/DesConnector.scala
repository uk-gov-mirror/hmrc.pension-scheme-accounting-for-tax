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

package connectors

import java.util.UUID.randomUUID

import audit.{AuditService, FileAftReturn}
import com.google.inject.Inject
import config.AppConfig
import play.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class DesConnector @Inject()(http: HttpClient, config: AppConfig, auditService: AuditService) extends HttpErrorFunctions {

  def fileAFTReturn(pstr: String, data: JsValue, psaId: String = "", quarterStartDate: String = "")(implicit headerCarrier: HeaderCarrier,
                                                 ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {

    val fileAFTReturnURL = config.fileAFTReturnURL.format(pstr)
    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = desHeader(implicitly[HeaderCarrier](headerCarrier)))
    http.POST[JsValue, HttpResponse](fileAFTReturnURL, data)(implicitly, implicitly, hc, implicitly) andThen {
      case Success(httpResponse) =>
        sendFileAftEvent(data, pstr, psaId, quarterStartDate, Status.OK, Some(httpResponse.json))
      case Failure(error: HttpException) =>
        sendFileAftEvent(data, pstr, psaId, quarterStartDate, error.responseCode, None)
    }
  }

  private def sendFileAftEvent(aftReturn: JsValue, pstr: String,
                               psaId: String, quarterStartDate: String,
                               status: Int, response: Option[JsValue]
                              )(implicit request: RequestHeader, ec: ExecutionContext): Unit =
    auditService.sendEvent(FileAftReturn(psaId, pstr, quarterStartDate, status, aftReturn, response))

  def getAftDetails(pstr: String, startDate: String, aftVersion: String)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[JsValue] = {

    val getAftUrl: String = config.getAftDetailsUrl.format(pstr, startDate, aftVersion)
    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = desHeader(implicitly[HeaderCarrier](headerCarrier)))

    http.GET[JsValue](getAftUrl)(implicitly, hc, implicitly)
  }

  def getAftVersions(pstr: String, startDate: String)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Seq[Int]] = {

    val getAftVersionUrl: String = config.getAftVersionUrl.format(pstr, startDate)
    implicit val hc: HeaderCarrier = HeaderCarrier(extraHeaders = desHeader(implicitly[HeaderCarrier](headerCarrier)))

    http.GET[JsValue](getAftVersionUrl)(implicitly, hc, implicitly).map { responseJson =>
      (responseJson \ 0 \ "reportVersion").validate[Int] match {
        case JsSuccess(version, _) => Seq(version)
        case JsError(errors) => throw JsResultException(errors)
      }
    }
  }.recoverWith {
    case _: NotFoundException => Future.successful(Nil)
  }


  private def desHeader(implicit hc: HeaderCarrier): Seq[(String, String)] = {
    val requestId = getCorrelationId(hc.requestId.map(_.value))

    Seq("Environment" -> config.desEnvironment, "Authorization" -> config.authorization,
      "Content-Type" -> "application/json", "CorrelationId" -> requestId)
  }

  def getCorrelationId(requestId: Option[String]): String = {
    requestId.getOrElse {
      Logger.error("No Request Id found")
      randomUUID.toString
    }.replaceAll("(govuk-tax-|-)", "").slice(0, 32)
  }
}
