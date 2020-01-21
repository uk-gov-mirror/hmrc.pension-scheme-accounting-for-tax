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

package transformations.ETMPToUserAnswers

import org.scalatest.FreeSpec
import play.api.libs.json.Json
import transformations.generators.AFTETMPResponseGenerators

class AFTDetailsTransformerSpec extends FreeSpec with AFTETMPResponseGenerators {

  private val chargeATransformer = new ChargeATransformer
  private val chargeBTransformer = new ChargeBTransformer
  private val chargeCTransformer = new ChargeCTransformer
  private val chargeDTransformer = new ChargeDTransformer
  private val chargeETransformer = new ChargeETransformer
  private val chargeFTransformer = new ChargeFTransformer
  private val chargeGTransformer = new ChargeGTransformer

  "An AFT Details Transformer" - {
    "must transform from ETMP Get Details API Format to UserAnswers format" in {
      val transformer = new AFTDetailsTransformer(chargeATransformer, chargeBTransformer, chargeCTransformer,
        chargeDTransformer, chargeETransformer, chargeFTransformer, chargeGTransformer)
      val transformedUserAnswersJson = etmpResponseJson.transform(transformer.transformToUserAnswers).asOpt.value
      transformedUserAnswersJson mustBe userAnswersJson
    }
  }

  private val userAnswersJson = Json.parse(
    """{
      |  "aftStatus": "Compiled",
      |  "pstr": "1234",
      |  "schemeName": "Test Scheme",
      |  "quarter": {
      |    "startDate": "2019-01-01",
      |    "endDate": "2019-03-31"
      |  },
      |  "chargeADetails": {
      |    "numberOfMembers": 2,
      |    "totalAmtOfTaxDueAtLowerRate": 200.02,
      |    "totalAmtOfTaxDueAtHigherRate": 200.02,
      |    "totalAmount": 200.02
      |  },
      |  "chargeBDetails": {
      |    "numberOfDeceased": 2,
      |    "amountTaxDue": 100.02
      |  },
      |  "chargeCDetails": {
      |    "employers": [
      |      {
      |        "isSponsoringEmployerIndividual": true,
      |        "chargeDetails": {
      |          "paymentDate": "2020-01-01",
      |          "amountTaxDue": 500.02
      |        },
      |        "sponsoringIndividualDetails": {
      |          "firstName": "testFirst",
      |          "lastName": "testLast",
      |          "nino": "AB100100A",
      |          "isDeleted": false
      |        },
      |        "sponsoringEmployerAddress": {
      |          "line1": "line1",
      |          "line2": "line2",
      |          "line3": "line3",
      |          "line4": "line4",
      |          "postcode": "NE20 0GG",
      |          "country": "GB"
      |        }
      |      }
      |    ],
      |    "totalChargeAmount": 500.02
      |  },
      |  "chargeDDetails": {
      |    "members": [
      |      {
      |        "memberDetails": {
      |          "firstName": "Joy",
      |          "lastName": "Kenneth",
      |          "nino": "AA089000A",
      |          "isDeleted": false
      |        },
      |        "chargeDetails": {
      |          "dateOfEvent": "2016-02-29",
      |          "taxAt25Percent": 1.02,
      |          "taxAt55Percent": 9.02
      |        }
      |      }
      |    ],
      |    "totalChargeAmount": 2345.02
      |  },
      |  "chargeEDetails": {
      |    "members": [
      |      {
      |        "memberDetails": {
      |          "firstName": "eFirstName",
      |          "lastName": "eLastName",
      |          "nino": "AE100100A",
      |          "isDeleted": false
      |        },
      |        "annualAllowanceYear": "2020",
      |        "chargeDetails": {
      |          "dateNoticeReceived": "2020-01-11",
      |          "chargeAmount": 200.02,
      |          "isPaymentMandatory": true
      |        }
      |      }
      |    ],
      |    "totalChargeAmount": 200.02
      |  },
      |  "chargeFDetails": {
      |    "amountTaxDue": 200.02,
      |    "deRegistrationDate": "1980-02-29"
      |  },
      |  "chargeGDetails": {
      |    "members": [
      |      {
      |        "memberDetails": {
      |          "firstName": "Craig",
      |          "lastName": "White",
      |          "dob": "1980-02-29",
      |          "nino": "AA012000A",
      |          "isDeleted": false
      |        },
      |        "chargeDetails": {
      |          "qropsReferenceNumber": "Q300000",
      |          "qropsTransferDate": "2016-02-29"
      |        },
      |        "chargeAmounts": {
      |          "amountTransferred": 45670.02,
      |          "amountTaxDue": 4560.02
      |        }
      |      }
      |    ],
      |    "totalChargeAmount": 1230.02
      |  }
      |}""".stripMargin)

  private val etmpResponseJson = Json.parse(
    """{
      |  "aftDetails": {
      |    "aftStatus": "Compiled",
      |    "quarterStartDate": "2019-01-01",
      |    "quarterEndDate": "2019-03-31"
      |  },
      |  "schemeDetails": {
      |    "schemeName": "Test Scheme",
      |    "pstr": "1234"
      |  },
      |  "chargeDetails": {
      |    "chargeTypeADetails": {
      |      "numberOfMembers": 2,
      |      "totalAmtOfTaxDueAtLowerRate": 200.02,
      |      "totalAmtOfTaxDueAtHigherRate": 200.02,
      |      "totalAmount": 200.02
      |    },
      |    "chargeTypeBDetails": {
      |      "amendedVersion": 1,
      |      "numberOfMembers": 2,
      |      "totalAmount": 100.02
      |    },
      |    "chargeTypeCDetails": {
      |      "totalAmount": 500.02,
      |      "memberDetails": [
      |        {
      |          "memberStatus": "New",
      |          "memberTypeDetails": {
      |            "memberType": "Individual",
      |            "individualDetails": {
      |              "firstName": "testFirst",
      |              "lastName": "testLast",
      |              "nino": "AB100100A"
      |            }
      |          },
      |          "correspondenceAddressDetails": {
      |            "nonUKAddress": "False",
      |            "postCode": "NE20 0GG",
      |            "addressLine1": "line1",
      |            "addressLine2": "line2",
      |            "addressLine3": "line3",
      |            "addressLine4": "line4",
      |            "countryCode": "GB"
      |          },
      |          "dateOfPayment": "2020-01-01",
      |          "totalAmountOfTaxDue": 500.02
      |        }
      |      ]
      |    },
      |    "chargeTypeDDetails": {
      |      "amendedVersion": 1,
      |      "totalAmount": 2345.02,
      |      "memberDetails": [
      |        {
      |          "memberStatus": "New",
      |          "memberAFTVersion": 1,
      |          "individualsDetails": {
      |            "title": "Mr",
      |            "firstName": "Joy",
      |            "middleName": "H",
      |            "lastName": "Kenneth",
      |            "nino": "AA089000A"
      |          },
      |          "dateOfBenefitCrystalizationEvent": "2016-02-29",
      |          "totalAmtOfTaxDueAtLowerRate": 1.02,
      |          "totalAmtOfTaxDueAtHigherRate": 9.02
      |        }
      |      ]
      |    },
      |    "chargeTypeEDetails": {
      |      "totalAmount": 200.02,
      |      "memberDetails": [
      |        {
      |          "memberStatus": "New",
      |          "individualsDetails": {
      |            "firstName": "eFirstName",
      |            "lastName": "eLastName",
      |            "nino": "AE100100A"
      |          },
      |          "amountOfCharge": 200.02,
      |          "taxYearEnding": "2020",
      |          "dateOfNotice": "2020-01-11",
      |          "paidUnder237b": "Yes"
      |        }
      |      ]
      |    },
      |    "chargeTypeFDetails": {
      |      "totalAmount": 200.02,
      |      "dateRegiWithdrawn": "1980-02-29"
      |    },
      |    "chargeTypeGDetails": {
      |      "amendedVersion": 1,
      |      "totalOTCAmount": 1230.02,
      |      "memberDetails": [
      |        {
      |          "memberStatus": "New",
      |          "memberAFTVersion": 1,
      |          "individualsDetails": {
      |            "title": "Mr",
      |            "firstName": "Craig",
      |            "middleName": "H",
      |            "lastName": "White",
      |            "dateOfBirth": "1980-02-29",
      |            "nino": "AA012000A"
      |          },
      |          "qropsReference": "Q300000",
      |          "amountTransferred": 45670.02,
      |          "dateOfTransfer": "2016-02-29",
      |          "amountOfTaxDeducted": 4560.02
      |        }
      |      ]
      |    }
      |  }
      |}
      |""".stripMargin)
}
