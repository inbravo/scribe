# Scribe - stateless microservice to integrate cloud CRMs

- Talk to all CRMs in a generic methodology: REST
- Use single communication format for all types of CRM
- Avoid CRM level complexity and access CRM objects using XML or JSON
- Scribe supports following CRM,
	1. SalesForce
	2. NetSuite
	3. MS Dynamics XRM
	4. Zoho
	5. ZenDesk ticketing system

Search CRM objects with a Query criteria and Select criteria
--------------
-  [Query Criteria][get-all-objects.md]
-  [Query with Select Criteria][get-all-objects-with-some-fields.md]

--------------
##### For JSON  response add "?_type=json" Get JSON data for all Accounts: [cad-host]/cad/object/account?_type=json

[get-all-objects.md]: https://github.com/inbravo/scribe/blob/master/docs/md/get-all-objects.md
[get-all-objects-with-some-fields.md]: https://github.com/inbravo/scribe/blob/master/docs/md/get-all-objects-with-some-fields.md
