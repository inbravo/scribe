# CAD:  Facade for seemless integration of different CRMs
## Add a separate CRM layer to adapt new CRMs 
## Add any CRM using simple REST calls
### Access CRM objects using xml on http
### Supports following CRMs,
	1. SalesForce
	2. NetSuite
	3. MS Dynamics
	4. Zoho
	5. ZenDesk ticketing system

### To get all CRM objects of type Contact from SalesForce, Microsoft Dynamics, NetSuite
	http://<host>:<port>/cad/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=sfdc
	http://<host>:<port>/cad/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=ms
	http://<host>:<port>/cad/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=ns

### Get CRM objects by some field values 
	Get all Accounts:  HTTP method: GET
	http://<host>:<port>/cad/object/account
	
	Get all Accounts by id: HTTP method: GET
	http://<host>:<port>/cad/object/account/Id='001A000000E9ZEuIAN'
	
	Get all Accounts by name: HTTP method: GET
	http://<host>:<port>/cad/object/account/name='Dickenson plc'
	
	Get all Accounts from ‘Electronics’ Industries: HTTP method: GET
	http://<host>:<port>/cad/object/account/Industry=’Electronics’
	
	Get all Accounts from ‘Electronics’ or ‘Energy’ industry:  HTTP method: GET	http://<host>:<port>/cad/object/account/Industry=’Electronics’|Industry=’Energy’
	
	Get all Accounts from ‘Electronics’ industry and from ‘TX’ state: HTTP method: GET	http://<host>:<port>/cad/object/account/Industry='Electronics'&BillingState='TX'
	
	Get all Accounts from ‘Electronics’ industry and from ‘TX’ state and ‘Hot’ rating: 
	http://<host>:<port>/cad/object/account/Industry=’Electronics’&BillingState=’TX’&Rating=’Hot’
	
	Get all Accounts from ‘TX’ or ‘KS’ states:  HTTP method: GET
	http://<host>:<port>/cad/object/account/BillingState='TX'|BillingState='KS'
	
	Get all direct customer Accounts:  HTTP method: GET
	http://<host>:<port>/cad/object/account/type='Customer - Direct'
	
	Get an account with name like ‘Genepoint’:  HTTP method: GET
	http://<host>:<port>/cad/object/account/name like '%Genepoint%'
	
	Get an account with name and select Id, name, IsDeleted fields only:  HTTP method: GET
	http://<host>:<port>/cad/object/account/name='Genepoint'/id,name,isdeleted 
	
	Get an account with id and select Id, name, IsDeleted fields only:  HTTP method: GET
	http://<host>:<port>/cad/object/account/id='DDDASDVCSDFSD234'/id,name,isdeleted
	
	For searching JSON of any of the query of above just add ‘?_type=json’ for example 
	Get JSON data for all Accounts: http://<host>:<port>/cad/object/account?_type=json
	
	Delete an Account by id: HTTP method: DELETE
	http://<host>:<port>/cad/object/account/001A000000E9ZEuIAN
	
	Create an Account: HTTP method: POST + xml data in request
	http://<host>:<port>/cad/object/account
	
	Update an Account: HTTP method: PUT + xml data in request
	http://<host>:<port>/cad/object/account

