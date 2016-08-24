###		<b>NetSuite</b> CRM examples :+1:

####	Search all 'contact'
```
http://localhost:8080/scribe/object/contact?[CRM-MetaObject-Info]
```
####	Search 'contact' by a phone number
```
http://localhost:8080/scribe/object/contact/Phone___SearchStringField%20is%204083380698/?[CRM-MetaObject-Info]
```
####	CRM Meta Object is required for your NetSute CRM account details

Replace [CRM-MetaObject-Info] with following string after relacing all '[]' values with your CRM credentials
```
MetaObject.CrmUserId=[Your-CRM-User-Id]&MetaObject.CrmPassword=[Your-CRM-Password]&MetaObject.CrmType=NS&MetaObject.CrmAccountId=[Your-CRM-Account-Id]
```

- MetaObject.CrmUserId need your CRM user id
- MetaObject.CrmPassword is your CRM password
- MetaObject.CrmType is your CRM type, which is 'NS' in case of NetSuite CRM
- MetaObject.CrmAccountId is your <b>NetSuite</b> CRM account id