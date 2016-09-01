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

package com.inbravo.scribe.rest.service.crm.ns.v2k9;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.constants.HTTPConstants;
import com.inbravo.scribe.rest.constants.CRMConstants.NSCRMObjectType;
import com.inbravo.scribe.rest.resource.ScribeObject;
import com.inbravo.scribe.rest.service.crm.ns.NSCRMGenericService;
import com.inbravo.scribe.rest.service.crm.ns.NetSuiteMessageFormatUtils;
import com.inbravo.scribe.rest.service.crm.ns.type.NSCRMFieldTypes;
import com.netsuite.webservices.activities.scheduling.PhoneCall;
import com.netsuite.webservices.activities.scheduling.Task;
import com.netsuite.webservices.activities.scheduling.TaskSearch;
import com.netsuite.webservices.activities.scheduling.types.PhoneCallStatus;
import com.netsuite.webservices.activities.scheduling.types.TaskStatus;
import com.netsuite.webservices.lists.employees.EmployeeSearch;
import com.netsuite.webservices.lists.relationships.Contact;
import com.netsuite.webservices.lists.relationships.ContactSearch;
import com.netsuite.webservices.lists.relationships.Customer;
import com.netsuite.webservices.lists.relationships.CustomerSearch;
import com.netsuite.webservices.lists.support.SupportCase;
import com.netsuite.webservices.lists.support.SupportCaseSearch;
import com.netsuite.webservices.platform.common.ContactSearchBasic;
import com.netsuite.webservices.platform.common.CustomerSearchBasic;
import com.netsuite.webservices.platform.common.EmployeeSearchBasic;
import com.netsuite.webservices.platform.common.OpportunitySearchBasic;
import com.netsuite.webservices.platform.common.SupportCaseSearchBasic;
import com.netsuite.webservices.platform.common.TaskSearchBasic;
import com.netsuite.webservices.platform.core.BooleanCustomFieldRef;
import com.netsuite.webservices.platform.core.CustomFieldList;
import com.netsuite.webservices.platform.core.CustomFieldRef;
import com.netsuite.webservices.platform.core.DateCustomFieldRef;
import com.netsuite.webservices.platform.core.DoubleCustomFieldRef;
import com.netsuite.webservices.platform.core.Duration;
import com.netsuite.webservices.platform.core.ListOrRecordRef;
import com.netsuite.webservices.platform.core.LongCustomFieldRef;
import com.netsuite.webservices.platform.core.MultiSelectCustomFieldRef;
import com.netsuite.webservices.platform.core.RecordRef;
import com.netsuite.webservices.platform.core.SearchBooleanCustomField;
import com.netsuite.webservices.platform.core.SearchBooleanField;
import com.netsuite.webservices.platform.core.SearchCustomField;
import com.netsuite.webservices.platform.core.SearchCustomFieldList;
import com.netsuite.webservices.platform.core.SearchDateCustomField;
import com.netsuite.webservices.platform.core.SearchDateField;
import com.netsuite.webservices.platform.core.SearchDoubleField;
import com.netsuite.webservices.platform.core.SearchEnumMultiSelectField;
import com.netsuite.webservices.platform.core.SearchLongCustomField;
import com.netsuite.webservices.platform.core.SearchLongField;
import com.netsuite.webservices.platform.core.SearchMultiSelectCustomField;
import com.netsuite.webservices.platform.core.SearchRecord;
import com.netsuite.webservices.platform.core.SearchStringCustomField;
import com.netsuite.webservices.platform.core.SearchStringField;
import com.netsuite.webservices.platform.core.SearchTextNumberField;
import com.netsuite.webservices.platform.core.StringCustomFieldRef;
import com.netsuite.webservices.platform.core.types.DurationUnit;
import com.netsuite.webservices.platform.core.types.SearchDateFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchDoubleFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchEnumMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchLongFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchStringFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchTextNumberFieldOperator;
import com.netsuite.webservices.transactions.sales.Opportunity;
import com.netsuite.webservices.transactions.sales.OpportunitySearch;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class NSCRMV2K9Service implements NSCRMGenericService {

  private static final Logger logger = Logger.getLogger(NSCRMV2K9Service.class.getName());

  /**
   * 
   * @param query
   * @param order
   * @param crmFieldsSeparator
   * @param orderFieldsSeparator
   * @return
   * @throws Exception
   */
  public final SearchRecord createCustomerSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat)
      throws Exception {

    if (query != null) {

      /* Inform user that 'OR' is not supported */
      if (query.contains(HTTPConstants.orClause)) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Operator: '" + HTTPConstants.orClause + "' is not supported by Scribe");
      }

      /* Create customer search */
      final CustomerSearch customerSearch = new CustomerSearch();

      /* Create customer search basic object to set search variable */
      CustomerSearchBasic customerSearchBasic = new CustomerSearchBasic();

      /* Tokenize the where clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(query, HTTPConstants.andClause, false);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {

        /* Get next token */
        final String tempElement = stringTokenizer.nextToken();
        logger.debug("----Inside createCustomerSearch: tempElement: " + tempElement);

        /* Split the search string into key operator value */
        final String[] splitSearchStringResult = this.splitSearchString(tempElement);

        /**
         * Set the customerSearchBasic. We should not interpret NULL since we should support empty
         * value itself. Let the client to decide what to do.
         */
        customerSearchBasic =
            this.updateCustomerSearchBasic(customerSearchBasic, splitSearchStringResult[0], splitSearchStringResult[2], splitSearchStringResult[1],
                crmFieldIntraSeparator, inputDateFormat);
      }

      /* Set search criteria */
      customerSearch.setBasic(customerSearchBasic);

      return customerSearch;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param query
   * @return
   * @throws Exception
   */
  public final SearchRecord createContactSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat)
      throws Exception {

    if (query != null) {

      /* Inform user that 'OR' is not supported */
      if (query.contains(HTTPConstants.orClause)) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Operator: '" + HTTPConstants.orClause + "' is not supported by Scribe");
      }

      /* Create contact search */
      final ContactSearch contactSearch = new ContactSearch();

      /* Tokenize the where clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(query, HTTPConstants.andClause, false);

      /* Create new contact search basic object */
      ContactSearchBasic contactSearchBasic = new ContactSearchBasic();

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {

        /* Get next token */
        final String tempElement = stringTokenizer.nextToken();
        logger.debug("----Inside createContactSearch: tempElement: " + tempElement);

        /* Split the search string into key operator value */
        final String[] splitSearchStringResult = this.splitSearchString(tempElement);

        /* Update contact search object search */
        contactSearchBasic =
            this.updateContactSearchBasic(contactSearchBasic, splitSearchStringResult[0], splitSearchStringResult[2], splitSearchStringResult[1],
                crmFieldIntraSeparator, inputDateFormat);
      }

      /* Set search basic in search object */
      contactSearch.setBasic(contactSearchBasic);

      return contactSearch;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param query
   * @return
   * @throws Exception
   */
  public final SearchRecord createSupportCaseSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat)
      throws Exception {

    if (query != null) {

      /* Inform user that 'OR' is not supported */
      if (query.contains(HTTPConstants.orClause)) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Operator: '" + HTTPConstants.orClause + "' is not supported by Scribe");
      }

      /* Create SupportCaseSearch */
      final SupportCaseSearch supportCaseSearch = new SupportCaseSearch();

      /* Create SupportCaseSearchBasic object to set search varialble */
      SupportCaseSearchBasic supportCaseSearchBasic = new SupportCaseSearchBasic();

      /* Tokenize the where clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(query, HTTPConstants.andClause, false);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {

        /* Get next token */
        final String tempElement = stringTokenizer.nextToken();
        logger.debug("----Inside createSupportCaseSearch: tempElement: " + tempElement);

        /* Split the search string into key operator value */
        final String[] splitSearchStringResult = this.splitSearchString(tempElement);

        /* Update support case search object search */
        supportCaseSearchBasic =
            this.updateSupportCaseSearchBasic(supportCaseSearchBasic, splitSearchStringResult[0], splitSearchStringResult[2],
                splitSearchStringResult[1], crmFieldIntraSeparator, inputDateFormat);
      }

      /* Set search criteria */
      supportCaseSearch.setBasic(supportCaseSearchBasic);

      return supportCaseSearch;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param query
   * @return
   * @throws Exception
   */
  public final SearchRecord createTaskSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception {

    if (query != null) {

      /* Inform user that 'OR' is not supported */
      if (query.contains(HTTPConstants.orClause)) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Operator: '" + HTTPConstants.orClause + "' is not supported by Scribe");
      }

      /* Create TaskSearch */
      final TaskSearch taskSearch = new TaskSearch();

      /* Create TaskSearchBasic object to set search varialble */
      TaskSearchBasic taskSearchBasic = new TaskSearchBasic();

      /* Tokenize the where clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(query, HTTPConstants.andClause, false);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {

        /* Get next token */
        final String tempElement = stringTokenizer.nextToken();
        logger.debug("----Inside createTaskSearch: tempElement: " + tempElement);

        /* Split the search string into key operator value */
        final String[] splitSearchStringResult = this.splitSearchString(tempElement);

        /* Update task search object search */
        taskSearchBasic =
            this.updateTaskSearchBasic(taskSearchBasic, splitSearchStringResult[0], splitSearchStringResult[2], splitSearchStringResult[1],
                crmFieldIntraSeparator, inputDateFormat);
      }

      /* Set search criteria */
      taskSearch.setBasic(taskSearchBasic);

      return taskSearch;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param query
   * @return
   * @throws Exception
   */
  public final SearchRecord createOpportunitySearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat)
      throws Exception {

    if (query != null) {

      /* Inform user that 'OR' is not supported */
      if (query.contains(HTTPConstants.orClause)) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Operator: '" + HTTPConstants.orClause + "' is not supported by Scribe");
      }

      /* Create OpportunitySearch */
      final OpportunitySearch opportunitySearch = new OpportunitySearch();

      /* Create OpportunitySearchBasic object to set search varialble */
      OpportunitySearchBasic opportunitySearchBasic = new OpportunitySearchBasic();

      /* Tokenize the where clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(query, HTTPConstants.andClause, false);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {

        /* Get next token */
        final String tempElement = stringTokenizer.nextToken();
        logger.debug("----Inside createOpportunitySearch: tempElement: " + tempElement);

        /* Split the search string into key operator value */
        final String[] splitSearchStringResult = this.splitSearchString(tempElement);

        /* Update opportunity search object search */
        opportunitySearchBasic =
            this.updateOpportunitySearchBasic(opportunitySearchBasic, splitSearchStringResult[0], splitSearchStringResult[2],
                splitSearchStringResult[1], crmFieldIntraSeparator, inputDateFormat);
      }

      /* Set search criteria */
      opportunitySearch.setBasic(opportunitySearchBasic);

      return opportunitySearch;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param employeeSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private final SupportCaseSearchBasic updateSupportCaseSearchBasic(final SupportCaseSearchBasic supportCaseSearchBasic, final String cRMFieldName,
      final String cRMFieldValue, final String cRMFieldOperator, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception {

    logger.debug("----Inside updateSupportCaseSearch cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + cRMFieldValue + " & cRMFieldOperator: "
        + cRMFieldOperator);

    if (cRMFieldName != null) {

      /* Check if cRMFieldName contans crmFieldIntraSeparator */
      if (cRMFieldName.contains(crmFieldIntraSeparator)) {

        String nodeName = null;
        String nodeType = null;

        final int index = cRMFieldName.lastIndexOf(crmFieldIntraSeparator);

        /* If found a positive index */
        if (index > 0) {

          /* Get node name */
          nodeName = cRMFieldName.substring(0, index).trim();

          /* Get node type */
          nodeType = cRMFieldName.substring(index + crmFieldIntraSeparator.length(), cRMFieldName.length()).trim();
        }

        /* Validate node name/type */
        if (nodeName == null & nodeType == null) {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + cRMFieldName + "' is not in correct format");
        }

        try {

          /* If a custom field */
          if (nodeType.toUpperCase().contains("CUSTOM")) {

            /* Invoke the normal field setter method */
            SearchCustomFieldList searchCustomFieldList = supportCaseSearchBasic.getCustomFieldList();

            /* If already a list */
            if (searchCustomFieldList != null) {

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(searchCustomFieldList, nodeName, nodeType, cRMFieldValue,
                  cRMFieldOperator));
            } else {

              /* Create a new list */
              searchCustomFieldList = new SearchCustomFieldList();

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(null, nodeName, nodeType, cRMFieldValue, cRMFieldOperator));
            }

            /* Set custom field list */
            supportCaseSearchBasic.setCustomFieldList(searchCustomFieldList);
          } else {

            /* Invoke the normal field setter method */
            supportCaseSearchBasic
                .getClass()
                .getMethod("set" + nodeName.trim(), Class.forName(NetSuiteMessageFormatUtils.NS_WSDL_PACKAGE_NAME + nodeType.trim()))
                .invoke(supportCaseSearchBasic,
                    this.getNormalFieldForSearchOperation(nodeName.trim(), nodeType.trim(), cRMFieldValue, cRMFieldOperator, inputDateFormat));
          }

        } catch (final ClassNotFoundException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'SupportCase'");
        } catch (final NoSuchMethodException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'SupportCase'");
        } catch (final IllegalArgumentException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'SupportCase'");
        } catch (final IllegalAccessException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'SupportCase'");
        } catch (final InvocationTargetException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'SupportCase'");
        }
      } else {
        throw new ScribeException(ScribeResponseCodes._1003 + "CRM field format should be CRMFIELDNAME" + crmFieldIntraSeparator + "CRMFIELDTYPE");
      }
    }

    return supportCaseSearchBasic;
  }

  /**
   * 
   * @param employeeSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private final OpportunitySearchBasic updateOpportunitySearchBasic(final OpportunitySearchBasic opportunitySearchBasic, final String cRMFieldName,
      final String cRMFieldValue, final String cRMFieldOperator, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception {

    logger.debug("----Inside updateOpportunitySearchBasic cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + cRMFieldValue
        + " & cRMFieldOperator: " + cRMFieldOperator);

    if (cRMFieldName != null) {

      /* Check if cRMFieldName contans crmFieldIntraSeparator */
      if (cRMFieldName.contains(crmFieldIntraSeparator)) {

        String nodeName = null;
        String nodeType = null;

        final int index = cRMFieldName.lastIndexOf(crmFieldIntraSeparator);

        /* If found a positive index */
        if (index > 0) {

          /* Get node name */
          nodeName = cRMFieldName.substring(0, index).trim();

          /* Get node type */
          nodeType = cRMFieldName.substring(index + crmFieldIntraSeparator.length(), cRMFieldName.length()).trim();
        }

        /* Validate node name/type */
        if (nodeName == null & nodeType == null) {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + cRMFieldName + "' is not in correct format");
        }

        try {

          /* If a custom field */
          if (nodeType.toUpperCase().contains("CUSTOM")) {

            /* Invoke the normal field setter method */
            SearchCustomFieldList searchCustomFieldList = opportunitySearchBasic.getCustomFieldList();

            /* If already a list */
            if (searchCustomFieldList != null) {

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(searchCustomFieldList, nodeName, nodeType, cRMFieldValue,
                  cRMFieldOperator));
            } else {

              /* Create a new list */
              searchCustomFieldList = new SearchCustomFieldList();

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(null, nodeName, nodeType, cRMFieldValue, cRMFieldOperator));

            }

            /* Set custom field list */
            opportunitySearchBasic.setCustomFieldList(searchCustomFieldList);
          } else {

            /* Invoke the normal field setter method */
            opportunitySearchBasic
                .getClass()
                .getMethod("set" + nodeName.trim(), Class.forName(NetSuiteMessageFormatUtils.NS_WSDL_PACKAGE_NAME + nodeType.trim()))
                .invoke(opportunitySearchBasic,
                    this.getNormalFieldForSearchOperation(nodeName.trim(), nodeType.trim(), cRMFieldValue, cRMFieldOperator, inputDateFormat));
          }

        } catch (final ClassNotFoundException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Opportunity'");
        } catch (final NoSuchMethodException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Opportunity'");
        } catch (final IllegalArgumentException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Opportunity'");
        } catch (final IllegalAccessException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Opportunity'");
        } catch (final InvocationTargetException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + " Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Opportunity'");
        }
      } else {
        throw new ScribeException(ScribeResponseCodes._1003 + "CRM field format should be CRMFIELDNAME" + crmFieldIntraSeparator + "CRMFIELDTYPE");
      }
    }

    return opportunitySearchBasic;
  }

  /**
   * 
   * @param employeeSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private final TaskSearchBasic updateTaskSearchBasic(final TaskSearchBasic taskSearchBasic, final String cRMFieldName, final String cRMFieldValue,
      final String cRMFieldOperator, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception {

    logger.debug("----Inside updateTaskSearchBasic cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + cRMFieldValue + " & cRMFieldOperator: "
        + cRMFieldOperator);

    if (cRMFieldName != null) {

      /* Check if cRMFieldName contans crmFieldIntraSeparator */
      if (cRMFieldName.contains(crmFieldIntraSeparator)) {

        String nodeName = null;
        String nodeType = null;

        final int index = cRMFieldName.lastIndexOf(crmFieldIntraSeparator);

        /* If found a positive index */
        if (index > 0) {

          /* Get node name */
          nodeName = cRMFieldName.substring(0, index).trim();

          /* Get node type */
          nodeType = cRMFieldName.substring(index + crmFieldIntraSeparator.length(), cRMFieldName.length()).trim();
        }

        /* Validate node name/type */
        if (nodeName == null & nodeType == null) {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + cRMFieldName + "' is not in correct format");
        }

        try {

          /* If a custom field */
          if (nodeType.toUpperCase().contains("CUSTOM")) {

            /* Invoke the normal field setter method */
            SearchCustomFieldList searchCustomFieldList = taskSearchBasic.getCustomFieldList();

            /* If already a list */
            if (searchCustomFieldList != null) {

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(searchCustomFieldList, nodeName, nodeType, cRMFieldValue,
                  cRMFieldOperator));
            } else {

              /* Create a new list */
              searchCustomFieldList = new SearchCustomFieldList();

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(null, nodeName, nodeType, cRMFieldValue, cRMFieldOperator));

            }

            /* Set custom field list */
            taskSearchBasic.setCustomFieldList(searchCustomFieldList);
          } else {

            /* Invoke the normal field setter method */
            taskSearchBasic
                .getClass()
                .getMethod("set" + nodeName.trim(), Class.forName(NetSuiteMessageFormatUtils.NS_WSDL_PACKAGE_NAME + nodeType.trim()))
                .invoke(taskSearchBasic,
                    this.getNormalFieldForSearchOperation(nodeName.trim(), nodeType.trim(), cRMFieldValue, cRMFieldOperator, inputDateFormat));
          }

        } catch (final ClassNotFoundException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName + " in CRM Object type 'Task'");
        } catch (final NoSuchMethodException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName + " in CRM Object type 'Task'");
        } catch (final IllegalArgumentException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName + " in CRM Object type 'Task'");
        } catch (final IllegalAccessException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName + " in CRM Object type 'Task'");
        } catch (final InvocationTargetException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName + " in CRM Object type 'Task'");
        }
      } else {
        throw new ScribeException(ScribeResponseCodes._1003 + "CRM field format should be CRMFIELDNAME" + crmFieldIntraSeparator + "CRMFIELDTYPE");
      }
    }

    return taskSearchBasic;
  }

  /**
   * 
   * @param query
   * @return
   * @throws Exception
   */
  public final SearchRecord createEmployeeSearch(final String query, final String crmFieldIntraSeparator, final String inputDateFormat)
      throws Exception {

    if (query != null) {

      /* Inform user that 'OR' is not supported */
      if (query.contains(HTTPConstants.orClause)) {
        throw new ScribeException(ScribeResponseCodes._1003 + " Operator: '" + HTTPConstants.orClause + "' is not supported by NS CRM");
      }

      /* Create new employee search */
      final EmployeeSearch employeeSearch = new EmployeeSearch();

      /* Create new employee search basic */
      EmployeeSearchBasic employeeSearchBasic = new EmployeeSearchBasic();

      /* Tokenize the where clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(query, HTTPConstants.andClause, false);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {

        /* Get next token */
        final String tempElement = stringTokenizer.nextToken();
        logger.debug("----Inside createEmployeeSearch: tempElement: " + tempElement);

        /* Split the search string into key operator value */
        final String[] splitSearchStringResult = this.splitSearchString(tempElement);

        /* Update employee search object search */
        employeeSearchBasic =
            this.updateEmployeeSearchBasic(employeeSearchBasic, splitSearchStringResult[0], splitSearchStringResult[2], splitSearchStringResult[1],
                crmFieldIntraSeparator, inputDateFormat);
      }
      /* Search this basis in employee search object */
      employeeSearch.setBasic(employeeSearchBasic);

      return employeeSearch;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param customerSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private final CustomerSearchBasic updateCustomerSearchBasic(final CustomerSearchBasic customerSearchBasic, final String cRMFieldName,
      final String cRMFieldValue, final String cRMFieldOperator, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception {

    logger.debug("----Inside updateCustomerSearchBasic cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + cRMFieldValue + " & cRMFieldOperator: "
        + cRMFieldOperator);

    if (cRMFieldName != null) {

      /* Check if cRMFieldName contans crmFieldIntraSeparator */
      if (cRMFieldName.contains(crmFieldIntraSeparator)) {

        String nodeName = null;
        String nodeType = null;

        /* Split the cRMFieldName on the basis of seperator */
        if (cRMFieldName != null && cRMFieldName.contains(crmFieldIntraSeparator)) {

          final int index = cRMFieldName.lastIndexOf(crmFieldIntraSeparator);

          /* If found a positive index */
          if (index > 0) {

            /* Get node name */
            nodeName = cRMFieldName.substring(0, index).trim();

            /* Get node type */
            nodeType = cRMFieldName.substring(index + crmFieldIntraSeparator.length(), cRMFieldName.length()).trim();
          }

          /* Validate node name/type */
          if (nodeName == null & nodeType == null) {

            /* Throw user error */
            throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + cRMFieldName + "' is not in correct format");
          }
        } else {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + cRMFieldName + "' is not in correct format");
        }

        try {

          /* If a custom field */
          if (nodeType.toUpperCase().contains("CUSTOM")) {

            /* Invoke the normal field setter method */
            SearchCustomFieldList searchCustomFieldList = customerSearchBasic.getCustomFieldList();

            /* If already a list */
            if (searchCustomFieldList != null) {

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(searchCustomFieldList, nodeName, nodeType, cRMFieldValue,
                  cRMFieldOperator));
            } else {

              /* Create a new list */
              searchCustomFieldList = new SearchCustomFieldList();

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(null, nodeName, nodeType, cRMFieldValue, cRMFieldOperator));
            }

            /* Set custom field list */
            customerSearchBasic.setCustomFieldList(searchCustomFieldList);
          } else {

            /* Invoke the normal field setter method */
            customerSearchBasic
                .getClass()
                .getMethod("set" + nodeName.trim(), Class.forName(NetSuiteMessageFormatUtils.NS_WSDL_PACKAGE_NAME + nodeType.trim()))
                .invoke(customerSearchBasic,
                    this.getNormalFieldForSearchOperation(nodeName.trim(), nodeType.trim(), cRMFieldValue, cRMFieldOperator, inputDateFormat));
          }
        } catch (final ClassNotFoundException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Customer'");
        } catch (final NoSuchMethodException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Customer'");
        } catch (final IllegalArgumentException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Customer'");
        } catch (final IllegalAccessException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Customer'");
        } catch (final InvocationTargetException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Customer'");
        }
      } else {
        throw new ScribeException(ScribeResponseCodes._1003 + "CRM field format should be CRMFIELDNAME" + crmFieldIntraSeparator + "CRMFIELDTYPE");
      }
    }

    return customerSearchBasic;
  }

  /**
   * 
   * @param employeeSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private final EmployeeSearchBasic updateEmployeeSearchBasic(final EmployeeSearchBasic employeeSearchBasic, final String cRMFieldName,
      final String cRMFieldValue, final String cRMFieldOperator, final String crmFieldIntraSeparator, final String inputDateFormat) throws Exception {

    logger.debug("----Inside updateEmployeeSearchBasic cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + cRMFieldValue + " & cRMFieldOperator: "
        + cRMFieldOperator);

    if (cRMFieldName != null) {

      /* Check if cRMFieldName contans crmFieldIntraSeparator */
      if (cRMFieldName.contains(crmFieldIntraSeparator)) {

        String nodeName = null;
        String nodeType = null;

        final int index = cRMFieldName.lastIndexOf(crmFieldIntraSeparator);

        /* If found a positive index */
        if (index > 0) {

          /* Get node name */
          nodeName = cRMFieldName.substring(0, index).trim();

          /* Get node type */
          nodeType = cRMFieldName.substring(index + crmFieldIntraSeparator.length(), cRMFieldName.length()).trim();
        }

        /* Validate node name/type */
        if (nodeName == null & nodeType == null) {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + cRMFieldName + "' is not in correct format");
        }

        try {

          /* If a custom field */
          if (nodeType.toUpperCase().contains("CUSTOM")) {

            /* Invoke the normal field setter method */
            SearchCustomFieldList searchCustomFieldList = employeeSearchBasic.getCustomFieldList();

            /* If already a list */
            if (searchCustomFieldList != null) {

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(searchCustomFieldList, nodeName, nodeType, cRMFieldValue,
                  cRMFieldOperator));
            } else {

              /* Create a new list */
              searchCustomFieldList = new SearchCustomFieldList();

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(null, nodeName, nodeType, cRMFieldValue, cRMFieldOperator));
            }

            /* Set custom field list */
            employeeSearchBasic.setCustomFieldList(searchCustomFieldList);
          } else {

            /* Invoke the normal field setter method */
            employeeSearchBasic
                .getClass()
                .getMethod("set" + nodeName.trim(), Class.forName(NetSuiteMessageFormatUtils.NS_WSDL_PACKAGE_NAME + nodeType.trim()))
                .invoke(employeeSearchBasic,
                    this.getNormalFieldForSearchOperation(nodeName.trim(), nodeType.trim(), cRMFieldValue, cRMFieldOperator, inputDateFormat));
          }

        } catch (final ClassNotFoundException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Employee'");
        } catch (final NoSuchMethodException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Employee'");
        } catch (final IllegalArgumentException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Employee'");
        } catch (final IllegalAccessException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Employee'");
        } catch (final InvocationTargetException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Employee'");
        }
      } else {
        throw new ScribeException(ScribeResponseCodes._1003 + "CRM field format should be CRMFIELDNAME" + crmFieldIntraSeparator + "CRMFIELDTYPE");
      }
    }

    return employeeSearchBasic;
  }

  private final Object getNormalFieldForSearchOperation(final String cRMFieldName, final String cRMFieldType, final String cRMFieldValue,
      final String cRMFieldOperator, final String inputDateFormat) {

    logger.debug("----Inside getNormalFieldForSearchOperation cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + cRMFieldValue
        + " & cRMFieldType: " + cRMFieldType);

    /* Create date formatter */
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(inputDateFormat);

    if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_LONG_FIELD)) {

      /* Create new search long field */
      final SearchLongField searchLongField = new SearchLongField();

      final SearchLongFieldOperator operator;
      try {

        /* Create search operator from user supplied operator */
        operator = SearchLongFieldOperator.fromString(cRMFieldOperator);
        searchLongField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for type SearchLongFieldOperator '" + cRMFieldOperator + "'.");
      }

      try {
        /* Set value */
        searchLongField.setSearchValue(Long.valueOf(cRMFieldValue));

      } catch (final NumberFormatException e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'int/long' format");
      }

      return searchLongField;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_STRING_FIELD)) {

      logger.info("----Inside getNormalFieldForSearchOperation SearchStringField, with operator: " + cRMFieldOperator);

      /* Create new search string field */
      final SearchStringField searchStringField = new SearchStringField();

      final SearchStringFieldOperator operator;
      try {
        /* Create search operator from user supplied operator */
        operator = SearchStringFieldOperator.fromString(cRMFieldOperator);
        searchStringField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for type SearchStringFieldOperator '" + cRMFieldOperator + "'.");
      }
      try {
        /* Set value */
        searchStringField.setSearchValue(cRMFieldValue);

      } catch (final NumberFormatException e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'string' format");
      }

      return searchStringField;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_BOOLEAN_FIELD)) {

      /* TODO how to add operator? */
      /* Create new search boolean field */
      final SearchBooleanField searchBooleanField = new SearchBooleanField();

      try {
        /* Set value */
        searchBooleanField.setSearchValue(Boolean.valueOf(cRMFieldValue));
      } catch (final NumberFormatException e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'boolean' format");
      }
      return searchBooleanField;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_DATE_FIELD)) {

      /* Create new date boolean field */
      final SearchDateField searchDateField = new SearchDateField();

      final SearchDateFieldOperator operator;
      try {
        /* Create search operator from user supplied operator */
        operator = SearchDateFieldOperator.fromString(cRMFieldOperator);
        searchDateField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for class SearchDateFieldOperator '" + cRMFieldOperator + "'.");
      }

      /* Create calander object */
      final Calendar calendar = Calendar.getInstance();

      try {
        /* Set time */
        calendar.setTime(simpleDateFormat.parse(cRMFieldValue));

        /* Set value */
        searchDateField.setSearchValue(calendar);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be od format: '"
            + inputDateFormat + "'");
      }

      return searchDateField;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_TEXT_NUMBER_FIELD)) {

      /* Create new text string field */
      final SearchTextNumberField searchTextNumberField = new SearchTextNumberField();

      final SearchTextNumberFieldOperator operator;
      try {
        /* Create search operator from user supplied operator */
        operator = SearchTextNumberFieldOperator.fromString(cRMFieldOperator);
        searchTextNumberField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for type SearchTextNumberField '" + cRMFieldOperator + "'.");
      }

      try {
        /* Set value */
        searchTextNumberField.setSearchValue(cRMFieldValue);

      } catch (final NumberFormatException e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be of format 'text'");
      }

      return searchTextNumberField;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_DOUBLE_FIELD)) {

      /* Create new double string field */
      final SearchDoubleField searchDoubleField = new SearchDoubleField();

      final SearchDoubleFieldOperator operator;
      try {
        /* Create search operator from user supplied operator */
        operator = SearchDoubleFieldOperator.fromString(cRMFieldOperator);
        searchDoubleField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for type SearchDoubleFieldOperator '" + cRMFieldOperator + "'.");
      }

      try {
        /* Set value */
        searchDoubleField.setSearchValue(Double.valueOf(cRMFieldValue));

      } catch (final NumberFormatException e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be of format 'double'");
      }

      return searchDoubleField;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_ENUM_MULTI_SELECT_FIELD)) {

      /* Create new Search Enum Multi Select Field field */
      final SearchEnumMultiSelectField searchEnumMultiSelectField = new SearchEnumMultiSelectField();

      final SearchEnumMultiSelectFieldOperator operator;
      try {
        /* Create search operator from user supplied operator */
        operator = SearchEnumMultiSelectFieldOperator.fromString(cRMFieldOperator);
        searchEnumMultiSelectField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for class SearchEnumMultiSelectField '" + cRMFieldOperator + "'.");
      }

      try {
        /* Set value */
        final String[] value = new String[1];

        value[0] = cRMFieldValue;
        searchEnumMultiSelectField.setSearchValue(value);

      } catch (final NumberFormatException e) {
        throw new ScribeException(ScribeResponseCodes._1003 + " Value of CRM object field '" + cRMFieldName + "' should be of type: 'Select'");
      }

      return searchEnumMultiSelectField;

    } else {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field type: " + cRMFieldType);
    }
  }

  /**
   * 
   * @param contactSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public final Customer createCustomer(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    logger.debug("----Inside createCustomer");

    /* Create new Customer */
    final Customer customer = new Customer();

    /* Create list of element */
    final List<Element> elementList = cADbject.getXmlContent();

    /* An array list custom reference object */
    final List<CustomFieldRef> customFieldRefArrayList = new ArrayList<CustomFieldRef>();

    if (elementList != null) {
      logger.debug("----Inside createCustomer, processing elementList with size " + elementList.size());

      for (final Element element : elementList) {

        /* Check if node name is not null */
        if (element.getNodeName() != null) {

          /* Break the element into name and type */
          final String node = element.getNodeName();

          String nodeName = null;
          String nodeType = null;

          /* Split the name on the basis of seperator */
          if (node != null && node.contains(crmFieldIntraSeparator)) {

            final int index = node.lastIndexOf(crmFieldIntraSeparator);

            /* If found a positive index */
            if (index > 0) {

              /* Get node name */
              nodeName = node.substring(0, index).trim();

              /* Get node type */
              nodeType = node.substring(index + crmFieldIntraSeparator.length(), node.length()).trim();
            }

            /* Validate node name/type */
            if (nodeName == null & nodeType == null) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1008 + " Field: '" + node + "' is not in correct format");
            }

          } else if (node != null && node.trim().equalsIgnoreCase(NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID)) {

            /* Get node name */
            nodeName = node;

            /* Node type */
            nodeType = NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID;

          } else {
            /* Throw user error */
            throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + node + "' is not in correct format");
          }

          logger.debug("----Inside createCustomer, processing node " + nodeName + " of type: " + nodeType);

          try {

            /* If a custom field */
            if (nodeType.toUpperCase().contains("CUSTOM")) {

              /* build a customfield reference */
              final CustomFieldRef customFieldRef = this.getCustomFieldForCreateOperation(nodeName.trim(), nodeType.trim(), element.getTextContent());

              /* Add reference in list */
              customFieldRefArrayList.add(customFieldRef);

              logger.debug("----Inside createCustomer, added custom field " + nodeName);
            } else {

              /* Invoke the normal field setter method */
              customer.getClass().getMethod("set" + nodeName, this.getNSCRMFieldParamType(nodeType))
                  .invoke(customer, this.getNSCRMFieldObject(nodeName, nodeType, element.getTextContent()));
            }

          } catch (final NoSuchMethodException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                + " in CRM Object type 'Customer'");
          } catch (final IllegalArgumentException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                + " in CRM Object type 'Customer'");
          } catch (final IllegalAccessException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                + " in CRM Object type 'Customer'");
          } catch (final InvocationTargetException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                + " in CRM Object type 'Customer'");
          }

        } else {
          logger.debug("----Inside createCustomer, no node name, skipped");
        }
      }

      /* check if any custom field and set it to the task record */
      final CustomFieldRef[] customFieldList = this.getCustomFieldRefArray(customFieldRefArrayList);

      /* Set custom field list */
      if (customFieldList != null) {

        /* Create new custom field list */
        final CustomFieldList newCustomFieldList = new CustomFieldList();
        newCustomFieldList.setCustomField(customFieldList);

        /* Add list in customer */
        customer.setCustomFieldList(newCustomFieldList);
      }
    } else {
      logger.warn("----Inside createCustomer, empty element list.");
      throw new ScribeException(ScribeResponseCodes._1008 + " Request data is not found in request");
    }

    return customer;
  }

  /**
   * 
   * @param contactSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public final Contact createContact(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    logger.debug("----Inside createContact");

    /* Create new contact */
    final Contact contact = new Contact();

    /* Create list of element */
    final List<Element> elementList = cADbject.getXmlContent();

    /* An array list custom reference object */
    final List<CustomFieldRef> customFieldRefArrayList = new ArrayList<CustomFieldRef>();

    if (elementList != null) {
      logger.debug("----Inside createContact, processing elementList with size " + elementList.size());

      for (final Element element : elementList) {

        /* Check if node name is not null */
        if (element.getNodeName() != null) {

          /* Break the element into name and type */
          final String node = element.getNodeName();

          String nodeName = null;
          String nodeType = null;

          /* Split the name on the basis of seperator */
          if (node != null && node.contains(crmFieldIntraSeparator)) {

            final int index = node.lastIndexOf(crmFieldIntraSeparator);

            /* If found a positive index */
            if (index > 0) {

              /* Get node name */
              nodeName = node.substring(0, index).trim();

              /* Get node type */
              nodeType = node.substring(index + crmFieldIntraSeparator.length(), node.length()).trim();
            }

            /* Validate node name/type */
            if (nodeName == null & nodeType == null) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1008 + " Field: '" + node + "' is not in correct format");
            }

          } else if (node != null && node.trim().equalsIgnoreCase(NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID)) {

            /* Get node name */
            nodeName = node;

            /* Node type */
            nodeType = NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID;

          } else {
            /* Throw user error */
            throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + node + "' is not in correct format");
          }

          logger.debug("----Inside createContact, processing node " + nodeName + " of type: " + nodeType);

          try {

            /* If a custom field */
            if (nodeType.toUpperCase().contains("CUSTOM")) {

              /* build a customfield reference */
              final CustomFieldRef customFieldRef = this.getCustomFieldForCreateOperation(nodeName.trim(), nodeType.trim(), element.getTextContent());

              /* Add reference in list */
              customFieldRefArrayList.add(customFieldRef);

              logger.debug("----Inside createContact, added custom field " + nodeName);
            } else {

              /* Invoke the normal field setter method */
              contact.getClass().getMethod("set" + nodeName, this.getNSCRMFieldParamType(nodeType))
                  .invoke(contact, this.getNSCRMFieldObject(nodeName, nodeType, element.getTextContent()));
            }

          } catch (final NoSuchMethodException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName + " in CRM Object type 'Contact'");
          } catch (final IllegalArgumentException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName + " in CRM Object type 'Contact'");
          } catch (final IllegalAccessException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName + " in CRM Object type 'Contact'");
          } catch (final InvocationTargetException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName + " in CRM Object type 'Contact'");
          }

        } else {
          logger.debug("----Inside createCustomer, no node name, skipped");
        }

        /* check if any custom field and set it to the task record */
        final CustomFieldRef[] customFieldList = this.getCustomFieldRefArray(customFieldRefArrayList);

        /* Set custom field list */
        if (customFieldList != null) {

          /* Create new custom field list */
          final CustomFieldList newCustomFieldList = new CustomFieldList();
          newCustomFieldList.setCustomField(customFieldList);

          /* Add list in customer */
          contact.setCustomFieldList(newCustomFieldList);
        }
      }
    } else {
      logger.warn("----Inside createCustomer, empty element list.");
      throw new ScribeException(ScribeResponseCodes._1008 + " Request data is not found in request");
    }

    return contact;
  }

  /**
   * 
   * @param contactSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public final Opportunity createOpportunity(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    logger.debug("----Inside createOpportunity");

    /* Create new opportunity */
    final Opportunity opportunity = new Opportunity();

    /* Create list of element */
    final List<Element> elementList = cADbject.getXmlContent();

    /* An array list custom reference object */
    final List<CustomFieldRef> customFieldRefArrayList = new ArrayList<CustomFieldRef>();

    if (elementList != null) {
      logger.debug("----Inside createOpportunity, processing elementList with size " + elementList.size());

      for (final Element element : elementList) {

        /* Check if node name is not null */
        if (element.getNodeName() != null) {

          /* Break the element into name and type */
          final String node = element.getNodeName();

          String nodeName = null;
          String nodeType = null;

          /* Split the name on the basis of seperator */
          if (node != null && node.contains(crmFieldIntraSeparator)) {

            final int index = node.lastIndexOf(crmFieldIntraSeparator);

            /* If found a positive index */
            if (index > 0) {

              /* Get node name */
              nodeName = node.substring(0, index).trim();

              /* Get node type */
              nodeType = node.substring(index + crmFieldIntraSeparator.length(), node.length()).trim();
            }

            /* Validate node name/type */
            if (nodeName == null & nodeType == null) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + node + "' is not in correct format");
            }

          } else if (node != null && node.trim().equalsIgnoreCase(NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID)) {

            /* Get node name */
            nodeName = node;

            /* Node type */
            nodeType = NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID;

          } else {
            /* Throw user error */
            throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + node + "' is not in correct format");
          }

          logger.debug("----Inside createOpportunity, processing node " + nodeName + " of type: " + nodeType);

          try {

            /* If a custom field */
            if (nodeType.toUpperCase().contains("CUSTOM")) {

              /* build a customfield reference */
              final CustomFieldRef customFieldRef = this.getCustomFieldForCreateOperation(nodeName.trim(), nodeType.trim(), element.getTextContent());

              /* Add reference in list */
              customFieldRefArrayList.add(customFieldRef);

              logger.debug("----Inside createOpportunity, added custom field " + nodeName);

            } else {

              /* Invoke the normal field setter method */
              opportunity.getClass().getMethod("set" + nodeName, this.getNSCRMFieldParamType(nodeType))
                  .invoke(opportunity, this.getNSCRMFieldObject(nodeName, nodeType, element.getTextContent()));
            }

          } catch (final NoSuchMethodException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                + " in CRM Object type 'Opportunity'");
          } catch (final IllegalArgumentException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                + " in CRM Object type 'Opportunity'");
          } catch (final IllegalAccessException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                + " in CRM Object type 'Opportunity'");
          } catch (final InvocationTargetException e) {
            throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                + " in CRM Object type 'Opportunity'");
          }

        } else {
          logger.debug("----Inside createCustomer, no node name, skipped");
        }
      }

      /* check if any custom field and set it to the task record */
      final CustomFieldRef[] customFieldList = this.getCustomFieldRefArray(customFieldRefArrayList);

      /* Set custom field list */
      if (customFieldList != null) {

        /* Create new custom field list */
        final CustomFieldList newCustomFieldList = new CustomFieldList();
        newCustomFieldList.setCustomField(customFieldList);

        /* Add list in opportunity */
        opportunity.setCustomFieldList(newCustomFieldList);
      }
    } else {
      logger.warn("----Inside createCustomer, empty element list.");
      throw new ScribeException(ScribeResponseCodes._1008 + " Request data is not found in request");
    }

    return opportunity;
  }

  /**
   * 
   * @param contactSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws
   * @throws Exception
   */
  public final Task createTask(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    logger.debug("----Inside createTask");

    /* Create new Task */
    final Task task = new Task();

    /* Create list of element */
    final List<Element> elementList = cADbject.getXmlContent();

    /* An array list custom reference object */
    final List<CustomFieldRef> customFieldRefArrayList = new ArrayList<CustomFieldRef>();

    if (elementList != null) {
      logger.debug("----Inside createTask, processing elementList with size " + elementList.size());

      for (final Element element : elementList) {

        /* Check if node name is not null */
        if (element.getNodeName() != null) {

          /* Break the element into name and type */
          final String node = element.getNodeName();

          String nodeName = null;
          String nodeType = null;

          /* Split the name on the basis of seperator */
          if (node != null && node.contains(crmFieldIntraSeparator)) {

            final int index = node.lastIndexOf(crmFieldIntraSeparator);

            /* If found a positive index */
            if (index > 0) {

              /* Get node name */
              nodeName = node.substring(0, index).trim();

              /* Get node type */
              nodeType = node.substring(index + crmFieldIntraSeparator.length(), node.length()).trim();
            }

            /* Validate node name/type */
            if (nodeName == null & nodeType == null) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + node + "' is not in correct format");
            }

          } else if (node != null && node.trim().equalsIgnoreCase(NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID)) {

            /* Get node name */
            nodeName = node;

            /* Node type */
            nodeType = NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID;

          } else {
            /* Throw user error */
            throw new ScribeException(ScribeResponseCodes._1008 + " Field: '" + node + "' is not in correct format");
          }

          logger.debug("----Inside createTask, processing node " + nodeName + " of type: " + nodeType);

          /* Check if regarding object id tag is found */
          if (nodeName.trim().equalsIgnoreCase(NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID)) {

            if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null) {
              logger.debug("----Inside createTask, node regardingobjectid is found with TYPE: "
                  + element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE));
            } else {
              logger.debug("----Inside createTask, regardingobjectid is found but no TYPE");
            }

            /* Check if its contact */
            if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null
                && element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE).trim().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

              final RecordRef contactRef = new RecordRef();
              logger.debug("----Inside createTask, contact is being processed");

              /* Check if contact has an internal id */
              if (element.getTextContent() != null) {
                logger.debug("----Inside createTask, regardingobjectid is " + element.getTextContent().trim());

                /* Set internal id of referenced contact */
                contactRef.setInternalId(element.getTextContent().trim());
              } else {
                logger.debug("----Inside createTask, regardingobjectid doesn't carry id");
              }

              /* Set this contact info in task */
              task.setContact(contactRef);

            } else if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null
                && element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE).trim().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

              final RecordRef companyRef = new RecordRef();
              logger.debug("----Inside createTask, customer is being processed");

              /* Check if contact has an internal id */
              if (element.getTextContent() != null) {
                logger.debug("----Inside createTask, regardingobjectid is " + element.getTextContent().trim());
                /* Set internal id of referenced contact */
                companyRef.setInternalId(element.getTextContent().trim());
              } else {
                logger.debug("----Inside createTask, regardingobjectid doesn't carry id");
              }

              /* Set company information in task */
              task.setCompany(companyRef);
            }
            /* Check if object is support case */
            else if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null
                && element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE).trim().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

              final RecordRef supportRef = new RecordRef();
              logger.debug("----Inside createTask, support case is being processed");

              /* Check if contact has an internal id */
              if (element.getTextContent() != null) {
                logger.debug("----Inside createTask, regardingobjectid is " + element.getTextContent().trim());
                /* Set internal id of referenced support case */
                supportRef.setInternalId(element.getTextContent().trim());
              } else {
                logger.debug("----Inside createTask, regardingobjectid doesn't carry id");
              }

              /* Set support case information in task */
              task.setSupportCase(supportRef);
            }
            /* Check if object is opportunity */
            else if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null
                && element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE).trim().equalsIgnoreCase(NSCRMObjectType.Opportunity.toString())) {

              final RecordRef opportunityRef = new RecordRef();
              logger.debug("----Inside createTask, opportunity is being processed");

              /* Check if opportunity has an internal id */
              if (element.getTextContent() != null) {
                logger.debug("----Inside createTask, regardingobjectid is " + element.getTextContent().trim());

                /* Set internal id of referenced support case */
                opportunityRef.setInternalId(element.getTextContent().trim());
              } else {
                logger.debug("----Inside createTask, regardingobjectid doesn't carry id");
              }

              /* Set transaction information in task */
              task.setTransaction(opportunityRef);
            } else {
              throw new ScribeException(ScribeResponseCodes._1008 + "Following 'TYPE' is not supported in 'REGARDINGOBJECTID' node. Please provide ."
                  + NSCRMObjectType.Contact + "/" + NSCRMObjectType.SupportCase + "/" + NSCRMObjectType.Customer + "/" + NSCRMObjectType.SupportCase
                  + " only");
            }
          } else {
            try {

              /* If a custom field */
              if (nodeType.toUpperCase().contains("CUSTOM")) {

                /* build a customfield reference */
                final CustomFieldRef customFieldRef =
                    this.getCustomFieldForCreateOperation(nodeName.trim(), nodeType.trim(), element.getTextContent());

                /* Add reference in list */
                customFieldRefArrayList.add(customFieldRef);
                logger.debug("----Inside createTask, added custom field " + nodeName);
              } else {

                /* Invoke the normal field setter method */
                task.getClass().getMethod("set" + nodeName, this.getNSCRMFieldParamType(nodeType))
                    .invoke(task, this.getNSCRMFieldObject(nodeName, nodeType, element.getTextContent()));
              }

            } catch (final NoSuchMethodException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName + " in CRM Object type 'Task'");
            } catch (final IllegalArgumentException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName + " in CRM Object type 'Task'");
            } catch (final IllegalAccessException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName + " in CRM Object type 'Task'");
            } catch (final InvocationTargetException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName + " in CRM Object type 'Task'");
            }
          }

        } else {
          logger.debug("----Inside createTask, no node name, skipped");
        }
      }

      /* Dont set contact/case if company is not found */
      if (task.getCompany() == null) {
        logger.debug("----Inside createTask, company is not found, hence removing contact/support case");

        /* Contact without company dont work at NS */
        task.setContact(null);

        /* Support case without company dont work at NS */
        task.setSupportCase(null);
      } else {
        final RecordRef companyRef = task.getCompany();

        /* Check if internal id is found */
        if (companyRef.getInternalId() != null && companyRef.getInternalId().trim().equalsIgnoreCase("")) {
          logger.debug("----Inside createTask, internal id of company is not found, hence removing contact/support case");

          /* Contact without company dont work at NS */
          task.setContact(null);

          /* Support case without company dont work at NS */
          task.setSupportCase(null);

          /* Support case without company dont work at NS */
          task.setCompany(null);
        }
      }

      /* check if any custom field and set it to the task record */
      final CustomFieldRef[] customFieldList = this.getCustomFieldRefArray(customFieldRefArrayList);

      /* Set custom field list */
      if (customFieldList != null) {

        /* Create new custom field list */
        final CustomFieldList newCustomFieldList = new CustomFieldList();
        newCustomFieldList.setCustomField(customFieldList);

        /* Add list in task */
        task.setCustomFieldList(newCustomFieldList);
      }

    } else {
      logger.warn("----Inside createTask, empty element list.");
      throw new ScribeException(ScribeResponseCodes._1008 + " Request data is not found in request");
    }

    return task;
  }

  @SuppressWarnings("unused")
  private final Object getNSCRMFieldObject(final String cRMFieldName, final String cRMFieldType, final String cRMFieldValue) {

    logger.debug("----Inside getNSCRMFieldObject cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + cRMFieldValue + " & cRMFieldType: "
        + cRMFieldType);

    /* Trim the value */
    if (cRMFieldValue != null) {

      if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_LONG)) {

        try {
          /* Set value */
          return Long.valueOf(cRMFieldValue.trim());

        } catch (final Exception e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be 'int/long'");
        }
      } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_STRING)) {

        try {
          /* Set value */
          return String.valueOf(cRMFieldValue.trim());

        } catch (final Exception e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be 'string'");
        }

      } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_BOOLEAN)) {

        try {
          /* Set value */
          return Boolean.valueOf(cRMFieldValue.trim());

        } catch (final Exception e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be 'boolean'");
        }

      } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_CALANDER)) {

        /* Create calander object */
        final Calendar calendar = Calendar.getInstance();

        try {
          /* Set time */
          calendar.setTimeInMillis(Long.parseLong(cRMFieldValue.trim()));

          return calendar;
        } catch (final Exception e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'long/int' format");
        }

      } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_DURATION)) {

        try {
          /* Create duration object */
          final Duration duration = new Duration();

          duration.setUnit(DurationUnit.hour);

          /* Set value */
          duration.setTimeSpan(Double.valueOf(cRMFieldValue.trim()));

          return duration;

        } catch (final Exception e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'double' format");
        }

      } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_DOUBLE)) {

        try {
          /* Set value */
          return Double.valueOf(cRMFieldValue.trim());

        } catch (final NumberFormatException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'double' format");
        }

      } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_RECORD_REF)) {

        final RecordRef recordRef = new RecordRef();

        /* Check if record has an internal id */
        if (cRMFieldValue != null) {

          /* Set internal id of referenced contact */
          recordRef.setInternalId(cRMFieldValue.trim().trim());
        } else {
          logger.debug("----Inside createTask, regardingobjectid doesn't carry id");
        }

        return recordRef;

      } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_TASK_STATUS)) {

        /* Check if record has an internal id */
        if (cRMFieldValue != null && cRMFieldValue.equalsIgnoreCase("COMPLETED")) {

          /* Set completed */
          return TaskStatus._completed;
        } else {
          /* Set inprogress */
          return TaskStatus._inProgress;
        }

      } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_PHONE_CALL_STATUS)) {

        /* Check if record has an internal id */
        if (cRMFieldValue != null && cRMFieldValue.equalsIgnoreCase("COMPLETED")) {

          /* Set completed */
          return PhoneCallStatus._completed;
        } else {

          /* Set inprogress */
          return PhoneCallStatus._scheduled;
        }

      } else {

        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field type: " + cRMFieldType);
      }
    } else {
      return null;
    }
  }

  private final Class<?> getNSCRMFieldParamType(final String cRMFieldType) {

    logger.debug("----Inside getNSCRMFieldParamType cRMFieldType: " + cRMFieldType);

    if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_LONG)) {

      return Long.class;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_STRING)) {

      return String.class;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_BOOLEAN)) {

      return Boolean.class;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_CALANDER)) {

      return Calendar.class;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_DURATION)) {

      return Duration.class;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_DOUBLE)) {

      return Double.class;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_RECORD_REF)) {

      return RecordRef.class;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_TASK_STATUS)) {

      return TaskStatus.class;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_PHONE_CALL_STATUS)) {

      return PhoneCallStatus.class;

    } else {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field type: " + cRMFieldType);
    }
  }

  /**
   * 
   * @param cRMFieldName
   * @param cRMFieldType
   * @param cRMFieldValue
   * @return
   */
  private final CustomFieldRef getCustomFieldForCreateOperation(final String cRMFieldName, final String cRMFieldType, final String cRMFieldValue) {

    logger.debug("----Inside getCustomFieldForCreateOperation cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + cRMFieldValue
        + " & cRMFieldType: " + cRMFieldType);

    if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_STRING_CUSTOM_FIELD_REF)) {

      /* Create new custom string field */
      final StringCustomFieldRef stringCustomFieldRef = new StringCustomFieldRef();

      /* Set internal id */
      stringCustomFieldRef.setInternalId(cRMFieldName);

      if (cRMFieldValue != null) {

        /* Set value */
        stringCustomFieldRef.setValue(cRMFieldValue.trim());
      }

      return stringCustomFieldRef;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_MUTISELECT_CUSTOM_FIELD_REF)) {

      /* Create new multi select field reference */
      final MultiSelectCustomFieldRef multiSelectCustomFieldRef = new MultiSelectCustomFieldRef();

      multiSelectCustomFieldRef.setInternalId(cRMFieldName);

      if (cRMFieldValue != null) {
        final ListOrRecordRef[] list = new ListOrRecordRef[1];
        list[0] = new ListOrRecordRef();
        list[0].setInternalId(cRMFieldValue.trim());
        list[0].setTypeId(cRMFieldName);
        multiSelectCustomFieldRef.setValue(list);
      } else {
        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1003 + "Field value is is invalid: " + cRMFieldType);
      }
      return multiSelectCustomFieldRef;

    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_BOOLEAN_CUSTOM_FIELD_REF)) {

      /* Create new boolean field reference */
      final BooleanCustomFieldRef booleanCustomFieldRef = new BooleanCustomFieldRef();

      /* Set internal id */
      booleanCustomFieldRef.setInternalId(cRMFieldName);

      if (cRMFieldValue != null) {

        /* Set value */
        booleanCustomFieldRef.setValue(Boolean.valueOf((cRMFieldValue.trim())));
      } else {
        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'boolean' format");
      }
      return booleanCustomFieldRef;
    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_LONG_CUSTOM_FIELD_REF)) {

      /* Create new long field reference */
      final LongCustomFieldRef longCustomFieldRef = new LongCustomFieldRef();

      /* Set internal id */
      longCustomFieldRef.setInternalId(cRMFieldName);

      if (cRMFieldValue != null) {

        /* Set value */
        longCustomFieldRef.setValue(Long.valueOf((cRMFieldValue.trim())));
      } else {
        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'long/int' format");
      }
      return longCustomFieldRef;
    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_DOUBLE_CUSTOM_FIELD_REF)) {

      /* Create new double field reference */
      final DoubleCustomFieldRef doubleCustomFieldRef = new DoubleCustomFieldRef();

      /* Set internal id */
      doubleCustomFieldRef.setInternalId(cRMFieldName);

      if (cRMFieldValue != null) {

        /* Set value */
        doubleCustomFieldRef.setValue(Double.valueOf((cRMFieldValue.trim())));
      } else {
        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'double/float' format");
      }
      return doubleCustomFieldRef;
    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.CREATE_DATE_CUSTOM_FIELD_REF)) {

      /* Create new date field reference */
      final DateCustomFieldRef dateCustomFieldRef = new DateCustomFieldRef();

      /* Set internal id */
      dateCustomFieldRef.setInternalId(cRMFieldName);

      /* Create calander object */
      final Calendar calendar = Calendar.getInstance();

      try {
        /* Set time */
        calendar.setTimeInMillis(Long.parseLong(cRMFieldValue.trim()));

        /* Set value */
        dateCustomFieldRef.setValue(calendar);

      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'long/int' format");
      }
      return dateCustomFieldRef;
    } else {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field type: " + cRMFieldType);
    }
  }

  /**
   * 
   * @param cRMFieldName
   * @param cRMFieldType
   * @param cRMFieldValue
   * @param cRMFieldOperator
   * @return
   */
  private final SearchCustomField getCustomCRMSearchObject(final String cRMFieldName, final String cRMFieldType, final String cRMFieldValue,
      final String cRMFieldOperator) {

    logger.debug("----Inside getCustomCRMSearchObject cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + cRMFieldValue + " & cRMFieldType: "
        + cRMFieldType);

    /* If field is string type */
    if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_STRING_CUSTOM_FIELD)) {

      /* Create new custom string field */
      final SearchStringCustomField searchStringCustomField = new SearchStringCustomField();

      /* Set internal id */
      searchStringCustomField.setInternalId(cRMFieldName);

      final SearchStringFieldOperator operator;
      try {

        /* Create search operator from user supplied value */
        operator = SearchStringFieldOperator.fromString(cRMFieldOperator);
        searchStringCustomField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for type SearchStringCustomField '" + cRMFieldOperator + "'.");
      }
      if (cRMFieldValue != null) {

        /* Set value */
        searchStringCustomField.setSearchValue(cRMFieldValue);
      }

      return searchStringCustomField;

    } else
    /* Check if type of custom field is long */
    if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_LONG_CUSTOM_FIELD)) {

      /* Create new custom string field */
      final SearchLongCustomField searchLongCustomField = new SearchLongCustomField();

      /* Set internal id */
      searchLongCustomField.setInternalId(cRMFieldName);

      final SearchLongFieldOperator operator;
      try {

        /* Create search operator from user supplied value */
        operator = SearchLongFieldOperator.fromString(cRMFieldOperator);
        searchLongCustomField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for type SearchLongCustomFields '" + cRMFieldOperator + "'.");
      }

      if (cRMFieldValue != null) {

        try {
          /* Set value */
          searchLongCustomField.setSearchValue(Long.valueOf(cRMFieldValue));

        } catch (final NumberFormatException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in int/long format");
        }
      }

      return searchLongCustomField;
    } else
    /* Check if type of custom field is date */
    if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_DATE_CUSTOM_FIELD)) {

      /* Create new custom date field */
      final SearchDateCustomField searchDateCustomField = new SearchDateCustomField();

      /* Set internal id */
      searchDateCustomField.setInternalId(cRMFieldName);

      final SearchDateFieldOperator operator;
      try {

        /* Create search operator from user supplied value */
        operator = SearchDateFieldOperator.fromString(cRMFieldOperator);
        searchDateCustomField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for type SearchDateCustomField '" + cRMFieldOperator + "'.");
      }

      if (cRMFieldValue != null) {

        /* Create calander object */
        final Calendar calendar = Calendar.getInstance();

        try {
          /* Set time */
          calendar.setTimeInMillis(Long.parseLong(cRMFieldValue.trim()));

          /* Set value */
          searchDateCustomField.setSearchValue(calendar);

        } catch (final Exception e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'long/int' format");
        }
      }

      return searchDateCustomField;
    } else
    /* Check if type of custom field is boolean */
    if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_BOOLEAN_CUSTOM_FIELD)) {

      /* Create new custom boolean field */
      final SearchBooleanCustomField searchBooleanCustomField = new SearchBooleanCustomField();

      /* TODO how to set operator? */
      /* Set internal id */
      searchBooleanCustomField.setInternalId(cRMFieldName);

      if (cRMFieldValue != null) {

        try {
          /* Set value */
          searchBooleanCustomField.setSearchValue(Boolean.valueOf(cRMFieldValue));

        } catch (final Exception e) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Value of CRM object field '" + cRMFieldName + "' should be in 'boolean' format");
        }
      }

      return searchBooleanCustomField;
    } else if (cRMFieldType.equalsIgnoreCase(NSCRMFieldTypes.SEARCH_MULTI_SELECT_CUSTOM_FIELD)) {

      logger.info("----Inside getCustomCRMSearchObject, found multiselect value: " + cRMFieldValue);
      /* Create new Search Enum Multi Select Field field */
      final SearchMultiSelectCustomField searchMultiSelectCustomField = new SearchMultiSelectCustomField();

      final SearchMultiSelectFieldOperator operator;
      try {

        /* Create search operator from user supplied value */
        operator = SearchMultiSelectFieldOperator.fromString(cRMFieldOperator);
        searchMultiSelectCustomField.setOperator(operator);
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid operator for class SearchMultiSelectCustomField '" + cRMFieldOperator + "'.");
      }

      /* Set value */
      final SearchCustomFieldList searchCustomFieldList = new SearchCustomFieldList();

      /* Split the value */
      final String[] splitIdValue = this.splitCustomMutliSelectValue(cRMFieldValue);

      final ListOrRecordRef listOrRecordRef = new ListOrRecordRef();

      /* Set internal id */
      listOrRecordRef.setInternalId(splitIdValue[1]);

      /* Set type id */
      listOrRecordRef.setTypeId(splitIdValue[0]);

      /* Set internal id of the custom field name */
      searchMultiSelectCustomField.setInternalId(cRMFieldName);
      searchCustomFieldList.setCustomField(new SearchCustomField[] {searchMultiSelectCustomField});
      searchMultiSelectCustomField.setSearchValue(new ListOrRecordRef[] {listOrRecordRef});
      logger.info("----Inside getCustomCRMSearchObject, returning multi-select custom field reference. " + cRMFieldName);
      return searchMultiSelectCustomField;

    } else {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1003 + "Following type: " + cRMFieldType + " is not supported");
    }
  }

  /**
   * 
   * @param contactSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public final SupportCase createSupportCase(final ScribeObject cADbject, final String crmFieldIntraSeparator) {

    logger.debug("----Inside createSupportCase");

    /* Create new support case */
    final SupportCase supportCase = new SupportCase();

    /* Create list of element */
    final List<Element> elementList = cADbject.getXmlContent();

    /* An array list custom reference object */
    final List<CustomFieldRef> customFieldRefArrayList = new ArrayList<CustomFieldRef>();

    if (elementList != null) {
      logger.debug("----Inside createSupportCase, processing elementList with size " + elementList.size());

      for (final Element element : elementList) {

        /* Check if node name is not null */
        if (element.getNodeName() != null) {

          /* Break the element into name and type */
          final String node = element.getNodeName();

          String nodeName = null;
          String nodeType = null;

          /* Split the name on the basis of seperator */
          if (node != null && node.contains(crmFieldIntraSeparator)) {

            final int index = node.lastIndexOf(crmFieldIntraSeparator);

            /* If found a positive index */
            if (index > 0) {

              /* Get node name */
              nodeName = node.substring(0, index).trim();

              /* Get node type */
              nodeType = node.substring(index + crmFieldIntraSeparator.length(), node.length()).trim();
            }

            /* Validate node name/type */
            if (nodeName == null & nodeType == null) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1008 + " Field: '" + node + "' is not in correct format");
            }

          } else if (node != null && node.trim().equalsIgnoreCase(NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID)) {

            /* Get node name */
            nodeName = node;

            /* Node type */
            nodeType = NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID;

          } else {
            /* Throw user error */
            throw new ScribeException(ScribeResponseCodes._1008 + " Field: '" + node + "' is not in correct format");
          }

          logger.debug("----Inside createSupportCase, processing node " + nodeName + " of type: " + nodeType);

          /* Check if regarding object id tag is found */
          if (nodeName.trim().equalsIgnoreCase(NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID)) {

            if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null) {
              logger.debug("----Inside createSupportCase, node regardingobjectid is found with TYPE: "
                  + element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE));
            } else {
              logger.debug("----Inside createSupportCase, regardingobjectid is found but no TYPE");
            }

            /* Check if its contact */
            if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null
                && element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE).trim().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

              final RecordRef contactRef = new RecordRef();
              logger.debug("----Inside createSupportCase, contact is being processed");

              /* Check if contact has an internal id */
              if (element.getTextContent() != null) {
                logger.debug("----Inside createSupportCase, regardingobjectid is " + element.getTextContent().trim());

                /* Set internal id of referenced contact */
                contactRef.setInternalId(element.getTextContent().trim());
              } else {
                logger.debug("----Inside createSupportCase, regardingobjectid doesn't carry id");
              }

              /* Set this contact info in supportCase */
              supportCase.setContact(contactRef);

            } else if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null
                && element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE).trim().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

              final RecordRef companyRef = new RecordRef();
              logger.debug("----Inside createSupportCase, customer is being processed");

              /* Check if contact has an internal id */
              if (element.getTextContent() != null) {
                logger.debug("----Inside createSupportCase, regardingobjectid is " + element.getTextContent().trim());
                /* Set internal id of referenced contact */
                companyRef.setInternalId(element.getTextContent().trim());
              } else {
                logger.debug("----Inside createSupportCase, regardingobjectid doesn't carry id");
              }

              /* Set company information in supportCase */
              supportCase.setCompany(companyRef);

            } else {
              throw new ScribeException(ScribeResponseCodes._1008 + "Following 'TYPE' is not supported in 'REGARDINGOBJECTID' node. Please provide ."
                  + NSCRMObjectType.Contact + "/" + NSCRMObjectType.SupportCase + "/" + NSCRMObjectType.Customer + " only");
            }
          } else {
            try {

              /* If a custom field */
              if (nodeType.toUpperCase().contains("CUSTOM")) {

                /* build a customfield reference */
                final CustomFieldRef customFieldRef =
                    this.getCustomFieldForCreateOperation(nodeName.trim(), nodeType.trim(), element.getTextContent());

                /* Add reference in list */
                customFieldRefArrayList.add(customFieldRef);
                logger.debug("----Inside createSupportCase, added custom field " + nodeName);
              } else {

                /* Invoke the normal field setter method */
                supportCase.getClass().getMethod("set" + nodeName, this.getNSCRMFieldParamType(nodeType))
                    .invoke(supportCase, this.getNSCRMFieldObject(nodeName, nodeType, element.getTextContent()));
              }

            } catch (final NoSuchMethodException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                  + " in CRM Object type 'SupportCase'");
            } catch (final IllegalArgumentException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                  + " in CRM Object type 'SupportCase'");
            } catch (final IllegalAccessException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                  + " in CRM Object type 'SupportCase'");
            } catch (final InvocationTargetException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                  + " in CRM Object type 'SupportCase'");
            }
          }

        } else {
          logger.debug("----Inside createSupportCase, no node name, skipped");
        }
      }

      /* Don't set contact if company is not found */
      if (supportCase.getCompany() == null) {

        logger.debug("----Inside createSupportCase, company is not found, hence removing contact");

        /* Contact without company dont work at NS */
        supportCase.setContact(null);
      } else {
        final RecordRef companyRef = supportCase.getCompany();

        /* Check if internal id is found */
        if (companyRef.getInternalId() != null && companyRef.getInternalId().trim().equalsIgnoreCase("")) {
          logger.debug("----Inside createSupportCase, internal id of company is not found, hence removing contact");

          /* Contact without company dont work at NS */
          supportCase.setContact(null);

          /* Contact without company dont work at NS */
          supportCase.setCompany(null);
        }
      }

      /* check if any custom field and set it to the task record */
      final CustomFieldRef[] customFieldList = this.getCustomFieldRefArray(customFieldRefArrayList);

      /* Set custom field list */
      if (customFieldList != null) {

        /* Create new custom field list */
        final CustomFieldList newCustomFieldList = new CustomFieldList();
        newCustomFieldList.setCustomField(customFieldList);

        /* Add list in task */
        supportCase.setCustomFieldList(newCustomFieldList);
      }

    } else {
      logger.warn("----Inside createSupportCase, empty element list.");
      throw new ScribeException(ScribeResponseCodes._1008 + " Request data is not found in request");
    }

    return supportCase;
  }

  /**
   * 
   * @param contactSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public final PhoneCall createPhoneCall(final ScribeObject cADbject, final String crmFieldIntraSeparator) {

    logger.debug("----Inside createPhoneCall");

    /* Create new Phone Call */
    final PhoneCall phoneCall = new PhoneCall();

    /* Create list of element */
    final List<Element> elementList = cADbject.getXmlContent();

    /* An array list custom reference object */
    final List<CustomFieldRef> customFieldRefArrayList = new ArrayList<CustomFieldRef>();

    if (elementList != null) {
      logger.debug("----Inside createPhoneCall, processing elementList with size " + elementList.size());

      for (final Element element : elementList) {

        /* Check if node name is not null */
        if (element.getNodeName() != null) {

          /* Break the element into name and type */
          final String node = element.getNodeName();

          String nodeName = null;
          String nodeType = null;

          /* Split the name on the basis of seperator */
          if (node != null && node.contains(crmFieldIntraSeparator)) {

            final int index = node.lastIndexOf(crmFieldIntraSeparator);

            /* If found a positive index */
            if (index > 0) {

              /* Get node name */
              nodeName = node.substring(0, index).trim();

              /* Get node type */
              nodeType = node.substring(index + crmFieldIntraSeparator.length(), node.length()).trim();
            }

            /* Validate node name/type */
            if (nodeName == null & nodeType == null) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1008 + " Field: '" + node + "' is not in correct format");
            }

          } else if (node != null && node.trim().equalsIgnoreCase(NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID)) {

            /* Get node name */
            nodeName = node;

            /* Node type */
            nodeType = NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID;

          } else {
            /* Throw user error */
            throw new ScribeException(ScribeResponseCodes._1008 + " Field: '" + node + "' is not in correct format");
          }

          logger.debug("----Inside createPhoneCall, processing node " + nodeName + " of type: " + nodeType);

          /* Check if regarding object id tag is found */
          if (nodeName.trim().equalsIgnoreCase(NSCRMFieldTypes.CRM_FIELD_REGARDINGOBJECTID)) {

            if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null) {
              logger.debug("----Inside createPhoneCall, node regardingobjectid is found with TYPE: "
                  + element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE));
            } else {
              logger.debug("----Inside createPhoneCall, regardingobjectid is found but no TYPE");
            }

            /* Check if its contact */
            if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null
                && element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE).trim().equalsIgnoreCase(NSCRMObjectType.Contact.toString())) {

              final RecordRef contactRef = new RecordRef();
              logger.debug("----Inside createPhoneCall, contact is being processed");

              /* Check if contact has an internal id */
              if (element.getTextContent() != null) {
                logger.debug("----Inside createPhoneCall, regardingobjectid is " + element.getTextContent().trim());
                /* Set internal id of referenced contact */
                contactRef.setInternalId(element.getTextContent().trim());
              } else {
                logger.debug("----Inside createPhoneCall, regardingobjectid doesn't carry id");
              }

              /* Set this contact info in phoneCall */
              phoneCall.setContact(contactRef);

            } else if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null
                && element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE).trim().equalsIgnoreCase(NSCRMObjectType.Customer.toString())) {

              final RecordRef companyRef = new RecordRef();
              logger.debug("----Inside createPhoneCall, customer is being processed");

              /* Check if contact has an internal id */
              if (element.getTextContent() != null) {
                logger.debug("----Inside createPhoneCall, regardingobjectid is " + element.getTextContent().trim());
                /* Set internal id of referenced contact */
                companyRef.setInternalId(element.getTextContent().trim());
              } else {
                logger.debug("----Inside createPhoneCall, regardingobjectid doesn't carry id");
              }

              /* Set company information in task */
              phoneCall.setCompany(companyRef);

            }
            /* Check if object is support case */
            else if (element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE) != null
                && element.getAttribute(NSCRMFieldTypes.CRM_FIELD_ATTRIBUTE_TYPE).trim().equalsIgnoreCase(NSCRMObjectType.SupportCase.toString())) {

              final RecordRef supportRef = new RecordRef();
              logger.debug("----Inside createPhoneCall, support case is being processed");

              /* Check if contact has an internal id */
              if (element.getTextContent() != null) {
                logger.debug("----Inside createPhoneCall, regardingobjectid is " + element.getTextContent().trim());
                /* Set internal id of referenced support case */
                supportRef.setInternalId(element.getTextContent().trim());
              } else {
                logger.debug("----Inside createPhoneCall, regardingobjectid doesn't carry id");
              }

              /* Set support case information in phoneCall */
              phoneCall.setSupportCase(supportRef);
            } else {
              throw new ScribeException(ScribeResponseCodes._1008 + "Following 'TYPE' is not supported in 'REGARDINGOBJECTID' node. Please provide ."
                  + NSCRMObjectType.Contact + "/" + NSCRMObjectType.SupportCase + "/" + NSCRMObjectType.Customer + " only");
            }
          } else {
            try {

              /* If a custom field */
              if (nodeType.toUpperCase().contains("CUSTOM")) {

                /* build a custom field reference */
                final CustomFieldRef customFieldRef =
                    this.getCustomFieldForCreateOperation(nodeName.trim(), nodeType.trim(), element.getTextContent());

                /* Add reference in list */
                customFieldRefArrayList.add(customFieldRef);
              } else {

                /* Invoke the normal field setter method */
                phoneCall.getClass().getMethod("set" + nodeName, this.getNSCRMFieldParamType(nodeType))
                    .invoke(phoneCall, this.getNSCRMFieldObject(nodeName, nodeType, element.getTextContent()));
              }
            } catch (final NoSuchMethodException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                  + " in CRM Object type 'PhoneCall'");
            } catch (final IllegalArgumentException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                  + " in CRM Object type 'PhoneCall'");
            } catch (final IllegalAccessException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                  + " in CRM Object type 'PhoneCall'");
            } catch (final InvocationTargetException e) {
              throw new ScribeException(ScribeResponseCodes._1003 + "Found problem in tracing CRM field: " + nodeName
                  + " in CRM Object type 'PhoneCall'");
            }
          }

        } else {
          logger.debug("----Inside createPhoneCall, no node name, skipped");
        }
      }

      /* Dont set contact/case if company is not found */
      if (phoneCall.getCompany() == null) {

        logger.debug("----Inside createPhoneCall, company is not found, hence removing contact/support case");

        /* Contact without company dont work at NS */
        phoneCall.setContact(null);

        /* Support case without company dont work at NS */
        phoneCall.setSupportCase(null);
      } else {
        final RecordRef companyRef = phoneCall.getCompany();

        /* Check if internal id is found */
        if (companyRef.getInternalId() != null && companyRef.getInternalId().trim().equalsIgnoreCase("")) {
          logger.debug("----Inside createPhoneCall, internal id of company is not found, hence removing contact");

          /* Contact without company dont work at NS */
          phoneCall.setContact(null);

          /* Support case without company dont work at NS */
          phoneCall.setSupportCase(null);

          /* Support case without company dont work at NS */
          phoneCall.setCompany(null);
        }
      }

      /* check if any custom field and set it to the task record */
      final CustomFieldRef[] customFieldList = this.getCustomFieldRefArray(customFieldRefArrayList);

      /* Set custom field list */
      if (customFieldList != null) {

        /* Create new custom field list */
        final CustomFieldList newCustomFieldList = new CustomFieldList();
        newCustomFieldList.setCustomField(customFieldList);

        /* Add list in task */
        phoneCall.setCustomFieldList(newCustomFieldList);
      }
    } else {
      logger.warn("----Inside createPhoneCall, empty element list.");
      throw new ScribeException(ScribeResponseCodes._1008 + " Request data is not found in request");
    }

    return phoneCall;
  }

  /**
   * 
   * @param contactSearchBasic
   * @param searchStringField
   * @param cRMFieldName
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private final ContactSearchBasic updateContactSearchBasic(final ContactSearchBasic contactSearchBasic, final String cRMFieldName,
      final String cRMFieldValue, final String cRMFieldOperator, final String crmFieldIntraSeparator, final String inputDateFormat)
      throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

    logger.debug("----Inside updateContactSearchBasic cRMFieldName: " + cRMFieldName + " & cRMFieldValue: " + contactSearchBasic
        + " & cRMFieldOperator: " + cRMFieldOperator);

    if (cRMFieldName != null) {

      /* Check if cRMFieldName contans crmFieldIntraSeparator */
      if (cRMFieldName.contains(crmFieldIntraSeparator)) {

        String nodeName = null;
        String nodeType = null;

        final int index = cRMFieldName.lastIndexOf(crmFieldIntraSeparator);

        /* If found a positive index */
        if (index > 0) {

          /* Get node name */
          nodeName = cRMFieldName.substring(0, index).trim();

          /* Get node type */
          nodeType = cRMFieldName.substring(index + crmFieldIntraSeparator.length(), cRMFieldName.length()).trim();
        }

        /* Validate node name/type */
        if (nodeName == null & nodeType == null) {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1008 + "Field: '" + cRMFieldName + "' is not in correct format");
        }

        try {
          /* If a custom field */
          if (nodeType.toUpperCase().contains("CUSTOM")) {

            /* Invoke the normal field setter method */
            SearchCustomFieldList searchCustomFieldList = contactSearchBasic.getCustomFieldList();

            /* If already a list */
            if (searchCustomFieldList != null) {

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(searchCustomFieldList, nodeName, nodeType, cRMFieldValue,
                  cRMFieldOperator));
            } else {

              /* Create a new list */
              searchCustomFieldList = new SearchCustomFieldList();

              /* Update the list */
              searchCustomFieldList.setCustomField(this.getCustomFieldForSearchOperation(null, nodeName, nodeType, cRMFieldValue, cRMFieldOperator));

            }

            /* Set custom field list */
            contactSearchBasic.setCustomFieldList(searchCustomFieldList);
          } else {
            /* Invoke the method */
            contactSearchBasic
                .getClass()
                .getMethod("set" + nodeName.trim(), Class.forName(NetSuiteMessageFormatUtils.NS_WSDL_PACKAGE_NAME + nodeType.trim()))
                .invoke(contactSearchBasic,
                    this.getNormalFieldForSearchOperation(nodeName.trim(), nodeType.trim(), cRMFieldValue, cRMFieldOperator, inputDateFormat));
          }
        } catch (final ClassNotFoundException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + " Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Contact'");
        } catch (final NoSuchMethodException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + " Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Contact'");
        } catch (final IllegalArgumentException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + " Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Contact'");
        } catch (final IllegalAccessException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + " Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Contact'");
        } catch (final InvocationTargetException e) {
          throw new ScribeException(ScribeResponseCodes._1003 + " Found problem in tracing CRM field: " + cRMFieldName
              + " in CRM Object type 'Contact'");
        }
      } else {
        throw new ScribeException(ScribeResponseCodes._1003 + " CRM field format should be CRMFIELDNAME" + crmFieldIntraSeparator + "CRMFIELDTYPE");
      }
    }

    return contactSearchBasic;
  }

  private final SearchCustomField[] getCustomFieldForSearchOperation(final SearchCustomFieldList searchCustomFieldList, final String cRMFieldName,
      final String cRMFieldType, final String cRMFieldValue, final String cRMFieldOperator) {

    SearchCustomField[] searchCustomFieldUpdated = null;

    /* Check if any list is already set */
    if (searchCustomFieldList == null) {

      /* Initialize it */
      searchCustomFieldUpdated = new SearchCustomField[1];

      /* Populate new array with additional values */
      searchCustomFieldUpdated[0] = this.getCustomCRMSearchObject(cRMFieldName.trim(), cRMFieldType.trim(), cRMFieldValue, cRMFieldOperator);
    } else {
      searchCustomFieldUpdated = new SearchCustomField[searchCustomFieldList.getCustomField().length + 1];

      /* Copy old array into new one */
      System.arraycopy(searchCustomFieldList.getCustomField(), 0, searchCustomFieldUpdated, 0, searchCustomFieldList.getCustomField().length);

      /* Populate new array with additional values */
      searchCustomFieldUpdated[searchCustomFieldList.getCustomField().length] =
          this.getCustomCRMSearchObject(cRMFieldName.trim(), cRMFieldType.trim(), cRMFieldValue, cRMFieldOperator);
    }

    return searchCustomFieldUpdated;
  }

  /**
   * 
   * @param refArrayList
   * @return
   */
  private final CustomFieldRef[] getCustomFieldRefArray(List<CustomFieldRef> refArrayList) {

    CustomFieldRef[] customFieldRefUpdated = null;

    /* Check if any list is already set */
    if (refArrayList == null || refArrayList.size() == 0) {

      return null;
    }

    /* Initialize it */
    customFieldRefUpdated = new CustomFieldRef[refArrayList.size()];

    int counter = 0;

    for (final CustomFieldRef ref : refArrayList) {
      customFieldRefUpdated[counter] = ref;
      counter++;
    }
    return customFieldRefUpdated;
  }

  /**
   * Split the multiselect custom field value into 2 (format typeid__valueinternalid)
   * 
   * @param value
   * @return
   * @throws ScribeException
   */
  private final String[] splitCustomMutliSelectValue(final String value) throws ScribeException {

    /* Verify value */
    if (value == null) {
      throw new ScribeException(ScribeResponseCodes._1003 + "Value of MultiSelect Custom CRM object field value '" + value + "' invalid format");
    } else if (value != null && !value.matches("^..*__..*$")) {
      throw new ScribeException(ScribeResponseCodes._1003 + "Value of MultiSelect Custom CRM object field value '" + value + "' invalid format");
    }
    final String[] splitResult = new String[2];
    splitResult[0] = value.replaceAll("__.*$", "");
    splitResult[1] = value.replaceAll("^.*__", "");
    return splitResult;
  }

  /**
   * Split a search string into a 3 element String array.
   * 
   * @param searchString : search string using space and delimiter.
   * 
   * @return a 3 element String array. element 0 => search key, element 1 => search operator and
   *         element 2 => search value.
   */
  private final String[] splitSearchString(final String searchString) throws ScribeException {

    final String[] finalResult = new String[3];

    /* Split the search string from user */
    final String[] splitResult = searchString.split("\\s+");

    /* Check if length is less than 1 */
    if (splitResult.length <= 1) {

      logger.error("----Inside splitSearchString: invalid search string: " + searchString);
      throw new ScribeException(ScribeResponseCodes._1003 + "Invalid search string: '" + searchString
          + "' search string should contain at least 2 words for NS CRM");
    }
    /* Key */
    finalResult[0] = splitResult[0];

    /* Operator */
    finalResult[1] = splitResult[1];

    /* value, it will never null but can be empty string */
    finalResult[2] = searchString.replaceAll("^[ ]*" + finalResult[0] + "[ ]*" + finalResult[1] + "[ ]*", "").trim();
    logger.info("----Inside splitSearchString: split the search into [" + finalResult[0] + "],[" + finalResult[1] + "],[" + finalResult[2] + "]");
    return finalResult;
  }
}
