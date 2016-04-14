package com.inbravo.cad.external.test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.inbravo.cad.rest.service.crm.CRMMessageFormatUtils;
import com.inbravo.cad.rest.service.crm.ms.MSCRMMessageFormatUtils;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMOCPTest {

  public final static void main(String[] args) throws Exception {

    final String Username = "mailamitdixit@8x8xtrial.onmicrosoft.com";
    final String Password = "1qaz@WSX";
    final String CRMUrl = "https://8x8xtrial.crm5.dynamics.com/XRMServices/2011/Organization.svc";

    final String accountId = "081973E9-99D4-E111-A120-1CC1DEEA276A";

    /* Step 1: Get URN address and STS Enpoint dynamically from WSDL */
    final String WSDL = getMethod(CRMUrl + "?wsdl");
    final String WSDLImportURL = getValueFromXML(WSDL, "//*[local-name()='import' and namespace-uri()='http://schemas.xmlsoap.org/wsdl/']/@location");

    System.out.println("WSDLImportURL: " + WSDLImportURL);
    final String WSDKImport = getMethod(WSDLImportURL);
    final String URNAddress =
        getValueFromXML(
            WSDKImport,
            "//*[local-name()='AuthenticationPolicy' and namespace-uri()='http://schemas.microsoft.com/xrm/2011/Contracts/Services']/*[local-name()='SecureTokenService' and namespace-uri()='http://schemas.microsoft.com/xrm/2011/Contracts/Services']//*[local-name()='AppliesTo' and namespace-uri()='http://schemas.microsoft.com/xrm/2011/Contracts/Services']/text()");

    System.out.println("URNAddress: " + URNAddress);
    final String STSEnpoint =
        getValueFromXML(
            WSDKImport,
            "//*[local-name()='Issuer' and namespace-uri()='http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702']/*[local-name()='Address' and namespace-uri()='http://www.w3.org/2005/08/addressing']/text()");
    System.out.println("STSEnpoint: " + STSEnpoint);

    final URL fileURL = CRMMessageFormatUtils.getFileURL("SecurityTokenSoapTemplate.xml");

    System.out.println("file URL: " + fileURL.getPath());
    String msg = MSCRMMessageFormatUtils.readStringFromFile(fileURL.getPath());

    /* Get DB specific formatter */
    final DateTimeFormatter isoDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /* Get current time */
    final String timeCreated = isoDateFormat.print(DateTime.now(DateTimeZone.forID(("GMT"))));
    final String timeExpires = isoDateFormat.print(DateTime.now(DateTimeZone.forID(("GMT"))).plusMinutes(5));
    System.out.println("timeCreated: " + timeCreated);
    System.out.println("timeExpires: " + timeExpires);

    /* Replace all constants from document */
    msg = msg.replaceAll("%timeCreated%", timeCreated);
    msg = msg.replaceAll("%timeExpires%", timeExpires);
    msg = msg.replaceAll("%RandomTextFirst%", "ACQA");
    msg = msg.replaceAll("%UserName%", Username);
    msg = msg.replaceAll("%Password%", Password);
    msg = msg.replaceAll("%MessageID%", UUID.randomUUID().toString());
    msg = msg.replaceAll("%URNAddress%", URNAddress);
    msg = msg.replaceAll("%STSEndpoint%", STSEnpoint);

    System.out.println("request to MS login server: " + msg);
    /* Step 2: Get Security Token by sending OCP username, password */
    final String securityTokenXML = getSOAPResponse(STSEnpoint, msg);
    System.out.println("response from MS login server: " + securityTokenXML);

    final String securityToken0 = getValueFromXML(securityTokenXML, "//*[local-name()='CipherValue']/text()");
    final String securityToken1 = getValueFromXML(securityTokenXML, "//*[local-name()='CipherValue']/text()", 1);
    final String keyIdentifier = getValueFromXML(securityTokenXML, "//*[local-name()='KeyIdentifier']/text()");
    System.out.println("securityToken0: " + securityToken0);
    System.out.println("securityToken1: " + securityToken1);
    System.out.println("keyIdentifier: " + keyIdentifier);

    final URL fileURL1 = CRMMessageFormatUtils.getFileURL("CrmSoapRequest.xml");

    System.out.println("file URL: " + fileURL1.getPath());
    String msg1 = MSCRMMessageFormatUtils.readStringFromFile(fileURL1.getPath());

    /* Get current time */
    final String timeCreated1 = isoDateFormat.print(DateTime.now(DateTimeZone.forID(("GMT"))));
    final String timeExpires1 = isoDateFormat.print(DateTime.now(DateTimeZone.forID(("GMT"))).plusMinutes(5));

    System.out.println("timeCreated1: " + timeCreated1);
    System.out.println("timeExpires1: " + timeExpires1);

    /* Replace all constants from document */
    msg1 = msg1.replaceAll("%timeCreated%", timeCreated1);
    msg1 = msg1.replaceAll("%timeExpires%", timeExpires1);
    msg1 = msg1.replaceAll("%Action%", "Retrieve");
    msg1 = msg1.replaceAll("%CRMUrl%", CRMUrl);
    msg1 = msg1.replaceAll("%MessageID%", UUID.randomUUID().toString());
    msg1 = msg1.replaceAll("%KeyIdentifier%", keyIdentifier);
    msg1 = msg1.replaceAll("%SecurityToken0%", securityToken0);
    msg1 = msg1.replaceAll("%SecurityToken1%", securityToken1);
    msg1 = msg1.replaceAll("%AccountID%", accountId);

    /* Step 3: Get account information from MS CRM */
    System.out.println("request to CRM: " + msg1);
    final String accountInformation = getSOAPResponse(CRMUrl, msg1);
    System.out.println("response from CRM: " + accountInformation);
  }

  private final static String getMethod(final String url) {

    GetMethod getMethod = new GetMethod(url);
    getMethod.addRequestHeader("Content-Type", "application/xml");
    HttpClient httpclient = new HttpClient();
    try {

      try {
        System.out.println("URI: " + getMethod.getURI());
        int result = httpclient.executeMethod(getMethod);
        System.out.println("Response code: " + result);
        return getMethod.getResponseBodyAsString();
      } catch (Exception exception) {
        System.err.println(exception);
      }
    } finally {
      getMethod.releaseConnection();
    }

    return null;
  }

  private final static String getValueFromXML(final String inputXML, final String xPathQuery) throws Exception {
    return getValueFromXML(inputXML, xPathQuery, 0);
  }

  private final static String getValueFromXML(final String inputXML, final String xPathQuery, final int index) throws Exception {
    return getValueFromXML(inputXML, xPathQuery, index, null);
  }

  private final static String getValueFromXML(final String inputXML, final String xPathQuery, final int index, final String[] namespaces)
      throws Exception {

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document document = builder.parse(new ByteArrayInputStream(inputXML.getBytes()));

    /* Create new XPath object to query XML document */
    final XPath xpath = XPathFactory.newInstance().newXPath();

    /* XPath Query for showing all nodes value */
    final XPathExpression expr = xpath.compile(xPathQuery);

    if (index > 0) {

      /* Get node list from response document */
      final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

      /* Check if records founds */
      if (nodeList != null && nodeList.getLength() >= 0) {

        return nodeList.item(index).getTextContent();
      }

    } else {
      /* Get node list from response document */
      return expr.evaluate(document);
    }

    return null;
  }

  /**
   * 
   * @param url
   * @param soapEnvelope
   * @return
   * @throws UnsupportedEncodingException
   */
  public final static String getSOAPResponse(final String url, final String soapEnvelope) throws UnsupportedEncodingException {

    final PostMethod postMethod = new PostMethod(url);
    postMethod.addRequestHeader("Content-Type", "application/soap+xml; charset=UTF-8");
    final HttpClient httpclient = new HttpClient();
    RequestEntity entity = new StringRequestEntity(soapEnvelope, "application/x-www-form-urlencoded", null);
    postMethod.setRequestEntity(entity);
    try {

      try {
        System.out.println("URI: " + postMethod.getURI());
        final int result = httpclient.executeMethod(postMethod);
        System.out.println("Response code: " + result);
        return postMethod.getResponseBodyAsString();
      } catch (Exception exception) {
        System.err.println(exception);
      }
    } finally {
      postMethod.releaseConnection();
    }

    return null;
  }
}
