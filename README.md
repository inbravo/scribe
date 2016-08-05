# CAD:  REST based seemless integration of different cloud based CRMs
## Avoid CRM level complexity and access CRM objects using XML or JSON on http
### Supports following CRMs,
	* SalesForce
	2. NetSuite
	3. MS Dynamics XRM
	4. Zoho
	5. ZenDesk ticketing system

### To get all CRM objects of type Contact from various CRM
##### SalesForce: [cad-host]/cad/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=sfdc
##### Microsoft Dynamics XRM: [cad-host]/cad/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=ms
##### NetSuite: [cad-host]/cad/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=ns
 
* Get all Accounts:  HTTP GET [cad-host]/cad/object/account
* Get all Accounts by id: HTTP GET [cad-host]/cad/object/account/Id='001A000000E9ZEuIAN'
* Get all Accounts by name: HTTP GET [cad-host]/cad/object/account/name='Dickenson plc'
* Get all Accounts from "Electronics" Industries: HTTP GET [cad-host]/cad/object/account/Industry="Electronics"
* Get all Accounts from "Electronics" or "Energy" industry:  HTTP GET	[cad-host]/cad/object/account/Industry="Electronics"|Industry="Energy"
* Get all Accounts from "Electronics" industry and from "TX" state: HTTP GET	[cad-host]/cad/object/account/Industry='Electronics'&BillingState='TX'
* Get all Accounts from "Electronics" industry and from "TX" state and "Hot" rating: [cad-host]/cad/object/account/Industry="Electronics"&BillingState="TX"&Rating="Hot"
* Get all Accounts from "TX" or "KS" states:  HTTP GET [cad-host]/cad/object/account/BillingState='TX'|BillingState='KS'
* Get all direct customer Accounts:  HTTP GET [cad-host]/cad/object/account/type='Customer - Direct'
* Get an account with name like "Genepoint":  HTTP GET [cad-host]/cad/object/account/name like '%Genepoint%'
* Get an account with name and select Id, name, IsDeleted fields only:  HTTP GET [cad-host]/cad/object/account/name='Genepoint'/id,name,isdeleted 
* Get an account with id and select Id, name, IsDeleted fields only:  HTTP GET [cad-host]/cad/object/account/id='DDDASDVCSDFSD234'/id,name,isdeleted

##### For JSON  response add "?_type=json" Get JSON data for all Accounts: [cad-host]/cad/object/account?_type=json
