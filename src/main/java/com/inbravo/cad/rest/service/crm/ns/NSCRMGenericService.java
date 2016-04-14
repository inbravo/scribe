package com.inbravo.cad.rest.service.crm.ns;

import com.inbravo.cad.rest.resource.CADObject;
import com.netsuite.webservices.activities.scheduling.PhoneCall;
import com.netsuite.webservices.activities.scheduling.Task;
import com.netsuite.webservices.lists.relationships.Contact;
import com.netsuite.webservices.lists.relationships.Customer;
import com.netsuite.webservices.lists.support.SupportCase;
import com.netsuite.webservices.platform.core.SearchRecord;
import com.netsuite.webservices.transactions.sales.Opportunity;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface NSCRMGenericService {

  SearchRecord createCustomerSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception;

  SearchRecord createContactSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception;

  SearchRecord createSupportCaseSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception;

  SearchRecord createTaskSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception;

  SearchRecord createOpportunitySearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception;

  SearchRecord createEmployeeSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception;

  Task createTask(final CADObject eDSAObject, final String crmFieldIntraSeparator) throws Exception;

  Customer createCustomer(final CADObject eDSAObject, final String crmFieldIntraSeparator) throws Exception;

  Opportunity createOpportunity(final CADObject eDSAObject, final String crmFieldIntraSeparator) throws Exception;

  Contact createContact(final CADObject eDSAObject, final String crmFieldIntraSeparator) throws Exception;

  SupportCase createSupportCase(final CADObject eDSAObject, final String crmFieldIntraSeparator) throws Exception;

  PhoneCall createPhoneCall(final CADObject eDSAObject, final String crmFieldIntraSeparator) throws Exception;
}
