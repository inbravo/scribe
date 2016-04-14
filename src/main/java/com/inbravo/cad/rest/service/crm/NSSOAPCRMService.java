package com.inbravo.cad.rest.service.crm;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.constants.CRMConstants.NSCRMObjectType;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.service.crm.ns.NSCRMGenericService;
import com.inbravo.cad.rest.service.crm.ns.NetSuiteMessageFormatUtils;
import com.inbravo.cad.rest.service.crm.ns.session.NetSuiteCRMSessionManager;
import com.inbravo.cad.rest.service.crm.ns.type.NSCRMFieldTypes;
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
  public final CADCommandObject createObject(final CADCommandObject eDSACommandObject) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside createObject");
    }

    NetSuiteBindingStub soapBindingStub = null;

    /* Get respective Stub */
    if (eDSACommandObject.getAgent() != null) {

      /* Get Sales Force stub for the agent */
      soapBindingStub = cRMSessionManager.getSoapBindingStubForAgent(eDSACommandObject.getAgent());
    } else {
      /* Get NetSuite stub for the tenant */
      soapBindingStub = cRMSessionManager.getSoapBindingStubForTenant(eDSACommandObject.getTenant());
    }

    /* Create NS CRM basic object type */
    Record record = null;

    /* Check if object type is task */
    if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Task.toString())) {

      /* Create a new SupportCase */
      record = nSCRMGenericService.createTask(eDSACommandObject.geteDSAObject()[0], crmFieldIntraSeparator);
    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

      /* Create a new Task */
      record = nSCRMGenericService.createSupportCase(eDSACommandObject.geteDSAObject()[0], crmFieldIntraSeparator);
    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.PhoneCall.toString())) {

      /* Create a new phone call */
      record = nSCRMGenericService.createPhoneCall(eDSACommandObject.geteDSAObject()[0], crmFieldIntraSeparator);
    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

      /* Create a new customer */
      record = nSCRMGenericService.createCustomer(eDSACommandObject.geteDSAObject()[0], crmFieldIntraSeparator);
    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

      /* Create a new customer */
      record = nSCRMGenericService.createContact(eDSACommandObject.geteDSAObject()[0], crmFieldIntraSeparator);
    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Opportunity.toString())) {

      /* Create a new customer */
      record = nSCRMGenericService.createOpportunity(eDSACommandObject.geteDSAObject()[0], crmFieldIntraSeparator);
    } else {
      throw new CADException(CADResponseCodes._1003 + " Following CRM object:" + eDSACommandObject.getObjectType() + ", is not supported by the EDSA");
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
        throw new CADException(CADResponseCodes._1021 + "Not able to create : " + eDSACommandObject.getObjectType() + " : " + stringBuffer);
      } else {
        /* Inform user about error */
        throw new CADException(CADResponseCodes._1021 + "Not able to create : " + eDSACommandObject.getObjectType());
      }

    } else {
      /* Get base reference */
      final RecordRef baseRef = (RecordRef) response.getBaseRef();

      /* Check if base ref is not null */
      if (baseRef != null) {

        logger.debug("---Inside createObject, created object with id: " + baseRef.getInternalId());

        /* Set object id in object before sending back */
        eDSACommandObject.geteDSAObject()[0] =
            NetSuiteMessageFormatUtils.addNode(NSCRMFieldTypes.CRM_FIELD_ID, baseRef.getInternalId(), eDSACommandObject.geteDSAObject()[0]);
      }
    }

    return eDSACommandObject;
  }

  @Override
  public final boolean deleteObject(final CADCommandObject eDSACommandObject, final String idToBeDeleted) throws Exception {

    /* Inform user about error */
    throw new CADException(CADResponseCodes._1003 + " Following operation is not supported by the CRM");
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject) throws Exception {
    logger.debug("---Inside getObjects");
    NetSuiteBindingStub soapBindingStub = null;

    /* Get respective Stub */
    if (eDSACommandObject.getAgent() != null) {

      /* Get NetSuite stub for the agent */
      soapBindingStub = cRMSessionManager.getSoapBindingStubForAgent(eDSACommandObject.getAgent());
    } else {

      /* Get NetSuite stub for the tenant */
      soapBindingStub = cRMSessionManager.getSoapBindingStubForTenant(eDSACommandObject.getTenant());
    }

    SearchRecord searchRecord = null;

    /* Check CRM object type */
    if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

      /* Get custom customer search record */
      searchRecord = new CustomerSearch();

    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

      /* Get custom contact search record */
      searchRecord = new ContactSearch();
    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Employee.toString())) {

      /* Get custom employee search record */
      searchRecord = new EmployeeSearch();
    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

      /* Get custom support case search record */
      searchRecord = new SupportCaseSearch();
    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Task.toString())) {

      /* Get task search record */
      searchRecord = new TaskSearch();
    } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Opportunity.toString())) {

      /* Get opportunity search record */
      searchRecord = new OpportunitySearch();
    } else {

      /* Inform user about user error */
      throw new CADException(CADResponseCodes._1003 + "Following CRM object is not supported by the EDSA");
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

      /* Get respective Stub */
      if (eDSACommandObject.getAgent() != null) {

        /* If invalid session fault is given. Login again */
        if (cRMSessionManager.login(eDSACommandObject.getAgent())) {

          /* Get Sales Force stub for the agent */
          soapBindingStub = cRMSessionManager.getSoapBindingStubForAgent(eDSACommandObject.getAgent());

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
          throw new CADException(CADResponseCodes._1021 + " : Login Error : " + e.getMessage());
        }
      } else {

        /* If invalid session fault is given. Login again */
        if (cRMSessionManager.login(eDSACommandObject.getTenant())) {

          /* Get Sales Force stub for the tenant */
          soapBindingStub = cRMSessionManager.getSoapBindingStubForTenant(eDSACommandObject.getTenant());

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
          throw new CADException(CADResponseCodes._1021 + " : Login Error : " + e.getMessage());
        }
      }
    }

    /* Process the response */
    return NetSuiteMessageFormatUtils.processResponse(eDSACommandObject, searchResult, new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query) throws Exception {
    logger.debug("---Inside getObjects query: " + query);
    NetSuiteBindingStub soapBindingStub = null;

    /* Get respective Stub */
    if (eDSACommandObject.getAgent() != null) {

      /* Get Sales Force stub for the agent */
      soapBindingStub = cRMSessionManager.getSoapBindingStubForAgent(eDSACommandObject.getAgent());
    } else {
      /* Get NetSuite stub for the tenant */
      soapBindingStub = cRMSessionManager.getSoapBindingStubForTenant(eDSACommandObject.getTenant());
    }

    /* Add filter expression */
    if (query != null && !"NONE".equalsIgnoreCase(query)) {

      SearchRecord searchRecord = null;

      /* Check CRM object type */
      if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

        /* Get custom customer search record */
        searchRecord = nSCRMGenericService.createCustomerSearch(query, crmFieldIntraSeparator, inputDateFormat);

      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

        /* Get custom contact search record */
        searchRecord = nSCRMGenericService.createContactSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Employee.toString())) {

        /* Get custom employee search record */
        searchRecord = nSCRMGenericService.createEmployeeSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

        /* Get custom support case search record */
        searchRecord = nSCRMGenericService.createSupportCaseSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Task.toString())) {

        /* Get task search record */
        searchRecord = nSCRMGenericService.createTaskSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Opportunity.toString())) {

        /* Get opportunity search record */
        searchRecord = nSCRMGenericService.createOpportunitySearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else {

        /* Inform user about user error */
        throw new CADException(CADResponseCodes._1003 + "Following CRM object is not supported by the EDSA");
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
        /* Get respective Stub */
        if (eDSACommandObject.getAgent() != null) {

          /* If invalid session fault is given. Login again */
          if (cRMSessionManager.login(eDSACommandObject.getAgent())) {

            /* Get Sales Force stub for the agent */
            soapBindingStub = cRMSessionManager.getSoapBindingStubForAgent(eDSACommandObject.getAgent());

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
            throw new CADException(CADResponseCodes._1021 + " : Login Error : " + e.getMessage());
          }
        } else {

          /* If invalid session fault is given. Login again */
          if (cRMSessionManager.login(eDSACommandObject.getTenant())) {

            /* Get Sales Force stub for the tenant */
            soapBindingStub = cRMSessionManager.getSoapBindingStubForTenant(eDSACommandObject.getTenant());

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
            throw new CADException(CADResponseCodes._1021 + " : Login Error : " + e.getMessage());
          }
        }
      }

      /* Process the response */
      return NetSuiteMessageFormatUtils.processResponse(eDSACommandObject, searchResult, new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
    }

    return eDSACommandObject;
  }

  @Override
  public final CADCommandObject getObjectsCount(final CADCommandObject eDSACommandObject, final String query) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the EDSA");
  }

  @Override
  public final CADCommandObject updateObject(final CADCommandObject eDSACommandObject) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the EDSA");
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select) throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select);

    /* Trim the request variable */
    final String trimmedQuery = this.applyTrimming(query);

    /* Trim the request variable */
    final String trimmedSelect = this.applyTrimming(select);

    NetSuiteBindingStub soapBindingStub = null;

    /* Get respective Stub */
    if (eDSACommandObject.getAgent() != null) {

      /* Get Sales Force stub for the agent */
      soapBindingStub = cRMSessionManager.getSoapBindingStubForAgent(eDSACommandObject.getAgent());
    } else {
      /* Get NetSuite stub for the tenant */
      soapBindingStub = cRMSessionManager.getSoapBindingStubForTenant(eDSACommandObject.getTenant());
    }

    /* Add filter expression */
    if (trimmedQuery != null && !"NONE".equalsIgnoreCase(trimmedQuery)) {

      SearchRecord searchRecord = null;

      /* Check CRM object type */
      if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

        /* Get custom customer search record */
        searchRecord = nSCRMGenericService.createCustomerSearch(trimmedQuery, crmFieldIntraSeparator, inputDateFormat);

      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

        /* Get custom contact search record */
        searchRecord = nSCRMGenericService.createContactSearch(trimmedQuery, crmFieldIntraSeparator, inputDateFormat);
      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Employee.toString())) {

        /* Get custom employee search record */
        searchRecord = nSCRMGenericService.createEmployeeSearch(trimmedQuery, crmFieldIntraSeparator, inputDateFormat);
      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

        /* Get custom support case search record */
        searchRecord = nSCRMGenericService.createSupportCaseSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Task.toString())) {

        /* Get task search record */
        searchRecord = nSCRMGenericService.createTaskSearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else if (eDSACommandObject.getObjectType().equalsIgnoreCase(NSCRMObjectType.Opportunity.toString())) {

        /* Get opportunity search record */
        searchRecord = nSCRMGenericService.createOpportunitySearch(query, crmFieldIntraSeparator, inputDateFormat);
      } else {

        /* Inform user about user error */
        throw new CADException(CADResponseCodes._1003 + "Following CRM object is not supported by the EDSA");
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

        logger.debug("---Inside found InvalidSessionFault from NS CRM; going for replogin");
        /* Get respective Stub */
        if (eDSACommandObject.getAgent() != null) {

          /* If invalid session fault is given. Login again */
          if (cRMSessionManager.login(eDSACommandObject.getAgent())) {

            /* Get Sales Force stub for the agent */
            soapBindingStub = cRMSessionManager.getSoapBindingStubForAgent(eDSACommandObject.getAgent());

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
            throw new CADException(CADResponseCodes._1021 + " : Login Error : " + e.getMessage());
          }
        } else {

          /* If invalid session fault is given. Login again */
          if (cRMSessionManager.login(eDSACommandObject.getTenant())) {

            /* Get Sales Force stub for the tenant */
            soapBindingStub = cRMSessionManager.getSoapBindingStubForTenant(eDSACommandObject.getTenant());

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
            throw new CADException(CADResponseCodes._1021 + " : Login Error : " + e.getMessage());
          }
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
      return NetSuiteMessageFormatUtils.processResponse(eDSACommandObject, searchResult, crmFieldToBeSelectedSet);
    }

    return eDSACommandObject;
  }

  @Override
  public final CADCommandObject getObjectsCount(final CADCommandObject eDSACommandObject) throws Exception {

    /* Inform user about user error */
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by EDSA");
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select, final String order)
      throws Exception {

    /* Inform user about user error */
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by EDSA");
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
