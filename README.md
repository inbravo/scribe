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

### To download all CRM objects of type Contact from SalesForce, Microsoft Dynamics, NetSuite
	http://<host>:<port>/cad/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=sfdc
	http://<host>:<port>/cad/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=ms
	http://<host>:<port>/cad/object/contact?CrmUserId=<crm-user-id>&CrmPassword=<crm-password>&crmtype=ns
