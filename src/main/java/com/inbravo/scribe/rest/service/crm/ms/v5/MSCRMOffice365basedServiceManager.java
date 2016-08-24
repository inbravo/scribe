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

package com.inbravo.scribe.rest.service.crm.ms.v5;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.resource.ScribeObject;
import com.inbravo.scribe.rest.service.crm.CRMMessageFormatUtils;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMMessageFormatUtils;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMSchemaConstants;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMServiceManager;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMSchemaConstants.MSCRM_2011_Schema_Constants;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMSchemaConstants.MSCRM_SAML_Constants;
import com.inbravo.scribe.rest.service.crm.ms.auth.SOAPExecutor;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;
import com.microsoft.schemas.xrm._2011.contracts.ArrayOfEntity;
import com.microsoft.schemas.xrm._2011.contracts.ColumnSet;
import com.microsoft.schemas.xrm._2011.contracts.Entity;
import com.microsoft.schemas.xrm._2011.contracts.EntityCollection;
import com.microsoft.schemas.xrm._2011.contracts.EntityReference;
import com.microsoft.schemas.xrm._2011.contracts.EntityReferenceCollection;
import com.microsoft.schemas.xrm._2011.contracts.EntityRole;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_Associate_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_Create_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_RetrieveMultiple_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.OrganizationServiceStub;
import com.microsoft.schemas.xrm._2011.contracts.PagingInfo;
import com.microsoft.schemas.xrm._2011.contracts.QueryExpression;
import com.microsoft.schemas.xrm._2011.contracts.Relationship;
import com.microsoft.schemas.xrm._2011.contracts.services.AssociateDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.AssociateDocument.Associate;
import com.microsoft.schemas.xrm._2011.contracts.services.AssociateResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.AssociateResponseDocument.AssociateResponse;
import com.microsoft.schemas.xrm._2011.contracts.services.CreateDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.CreateDocument.Create;
import com.microsoft.schemas.xrm._2011.contracts.services.CreateResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.CreateResponseDocument.CreateResponse;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveMultipleDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveMultipleDocument.RetrieveMultiple;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveMultipleResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveMultipleResponseDocument.RetrieveMultipleResponse;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMOffice365basedServiceManager implements MSCRMServiceManager {

  private final Logger logger = Logger.getLogger(MSCRMOffice365basedServiceManager.class.getName());

  /* MS web service client timeout */
  private String timeout;

  /* String constant for MS CRM authentication type */
  private String crmAuthType;

  /* Max records required in search */
  private int maxRecordInSearch = 20;

  /* String constant for seperating crm fields */
  private String crmFieldsSeparator;

  /* Allowed date formats */
  private String permittedDateFormats;

  /* String constant for seperating order by fields */
  private String orderFieldsSeparator;

  /* MS CRM field seperator */
  private String crmFieldIntraSeparator;

  /* MS office 365 login validity */
  private int loginExpirationInMinutes = 5;

  /* Path of axis client config file */
  private String axisConfigFile = "axis2.xml";

  /* Version 5 MS CRM service end point */
  private String crmServiceEndpoint = SOAPExecutor.CRM_ENDPOINT_V5;

  /* MS office 365 login date timezone */
  private String msOffice365RequestTimeZone = SOAPExecutor.OFFICE_365_REQUEST_TZ;

  /* MS office 365 login date format */
  private String msOffice365RequestDateFormat = SOAPExecutor.OFFICE_365_REQUEST_DATE_FORMAT;

  /* MS CRM operation name for create use case */
  private static final String msCRMCreateCommand = "Create";

  /* MS CRM operation name for create use case */
  private static final String msCRMAssociateCommand = "Associate";

  /* MS CRM operation name for retrieve multiple use case */
  private static final String msCRMRetrieveMultipleCommand = "RetrieveMultiple";

  /**
   * 
   */
  public final ScribeObject createObject(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final ScribeObject scribeObject) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside createObject crmHost: " + crmHost + " & appProtocolType: " + appProtocolType + " & userId: " + userId + "& password: "
          + password + " & orgName: " + orgName + " crm object type: " + mSCRMObjectType + " & crmTicket: " + crmSecurityToken + " & scribeObject: "
          + scribeObject);
    }

    try {
      /* Enable SOAP debugging using axis library */
      MSCRMMessageFormatUtils.debugSOAPMessage();

      /* Create main SOAP request document */
      final CreateDocument createDocument = CreateDocument.Factory.newInstance();

      final Create createRequest = createDocument.addNewCreate();

      /* Set object type in Scribe object */
      scribeObject.setObjectType(mSCRMObjectType.toLowerCase());

      /* Add new account type xml beans object */
      createRequest.setEntity(MSCRMMessageFormatUtils.createV5RetrieveCRMObjectReq(scribeObject, crmFieldIntraSeparator, permittedDateFormats));

      /* Create new stub */
      final OrganizationServiceStub stub =
          this.createOrganizationServiceStub(appProtocolType + "://" + crmHost + crmServiceEndpoint, crmSecurityToken, msCRMCreateCommand);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside createObject request: " + createDocument);
      }

      /* Send SOAP request */
      final CreateResponseDocument responseDocument = stub.create(createDocument);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside createObject response: " + responseDocument);
      }
      final CreateResponse createResponse = responseDocument.getCreateResponse();

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside createObject result: " + createResponse.getCreateResult());
      }

      /* Check if object association is desired */
      final String[] regardingObjInfo = MSCRMMessageFormatUtils.getRegardingObjectInfo(scribeObject);

      /* Check if valid object association request from user */
      if (regardingObjInfo != null && regardingObjInfo.length == 3) {

        /* Go for object association */
        this.associateObject(mSCRMObjectType, appProtocolType, crmHost, userId, password, orgName, crmSecurityToken,
            createResponse.getCreateResult(), regardingObjInfo[0], regardingObjInfo[1], regardingObjInfo[2]);
      } else {
        logger.debug("---Inside createObject no object reference found in request");
      }

      /* Call stub cleanup */
      stub.cleanup();

      /* Add new node and return */
      return MSCRMMessageFormatUtils.addNode("id", createResponse.getCreateResult(), scribeObject);
    } catch (final Exception e) {

      /* Sentralized exception handling */
      this.handleException(e);
    }

    return null;
  }

  /**
   * This method is responsible for association between MS CRM obejcts
   * 
   * @param mSCRMObjectType
   * @param appProtocolType
   * @param crmHost
   * @param userId
   * @param password
   * @param orgName
   * @param crmSecurityToken
   * @param objectId
   * @param regardingObjectId
   * @param regardingObjectType
   * @param regardingObjectSchema
   * @return
   * @throws Exception
   */
  private final boolean associateObject(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String objectId, final String regardingObjectId,
      final String regardingObjectType, final String regardingObjectSchema) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside associateObject crmHost: " + crmHost + " & appProtocolType: " + appProtocolType + " & userId: " + userId
          + "& password: " + password + " & orgName: " + orgName + " crm object type: " + mSCRMObjectType + " & crmTicket: " + crmSecurityToken
          + " & objectId: " + objectId + " & mSCRMObjectType: " + mSCRMObjectType + " & regardingObjectId: " + regardingObjectId
          + " & regardingObjectType: " + regardingObjectType + " & regardingObjectSchema: " + regardingObjectSchema);
    }

    try {
      /* Enable SOAP debugging using axis library */
      MSCRMMessageFormatUtils.debugSOAPMessage();

      /* Create main SOAP request document */
      final AssociateDocument associateDocument = AssociateDocument.Factory.newInstance();

      /* create new associate reference */
      final Associate associateRequest = associateDocument.addNewAssociate();

      /* Set parent information */
      associateRequest.setEntityId(objectId);
      associateRequest.setEntityName(mSCRMObjectType.toLowerCase());

      /* create new reference collection */
      final EntityReferenceCollection relatedEntities = associateRequest.addNewRelatedEntities();

      /* create new entity reference */
      final EntityReference er = relatedEntities.addNewEntityReference();

      /* Set child information */
      er.setId(regardingObjectId);
      er.setLogicalName(regardingObjectType);

      /* Create object that defines the relationship between objects */
      final Relationship relationship = associateRequest.addNewRelationship();

      /* Set relationship definition */
      relationship.setPrimaryEntityRole(EntityRole.REFERENCING);

      /* Set schema type */
      relationship.setSchemaName(regardingObjectSchema);

      /* Create new stub */
      final OrganizationServiceStub stub =
          this.createOrganizationServiceStub(appProtocolType + "://" + crmHost + crmServiceEndpoint, crmSecurityToken, msCRMAssociateCommand);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside associateObject request: " + associateDocument);
      }

      /* Send SOAP request */
      final AssociateResponseDocument responseDocument = stub.associate(associateDocument);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside associateObject response: " + responseDocument);
      }

      final AssociateResponse associateResponse = responseDocument.getAssociateResponse();

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside associateObject result: " + associateResponse.xmlText());
      }

      /* Call stub cleanup */
      stub.cleanup();

      /* Add new node and return */
      return true;
    } catch (final Exception e) {

      /* Sentralized exception handling */
      this.handleException(e);
    }

    return false;
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
  public final List<ScribeObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFields) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects crmHost: " + crmHost + " & userId: " + userId + " & appProtocolType: " + appProtocolType + " & password: "
          + password + " & orgName: " + orgName + " & crm object type: " + mSCRMObjectType + " & crmTicket length: " + crmSecurityToken.length
          + " & crmFields: " + crmFields);
    }

    try {
      /* Enable SOAP debugging using axis library */
      MSCRMMessageFormatUtils.debugSOAPMessage();

      /* Create main SOAP request document */
      final RetrieveMultipleDocument requestDocument = RetrieveMultipleDocument.Factory.newInstance();

      /* Create request document */
      final RetrieveMultiple retrieveMultiple = requestDocument.addNewRetrieveMultiple();

      /* Create new query expression */
      final QueryExpression queryExpression = QueryExpression.Factory.newInstance();

      /* Set CRM object type information */
      queryExpression.setEntityName(mSCRMObjectType.toLowerCase());

      /* Add pagination information to out outofmemory error */
      final PagingInfo pInfo = queryExpression.addNewPageInfo();
      pInfo.setCount(maxRecordInSearch);

      /* TODO: Remove this page number while applying pagination */
      pInfo.setPageNumber(1);

      if (crmFields != null) {

        /* Add attribute */
        final ColumnSet columnSet = ColumnSet.Factory.newInstance();
        final ArrayOfstring arrayOfString = columnSet.addNewColumns();
        arrayOfString.setStringArray(crmFields);
        queryExpression.setColumnSet(columnSet);
      } else {

        /* Add attribute */
        final ColumnSet columnSet = ColumnSet.Factory.newInstance();

        /* If crmFields is null; fetch all columns */
        columnSet.setAllColumns(true);
        queryExpression.setColumnSet(columnSet);
      }

      /* Set query information */
      retrieveMultiple.setQuery(queryExpression);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects request: " + requestDocument);
      }

      /* Create new stub */
      final OrganizationServiceStub stub =
          this.createOrganizationServiceStub(appProtocolType + "://" + crmHost + crmServiceEndpoint, crmSecurityToken, msCRMRetrieveMultipleCommand);

      /* Send SOAP request */
      final RetrieveMultipleResponseDocument responseDocument = stub.retrieveMultiple(requestDocument);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects response: " + responseDocument);
      }

      /* Parse the response */
      final RetrieveMultipleResponse multipleResponse = responseDocument.getRetrieveMultipleResponse();
      final EntityCollection be = multipleResponse.getRetrieveMultipleResult();
      final ArrayOfEntity entityArray = be.getEntities();
      final Entity[] entityStringArray = entityArray.getEntityArray();

      final List<ScribeObject> ScribebjectList = new ArrayList<ScribeObject>();
      for (int i = 0; i < entityStringArray.length; i++) {

        /* Create new Scribe object */
        final ScribeObject Scribebject = new ScribeObject();

        /* Add all CRM fields */
        Scribebject.setXmlContent(MSCRMMessageFormatUtils.createV5EntityFromBusinessObject(entityStringArray[i]));

        /* Add Scribe object in list */
        ScribebjectList.add(Scribebject);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects ScribebjectList.size: " + ScribebjectList.size());
      }

      /* Return error message for no record found */
      if (ScribebjectList.size() == 0) {
        logger.debug("---Inside getObjects no records in response");
        throw new ScribeException(ScribeResponseCodes._1004 + mSCRMObjectType);
      }

      /* Call stub cleanup */
      stub.cleanup();
      return ScribebjectList;
    } catch (final Exception e) {

      /* Sentralized exception handling */
      this.handleException(e);
    }

    return null;
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
  public final List<ScribeObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFieldsToSelect, final String query)
      throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects crmHost: " + crmHost + " & userId: " + userId + " & appProtocolType: " + appProtocolType + " & password: "
          + password + " & orgName: " + orgName + " & crm object type: " + mSCRMObjectType + " & crmTicket length: " + crmSecurityToken.length
          + " & crmFieldsToSelect: " + crmFieldsToSelect + " & query: " + query);
    }

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

      /* Add pagination information to out outofmemory error */
      final PagingInfo pInfo = queryExpression.addNewPageInfo();
      pInfo.setCount(maxRecordInSearch);

      /* TODO: Remove this page number while applying pagination */
      pInfo.setPageNumber(1);

      if (crmFieldsToSelect != null && !"ALL".equalsIgnoreCase(crmFieldsToSelect[0])) {

        /* Add attribute */
        final ColumnSet columnSet = ColumnSet.Factory.newInstance();
        final ArrayOfstring arrayOfString = columnSet.addNewColumns();
        arrayOfString.setStringArray(crmFieldsToSelect);
        queryExpression.setColumnSet(columnSet);
      } else {

        /* Add attribute */
        final ColumnSet columnSet = ColumnSet.Factory.newInstance();

        /* If crmFields is null; fetch all columns */
        columnSet.setAllColumns(true);
        queryExpression.setColumnSet(columnSet);
      }

      /* Add filter expression */
      if (query != null && !"NONE".equalsIgnoreCase(query)) {

        /* Add in query expression */
        MSCRMMessageFormatUtils.createV5FilterInQuery(query, null, queryExpression, crmFieldsSeparator, orderFieldsSeparator);
      }

      /* Set query information */
      retrieveMultiple.setQuery(queryExpression);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects request: " + requestDocument);
      }

      /* Create new stub */
      final OrganizationServiceStub stub =
          this.createOrganizationServiceStub(appProtocolType + "://" + crmHost + crmServiceEndpoint, crmSecurityToken, msCRMRetrieveMultipleCommand);

      /* Send SOAP request */
      final RetrieveMultipleResponseDocument responseDocument = stub.retrieveMultiple(requestDocument);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects response: " + responseDocument);
      }

      final RetrieveMultipleResponse multipleResponse = responseDocument.getRetrieveMultipleResponse();
      final EntityCollection be = multipleResponse.getRetrieveMultipleResult();
      final ArrayOfEntity entityArray = be.getEntities();
      final Entity[] entities = entityArray.getEntityArray();

      final List<ScribeObject> ScribebjectList = new ArrayList<ScribeObject>();

      /* Iterate over entities and create Scribe object */
      for (int i = 0; i < entities.length; i++) {

        /* Create new Scribe object */
        final ScribeObject Scribebject = new ScribeObject();

        /* Add all CRM fields */
        Scribebject.setXmlContent(MSCRMMessageFormatUtils.createV5EntityFromBusinessObject(entities[i]));

        /* Add Scribe object in list */
        ScribebjectList.add(Scribebject);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects ScribebjectList.size: " + ScribebjectList.size());
      }

      /* Return error message for no record found */
      if (ScribebjectList.size() == 0) {
        logger.debug("---Inside getObjects no records in response");
        throw new ScribeException(ScribeResponseCodes._1004 + mSCRMObjectType);
      }

      /* Call stub cleanup */
      return ScribebjectList;
    } catch (final Exception e) {

      /* Sentralized exception handling */
      this.handleException(e);
    }

    return null;
  }

  /**
	 * 
	 */
  public final List<ScribeObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFieldsToSelect, final String query,
      final String order) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects crmHost: " + crmHost + " & userId: " + userId + " & appProtocolType: " + appProtocolType + " & password: "
          + password + " & orgName: " + orgName + " crm object type: " + mSCRMObjectType + " & crmTicket length: " + crmSecurityToken.length
          + " & crmFieldsToSelect: " + crmFieldsToSelect + " & query: " + query + " & order: " + order);
    }

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

      /* Add pagination information to out outofmemory error */
      final PagingInfo pInfo = queryExpression.addNewPageInfo();
      pInfo.setCount(maxRecordInSearch);

      /* TODO: Remove this page number while applying pagination */
      pInfo.setPageNumber(1);

      if (crmFieldsToSelect != null && !"ALL".equalsIgnoreCase(crmFieldsToSelect[0])) {
        /* Add attribute */
        final ColumnSet columnSet = ColumnSet.Factory.newInstance();
        final ArrayOfstring arrayOfString = columnSet.addNewColumns();
        arrayOfString.setStringArray(crmFieldsToSelect);
        queryExpression.setColumnSet(columnSet);
      } else {

        /* Add attribute */
        final ColumnSet columnSet = ColumnSet.Factory.newInstance();

        /* If crmFields is null; fetch all columns */
        columnSet.setAllColumns(true);
        queryExpression.setColumnSet(columnSet);
      }

      /* Add filter expression */
      if (query != null && !"NONE".equalsIgnoreCase(query)) {
        MSCRMMessageFormatUtils.createV5FilterInQuery(query, order, queryExpression, crmFieldsSeparator, orderFieldsSeparator);
      } else if (order != null) {
        MSCRMMessageFormatUtils.createV5FilterInQuery(null, order, queryExpression, crmFieldsSeparator, orderFieldsSeparator);
      }

      /* Set query information */
      retrieveMultiple.setQuery(queryExpression);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects request: " + requestDocument);
      }

      /* Create new stub */
      final OrganizationServiceStub stub =
          this.createOrganizationServiceStub(appProtocolType + "://" + crmHost + crmServiceEndpoint, crmSecurityToken, msCRMRetrieveMultipleCommand);

      /* Send SOAP request */
      final RetrieveMultipleResponseDocument responseDocument = stub.retrieveMultiple(requestDocument);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects response: " + responseDocument);
      }

      final RetrieveMultipleResponse multipleResponse = responseDocument.getRetrieveMultipleResponse();
      final EntityCollection be = multipleResponse.getRetrieveMultipleResult();
      final ArrayOfEntity entityArray = be.getEntities();
      final Entity[] entityStringArray = entityArray.getEntityArray();

      final List<ScribeObject> ScribebjectList = new ArrayList<ScribeObject>();
      for (int i = 0; i < entityStringArray.length; i++) {

        /* Create new Scribe object */
        final ScribeObject Scribebject = new ScribeObject();

        /* Add all CRM fields */
        Scribebject.setXmlContent(MSCRMMessageFormatUtils.createV5EntityFromBusinessObject(entityStringArray[i]));

        /* Add Scribe object in list */
        ScribebjectList.add(Scribebject);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getObjects ScribebjectList.size: " + ScribebjectList.size());
      }

      /* Return error message for no record found */
      if (ScribebjectList.size() == 0) {
        logger.debug("---Inside getObjects no records in response");
        throw new ScribeException(ScribeResponseCodes._1004 + mSCRMObjectType);
      }

      /* Call stub cleanup */
      return ScribebjectList;
    } catch (final Exception e) {

      /* Sentralized exception handling */
      this.handleException(e);
    }

    return null;
  }

  /**
   * 
   * @param organizationServiceURL
   * @param securityTokens
   * @return
   * @throws RemoteException
   */
  private final OrganizationServiceStub createOrganizationServiceStub(final String organizationServiceURL, final String[] securityTokens,
      final String msCRMOperationType) throws Exception {
    try {

      /* Get axis config file URl */
      final URL fileURL = CRMMessageFormatUtils.getFileURL(axisConfigFile);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside createOrganizationServiceStub, organizationServiceURL: " + organizationServiceURL + " & axis config file path "
            + fileURL.getPath());
      }

      final ConfigurationContext ctx =
          ConfigurationContextFactory.createConfigurationContextFromFileSystem(System.getProperty("user.dir"), fileURL.getPath());

      /* Create new organization stub */
      final OrganizationServiceStub stub = new OrganizationServiceStub(ctx, organizationServiceURL);

      /* Service client */
      final ServiceClient sc = stub._getServiceClient();

      /* Options for service client */
      final Options options = sc.getOptions();

      /* Set unique GUID */
      options.setMessageId("urn:uuid:" + UUID.randomUUID().toString());

      /* Set endpoint reference */
      final EndpointReference endPoint = new EndpointReference("http://www.w3.org/2005/08/addressing/anonymous");
      options.setReplyTo(endPoint);

      try {
        /* Set timeout */
        options.setTimeOutInMilliSeconds(Integer.parseInt(this.getTimeout()));
      } catch (final NumberFormatException e) {
        throw new ScribeException(ScribeResponseCodes._1002 + "'MS_Session_Timeout' in scribe.properties");
      }

      /* Set all options */
      sc.setOptions(options);

      /* Get all optional headers */
      final SOAPHeaderBlock[] blocks = this.createCRMOptionsHeaderBlock(organizationServiceURL, securityTokens, msCRMOperationType);

      /* Set other valid optional headers */
      if (blocks != null && blocks.length > 0) {

        for (SOAPHeaderBlock block : blocks) {
          sc.addHeader(block);
        }
      }

      return stub;
    } catch (final AxisFault e) {
      logger.debug("---Inside createOrganizationServiceStub, AxisFault: " + e, e);
      throw new ScribeException(ScribeResponseCodes._1015 + "Problem in creating SOAP request for MS office 365 login server", e);
    }
  }

  /**
   * 
   * @param securityTokens
   * @return
   */
  private final SOAPHeaderBlock[] createCRMOptionsHeaderBlock(final String organizationServiceURL, final String[] securityTokens,
      final String msCRMOperationType) {

    /* Get DB specific formatter */
    final DateTimeFormatter isoDateFormat = DateTimeFormat.forPattern(msOffice365RequestDateFormat);

    /* Get current time */
    final String currentDateTime = isoDateFormat.print(DateTime.now(DateTimeZone.forID((msOffice365RequestTimeZone))));

    /* Add 5 minutes expiry time from now */
    final String expireDateTime =
        isoDateFormat.print(DateTime.now(DateTimeZone.forID((msOffice365RequestTimeZone))).plusMinutes(loginExpirationInMinutes));

    /* The final customer specific security header */
    final String securityHeader = String.format(MSCRMSchemaConstants.securityHeaderTemplate, securityTokens[2], securityTokens[0], securityTokens[1]);

    try {

      /* Create new factory */
      final OMFactory factory = OMAbstractFactory.getOMFactory();

      /* Create security/addressing headers */
      final OMNamespace addressingNS = factory.createOMNamespace(MSCRM_SAML_Constants._ADDRESSING, "a");
      final OMNamespace securityNS = factory.createOMNamespace(MSCRM_SAML_Constants._WSSSecurity, "o");
      final OMNamespace utitlityNS = factory.createOMNamespace(MSCRM_SAML_Constants._WSSSecurityUtility, "u");

      final OMElement timeStamp = factory.createOMElement("Timestamp", utitlityNS);
      timeStamp.addAttribute("Id", "_0", utitlityNS);

      /* Add created timestamp information */
      final OMElement created = factory.createOMElement("Created", utitlityNS);
      final OMText createdTime = factory.createOMText(currentDateTime + "Z");
      created.addChild(createdTime);

      /* Add expires timestamp information */
      final OMElement expires = factory.createOMElement("Expires", utitlityNS);
      final OMText expiresTime = factory.createOMText(expireDateTime + "Z");
      expires.addChild(expiresTime);

      timeStamp.addChild(created);
      timeStamp.addChild(expires);

      /* Create security header block */
      final SOAPHeaderBlock wsseHeader = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock("Security", securityNS);
      wsseHeader.setMustUnderstand(true);

      /* Add time validity information */
      wsseHeader.addChild(timeStamp);
      wsseHeader.addChild(AXIOMUtil.stringToOM(factory, securityHeader));

      /* Create action header block for action */
      final SOAPHeaderBlock actionHeader = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock("Action", addressingNS);
      actionHeader.setMustUnderstand(true);
      final OMText actionText = factory.createOMText(MSCRM_2011_Schema_Constants._IORGANIZATIONSERVICE + msCRMOperationType);
      actionHeader.addChild(actionText);

      /* Create messageId header block for action */
      final SOAPHeaderBlock messageIdHeader = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock("MessageID", addressingNS);
      final OMText messageIdText = factory.createOMText(UUID.randomUUID().toString());
      messageIdHeader.addChild(messageIdText);

      /* Create replyTo header block for action */
      final SOAPHeaderBlock replyToHeader = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock("ReplyTo", addressingNS);
      final OMElement address = factory.createOMElement("Address", addressingNS);
      final OMText addressText = factory.createOMText("http://www.w3.org/2005/08/addressing/anonymous");
      address.addChild(addressText);
      replyToHeader.addChild(address);

      /* Create To header block for action */
      final SOAPHeaderBlock toHeader = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock("To", addressingNS);
      toHeader.setMustUnderstand(true);
      final OMText toText = factory.createOMText(organizationServiceURL);
      toHeader.addChild(toText);

      return new SOAPHeaderBlock[] {actionHeader, messageIdHeader, replyToHeader, toHeader, wsseHeader};

    } catch (final XMLStreamException e) {
      throw new ScribeException(ScribeResponseCodes._1015 + "Problem in adding security information to SOAP request to MS office 365 login server");
    }
  }

  /**
   * 
   * @param e
   * @throws Exception
   */
  private final void handleException(final Exception e) throws Exception {

    /* If axis fault */
    if (e instanceof AxisFault) {

      final AxisFault fault = (AxisFault) e;
      if (fault.getFaultReasonElement() != null) {
        throw new ScribeException(ScribeResponseCodes._1013 + "Recieved a web service error : " + fault.getFaultReasonElement().getText(), e);
      } else if (fault.getFaultDetailElement() != null) {
        throw new ScribeException(ScribeResponseCodes._1013 + "Recieved a web service error : " + fault.getFaultDetailElement().getText(), e);
      } else {
        throw new ScribeException(ScribeResponseCodes._1013 + "Recieved a web service error: " + fault.getReason(), e);
      }

    } else if (e instanceof RemoteException) {

      /* Wrap this into runtime exception and throw to user */
      throw new ScribeException(ScribeResponseCodes._1015 + "Remote error", e);
    } else if (e instanceof ScribeException) {

      /* Throw Scribe exception to user */
      throw e;
    } else {

      /* If exception from MS */
      if (e instanceof IOrganizationService_RetrieveMultiple_OrganizationServiceFaultFault_FaultMessage) {

        final IOrganizationService_RetrieveMultiple_OrganizationServiceFaultFault_FaultMessage message =
            (IOrganizationService_RetrieveMultiple_OrganizationServiceFaultFault_FaultMessage) e;

        if (message.getFaultMessage() != null && message.getFaultMessage().getOrganizationServiceFault() != null) {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1013 + "SOAP error from MS CRM : "
              + message.getFaultMessage().getOrganizationServiceFault().getMessage());
        } else {
          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1013 + "Problem while communicating with MS CRM server", e);
        }
      } else if (e instanceof IOrganizationService_Create_OrganizationServiceFaultFault_FaultMessage) {

        final IOrganizationService_Create_OrganizationServiceFaultFault_FaultMessage message =
            (IOrganizationService_Create_OrganizationServiceFaultFault_FaultMessage) e;

        if (message.getFaultMessage() != null && message.getFaultMessage().getOrganizationServiceFault() != null) {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1013 + "SOAP error from MS CRM : "
              + message.getFaultMessage().getOrganizationServiceFault().getMessage());
        } else {
          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1013 + "Problem while communicating with MS CRM server", e);
        }
      } else if (e instanceof IOrganizationService_Associate_OrganizationServiceFaultFault_FaultMessage) {

        final IOrganizationService_Associate_OrganizationServiceFaultFault_FaultMessage message =
            (IOrganizationService_Associate_OrganizationServiceFaultFault_FaultMessage) e;

        if (message.getFaultMessage() != null && message.getFaultMessage().getOrganizationServiceFault() != null) {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1013 + "SOAP error from MS CRM : "
              + message.getFaultMessage().getOrganizationServiceFault().getMessage());
        } else {
          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1013 + "Problem while communicating with MS CRM server", e);
        }
      } else {
        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1000 + "Problem while communicating with MS CRM server", e);
      }

    }
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

  /**
   * @return the crmServiceEndpoint
   */
  public final String getCrmServiceEndpoint() {
    return this.crmServiceEndpoint;
  }

  /**
   * @param crmServiceEndpoint the crmServiceEndpoint to set
   */
  public final void setCrmServiceEndpoint(final String crmServiceEndpoint) {
    this.crmServiceEndpoint = crmServiceEndpoint;
  }

  /**
   * @return the msOffice365RequestTimeZone
   */
  public final String getMsOffice365RequestTimeZone() {
    return this.msOffice365RequestTimeZone;
  }

  /**
   * @param msOffice365RequestTimeZone the msOffice365RequestTimeZone to set
   */
  public final void setMsOffice365RequestTimeZone(final String msOffice365RequestTimeZone) {
    this.msOffice365RequestTimeZone = msOffice365RequestTimeZone;
  }

  /**
   * @return the msOffice365RequestDateFormat
   */
  public final String getMsOffice365RequestDateFormat() {
    return this.msOffice365RequestDateFormat;
  }

  /**
   * @param msOffice365RequestDateFormat the msOffice365RequestDateFormat to set
   */
  public final void setMsOffice365RequestDateFormat(final String msOffice365RequestDateFormat) {
    this.msOffice365RequestDateFormat = msOffice365RequestDateFormat;
  }

  /**
   * @return the timeout
   */
  public final String getTimeout() {
    return this.timeout;
  }

  /**
   * @param timeout the timeout to set
   */
  public final void setTimeout(final String timeout) {
    this.timeout = timeout;
  }

  /**
   * @return the loginEspirationInMinutes
   */
  public final int getLoginExpirationInMinutes() {
    return this.loginExpirationInMinutes;
  }

  /**
   * @param loginEspirationInMinutes the loginEspirationInMinutes to set
   */
  public final void setLoginExpirationInMinutes(final int loginExpirationInMinutes) {
    this.loginExpirationInMinutes = loginExpirationInMinutes;
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

  /**
   * @return the permittedDateFormats
   */
  public final String getPermittedDateFormats() {
    return this.permittedDateFormats;
  }

  /**
   * @param permittedDateFormats the permittedDateFormats to set
   */
  public final void setPermittedDateFormats(final String permittedDateFormats) {
    this.permittedDateFormats = permittedDateFormats;
  }

  /**
   * @return the maxRecordInSearch
   */
  public final int getMaxRecordInSearch() {
    return this.maxRecordInSearch;
  }

  /**
   * @param maxRecordInSearch the maxRecordInSearch to set
   */
  public final void setMaxRecordInSearch(final int maxRecordInSearch) {
    this.maxRecordInSearch = maxRecordInSearch;
  }
}
