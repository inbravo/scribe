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

package com.inbravo.cad.rest.service.crm.ms.auth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class SOAPExecutor {

  private final Logger logger = Logger.getLogger(SOAPExecutor.class.getName());

  /* Properties to communicate with micrososft services */
  public final static String CRM_ENDPOINT = "/MSCRMServices/2007/CrmService.asmx";
  public final static String CRM_ENDPOINT_V5 = "/XRMServices/2011/Organization.svc";
  public final static String CRM_META_SERVICE_ENDPOINT = "/MSCRMServices/2007/MetadataService.asmx";
  public final static String CRM_DISCOVERY_SERVICE_ENDPOINT = "/MSCRMServices/2007/Passport/CrmDiscoveryService.asmx";
  public final static String CRM_TICKET = "%CRM_TICKET%";
  public final static String ORG_NAME = "%ORG_NAME%";
  public final static String FETCH_XML = "%FETCH_XML%";
  public final static String USER_POLICY = "MBI_SSL";
  public final static String LIVE_ID_ENDPOINT = "/wstlogin.srf";
  public final static String LIVE_ID_HOST = "dev.login.live.com";
  public final static String OFFICE_365_REQUEST_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
  public final static String OFFICE_365_REQUEST_TZ = "GMT";
  public final static String OFFICE_365_HOST = "login.microsoftonline.com";
  public final static String OFFICE_365_ENDPOINT = "/RST2.srf";
  public final static String WSSE_XMLNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
  public final static String XMLDSIG_DS = "http://www.w3.org/2000/09/xmldsig#";
  public final static String XMLDSIG_WST = "http://schemas.xmlsoap.org/ws/2005/02/trust";
  public final static String XMLDSIG_SOAPENV = "http://www.w3.org/2003/05/soap-envelope";
  public final static String LIVE_ID_SERVER_PLACEHOLDER = "%SERVER%";
  public final static String LIVE_ID_USERNAME_PLACEHOLDER = "%USERNAME%";
  public final static String LIVE_ID_PASSWORD_PLACEHOLDER = "%PASSWORD%";
  public final static String LIVE_ID_POLICY_PLACEHOLDER = "%POLICY%";
  public final static String CRM_ENTITY = "%CRM_ENTITY%";
  public final static String CRM_ENTITY_FIELDS = "%CRM_ENTITY_FIELDS%";
  public final static String OFFICE_365_KEY_IDENTIFIER = "%KEY_IDENTIFIER%";
  public final static String OFFICE_365_ENCRYPTED_KEY_CYPHER_VALUE = "%ENCRYPTED_KEY_CYPHER_VALUE%";
  public final static String OFFICE_365_CYPHER_DATA_VALUE = "%CYPHER_DATA_VALUE%";

  /**
   * Executes SOAP message
   * 
   * @param endpointUrl SOAP endpoint
   * @param request SOAP request
   * @return SOAP response message
   * @throws SOAPException in case of a SOAP issue
   * @throws IOException in case of an IO issue
   */
  public final SOAPMessage execute(final String endpointUrl, final String request) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside execute endpointUrl: " + endpointUrl + " & request: " + request);
    }

    /* Create SOAP message */
    final SOAPMessage message =
        MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage(new MimeHeaders(), new ByteArrayInputStream(request.getBytes()));

    /* Execute the url */
    return this.execute(endpointUrl, message);
  }

  /**
   * 
   * @param url
   * @param soapEnvelope
   * @return
   * @throws UnsupportedEncodingException
   */
  public final String getSOAPResponse(final String url, final String soapEnvelope) throws UnsupportedEncodingException {

    /* Post the SOAP to MS CRM */
    final PostMethod postMethod = new PostMethod(url);
    postMethod.addRequestHeader("Content-Type", "application/soap+xml; charset=UTF-8");

    /* Create new HTTP client */
    final HttpClient httpclient = new HttpClient();
    RequestEntity entity = new StringRequestEntity(soapEnvelope, "application/x-www-form-urlencoded", null);

    postMethod.setRequestEntity(entity);
    try {

      try {
        if (logger.isDebugEnabled()) {
          logger.debug("---Inside execute url: " + postMethod.getURI());
        }

        final int result = httpclient.executeMethod(postMethod);

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside execute response code: " + result);
        }
        return postMethod.getResponseBodyAsString();
      } catch (final Exception exception) {
        /* Send user error */
        throw new CADException(CADResponseCodes._1009 + "Problem in calling MS CRM URL: " + url);
      }
    } finally {
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
  }

  /**
   * 
   * @param url
   * @return
   */
  public final String callHttpURL(final String url) {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getHttpURL, calling url: " + url);
    }

    final GetMethod getMethod = new GetMethod(url);
    getMethod.addRequestHeader("Content-Type", "application/xml");
    final HttpClient httpclient = new HttpClient();
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getHttpURL, url: " + getMethod.getURI());
      }

      int result = httpclient.executeMethod(getMethod);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getHttpURL, response code: " + result);
      }
      return getMethod.getResponseBodyAsString();

    } catch (final Exception exception) {
      logger.debug("---Inside getHttpURL, exception: " + exception);

      /* Send user error */
      throw new CADException(CADResponseCodes._1009 + "Problem in finding CRM integration information from customers CRM service URL");
    } finally {

      /* Release connection */
      if (getMethod != null) {
        getMethod.releaseConnection();
      }
    }
  }

  /**
   * Executes SOAP message
   * 
   * @param endpointUrl SOAP endpoint
   * @param message SOAP request
   * @return SOAP response message
   * @throws SOAPException in case of a SOAP issue
   * @throws IOException in case of an IO issue
   */
  private final SOAPMessage execute(final String endpointUrl, final SOAPMessage message) throws Exception {
    SOAPConnection conn = null;
    try {

      /* Create new SOAP connection */
      conn = SOAPConnectionFactory.newInstance().createConnection();

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside execute, going to execute SOAP message : " + message.getSOAPBody().getTextContent());
      }

      /* Call SOAP service */
      final SOAPMessage response = conn.call(message, endpointUrl);

      /* Get SOAP response body */
      final SOAPBody body = response.getSOAPBody();

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside execute, has fault? :  " + body.hasFault());
      }

      if (body.hasFault() && body.getFault() != null) {

        logger.error("---Inside execute, fault :  " + body.getFault().getTextContent() + ", fault reason : " + body.getFault().getFaultString());

        /* Throw error */
        throw new SOAPException(body.getFault().getTextContent());
      }
      return response;
    } catch (final Exception e) {

      logger.error("---Inside execute, error recieved :  " + e.getMessage() + ", error : " + e);

      /* Throw user error */
      throw new SOAPException(e);
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * Constructs XPath query over the SOAP message
   * 
   * @param query XPath query
   * @param response SOAP message
   * @return XPath query
   * @throws SOAPException in case of SOAP issue
   * @throws JaxenException XPath problem
   */
  public final XPath createXPath(final String query, final SOAPMessage response) throws SOAPException, JaxenException {

    /* Uses DOM to XPath mapping */
    final XPath xpath = new DOMXPath(query);

    /* Define a namespaces used in response */
    final SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
    final SOAPPart sp = response.getSOAPPart();
    final SOAPEnvelope env = sp.getEnvelope();
    final SOAPBody bdy = env.getBody();

    /* Add namespaces from SOAP envelope */
    addNamespaces(nsContext, env);

    /* Add namespaces of top body element */
    final Iterator<?> bodyElements = bdy.getChildElements();
    while (bodyElements.hasNext()) {
      SOAPElement element = (SOAPElement) bodyElements.next();
      addNamespaces(nsContext, element);
    }
    xpath.setNamespaceContext(nsContext);
    return xpath;
  }

  /**
   * Namespace context resolver
   * 
   * @param context namespace context
   * @param element SOAP message element
   */
  public static final void addNamespaces(final SimpleNamespaceContext context, final SOAPElement element) {
    final Iterator<?> namespaces = element.getNamespacePrefixes();
    while (namespaces.hasNext()) {
      final String prefix = (String) namespaces.next();
      final String uri = element.getNamespaceURI(prefix);
      context.addNamespace(prefix, uri);
    }
  }
}
