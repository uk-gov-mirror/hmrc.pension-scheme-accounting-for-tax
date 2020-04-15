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

import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.FreeSpec
import play.api.libs.json._
import transformations.generators.AFTUserAnswersGenerators

class ChargeDTransformerSpec extends FreeSpec with AFTUserAnswersGenerators {

  private val transformer = new ChargeDTransformer
  "A Charge D Transformer" - {
    "must transform ChargeDDetails from UserAnswers to ETMP" in {
      forAll(chargeDUserAnswersGenerator) {
        userAnswersJson =>
          val transformedJson = userAnswersJson.transform(transformer.transformToETMPData).asOpt.value

          def etmpMemberPath(i: Int): JsLookupResult = transformedJson \ "chargeDetails" \ "chargeTypeDDetails" \ "memberDetails" \ i

          def uaMemberPath(i: Int): JsLookupResult = userAnswersJson \ "chargeDDetails" \ "members" \ i

          (etmpMemberPath(0) \ "individualsDetails" \ "firstName").as[String] mustBe (uaMemberPath(0) \ "memberDetails" \ "firstName").as[String]
          (etmpMemberPath(0) \ "individualsDetails" \ "lastName").as[String] mustBe (uaMemberPath(0) \ "memberDetails" \ "lastName").as[String]
          (etmpMemberPath(0) \ "individualsDetails" \ "nino").as[String] mustBe (uaMemberPath(0) \ "memberDetails" \ "nino").as[String]

          (etmpMemberPath(0) \ "dateOfBeneCrysEvent").as[String] mustBe (uaMemberPath(0) \ "chargeDetails" \ "dateOfEvent").as[String]
          (etmpMemberPath(0) \ "totalAmtOfTaxDueAtLowerRate").as[BigDecimal] mustBe (uaMemberPath(0) \ "chargeDetails" \ "taxAt25Percent").as[BigDecimal]
          (etmpMemberPath(0) \ "totalAmtOfTaxDueAtHigherRate").as[BigDecimal] mustBe (uaMemberPath(0) \ "chargeDetails" \ "taxAt55Percent").as[BigDecimal]
          (etmpMemberPath(0) \ "memberStatus").as[String] mustBe "New"

          (etmpMemberPath(1) \ "individualsDetails" \ "firstName").as[String] mustBe (uaMemberPath(1) \ "memberDetails" \ "firstName").as[String]

          (transformedJson \ "chargeDetails" \ "chargeTypeDDetails" \ "totalAmount").as[BigDecimal] mustBe
            (userAnswersJson \ "chargeDDetails" \ "totalChargeAmount").as[BigDecimal]

          (transformedJson \ "chargeDetails" \ "chargeTypeDDetails" \ "amendedVersion").asOpt[Int] mustBe None

          (transformedJson \ "chargeDetails" \ "chargeTypeDDetails" \ "memberDetails").as[Seq[JsObject]].size mustBe 5
      }
    }

    "must transform optional element - amendedVersion of ChargeDDetails from UserAnswers to ETMP" in {
      forAll(chargeDUserAnswersGenerator, arbitrary[Int]) {
        (userAnswersJson, version) =>
          val updatedJson = userAnswersJson.transform(updateJson(__ \ 'chargeDDetails, name = "amendedVersion", version)).asOpt.value
          val transformedJson = updatedJson.transform(transformer.transformToETMPData).asOpt.value

          (transformedJson \ "chargeDetails" \ "chargeTypeDDetails" \ "amendedVersion").as[Int] mustBe
            (updatedJson \ "chargeDDetails" \ "amendedVersion").as[Int]
      }
    }
  }
}
