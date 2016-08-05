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

package com.inbravo.cad.rest.service.crm.ms.v4;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.resource.CADObject;
import com.inbravo.cad.rest.service.crm.ms.MSCRMMessageFormatUtils;
import com.inbravo.cad.rest.service.crm.ms.MSCRMSchemaConstants;
import com.inbravo.cad.rest.service.crm.ms.MSCRMServiceManager;
import com.inbravo.cad.rest.service.crm.ms.auth.SOAPExecutor;
import com.microsoft.schemas.crm._2006.query.AllColumns;
import com.microsoft.schemas.crm._2006.query.ArrayOfString;
import com.microsoft.schemas.crm._2006.query.ColumnSet;
import com.microsoft.schemas.crm._2006.query.QueryBase;
import com.microsoft.schemas.crm._2006.query.QueryExpression;
import com.microsoft.schemas.crm._2006.webservices.ArrayOfBusinessEntity;
import com.microsoft.schemas.crm._2006.webservices.BusinessEntity;
import com.microsoft.schemas.crm._2006.webservices.BusinessEntityCollection;
import com.microsoft.schemas.crm._2007.webservices.CreateDocument;
import com.microsoft.schemas.crm._2007.webservices.CreateDocument.Create;
import com.microsoft.schemas.crm._2007.webservices.CreateResponseDocument;
import com.microsoft.schemas.crm._2007.webservices.CreateResponseDocument.CreateResponse;
import com.microsoft.schemas.crm._2007.webservices.CrmServiceCrmServiceSoap12Stub;
import com.microsoft.schemas.crm._2007.webservices.RetrieveMultipleDocument;
import com.microsoft.schemas.crm._2007.webservices.RetrieveMultipleDocument.RetrieveMultiple;
import com.microsoft.schemas.crm._2007.webservices.RetrieveMultipleResponseDocument;
import com.microsoft.schemas.crm._2007.webservices.RetrieveMultipleResponseDocument.RetrieveMultipleResponse;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMLiveIdBasedServiceManager implements MSCRMServiceManager {

  private final Logger logger = Logger.getLogger(MSCRMLiveIdBasedServiceManager.class.getName());

  /* String constant for MS CRM authentication type */
  private String crmAuthType;

  /* String constant for seperating crm fields */
  private String crmFieldsSeparator;

  /* MS CRM field seperator */
  private String crmFieldIntraSeparator;

  /* String constant for seperating order by fields */
  private String orderFieldsSeparator;

  private String crmServiceEndpoint = SOAPExecutor.CRM_ENDPOINT;

  public final CADObject createObject(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final CADObject cADbject) throws Exception {
    logger.debug("---Inside createObject crmHost: " + crmHost + " & appProtocolType: " + appProtocolType + " & userId: " + userId + " & password: "
        + password + " & orgName: " + orgName + " crm object type: " + mSCRMObjectType + " & crmTicket: " + crmSecurityToken + " & cADbject: "
        + cADbject);

    try {
      /* Enable SOAP debugging using axis library */
      MSCRMMessageFormatUtils.debugSOAPMessage();

      /* Create main SOAP request document */
      final CreateDocument createDocument = CreateDocument.Factory.newInstance();

      final Create createRequest = createDocument.addNewCreate();

      /* Add new account type xml beans object */
      createRequest.setEntity(MSCRMMessageFormatUtils.createCRMObject(mSCRMObjectType, cADbject, crmFieldIntraSeparator));

      /* Create new stub */
      final CrmServiceCrmServiceSoap12Stub stub = new CrmServiceCrmServiceSoap12Stub(appProtocolType + "://" + crmHost + crmServiceEndpoint);

      /* Set security header information */
      this.addSecurityHeader(stub._getServiceClient(), orgName, crmSecurityToken[0]);
      logger.debug("---Inside createObject request: " + createDocument);

      /* Send SOAP request */
      final CreateResponseDocument responseDocument = stub.create(createDocument, null, null, null);

      logger.debug("---Inside createObject response: " + responseDocument);
      final CreateResponse createResponse = responseDocument.getCreateResponse();
      final String result = createResponse.getCreateResult();

      logger.debug("---Inside createObject result: " + result);

      /* Call stub cleanup */
      stub.cleanup();

      /* Add new node and return */
      return MSCRMMessageFormatUtils.addNode("id", result, cADbject);
    } catch (final AxisFault e) {
      throw new CADException(CADResponseCodes._1013 + " Recieved a web service error", e);
    } catch (final RemoteException e) {
      throw new CADException(CADResponseCodes._1015 + " Communication error", e);
    }
  }

  /**
   * 
   * @param mSCRMObjectType
   * @param crmHost
   * @param userId
   * @param password
   * @param orgName
   * @param crmTicket
   * @param crmFields
   * @return
   */
  public final List<CADObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFields) throws Exception {
    logger.debug("---Inside getObjects crmHost: " + crmHost + " & userId: " + " & appProtocolType: " + appProtocolType + userId + " & password: "
        + password + " & orgName: " + orgName + " crm object type: " + mSCRMObjectType + " & crmTicket: " + crmSecurityToken + " & crmFields: "
        + crmFields);

    try {
      /* Enable SOAP debugging using axis library */
      MSCRMMessageFormatUtils.debugSOAPMessage();

      /* Create main SOAP request document */
      final RetrieveMultipleDocument requestDocument = RetrieveMultipleDocument.Factory.newInstance();

      final RetrieveMultiple retrieveMultiple = requestDocument.addNewRetrieveMultiple();

      /* Create new query expression */
      final QueryExpression queryExpression = QueryExpression.Factory.newInstance();

      /* Set CRM object type information */
      queryExpression.setEntityName(mSCRMObjectType.toLowerCase());

      if (crmFields != null) {

        /* Add attribute */
        final ColumnSet columnSet = ColumnSet.Factory.newInstance();
        final ArrayOfString arrayOfString = columnSet.addNewAttributes();
        arrayOfString.setAttributeArray(crmFields);
        queryExpression.setColumnSet(columnSet);
      } else {

        /* If crmFields is null; fetch all columns */
        queryExpression.setColumnSet(AllColumns.Factory.newInstance());
      }

      /* Set query information */
      retrieveMultiple.setQuery(queryExpression);

      /* Add additional namespace in query */
      this.addAdditionalNamespace(queryExpression);

      /* Create new stub */
      final CrmServiceCrmServiceSoap12Stub stub = new CrmServiceCrmServiceSoap12Stub(appProtocolType + "://" + crmHost + crmServiceEndpoint);

      /* Set security header information */
      this.addSecurityHeader(stub._getServiceClient(), orgName, crmSecurityToken[0]);

      logger.debug("---Inside getCrmObjects request: " + requestDocument);

      /* Send SOAP request */
      final RetrieveMultipleResponseDocument responseDocument = stub.retrieveMultiple(requestDocument, null, null, null);

      logger.debug("---Inside getCrmObjects response: " + responseDocument);
      final RetrieveMultipleResponse multipleResponse = responseDocument.getRetrieveMultipleResponse();
      final BusinessEntityCollection be = multipleResponse.getRetrieveMultipleResult();
      final ArrayOfBusinessEntity entityArray = be.getBusinessEntities();
      final BusinessEntity[] entityStringArray = entityArray.getBusinessEntityArray();

      final List<CADObject> cADbjectList = new ArrayList<CADObject>();
      for (int i = 0; i < entityStringArray.length; i++) {

        /* Create new CAD object */
        final CADObject cADbject = new CADObject();

        /* Add all CRM fields */
        cADbject.setXmlContent(MSCRMMessageFormatUtils.createEntityFromBusinessObject(entityStringArray[i]));

        /* Add CAD object in list */
        cADbjectList.add(cADbject);
      }
      logger.debug("---Inside getObjects cADbjectList.size: " + cADbjectList.size());

      /* Return error message for no record found */
      if (cADbjectList.size() == 0) {
        logger.debug("---Inside getObjects no records in response");
        throw new CADException(CADResponseCodes._1004 + mSCRMObjectType);
      }

      /* Call stub cleanup */
      stub.cleanup();
      return cADbjectList;
    } catch (final AxisFault e) {
      throw new CADException(CADResponseCodes._1013 + " Recieved a web service error", e);
    } catch (final RemoteException e) {
      throw new CADException(CADResponseCodes._1015 + " Communication error", e);
    } catch (final CADException e) {
      /* Throw CAD exception to user */
      throw e;
    } catch (final Exception e) {
      throw new CADException(CADResponseCodes._1000 + " Problem while communicating with MS CRM server", e);
    }
  }

  /**
   * 
   * @param mSCRMObjectType
   * @param crmHost
   * @param userId
   * @param password
   * @param orgName
   * @param crmTicket
   * @param crmFields
   * @param query
   * @return
   */
  public final List<CADObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFieldsToSelect, final String query)
      throws Exception {
    logger.debug("---Inside getObjects crmHost: " + crmHost + " & userId: " + " & appProtocolType: " + appProtocolType + userId + " & password: "
        + password + " & orgName: " + orgName + " crm object type: " + mSCRMObjectType + " & crmTicket: " + crmSecurityToken
        + " & crmFieldsToSelect: " + crmFieldsToSelect + " & query: " + query);

    try {
      /* Enable SOAP debugging using axis library */
      MSCRMMessageFormatUtils.debugSOAPMessage();

      /* Create main SOAP request document */
      final RetrieveMultipleDocument requestDocument = RetrieveMultipleDocument.Factory.newInstance();

      final RetrieveMultiple retrieveMultiple = requestDocument.addNewRetrieveMultiple();

      /* Create new query expression */
      final QueryExpression queryExpression = QueryExpression.Factory.newInstance();

      /* Set CRM object type information */
      queryExpression.setEntityName(mSCRMObjectType.toLowerCase());

      if (crmFieldsToSelect != null && !"ALL".equalsIgnoreCase(crmFieldsToSelect[0])) {
        /* Add attribute */
        final ColumnSet columnSet = ColumnSet.Factory.newInstance();
        final ArrayOfString arrayOfString = columnSet.addNewAttributes();
        for (int i = 0; i < crmFieldsToSelect.length; i++) {
          logger.debug("---Inside getObjects crmFieldsToSelect: " + crmFieldsToSelect[i]);
        }
        arrayOfString.setAttributeArray(crmFieldsToSelect);
        queryExpression.setColumnSet(columnSet);
      } else {
        queryExpression.setColumnSet(AllColumns.Factory.newInstance());
      }

      /* Add filter expression */
      if (query != null && !"NONE".equalsIgnoreCase(query)) {
        MSCRMMessageFormatUtils.createFilterInQuery(query, null, queryExpression, crmFieldsSeparator, orderFieldsSeparator);
      }

      /* Set query information */
      retrieveMultiple.setQuery(queryExpression);

      /* Add additional namespace in query */
      this.addAdditionalNamespace(queryExpression);

      /* Create new stub */
      final CrmServiceCrmServiceSoap12Stub stub = new CrmServiceCrmServiceSoap12Stub(appProtocolType + "://" + crmHost + crmServiceEndpoint);

      /* Set security header information */
      this.addSecurityHeader(stub._getServiceClient(), orgName, crmSecurityToken[0]);

      logger.debug("---Inside getObjects request: " + requestDocument);

      /* Send SOAP request */
      final RetrieveMultipleResponseDocument responseDocument = stub.retrieveMultiple(requestDocument, null, null, null);

      logger.debug("---Inside getObjects response: " + responseDocument);
      final RetrieveMultipleResponse multipleResponse = responseDocument.getRetrieveMultipleResponse();
      final BusinessEntityCollection be = multipleResponse.getRetrieveMultipleResult();
      final ArrayOfBusinessEntity entityArray = be.getBusinessEntities();
      final BusinessEntity[] entityStringArray = entityArray.getBusinessEntityArray();

      final List<CADObject> cADbjectList = new ArrayList<CADObject>();
      for (int i = 0; i < entityStringArray.length; i++) {

        /* Create new CAD object */
        final CADObject cADbject = new CADObject();

        /* Add all CRM fields */
        cADbject.setXmlContent(MSCRMMessageFormatUtils.createEntityFromBusinessObject(entityStringArray[i]));

        /* Add CAD object in list */
        cADbjectList.add(cADbject);
      }
      logger.debug("---Inside getObjects cADbjectList.size: " + cADbjectList.size());

      /* Return error message for no record found */
      if (cADbjectList.size() == 0) {
        logger.debug("---Inside getObjects no records in response");
        throw new CADException(CADResponseCodes._1004 + mSCRMObjectType);
      }

      /* Call stub cleanup */
      return cADbjectList;
    } catch (final AxisFault e) {
      throw new CADException(CADResponseCodes._1013 + " Recieved a web service error", e);
    } catch (final RemoteException e) {
      throw new CADException(CADResponseCodes._1015 + " Communication error", e);
    } catch (final CADException e) {
      /* Throw CAD exception to user */
      throw e;
    } catch (final Exception e) {
      throw new CADException(CADResponseCodes._1000 + " Problem while communicating with MS CRM server", e);
    }
  }

  public final List<CADObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFieldsToSelect, final String query,
      final String order) throws Exception {
    logger.debug("---Inside getObjects crmHost: " + crmHost + " & userId: " + " & appProtocolType: " + appProtocolType + userId + " & password: "
        + password + " & orgName: " + orgName + " crm object type: " + mSCRMObjectType + " & crmTicket: " + crmSecurityToken
        + " & crmFieldsToSelect: " + crmFieldsToSelect + " & query: " + query + " & order: " + order);

    try {
      /* Enable SOAP debugging using axis library */
      MSCRMMessageFormatUtils.debugSOAPMessage();

      /* Create main SOAP request document */
      final RetrieveMultipleDocument requestDocument = RetrieveMultipleDocument.Factory.newInstance();

      final RetrieveMultiple retrieveMultiple = requestDocument.addNewRetrieveMultiple();

      /* Create new query expression */
      final QueryExpression queryExpression = QueryExpression.Factory.newInstance();

      /* Set CRM object type information */
      queryExpression.setEntityName(mSCRMObjectType.toLowerCase());

      if (crmFieldsToSelect != null && !"ALL".equalsIgnoreCase(crmFieldsToSelect[0])) {
        /* Add attribute */
        final ColumnSet columnSet = ColumnSet.Factory.newInstance();
        final ArrayOfString arrayOfString = columnSet.addNewAttributes();
        for (int i = 0; i < crmFieldsToSelect.length; i++) {
          logger.debug("---Inside getObjects crmFieldsToSelect: " + crmFieldsToSelect[i]);
        }
        arrayOfString.setAttributeArray(crmFieldsToSelect);
        queryExpression.setColumnSet(columnSet);
      } else {
        queryExpression.setColumnSet(AllColumns.Factory.newInstance());
      }

      /* Add filter expression */
      if (query != null && !"NONE".equalsIgnoreCase(query)) {
        MSCRMMessageFormatUtils.createFilterInQuery(query, order, queryExpression, crmFieldsSeparator, orderFieldsSeparator);
      } else if (order != null) {
        MSCRMMessageFormatUtils.createFilterInQuery(null, order, queryExpression, crmFieldsSeparator, orderFieldsSeparator);
      }

      /* Set query information */
      retrieveMultiple.setQuery(queryExpression);

      /* Add additional namespace in query */
      this.addAdditionalNamespace(queryExpression);

      /* Create new stub */
      final CrmServiceCrmServiceSoap12Stub stub = new CrmServiceCrmServiceSoap12Stub(appProtocolType + "://" + crmHost + crmServiceEndpoint);

      /* Set security header information */
      this.addSecurityHeader(stub._getServiceClient(), orgName, crmSecurityToken[0]);

      logger.debug("---Inside getObjects request: " + requestDocument);

      /* Send SOAP request */
      final RetrieveMultipleResponseDocument responseDocument = stub.retrieveMultiple(requestDocument, null, null, null);

      logger.debug("---Inside getObjects response: " + responseDocument);
      final RetrieveMultipleResponse multipleResponse = responseDocument.getRetrieveMultipleResponse();
      final BusinessEntityCollection be = multipleResponse.getRetrieveMultipleResult();
      final ArrayOfBusinessEntity entityArray = be.getBusinessEntities();
      final BusinessEntity[] entityStringArray = entityArray.getBusinessEntityArray();

      final List<CADObject> cADbjectList = new ArrayList<CADObject>();
      for (int i = 0; i < entityStringArray.length; i++) {

        /* Create new CAD object */
        final CADObject cADbject = new CADObject();

        /* Add all CRM fields */
        cADbject.setXmlContent(MSCRMMessageFormatUtils.createEntityFromBusinessObject(entityStringArray[i]));

        /* Add CAD object in list */
        cADbjectList.add(cADbject);
      }
      logger.debug("---Inside getObjects cADbjectList.size: " + cADbjectList.size());

      /* Return error message for no record found */
      if (cADbjectList.size() == 0) {
        logger.debug("---Inside getObjects no records in response");
        throw new CADException(CADResponseCodes._1004 + mSCRMObjectType);
      }

      /* Call stub cleanup */
      return cADbjectList;
    } catch (final AxisFault e) {
      throw new CADException(CADResponseCodes._1013 + " Recieved a web service error", e);
    } catch (final RemoteException e) {
      throw new CADException(CADResponseCodes._1015 + " Communication error", e);
    } catch (final CADException e) {
      /* Throw CAD exception to user */
      throw e;
    } catch (final Exception e) {
      throw new CADException(CADResponseCodes._1000 + " Problem while communicating with MS CRM server", e);
    }
  }

  /**
   * The reason for using explicit way of setting SOAP header is, WSDL classes does not work
   * perfectly
   * 
   * @param client
   * @param authType
   * @param orgName
   * @param crmTicket
   */
  private final void addSecurityHeader(final ServiceClient client, final String orgName, final String crmTicket) {

    /* Creating an Axiom factory */
    final OMFactory fac = OMAbstractFactory.getOMFactory();

    /* Creating header name spaces */
    final OMNamespace webNs = fac.createOMNamespace(MSCRMSchemaConstants.WEBSERVICES, "web");
    final OMNamespace coreNS = fac.createOMNamespace(MSCRMSchemaConstants.CORETYPE, "core");

    /* Creating a sub-element to set the text */
    final OMElement value = fac.createOMElement(MSCRMSchemaConstants.CRM_AUTH_TOKEN_TAG, webNs);

    /* Create an 'AuthenticationType' element for header */
    final OMElement AuthenticationType = fac.createOMElement(MSCRMSchemaConstants.CRM_AUTH_TYPE_TAG, coreNS);
    AuthenticationType.setText(crmAuthType);

    /* Create an 'CrmTicket' element for header */
    final OMElement CrmTicket = fac.createOMElement(MSCRMSchemaConstants.CRM_TICKET_TAG, coreNS);
    CrmTicket.setText(crmTicket);

    /* Create an 'OrganizationName' element for header */
    final OMElement OrganizationName = fac.createOMElement(MSCRMSchemaConstants.CRM_ORG_NAME_TAG, coreNS);
    OrganizationName.setText(orgName);

    /* Add all values in parent node: 'CrmAuthenticationToken' */
    value.addChild(AuthenticationType);
    value.addChild(CrmTicket);
    value.addChild(OrganizationName);

    /* Add header information at the client */
    client.addHeader(value);
  }

  /**
   * The reason behing adding additional namespace is, there is a difference between Microsoft and
   * Axis web service artifacts
   * 
   * @param queryBase
   */
  private final void addAdditionalNamespace(final QueryBase queryBase) {

    /* Get cursor from query node */
    final XmlCursor cursor = queryBase.newCursor();
    cursor.toNextToken();
    cursor.insertNamespace("query", MSCRMSchemaConstants.QUERY);

    /* Set cursor type information */
    cursor.insertAttributeWithValue("type", MSCRMSchemaConstants.XML_SCHEMA_INSTANCE, "query:QueryExpression");

    /* Dispose cursor */
    cursor.dispose();
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

  public final String getCrmAuthType() {
    return crmAuthType;
  }

  public final void setCrmAuthType(final String crmAuthType) {
    this.crmAuthType = crmAuthType;
  }

  public final String getCrmServiceEndpoint() {
    return crmServiceEndpoint;
  }

  public final void setCrmServiceEndpoint(final String crmServiceEndpoint) {
    this.crmServiceEndpoint = crmServiceEndpoint;
  }

  /**
   * 
   * @return
   */
  public final String getCrmFieldIntraSeparator() {
    return crmFieldIntraSeparator;
  }

  /**
   * 
   * @param crmFieldIntraSeparator
   */
  public void setCrmFieldIntraSeparator(final String crmFieldIntraSeparator) {
    this.crmFieldIntraSeparator = crmFieldIntraSeparator;
  }
}
