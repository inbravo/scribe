###		<b>Microsoft</b> CRM examples :+1:

####	Search all 'contact'
```
http://localhost:8080/scribe/object/contact?[CRM-MetaObject-Info]
```
####	Search 'contact' by a phone number
```
http://localhost:8080/scribe/object/contact/mobilephone=135-548-8797/?[CRM-MetaObject-Info]
```
####	CRM Meta Object is required for your SalesForce CRM account details

Replace [CRM-MetaObject-Info] with following string after relacing all '[]' values with your CRM credentials
```
MetaObject.CrmUserId=[Your-CRM-User-Id]&MetaObject.CrmPassword=[Your-CRM-Password]&MetaObject.CrmType=MS&MetaObject.crmServiceUrl=[Your-CRM-Service-IURL]&MetaObject.crmServiceProtocol=https&MetaObject.crmPort=80
```

- MetaObject.CrmUserId need your CRM user id
- MetaObject.CrmPassword is your CRM password
- MetaObject.CrmType is your CRM type, which is <b>'MS'</b> in case of Microsoft CRM
- MetaObject.CrmServiceUrl is your service URL; login online and copy your unique organization URL
- MetaObject.CrmServiceProtocol is by default https currently
- MetaObject.CrmPort is by default '80' currently