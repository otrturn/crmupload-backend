curl -v -X POST http://localhost:8086/auth/register-customer \
-H "Content-Type: application/json" \
-d '{
"firstname": "Ralf",
"lastname": "Scholler",
"emailAddress": "ralf@example.com",
"phoneNumber": "+49-111-222",
"adrline1": "Musterstraße 1",
"adrline2": "",
"postalcode": "12345",
"city": "Ingelbach",
"country": "DE",
"password": "test1234"
}'
