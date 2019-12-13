/*
 * Copyright 2019 HM Revenue & Customs
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

package transformations.userAnswersToETMP

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{__, _}

class ChargeETransformer {

  val doNothing: Reads[JsObject] = __.json.put(Json.obj())

  def transformToETMPData: Reads[JsObject] = {

    (__ \ 'chargeEDetails).readNullable {
      __.read(
        ((__ \ 'chargeDetails \ 'chargeTypeEDetails \ 'memberDetails).json.copyFrom((__ \ 'members).read(getMembers)) and
          //TODO Fix total amount reads after PODS-3783
          (__ \ 'chargeDetails \ 'chargeTypeEDetails \ 'totalAmount).json.put(JsNumber(0))).reduce
      )
    }.map {
      _.getOrElse(Json.obj())
    }
  }

  def getMembers: Reads[JsArray] = __.read(Reads.seq(getMemberDetails)).map(JsArray(_))

  def getMemberDetails: Reads[JsObject] =
    (__ \ 'individualsDetails \ 'firstName).json.copyFrom((__ \ 'memberDetails \ 'firstName).json.pick) and
      (__ \ 'individualsDetails \ 'lastName).json.copyFrom((__ \ 'memberDetails \ 'lastName).json.pick) and
      (__ \ 'individualsDetails \ 'nino).json.copyFrom((__ \ 'memberDetails \ 'nino).json.pick) and
      (__ \ 'amountOfCharge).json.copyFrom((__ \ 'chargeDetails \ 'chargeAmount).json.pick) and
      (__ \ 'dateOfNotice).json.copyFrom((__ \ 'chargeDetails \ 'dateNoticeReceived).json.pick) and
      getPaidUnder237b and
      (__ \ 'taxYearEnding).json.copyFrom((__ \ 'annualAllowanceYear).json.pick) and
      (__ \ 'memberStatus).json.put(JsString("New")) reduce

  def getPaidUnder237b: Reads[JsObject] =
    (__ \ 'chargeDetails \ 'isPaymentMandatory).read[Boolean].flatMap { flag =>
      (__ \ 'paidUnder237b).json.put(if (flag) JsString("Yes") else JsString("No"))
    } orElse doNothing

//  def getTotal =
//    (__ \ 'chargeDetails \ 'chargeTypeEDetails \ 'totalAmount).json
//      .copyFrom((__ \\ 'chargeAmount).read[Seq[JsNumber]].map(_.sum))

}
