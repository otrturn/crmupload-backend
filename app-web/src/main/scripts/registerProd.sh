curl -v -X POST https://www.crmupload.de/auth/register-customer \
-H "Content-Type: application/json" \
-d '{
"firstname": "Ralf",
"lastname": "Scholler",
"email_address": "ralf@example.com",
"phone_number": "+49-111-222",
"adrline1": "Musterstraße 1",
"adrline2": "",
"postalcode": "12345",
"city": "Ingelbach",
"country": "DE",
"password": "test1234"
}'
