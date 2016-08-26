# Scribe - Talk to all CRMs using a single Language

- Talk to all CRMs in a simple language: REST
- REST is very powerfull; it can Create, Read, Update, Delete (CRUD) CRM objects
- Use single communication format for all types of CRM:	XML or JSON on HTTP
- Avoid CRM level complexity and access Cloud CRM in a seamless way
- Remove any direct dependancy on a CRM version; Scribe takes care of all upgradations
- Switch CRM easily
- Scribe supports following CRM,
	1. SalesForce
	2. NetSuite
	3. MS Dynamics XRM
	4. Zoho
	5. Zendesk

Samples
--------------
-  [SalesForce][salesforce.md]
-  [NetSuite][netsuite.md]
-  [Microsoft][microsoft.md]

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
[microsoft.md]: https://github.com/inbravo/scribe/blob/master/docs/md/microsoft.md

