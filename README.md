# Scribe- stateless microservice to integrate cloud based CRMs

### Talk to all CRMs in a generic methodology: REST
### Use single communication format for all types of CRM
### Avoid CRM level complexity and access CRM objects using XML or JSON
### Supports following CRMs,
	1. SalesForce
	2. NetSuite
	3. MS Dynamics XRM
	4. Zoho
	5. ZenDesk ticketing system
### To get all CRM objects of type Contact from various CRM
	1. SalesForce: [scribe-host]/scribe/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=sfdc
	2. Microsoft Dynamics XRM: [scribe-host]/scribe/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=ms
	3. NetSuite: [scribe-host]/scribe/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=ns

#### To get CRM objects using query criteria 
* Get all Accounts:  HTTP GET [scribe-host]/scribe/object/account
* Get all Accounts by id: HTTP GET [scribe-host]/scribe/object/account/Id='001A000000E9ZEuIAN'
* Get all Accounts by name: HTTP GET [scribe-host]/scribe/object/account/name='Dickenson plc'
* Get all Accounts from "Electronics" Industries: HTTP GET [scribe-host]/scribe/object/account/Industry="Electronics"
* Get all Accounts from "Electronics" or "Energy" industry:  HTTP GET	[scribe-host]/scribe/object/account/Industry="Electronics"|Industry="Energy"
* Get all Accounts from "Electronics" industry and from "TX" state: HTTP GET	[scribe-host]/scribe/object/account/Industry='Electronics'&BillingState='TX'
* Get all Accounts from "Electronics" industry and from "TX" state and "Hot" rating: [scribe-host]/scribe/object/account/Industry="Electronics"&BillingState="TX"&Rating="Hot"
* Get all Accounts from "TX" or "KS" states:  HTTP GET [scribe-host]/scribe/object/account/BillingState='TX'|BillingState='KS'
* Get all direct customer Accounts:  HTTP GET [scribe-host]/scribe/object/account/type='Customer - Direct'
* Get an account with name like "Genepoint":  HTTP GET [scribe-host]/scribe/object/account/name like '%Genepoint%'

#### To get some fields of CRM objects using query criteria 
* Get an account with name and select Id, name, IsDeleted fields only:  HTTP GET [cad-host]/cad/object/account/name='Genepoint'/id,name,isdeleted 
* Get an account with id and select Id, name, IsDeleted fields only:  HTTP GET [cad-host]/cad/object/account/id='DDDASDVCSDFSD234'/id,name,isdeleted

##### For JSON  response add "?_type=json" Get JSON data for all Accounts: [cad-host]/cad/object/account?_type=json
