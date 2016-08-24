# Scribe - stateless microservice to integrate cloud CRM

- Talk to all CRMs in a generic methodology: REST
- Use single communication format for all types of CRM
- Avoid CRM level complexity and access CRM objects using XML or JSON
- Scribe supports following CRM,
	1. SalesForce
	2. NetSuite
	3. MS Dynamics XRM
	4. Zoho
	5. ZenDesk ticketing system

Samples
--------------
-  [SalesForce][salesforce.md]
-  [NetSuite][netsuite.md]

## How to run 

After checking out the repo, run `ant tomcat-start` to start the service using Apache Ant. 

## Contributing

1. Fork it ( https://github.com/[my-github-username]/scribe/fork )
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create a new Pull Request

## Selection of XML or JSON as response 

- Add "?_type=json" Get JSON data for all Accounts: [cad-host]/cad/object/account?_type=json
- Add "?_type=xml" Get XML data for all Accounts: [cad-host]/cad/object/account?_type=xml

[salesforce.md]: https://github.com/inbravo/scribe/blob/master/docs/md/salesforce.md
[netsuite.md]: https://github.com/inbravo/scribe/blob/master/docs/md/netsuite.md

