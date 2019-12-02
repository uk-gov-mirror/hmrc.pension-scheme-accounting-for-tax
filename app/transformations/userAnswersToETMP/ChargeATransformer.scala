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

class ChargeATransformer {

  def transformToETMPData: Reads[JsObject] =
    (__ \ 'chargeADetails).readNullable {
      __.read(
        ((__ \ 'chargeDetails \ 'chargeTypeADetails \ 'numberOfMembers).json.copyFrom((__ \ 'numberOfMembers).json.pick) and
          (__ \ 'chargeDetails \ 'chargeTypeADetails \ 'totalAmtOfTaxDueAtLowerRate).json.copyFrom((__ \ 'totalAmtOfTaxDueAtLowerRate).json.pick) and
          (__ \ 'chargeDetails \ 'chargeTypeADetails \ 'totalAmtOfTaxDueAtHigherRate).json.copyFrom((__ \ 'totalAmtOfTaxDueAtHigherRate).json.pick) and
          (__ \ 'chargeDetails \ 'chargeTypeADetails \ 'totalAmount).json.copyFrom((__ \ 'totalAmount).json.pick)).reduce
      )
    }.map {
      _.getOrElse(Json.obj())
    }
}
