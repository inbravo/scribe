### To get CRM objects using query and select criteria (HTTP GET [scribe-host]/scribe/object/[object-type]/[object-fields])
- Get all Accounts:  HTTP GET [scribe-host]/scribe/object/account/id,name
- Get all Accounts by id: HTTP GET [scribe-host]/scribe/object/account/Id='001A000000E9ZEuIAN'/id,name
- Get all Accounts by name: HTTP GET [scribe-host]/scribe/object/account/name='Dickenson plc'/id,name,createddate
