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

package transformations.userAnswersToETMP

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{__, _}

class ChargeBTransformer extends JsonTransformer {

  def transformToETMPData: Reads[JsObject] =
    (__ \ 'chargeBDetails).readNullable {
      __.read(
        (((__ \ 'chargeDetails \ 'chargeTypeBDetails \ 'amendedVersion).json.copyFrom((__ \ 'amendedVersion).json.pick)
          orElse doNothing) and
          (__ \ 'chargeDetails \ 'chargeTypeBDetails \ 'numberOfMembers).json.copyFrom((__ \ 'numberOfDeceased).json.pick) and
          (__ \ 'chargeDetails \ 'chargeTypeBDetails \ 'totalAmount).json.copyFrom((__ \ 'amountTaxDue).json.pick)).reduce
      )
    }.map {
      _.getOrElse(Json.obj())
    }
}
