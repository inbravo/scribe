###		<b>SalesForce</b> CRM examples :+1:

####	Search all 'contact'
```
http://localhost:8080/scribe/object/contact?[CRM-MetaObject-Info]
```
####	Search 'contact' by a phone number
```
http://localhost:8080/scribe/object/contact/Phone='999.999.9999'/?[CRM-MetaObject-Info]
```
####	CRM Meta Object is required for your SalesForce CRM account details

Replace [CRM-MetaObject-Info] with following string after relacing all '[]' values with your CRM credentials
```
MetaObject.CrmUserId=[Your-CRM-User-Id]&MetaObject.CrmPassword=[Your-CRM-Password]&MetaObject.CrmType=SF
```

- MetaObject.CrmUserId need your CRM user id
- MetaObject.CrmPassword is your CRM password
- MetaObject.CrmType is your CRM type, which is <b>'SF'</b> in case of SalesForce CRM