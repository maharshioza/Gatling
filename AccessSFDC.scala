package default

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.Predef._


class AccessSFDC extends Simulation {	


	val header_1 = Map(
		"Content-Type" -> """application/x-www-form-urlencoded"""
		)

	val header_2 = Map(
		"Authorization" -> "${TokenType} ${a_token}"	
		)

	val header_3 = Map(
		"Authorization" -> "Bearer ${a_token}",
		"Content-Type" -> "application/json"
		)

	val header_4 = Map(
		"Authorization" -> "${TypeOfToken} ${acc_token}"
		)

	val header_5 = Map(
		"Content-Type" -> "application/json",
		"Authorization" -> "Bearer ${acc_token}"
		)

	object Login {

		val SFDCaccess = exec(http("access token")
					.post("https://login.salesforce.com/services/oauth2/token")
					.headers(header_1)
					.formParam("username","abc@gmail.com")
					.formParam("password","************")
					.formParam("client_secret","*****************")
					.formParam("client_id","*************************************")
					.formParam("grant_type","password")
					.formParam("code","*********************")
					.formParam("redirect_uri","uri")
					
					.check(jsonPath("$.access_token").saveAs("a_token"))		//save token using jsonpath
					.check(jsonPath("$.instance_url").saveAs("baseURL"))
					.check(jsonPath("$.token_type").saveAs("TokenType"))
					)	

		val dataAccess = exec(http("data access")
					.get("${baseURL}/services/data/v36.0/sobjects/account")
					.headers(header_2)	
					)

		val AccUpdate = exec(http("Update City")
					.patch("${baseURL}/services/data/v36.0/sobjects/account/0012800000FrtTL")
					.headers(header_3)
					.body(RawFileBody("billingCity.json"))
					)

		}


	object SFDCLogin {

		val getToken = exec(http("get token")
						.post("https://login.salesforce.com/services/oauth2/token")
						.headers(header_1)
						.formParam("username","a@gmail.com")
						.formParam("password","************")
						.formParam("client_secret","*****************")
						.formParam("client_id","*************************************")
						.formParam("grant_type","password")
						.formParam("code","*********************")
						.formParam("redirect_uri","uri")

						.check(jsonPath("$.access_token").saveAs("acc_token"))			//save token using jsonpath
						.check(jsonPath("$.instance_url").saveAs("uri"))
						.check(jsonPath("$.token_type").saveAs("TypeOfToken"))
						)

		val TabCheck = exec(http("get data")
							.get("${uri}/services/data/v36.0/tabs")
							.headers(header_4)
						)

		val CreateField = exec(http("Create Field")
							.post("${uri}/services/data/v36.0/sobjects/account")
							.headers(header_5)
							.body(RawFileBody("newAccount.json"))
						)

		val SB_Billing_Quote = exec(http("SB Billing Quote")
							.get("${uri}/services/data/v36.0/sobjects/invoiceit_s__Quote__c")
							.headers(header_4)
							)

		val CreateQuote = exec(http("Create Quote")
							.post("${uri}/services/data/v36.0/sobjects/invoiceit_s__Quote__c")
							.headers(header_5)
							.body(RawFileBody("newQuote.json"))
						)

		val CreateOrder = exec(http("Create Order")
							.post("https://na30.salesforce.com/services/data/v36.0/sobjects/invoiceit_s__Job__c")
							.headers(header_5)
							.body(RawFileBody("newOrder.json"))
						)

		val Invoice = exec(http("Create Invoice")
							.post("${uri}/services/data/v36.0/sobjects/invoiceit_s__Invoice__c")
							.headers(header_5)
							.body(RawFileBody("createInvoice.json"))
						)
		}

		var user1 = scenario("user1").exec(Login.SFDCaccess, Login.dataAccess, Login.AccUpdate)
		var user2 = scenario("user2").exec(SFDCLogin.getToken, SFDCLogin.TabCheck,
										 SFDCLogin.CreateField, SFDCLogin.SB_Billing_Quote, 
										 SFDCLogin.CreateQuote, SFDCLogin.CreateOrder,
											 SFDCLogin.Invoice)

		
		setUp(
			
			user1.inject(rampUsers(2) over(1)),
			user2.inject(atOnceUsers(1))
			
		)

} 
	