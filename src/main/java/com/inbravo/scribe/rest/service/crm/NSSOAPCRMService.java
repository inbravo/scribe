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

package com.inbravo.scribe.rest.service.crm;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.constants.CRMConstants.NSCRMObjectType;
import com.inbravo.scribe.rest.resource.ScribeCommandObject;
import com.inbravo.scribe.rest.service.crm.ns.NSCRMGenericService;
import com.inbravo.scribe.rest.service.crm.ns.NetSuiteMessageFormatUtils;
import com.inbravo.scribe.rest.service.crm.ns.session.NetSuiteCRMSessionManager;
import com.inbravo.scribe.rest.service.crm.ns.type.NSCRMFieldTypes;
import com.netsuite.webservices.activities.scheduling.TaskSearch;
import com.netsuite.webservices.lists.employees.EmployeeSearch;
import com.netsuite.webservices.lists.relationships.ContactSearch;
import com.netsuite.webservices.lists.relationships.CustomerSearch;
import com.netsuite.webservices.lists.support.SupportCaseSearch;
import com.netsuite.webservices.platform.NetSuiteBindingStub;
import com.netsuite.webservices.platform.core.Record;
import com.netsuite.webservices.platform.core.RecordRef;
import com.netsuite.webservices.platform.core.SearchRecord;
import com.netsuite.webservices.platform.core.SearchResult;
import com.netsuite.webservices.platform.core.Status;
import com.netsuite.webservices.platform.core.StatusDetail;
import com.netsuite.webservices.platform.faults.InvalidSessionFault;
import com.netsuite.webservices.platform.messages.WriteResponse;
import com.netsuite.webservices.transactions.sales.OpportunitySearch;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class NSSOAPCRMService extends CRMService {

  private final Logger logger = Logger.getLogger(NSSOAPCRMService.class.getName());

  private NetSuiteCRMSessionManager cRMSessionManager;

  private NSCRMGenericService nSCRMGenericService;

  private String crmFieldsSeparator;

  private String orderFieldsSeparator;

  private String inputDateFormat;

  private String crmFieldIntraSeparator;

  @Override
  public final ScribeCommandObject createObject(final ScribeCommandObject scribeCommandObject) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside createObject");
    }

    /* Get NS stub for the agent */
    final NetSuiteBindingStub soapBindingStub =
        cRMSessionManager.getSoapBindingStub(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword());

    /* Create NS CRM basic object type */
    Record record = null;

    /* Check if object type is task */
    if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Task.toString())) {

      /* Create a new SupportCase */
      record = nSCRMGenericService.createTask(scribeCommandObject.getObject()[0], crmFieldIntraSeparator);
    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

      /* Create a new Task */
      record = nSCRMGenericService.createSupportCase(scribeCommandObject.getObject()[0], crmFieldIntraSeparator);
    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.PhoneCall.toString())) {

      /* Create a new phone call */
      record = nSCRMGenericService.createPhoneCall(scribeCommandObject.getObject()[0], crmFieldIntraSeparator);
    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

      /* Create a new customer */
      record = nSCRMGenericService.createCustomer(scribeCommandObject.getObject()[0], crmFieldIntraSeparator);
    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

      /* Create a new customer */
      record = nSCRMGenericService.createContact(scribeCommandObject.getObject()[0], crmFieldIntraSeparator);
    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Opportunity.toString())) {

      /* Create a new customer */
      record = nSCRMGenericService.createOpportunity(scribeCommandObject.getObject()[0], crmFieldIntraSeparator);
    } else {
      throw new ScribeException(ScribeResponseCodes._1003 + " Following CRM object:" + scribeCommandObject.getObjectType()
          + ", is not supported by the Scribe");
    }

    /* Get current system time before transaction */
    final long transStartTime = System.currentTimeMillis();

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside createObject : NS CRM create transaction started at : " + transStartTime);
    }

    /* Create task */
    final WriteResponse response = soapBindingStub.add(record);

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects : NS CRM create transaction completed in : '" + (System.currentTimeMillis() - transStartTime) + "' msec(s)");
    }

    /* Get status */
    final Status status = response.getStatus();
    final StatusDetail[] statusDetail = status.getStatusDetail();

    if (!response.getStatus().isIsSuccess()) {

      final StringBuffer stringBuffer = new StringBuffer();

      if (statusDetail != null) {
        for (int i = 0; i < statusDetail.length; i++) {
          stringBuffer.append(statusDetail[i].getType().getValue() + ":" + statusDetail[i].getMessage());
        }
      }

      if (stringBuffer.length() != 0) {
        /* Inform user about error */
        throw new ScribeException(ScribeResponseCodes._1021 + "Not able to create : " + scribeCommandObject.getObjectType() + " : " + stringBuffer);
      } else {
        /* Inform user about error */
        throw new ScribeException(ScribeResponseCodes._1021 + "Not able to create : " + scribeCommandObject.getObjectType());
      }

    } else {
      /* Get base reference */
      final RecordRef baseRef = (RecordRef) response.getBaseRef();

      /* Check if base ref is not null */
      if (baseRef != null) {

        logger.debug("---Inside createObject, created object with id: " + baseRef.getInternalId());

        /* Set object id in object before sending back */
        scribeCommandObject.getObject()[0] =
            NetSuiteMessageFormatUtils.addNode(NSCRMFieldTypes.CRM_FIELD_ID, baseRef.getInternalId(), scribeCommandObject.getObject()[0]);

        scribeCommandObject.getObject()[0].setObjectType(baseRef.getName());
      }
    }

    return scribeCommandObject;
  }

  @Override
  public final boolean deleteObject(final ScribeCommandObject scribeCommandObject, final String idToBeDeleted) throws Exception {

    /* Inform user about error */
    throw new ScribeException(ScribeResponseCodes._1003 + " Following operation is not supported by the CRM");
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject scribeCommandObject) throws Exception {
    logger.debug("---Inside getObjects");

    /* Get NS stub for the agent */
    NetSuiteBindingStub soapBindingStub =
        cRMSessionManager.getSoapBindingStub(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword());

    SearchRecord searchRecord = null;

    /* Check CRM object type */
    if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

      /* Get custom customer search record */
      searchRecord = new CustomerSearch();

    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

      /* Get custom contact search record */
      searchRecord = new ContactSearch();
    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Employee.toString())) {

      /* Get custom employee search record */
      searchRecord = new EmployeeSearch();
    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

      /* Get custom support case search record */
      searchRecord = new SupportCaseSearch();
    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Task.toString())) {

      /* Get task search record */
      searchRecord = new TaskSearch();
    } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Opportunity.toString())) {

      /* Get opportunity search record */
      searchRecord = new OpportunitySearch();
    } else {

      /* Inform user about user error */
      throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM object is not supported by the Scribe");
    }

    SearchResult searchResult = null;

    try {

      /* Get current system time before transaction */
      final long transStartTime = System.currentTimeMillis();

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects : NS CRM query transaction started at : " + transStartTime);
      }

      /* Invoke search operation */
      searchResult = soapBindingStub.search(searchRecord);

      if (logger.isDebugEnabled()) {
        logger
            .debug("---Inside getObjects : NS CRM query transaction completed in : '" + (System.currentTimeMillis() - transStartTime) + "' msec(s)");
      }

    } catch (final InvalidSessionFault e) {

      logger.debug("---Inside getObjects found InvalidSessionFault from NS CRM; going to relogin");

      /* If invalid session fault is given. Login again */
      if (cRMSessionManager.login(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword())) {

        /* Get Sales Force stub for the agent */
        soapBindingStub = cRMSessionManager.getSoapBindingStub(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword());

        /* Get current system time before transaction */
        final long transStartTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside getObjects : NS CRM query transaction started at : " + transStartTime);
        }

        /* Invoke search operation */
        searchResult = soapBindingStub.search(searchRecord);

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside getObjects : NS CRM query transaction completed in : '" + (System.currentTimeMillis() - transStartTime)
              + "' msec(s)");
        }
      } else {

        /* Inform user about remote error */
        throw new ScribeException(ScribeResponseCodes._1021 + " : Login Error : " + e.getMessage());
      }
    }

    /* Process the response */
    return NetSuiteMessageFormatUtils.processResponse(scribeCommandObject, searchResult, new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject scribeCommandObject, final String query) throws Exception {
    logger.debug("---Inside getObjects query: " + query);


    /* Get NS stub for the agent */
    NetSuiteBindingStub soapBindingStub =
        cRMSessionManager.getSoapBindingStub(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword());

    /* Add filter expression */
    if (query != null && !"NONE".equalsIgnoreCase(query)) {

      SearchRecord searchRecord = null;

      /* Check CRM object type */
      if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

        /* Get custom customer search record */
        searchRecord = nSCRMGenericService.createCustomerSearch(query, crmFieldIntraSeparator, inputDateFormat);

      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

        /* Get custom contact search record */
        searchRecord = nSCRMGenericService.createContactSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Employee.toString())) {

        /* Get custom employee search record */
        searchRecord = nSCRMGenericService.createEmployeeSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

        /* Get custom support case search record */
        searchRecord = nSCRMGenericService.createSupportCaseSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Task.toString())) {

        /* Get task search record */
        searchRecord = nSCRMGenericService.createTaskSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Opportunity.toString())) {

        /* Get opportunity search record */
        searchRecord = nSCRMGenericService.createOpportunitySearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else {

        /* Inform user about user error */
        throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM object is not supported by the Scribe");
      }

      SearchResult searchResult = null;

      try {

        /* Get current system time before transaction */
        final long transStartTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside getObjects : NS CRM query transaction started at : " + transStartTime);
        }

        /* Invoke search operation */
        searchResult = soapBindingStub.search(searchRecord);

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside getObjects : NS CRM query transaction completed in : '" + (System.currentTimeMillis() - transStartTime)
              + "' msec(s)");
        }

      } catch (final InvalidSessionFault e) {

        logger.debug("---Inside getObjects found InvalidSessionFault from NS CRM; going to relogin");

        /* If invalid session fault is given. Login again */
        if (cRMSessionManager.login(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword())) {

          /* Get Sales Force stub for the agent */
          soapBindingStub = cRMSessionManager.getSoapBindingStub(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword());

          /* Get current system time before transaction */
          final long transStartTime = System.currentTimeMillis();

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside getObjects : NS CRM query transaction started at : " + transStartTime);
          }

          /* Invoke search operation */
          searchResult = soapBindingStub.search(searchRecord);

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside getObjects : NS CRM query transaction completed in : '" + (System.currentTimeMillis() - transStartTime)
                + "' msec(s)");
          }
        } else {

          /* Inform user about remote error */
          throw new ScribeException(ScribeResponseCodes._1021 + " : Login Error : " + e.getMessage());
        }
      }

      /* Process the response */
      return NetSuiteMessageFormatUtils.processResponse(scribeCommandObject, searchResult, new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
    }

    return scribeCommandObject;
  }

  @Override
  public final ScribeCommandObject getObjectsCount(final ScribeCommandObject scribeCommandObject, final String query) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + "Following operation is not supported by the Scribe");
  }

  @Override
  public final ScribeCommandObject updateObject(final ScribeCommandObject scribeCommandObject) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + "Following operation is not supported by the Scribe");
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject scribeCommandObject, final String query, final String select)
      throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select);

    /* Trim the request variable */
    final String trimmedQuery = this.applyTrimming(query);

    /* Trim the request variable */
    final String trimmedSelect = this.applyTrimming(select);

    /* Get NS stub for the agent */
    NetSuiteBindingStub soapBindingStub =
        cRMSessionManager.getSoapBindingStub(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword());

    /* Add filter expression */
    if (trimmedQuery != null && !"NONE".equalsIgnoreCase(trimmedQuery)) {

      SearchRecord searchRecord = null;

      /* Check CRM object type */
      if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

        /* Get custom customer search record */
        searchRecord = nSCRMGenericService.createCustomerSearch(trimmedQuery, crmFieldIntraSeparator, inputDateFormat);

      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

        /* Get custom contact search record */
        searchRecord = nSCRMGenericService.createContactSearch(trimmedQuery, crmFieldIntraSeparator, inputDateFormat);
      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Employee.toString())) {

        /* Get custom employee search record */
        searchRecord = nSCRMGenericService.createEmployeeSearch(trimmedQuery, crmFieldIntraSeparator, inputDateFormat);
      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

        /* Get custom support case search record */
        searchRecord = nSCRMGenericService.createSupportCaseSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Task.toString())) {

        /* Get task search record */
        searchRecord = nSCRMGenericService.createTaskSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (scribeCommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Opportunity.toString())) {

        /* Get opportunity search record */
        searchRecord = nSCRMGenericService.createOpportunitySearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else {

        /* Inform user about user error */
        throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM object is not supported by the Scribe");
      }

      SearchResult searchResult = null;

      try {

        /* Get current system time before transaction */
        final long transStartTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside getObjects : NS CRM query transaction started at : " + transStartTime);
        }

        /* Invoke search operation */
        searchResult = soapBindingStub.search(searchRecord);

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside getObjects : NS CRM query transaction completed in : '" + (System.currentTimeMillis() - transStartTime)
              + "' msec(s)");
        }

      } catch (final InvalidSessionFault e) {

        logger.debug("---Inside getObjects found InvalidSessionFault from NS CRM; going to relogin");

        /* If invalid session fault is given. Login again */
        if (cRMSessionManager.login(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword())) {

          /* Get Sales Force stub for the agent */
          soapBindingStub = cRMSessionManager.getSoapBindingStub(scribeCommandObject.getCrmUserId(), scribeCommandObject.getCrmPassword());

          /* Get current system time before transaction */
          final long transStartTime = System.currentTimeMillis();

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside getObjects : NS CRM query transaction started at : " + transStartTime);
          }

          /* Invoke search operation */
          searchResult = soapBindingStub.search(searchRecord);

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside getObjects : NS CRM query transaction completed in : '" + (System.currentTimeMillis() - transStartTime)
                + "' msec(s)");
          }
        } else {

          /* Inform user about remote error */
          throw new ScribeException(ScribeResponseCodes._1021 + " : Login Error : " + e.getMessage());
        }
      }

      /* This set is to contain. CRM fields to be selected */
      final Set<String> crmFieldToBeSelectedSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

      /* Check for a valid select */
      if (trimmedSelect != null && trimmedSelect.contains(",")) {

        /* Tokenize the select clause */
        final StringTokenizer stringTokenizer = new StringTokenizer(trimmedSelect, ",");

        /* Iterate over elements */
        while (stringTokenizer.hasMoreElements()) {

          final String nextToken = stringTokenizer.nextToken();

          if (nextToken != null) {

            /* Add token in list */
            crmFieldToBeSelectedSet.add(nextToken.trim());
          }
        }
      } else if (trimmedSelect != null && (!trimmedSelect.equalsIgnoreCase("") && !trimmedSelect.equalsIgnoreCase("ALL"))) {

        /* Check of request is not to select ALL */
        crmFieldToBeSelectedSet.add(trimmedSelect);
      }

      /* Process the response */
      return NetSuiteMessageFormatUtils.processResponse(scribeCommandObject, searchResult, crmFieldToBeSelectedSet);
    }

    return scribeCommandObject;
  }

  @Override
  public final ScribeCommandObject getObjectsCount(final ScribeCommandObject scribeCommandObject) throws Exception {

    /* Inform user about user error */
    throw new ScribeException(ScribeResponseCodes._1003 + "Following operation is not supported by Scribe");
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject scribeCommandObject, final String query, final String select,
      final String order) throws Exception {

    /* Inform user about user error */
    throw new ScribeException(ScribeResponseCodes._1003 + "Following operation is not supported by Scribe");
  }

  /**
   * 
   * @param stringToBeTrimmed
   * @return
   */
  private final String applyTrimming(final String stringToBeTrimmed) {

    if (stringToBeTrimmed != null) {

      /* Return trimmed string */
      return stringToBeTrimmed.trim();
    } else {
      return stringToBeTrimmed;
    }
  }

  public final NetSuiteCRMSessionManager getcRMSessionManager() {
    return cRMSessionManager;
  }

  public final void setcRMSessionManager(final NetSuiteCRMSessionManager cRMSessionManager) {
    this.cRMSessionManager = cRMSessionManager;
  }

  public final NSCRMGenericService getnSCRMGenericService() {
    return nSCRMGenericService;
  }

  public final void setnSCRMGenericService(final NSCRMGenericService nSCRMGenericService) {
    this.nSCRMGenericService = nSCRMGenericService;
  }

  public final String getCrmFieldsSeparator() {
    return crmFieldsSeparator;
  }

  public final void setCrmFieldsSeparator(final String crmFieldsSeparator) {
    this.crmFieldsSeparator = crmFieldsSeparator;
  }

  public final String getOrderFieldsSeparator() {
    return orderFieldsSeparator;
  }

  public final void setOrderFieldsSeparator(final String orderFieldsSeparator) {
    this.orderFieldsSeparator = orderFieldsSeparator;
  }

  public final String getInputDateFormat() {
    return inputDateFormat;
  }

  public final void setInputDateFormat(final String inputDateFormat) {
    this.inputDateFormat = inputDateFormat;
  }

  public final String getCrmFieldIntraSeparator() {
    return crmFieldIntraSeparator;
  }

  public void setCrmFieldIntraSeparator(final String crmFieldIntraSeparator) {
    this.crmFieldIntraSeparator = crmFieldIntraSeparator;
  }
}
