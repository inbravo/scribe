/*
 * MIT License
 * 
 * Copyright (c) 2016 Amit Dixit (github.com/inbravo)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.inbravo.scribe.rest.service.crm.ns;

import com.inbravo.scribe.rest.resource.ScribeObject;
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

  Task createTask(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception;

  Customer createCustomer(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception;

  Opportunity createOpportunity(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception;

  Contact createContact(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception;

  SupportCase createSupportCase(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception;

  PhoneCall createPhoneCall(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception;
}
