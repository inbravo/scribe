package com.inbravo.cad.external.test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.client.ServiceClient;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlString;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.function.StringFunction;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.constants.CRMConstants.MSCRMObjectType;
import com.inbravo.cad.rest.service.crm.CRMMessageFormatUtils;
import com.inbravo.cad.rest.service.crm.ms.MSCRMMessageFormatUtils;
import com.inbravo.cad.rest.service.crm.ms.MSCRMSchemaConstants;
import com.inbravo.cad.rest.service.crm.ms.auth.SOAPExecutor;
import com.microsoft.schemas.crm._2006.query.AllColumns;
import com.microsoft.schemas.crm._2006.query.ArrayOfAnyType;
import com.microsoft.schemas.crm._2006.query.ArrayOfConditionExpression;
import com.microsoft.schemas.crm._2006.query.ArrayOfString;
import com.microsoft.schemas.crm._2006.query.ColumnSet;
import com.microsoft.schemas.crm._2006.query.ConditionExpression;
import com.microsoft.schemas.crm._2006.query.ConditionOperator;
import com.microsoft.schemas.crm._2006.query.FilterExpression;
import com.microsoft.schemas.crm._2006.query.QueryBase;
import com.microsoft.schemas.crm._2006.query.QueryExpression;
import com.microsoft.schemas.crm._2006.webservices.ArrayOfBusinessEntity;
import com.microsoft.schemas.crm._2006.webservices.BusinessEntity;
import com.microsoft.schemas.crm._2006.webservices.BusinessEntityCollection;
import com.microsoft.schemas.crm._2007.webservices.Account;
import com.microsoft.schemas.crm._2007.webservices.CrmServiceCrmServiceSoap12Stub;
import com.microsoft.schemas.crm._2007.webservices.ExecuteDocument;
import com.microsoft.schemas.crm._2007.webservices.ExecuteResponseDocument;
import com.microsoft.schemas.crm._2007.webservices.RetrieveMultipleDocument;
import com.microsoft.schemas.crm._2007.webservices.RetrieveMultipleResponseDocument;
import com.microsoft.schemas.crm._2007.webservices.WhoAmIRequest;
import com.microsoft.schemas.crm._2007.webservices.WhoAmIResponse;
import com.microsoft.schemas.crm._2007.webservices.ExecuteDocument.Execute;
import com.microsoft.schemas.crm._2007.webservices.ExecuteResponseDocument.ExecuteResponse;
import com.microsoft.schemas.crm._2007.webservices.RetrieveMultipleDocument.RetrieveMultiple;
import com.microsoft.schemas.crm._2007.webservices.RetrieveMultipleResponseDocument.RetrieveMultipleResponse;

/**
 * 
 * @author amit.dixit
 * 
 */
public class MSCRMTest {

  public static void main(String[] args) throws Exception {
    /* Enable SOAP debugging using axis library */
    MSCRMMessageFormatUtils.debugSOAPMessage();
    retrieveMultiple();
  }

  private static final void addSecurityHeader(final ServiceClient client, final String authType, final String orgName, final String crmTicket) {

    /* Creating an Axiom factory */
    final OMFactory fac = OMAbstractFactory.getOMFactory();

    /* Creating header name spaces */
    final OMNamespace webNs = fac.createOMNamespace(MSCRMSchemaConstants.WEBSERVICES, "web");
    final OMNamespace coreNS = fac.createOMNamespace(MSCRMSchemaConstants.CORETYPE, "core");

    /* Creating a sub-element to set the text */
    final OMElement value = fac.createOMElement(MSCRMSchemaConstants.CRM_AUTH_TOKEN_TAG, webNs);

    /* Create an AuthenticationType element for header */
    final OMElement AuthenticationType = fac.createOMElement(MSCRMSchemaConstants.CRM_AUTH_TYPE_TAG, coreNS);
    AuthenticationType.setText(authType);

    /* Create an CrmTicket element for header */
    final OMElement CrmTicket = fac.createOMElement(MSCRMSchemaConstants.CRM_TICKET_TAG, coreNS);
    CrmTicket.setText(crmTicket);

    /* Create an OrganizationName element for header */
    final OMElement OrganizationName = fac.createOMElement(MSCRMSchemaConstants.CRM_ORG_NAME_TAG, coreNS);
    OrganizationName.setText(orgName);

    /* Add all values in parent node: CrmAuthenticationToken */
    value.addChild(AuthenticationType);
    value.addChild(CrmTicket);
    value.addChild(OrganizationName);

    /* Add header information at the client */
    client.addHeader(value);
  }

  private static final void addAdditionalNamespace(final QueryBase queryBase) {

    /* Get cursor from query node */
    final XmlCursor cursor = queryBase.newCursor();
    cursor.toNextToken();
    cursor.insertNamespace("query", MSCRMSchemaConstants.QUERY);

    /* Set cursor type information */
    cursor.insertAttributeWithValue("type", MSCRMSchemaConstants.XML_SCHEMA_INSTANCE, "query:QueryExpression");

    /* Dispose cursor */
    cursor.dispose();
  }

  @SuppressWarnings("unused")
  private static void who() throws Exception {

    final RetrieveMultipleDocument document = RetrieveMultipleDocument.Factory.newInstance();
    final RetrieveMultiple retrieveMultiple = document.addNewRetrieveMultiple();

    final QueryBase queryBase = retrieveMultiple.addNewQuery();
    queryBase.setEntityName("account");

    System.out.println(document);

    final CrmServiceCrmServiceSoap12Stub stub =
        new CrmServiceCrmServiceSoap12Stub("https://amitdixitinc.crm5.dynamics.com" + SOAPExecutor.CRM_ENDPOINT);

    addSecurityHeader(
        stub._getServiceClient(),
        "1",
        "crm5org5d97a",
        "PAA/AHgAbQBsACAAdgBlAHIAcwBpAG8AbgA9ACIAMQAuADAAIgA/AD4ADQAKADwAVABpAGMAawBlAHQAIAB4AG0AbABuAHMAOgB4AHMAZAA9ACIAaAB0AHQAcAA6AC8ALwB3AHcAdwAuAHcAMwAuAG8AcgBnAC8AMgAwADAAMQAvAFgATQBMAFMAYwBoAGUAbQBhACIAIAB4AG0AbABuAHMAOgB4AHMAaQA9ACIAaAB0AHQAcAA6AC8ALwB3AHcAdwAuAHcAMwAuAG8AcgBnAC8AMgAwADAAMQAvAFgATQBMAFMAYwBoAGUAbQBhAC0AaQBuAHMAdABhAG4AYwBlACIAPgANAAoAIAAgADwAQwByAGUAYQB0AGUAZAA+ADIAMAAxADEALQAwADcALQAyADUAVAAwADgAOgAyADAAOgAxADAALgA0ADYAOQAxADcAMAA4AFoAPAAvAEMAcgBlAGEAdABlAGQAPgANAAoAIAAgADwARQB4AHAAaQByAGUAcwBPAG4APgAyADAAMQAxAC0AMAA3AC0AMgA2AFQAMAA4ADoAMgAwADoAMQAwAC4ANAA2ADkAMQA3ADAAOABaADwALwBFAHgAcABpAHIAZQBzAE8AbgA+AA0ACgAgACAAPABPAHIAZwBhAG4AaQB6AGEAdABpAG8AbgBJAGQAPgA1AGQAOQA3AGEAMQBmAGMALQBkAGQAYwBjAC0ANABjADMAMQAtAGEAZgBhADkALQAyADUANgAzAGQAMQBhADgAZQAyADAAZgA8AC8ATwByAGcAYQBuAGkAegBhAHQAaQBvAG4ASQBkAD4ADQAKACAAIAA8AFUAcwBlAHIASQBkAD4AZABhAGYAMAA3ADgAZgA0AC0AMABlAGIANAAtADQAOQBhADAALQA4AGYAZQA0AC0AZgBmAGUAYwBhADAANAA1ADcANwAzAGEAPAAvAFUAcwBlAHIASQBkAD4ADQAKACAAIAA8AEMAaABlAGMAawBzAHUAbQA+AFoAMQA1AGgAdABMAFgATgBFAGUAQwBHAFUAUgB6AEIAMwB1AG0AeABVAG0AYgBoAEgAVAB6AE8AOQBjAG8AcABvAGsAYgA5AGMAUwBLAGoAdQB2AEcAWQAwAFQAQwBRAEwAUABSAGgATQBnADAATQBjAHgANgBpAGQARABLAEUAPAAvAEMAaABlAGMAawBzAHUAbQA+AA0ACgA8AC8AVABpAGMAawBlAHQAPgA=");

    ExecuteDocument executeDocument = ExecuteDocument.Factory.newInstance();
    Execute execute = executeDocument.addNewExecute();
    execute.setRequest(WhoAmIRequest.Factory.newInstance());
    ExecuteResponseDocument response = (ExecuteResponseDocument) stub.execute(executeDocument, null, null, null);

    ExecuteResponse executeResponse = response.getExecuteResponse();

    WhoAmIResponse actualResponse = (WhoAmIResponse) executeResponse.getResponse();

    System.out.println(actualResponse.getOrganizationId());
    System.out.println(actualResponse.getUserId());
    System.out.println(actualResponse.getBusinessUnitId());
  }

  private static void retrieveMultiple() throws Exception {

    final RetrieveMultipleDocument document = RetrieveMultipleDocument.Factory.newInstance();

    final RetrieveMultiple retrieveMultiple = document.addNewRetrieveMultiple();

    final QueryExpression queryExpression = QueryExpression.Factory.newInstance();
    queryExpression.setEntityName("account");

    final ColumnSet columnSet = ColumnSet.Factory.newInstance();
    final ArrayOfString arrayOfString = columnSet.addNewAttributes();
    arrayOfString.setAttributeArray(new String[] {"name", "telephone1", "telephone2", "telephone3"});

    queryExpression.setColumnSet(columnSet);

    FilterExpression filterExpression = queryExpression.addNewCriteria();
    ArrayOfConditionExpression arrayOfConditionExpression = filterExpression.addNewConditions();
    ConditionExpression conditionExpression = arrayOfConditionExpression.addNewCondition();
    conditionExpression.setAttributeName("telephone1");
    conditionExpression.setOperator(ConditionOperator.EQUAL);
    ArrayOfAnyType arrayOfAnyType = ArrayOfAnyType.Factory.newInstance();
    arrayOfAnyType.addNewValue();
    arrayOfAnyType.setValueArray(0, XmlString.Factory.newValue("555-0135"));
    conditionExpression.setValues(arrayOfAnyType);

    retrieveMultiple.setQuery(queryExpression);

    /* Add additional namespace in query */
    addAdditionalNamespace(queryExpression);
    System.out.println(document);

    final CrmServiceCrmServiceSoap12Stub stub =
        new CrmServiceCrmServiceSoap12Stub("https://amitdixitinc.crm5.dynamics.com" + SOAPExecutor.CRM_ENDPOINT);

    addSecurityHeader(
        stub._getServiceClient(),
        "1",
        "crm5org5d97a",
        "PAA/AHgAbQBsACAAdgBlAHIAcwBpAG8AbgA9ACIAMQAuADAAIgA/AD4ADQAKADwAVABpAGMAawBlAHQAIAB4AG0AbABuAHMAOgB4AHMAZAA9ACIAaAB0AHQAcAA6AC8ALwB3AHcAdwAuAHcAMwAuAG8AcgBnAC8AMgAwADAAMQAvAFgATQBMAFMAYwBoAGUAbQBhACIAIAB4AG0AbABuAHMAOgB4AHMAaQA9ACIAaAB0AHQAcAA6AC8ALwB3AHcAdwAuAHcAMwAuAG8AcgBnAC8AMgAwADAAMQAvAFgATQBMAFMAYwBoAGUAbQBhAC0AaQBuAHMAdABhAG4AYwBlACIAPgANAAoAIAAgADwAQwByAGUAYQB0AGUAZAA+ADIAMAAxADEALQAwADcALQAyADYAVAAwADkAOgAxADAAOgAwADgALgAwADcANwA4ADgAMwBaADwALwBDAHIAZQBhAHQAZQBkAD4ADQAKACAAIAA8AEUAeABwAGkAcgBlAHMATwBuAD4AMgAwADEAMQAtADAANwAtADIANwBUADAAOQA6ADEAMAA6ADAAOAAuADAANwA3ADgAOAAzAFoAPAAvAEUAeABwAGkAcgBlAHMATwBuAD4ADQAKACAAIAA8AE8AcgBnAGEAbgBpAHoAYQB0AGkAbwBuAEkAZAA+ADUAZAA5ADcAYQAxAGYAYwAtAGQAZABjAGMALQA0AGMAMwAxAC0AYQBmAGEAOQAtADIANQA2ADMAZAAxAGEAOABlADIAMABmADwALwBPAHIAZwBhAG4AaQB6AGEAdABpAG8AbgBJAGQAPgANAAoAIAAgADwAVQBzAGUAcgBJAGQAPgBkAGEAZgAwADcAOABmADQALQAwAGUAYgA0AC0ANAA5AGEAMAAtADgAZgBlADQALQBmAGYAZQBjAGEAMAA0ADUANwA3ADMAYQA8AC8AVQBzAGUAcgBJAGQAPgANAAoAIAAgADwAQwBoAGUAYwBrAHMAdQBtAD4AWAAyAC8AKwBwAHIAYQBnAEUAZQBDAE4AbgBkAGoAVABoAFYAcwAxAFQAaABzAEkANABLAE4AUwBiAEEASQByAHIAKwBpAGcAOQBQADgAZQB5ADUAbQBFAHkAYQBTAGMAbgA2AE8AYwBRAGgAbwAxAHkAUAB1ADkAdABlAEwAdQA8AC8AQwBoAGUAYwBrAHMAdQBtAD4ADQAKADwALwBUAGkAYwBrAGUAdAA+AA==");

    RetrieveMultipleResponseDocument responseDocument = stub.retrieveMultiple(document, null, null, null);
    RetrieveMultipleResponse multipleResponse = responseDocument.getRetrieveMultipleResponse();
    BusinessEntityCollection be = multipleResponse.getRetrieveMultipleResult();

    ArrayOfBusinessEntity entityArray = be.getBusinessEntities();
    BusinessEntity entityStringArray[] = entityArray.getBusinessEntityArray();

    for (int i = 0; i < entityStringArray.length; i++) {
      Account account = (Account) entityStringArray[i];
      System.out.println(account.getAccountid().getStringValue());
    }
  }

  @SuppressWarnings("unused")
  private static void retrieveAllColumns() throws Exception {

    final RetrieveMultipleDocument document = RetrieveMultipleDocument.Factory.newInstance();

    final RetrieveMultiple retrieveMultiple = document.addNewRetrieveMultiple();

    final QueryExpression queryExpression = QueryExpression.Factory.newInstance();
    queryExpression.setEntityName("account");

    queryExpression.setColumnSet(AllColumns.Factory.newInstance());

    retrieveMultiple.setQuery(queryExpression);

    /* Add additional namespace in query */
    addAdditionalNamespace(queryExpression);
    System.out.println(document);

    final CrmServiceCrmServiceSoap12Stub stub =
        new CrmServiceCrmServiceSoap12Stub("https://amitdixitinc.crm5.dynamics.com" + SOAPExecutor.CRM_ENDPOINT);

    addSecurityHeader(
        stub._getServiceClient(),
        "1",
        "crm5org5d97a",
        "PAA/AHgAbQBsACAAdgBlAHIAcwBpAG8AbgA9ACIAMQAuADAAIgA/AD4ADQAKADwAVABpAGMAawBlAHQAIAB4AG0AbABuAHMAOgB4AHMAZAA9ACIAaAB0AHQAcAA6AC8ALwB3AHcAdwAuAHcAMwAuAG8AcgBnAC8AMgAwADAAMQAvAFgATQBMAFMAYwBoAGUAbQBhACIAIAB4AG0AbABuAHMAOgB4AHMAaQA9ACIAaAB0AHQAcAA6AC8ALwB3AHcAdwAuAHcAMwAuAG8AcgBnAC8AMgAwADAAMQAvAFgATQBMAFMAYwBoAGUAbQBhAC0AaQBuAHMAdABhAG4AYwBlACIAPgANAAoAIAAgADwAQwByAGUAYQB0AGUAZAA+ADIAMAAxADEALQAwADcALQAyADYAVAAwADkAOgAxADAAOgAwADgALgAwADcANwA4ADgAMwBaADwALwBDAHIAZQBhAHQAZQBkAD4ADQAKACAAIAA8AEUAeABwAGkAcgBlAHMATwBuAD4AMgAwADEAMQAtADAANwAtADIANwBUADAAOQA6ADEAMAA6ADAAOAAuADAANwA3ADgAOAAzAFoAPAAvAEUAeABwAGkAcgBlAHMATwBuAD4ADQAKACAAIAA8AE8AcgBnAGEAbgBpAHoAYQB0AGkAbwBuAEkAZAA+ADUAZAA5ADcAYQAxAGYAYwAtAGQAZABjAGMALQA0AGMAMwAxAC0AYQBmAGEAOQAtADIANQA2ADMAZAAxAGEAOABlADIAMABmADwALwBPAHIAZwBhAG4AaQB6AGEAdABpAG8AbgBJAGQAPgANAAoAIAAgADwAVQBzAGUAcgBJAGQAPgBkAGEAZgAwADcAOABmADQALQAwAGUAYgA0AC0ANAA5AGEAMAAtADgAZgBlADQALQBmAGYAZQBjAGEAMAA0ADUANwA3ADMAYQA8AC8AVQBzAGUAcgBJAGQAPgANAAoAIAAgADwAQwBoAGUAYwBrAHMAdQBtAD4AWAAyAC8AKwBwAHIAYQBnAEUAZQBDAE4AbgBkAGoAVABoAFYAcwAxAFQAaABzAEkANABLAE4AUwBiAEEASQByAHIAKwBpAGcAOQBQADgAZQB5ADUAbQBFAHkAYQBTAGMAbgA2AE8AYwBRAGgAbwAxAHkAUAB1ADkAdABlAEwAdQA8AC8AQwBoAGUAYwBrAHMAdQBtAD4ADQAKADwALwBUAGkAYwBrAGUAdAA+AA==");

    RetrieveMultipleResponseDocument responseDocument = stub.retrieveMultiple(document, null, null, null);
    RetrieveMultipleResponse multipleResponse = responseDocument.getRetrieveMultipleResponse();
    BusinessEntityCollection be = multipleResponse.getRetrieveMultipleResult();

    ArrayOfBusinessEntity entityArray = be.getBusinessEntities();
    BusinessEntity entityStringArray[] = entityArray.getBusinessEntityArray();

    for (int i = 0; i < entityStringArray.length; i++) {
      Account account = (Account) entityStringArray[i];
      System.out.println(account.getAccountid().getStringValue());
    }
  }

  @SuppressWarnings("unchecked")
  public final List<String> getCRMObjectFields(final MSCRMObjectType mSCRMObjectType, final String crmHost, final String userId,
      final String password, final String orgName, final String crmTicket) throws Exception {
    try {
      final List<String> crmFields = new ArrayList<String>();

      final URL fileURL = CRMMessageFormatUtils.getFileURL("CrmMetaService-Execute.xml");

      String msg = MSCRMMessageFormatUtils.readStringFromFile(fileURL.getPath());

      /* Replace all constants */
      msg = msg.replaceAll(SOAPExecutor.CRM_TICKET, crmTicket);
      msg = msg.replaceAll(SOAPExecutor.ORG_NAME, orgName);
      msg = msg.replaceAll(SOAPExecutor.CRM_ENTITY, mSCRMObjectType.toString().toLowerCase());

      final SOAPExecutor sOAPExecutor = new SOAPExecutor();
      final SOAPMessage response = sOAPExecutor.execute("https://" + crmHost + SOAPExecutor.CRM_META_SERVICE_ENDPOINT, msg);

      final XPath xPath = sOAPExecutor.createXPath("//edx:LogicalName/text()", response);

      /* Create map to hold namespace information */
      final HashMap<String, String> map = new HashMap<String, String>();

      /*
       * This map information is required to extract value from SOAP response
       */
      map.put("edx", "http://schemas.microsoft.com/crm/2007/WebServices");

      /* Set name space information in xpath object */
      xPath.setNamespaceContext(new SimpleNamespaceContext(map));

      /* Create navigator object */
      final Navigator navigator = xPath.getNavigator();

      /* Get all nodes */
      final List<Node> nodeList = xPath.selectNodes(response.getSOAPBody());
      final Iterator<Node> iterator = nodeList.iterator();

      /* Iterate over nodes */
      while (iterator.hasNext()) {
        final Node result = iterator.next();
        final String crmFieldName = StringFunction.evaluate(result, navigator);
        if (!crmFieldName.equalsIgnoreCase(mSCRMObjectType.toString())) {

          /* Add crm field name in list */
          crmFields.add(StringFunction.evaluate(result, navigator));
        }
      }
      return crmFields;
    } catch (final IOException e) {
      throw new CADException(CADResponseCodes._1015 + " Not able to connect to MS CRM server");
    } catch (final SOAPException e) {
      throw new CADException(CADResponseCodes._1013 + " CRM server invalidated your request");
    } catch (final JaxenException e) {
      throw new CADException(CADResponseCodes._1013 + " CRM server invalidated your request");
    }
  }
}
