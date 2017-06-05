package models

case class AboutSeller(receiver_email: Option[String], receiver_id: Option[String], residence_country: Option[String])
case class AboutTransaction(test_ipn: Option[String], transaction_subject: Option[String], txn_id: Option[String], txn_type: Option[String])
case class AboutBuyer(payer_email: Option[String] = None, payer_id: Option[String] = None, payer_status: Option[String] = None, first_name: Option[String] = None, last_name: Option[String] = None, address_city: Option[String] = None, address_country: Option[String] = None, address_state: Option[String] = None, address_status: Option[String] = None, address_country_code: Option[String] = None, address_name: Option[String] = None, address_street: Option[String] = None, address_zip: Option[String] = None)
case class AboutPayment(custom: Option[String] = None, handling_amount: Option[String] = None, item_name: Option[String] = None, item_number: Option[String] = None, mc_currency: Option[String] = None, mc_fee: Option[String] = None, mc_gross: Option[String] = None, payment_date: Option[String] = None, payment_fee: Option[String] = None, payment_gross: Option[String] = None, payment_status: Option[String] = None, payment_type: Option[String] = None, protection_eligibility: Option[String] = None, quantity: Option[String] = None, shipping: Option[String] = None, tax: Option[String] = None)
case class Other(notify_version: Option[String] = None, charset: Option[String] = None, verify_sign: Option[String] = None)

case class IPN(
                aboutSeller: AboutSeller,
                aboutTransaction: AboutTransaction,
                aboutBuyer: AboutBuyer,
                aboutPayment: AboutPayment,
                other: Other
              )