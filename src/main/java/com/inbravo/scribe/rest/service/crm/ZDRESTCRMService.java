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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.internal.service.dto.CADUser;
import com.inbravo.scribe.rest.constants.HTTPConstants;
import com.inbravo.scribe.rest.resource.CADCommandObject;
import com.inbravo.scribe.rest.resource.CADObject;
import com.inbravo.scribe.rest.service.crm.zd.ZDCRMMessageFormatUtils;
import com.inbravo.scribe.rest.service.crm.zd.session.ZDCRMSessionManager;

/**
 * This class is the central service provider for Zendesk ticketing system
 * 
 * @author amit.dixit
 * 
 */
public final class ZDRESTCRMService extends CRMService {

  private final Logger logger = Logger.getLogger(ZDRESTCRMService.class.getName());

  private String crmFieldsSeparator;

  private String orderFieldsSeparator;

  private ZDCRMSessionManager zDCRMSessionManager;

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject) throws Exception {
    logger.debug("---Inside getObjects");

    /* Check if all record types are to be searched */
    if (cADCommandObject.getObjectType().trim().equalsIgnoreCase(HTTPConstants.anyObject)) {
      return this.searchAllTypeOfObjects(cADCommandObject, null, null, null);
    } else {
      GetMethod getMethod = null;
      try {

        String serviceURL = null;
        String serviceProtocol = null;
        String userId = null;
        String password = null;
        int crmPort = 80;

        /* Check if agent is present in request */
        if (cADCommandObject.getCrmUserId() != null) {

          /* Get agent from session manager */
          final CADUser agent = zDCRMSessionManager.getCrmUserIdWithCRMSessionInformation(cADCommandObject.getCrmUserId());

          /* Get CRM information from agent */
          serviceURL = agent.getCrmServiceURL();
          serviceProtocol = agent.getCrmServiceProtocol();
          userId = agent.getCrmUserId();
          password = agent.getCrmPassword();
          crmPort = agent.getCrmPort();
        }

        /* Create Zen desk URL */
        final String zenDeskURL = serviceProtocol + "://" + serviceURL + "/" + cADCommandObject.getObjectType() + "s.xml";

        logger.debug("---Inside getObjects zenDeskURL: " + zenDeskURL);

        /* Instantiate get method */
        getMethod = new GetMethod(zenDeskURL);

        /* Set request content type */
        getMethod.addRequestHeader("Content-Type", "application/xml");
        getMethod.addRequestHeader("accept", "application/xml");

        final HttpClient httpclient = new HttpClient();

        /* Set credentials */
        httpclient.getState().setCredentials(new AuthScope(serviceURL, crmPort), new UsernamePasswordCredentials(userId, password));

        /* Execute method */
        int result = httpclient.executeMethod(getMethod);
        logger.debug("---Inside getObjects response code: " + result + " & body: " + getMethod.getResponseBodyAsString());

        if (result == HttpStatus.SC_OK) {
          final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          final DocumentBuilder builder = factory.newDocumentBuilder();
          final Document document = builder.parse(getMethod.getResponseBodyAsStream());

          /* Create new XPath object to query XML document */
          final XPath xpath = XPathFactory.newInstance().newXPath();

          /* XPath Query for showing all nodes value */
          final XPathExpression expr = xpath.compile("/" + cADCommandObject.getObjectType() + "s/" + cADCommandObject.getObjectType());

          /* Get node list from response document */
          final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

          /* Create new CAD object list */
          final List<CADObject> cADbjectList = new ArrayList<CADObject>();

          /* Iterate over node list */
          for (int i = 0; i < nodeList.getLength(); i++) {

            /* Create list of elements */
            final List<Element> elementList = new ArrayList<Element>();

            /* Get node from node list */
            final Node node = nodeList.item(i);

            /* Create new CAD object */
            final CADObject cADbject = new CADObject();

            /* Check if node has child nodes */
            if (node.hasChildNodes()) {

              final NodeList subNodeList = node.getChildNodes();

              /* Iterate over sub node list and create elements */
              for (int j = 0; j < subNodeList.getLength(); j++) {

                final Node subNode = subNodeList.item(j);

                /* This trick is to avoid empty nodes */
                if (!subNode.getNodeName().contains("#text")) {

                  /* Create element from response */
                  final Element element = ZDCRMMessageFormatUtils.createMessageElement(subNode.getNodeName(), subNode.getTextContent());

                  /* Add element in list */
                  elementList.add(element);
                }
              }
            }
            /* Add all CRM fields */
            cADbject.setXmlContent(elementList);

            /* Add CAD object in list */
            cADbjectList.add(cADbject);
          }

          /* Check if no record found */
          if (cADbjectList.size() == 0) {
            throw new CADException(CADResponseCodes._1004 + "No records found at Zendesk");
          }

          /* Set the final object in command object */
          cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));
        } else if (result == HttpStatus.SC_FORBIDDEN) {
          throw new CADException(CADResponseCodes._1020 + "Query is forbidden by Zendesk CRM");
        } else if (result == HttpStatus.SC_BAD_REQUEST) {
          throw new CADException(CADResponseCodes._1003 + "Invalid request content");
        } else if (result == HttpStatus.SC_UNAUTHORIZED) {
          throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zendesk CRM");
        } else if (result == HttpStatus.SC_NOT_FOUND) {
          throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zendesk CRM");
        }
      } catch (final CADException exception) {
        throw exception;
      } catch (final ParserConfigurationException exception) {
        throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
      } catch (final SAXException exception) {
        throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
      } catch (final IOException exception) {
        throw new CADException(CADResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
      } finally {
        /* Release connection socket */
        if (getMethod != null) {
          getMethod.releaseConnection();
        }
      }
      return cADCommandObject;
    }
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query) throws Exception {
    logger.debug("---Inside getObjects query: " + query);

    /* Check if all record types are to be searched */
    if (cADCommandObject.getObjectType().trim().equalsIgnoreCase(HTTPConstants.anyObject)) {
      return this.searchAllTypeOfObjects(cADCommandObject, query, null, null);
    } else {
      throw new CADException(CADResponseCodes._1003
          + "Following operation is not supported by the CRM; Please search 'ANY' object type for searcing all record types");
    }
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select) throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select);

    /* Check if all record types are to be searched */
    if (cADCommandObject.getObjectType().trim().equalsIgnoreCase(HTTPConstants.anyObject)) {
      return this.searchAllTypeOfObjects(cADCommandObject, query, select, null);
    } else {
      throw new CADException(CADResponseCodes._1003
          + "Following operation is not supported by the CRM; Please search 'ANY' object type for searcing all record types");
    }
  }

  @Override
  public CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select, final String order)
      throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select + " & order: " + order);

    /* Check if all record types are to be searched */
    if (cADCommandObject.getObjectType().trim().equalsIgnoreCase(HTTPConstants.anyObject)) {
      if (order != null) {
        throw new CADException(CADResponseCodes._1003 + "Ordering is not supported");
      } else {
        return this.searchAllTypeOfObjects(cADCommandObject, query, select, null);
      }
    } else {
      throw new CADException(CADResponseCodes._1003
          + "Following operation is not supported by the CRM; Please search 'ANY' object type for searcing all record types");
    }
  }

  @Override
  public final CADCommandObject getObjectsCount(final CADCommandObject cADCommandObject) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the CRM");
  }

  @Override
  public final CADCommandObject getObjectsCount(final CADCommandObject cADCommandObject, final String query) throws Exception {
    throw new CADException(CADResponseCodes._1003 + " Following operation is not supported by the CRM");
  }

  @Override
  public final CADCommandObject updateObject(final CADCommandObject cADCommandObject) throws Exception {

    logger.debug("---Inside updateObject");
    PutMethod putMethod = null;
    try {
      String serviceURL = null;
      String serviceProtocol = null;
      String userId = null;
      String password = null;
      String sessionId = null;
      int crmPort = 80;

      /* Check if tenant is present in request */
      if (cADCommandObject.getCrmUserId() != null) {

        /* Get agent from session manager */
        final CADUser agent = zDCRMSessionManager.getCrmUserIdWithCRMSessionInformation(cADCommandObject.getCrmUserId());

        /* Get CRM information from agent */
        serviceURL = agent.getCrmServiceURL();
        serviceProtocol = agent.getCrmServiceProtocol();
        userId = agent.getCrmUserId();
        password = agent.getCrmPassword();
        sessionId = agent.getCrmSessionId();
        crmPort = agent.getCrmPort();
      }

      String crmObjectId = null;

      /* Check if XML content in request, is not null */
      if (cADCommandObject.getcADObject() != null && cADCommandObject.getcADObject().length == 1) {

        /* Get Id of CRM object */
        crmObjectId = ZDCRMMessageFormatUtils.getNodeValue("ID", cADCommandObject.getcADObject()[0]);

        if (crmObjectId == null) {
          /* Inform user about invalid request */
          throw new CADException(CADResponseCodes._1008 + "CRM object id is not present in request");
        }
      } else {
        /* Inform user about invalid request */
        throw new CADException(CADResponseCodes._1008 + "CRM object information is not present in request");
      }

      /* Create Zen desk URL */
      final String zenDeskURL = serviceProtocol + "://" + serviceURL + "/" + cADCommandObject.getObjectType() + "s/" + crmObjectId + ".xml";

      logger.debug("---Inside updateObject zenDeskURL: " + zenDeskURL);

      /* Instantiate put method */
      putMethod = new PutMethod(zenDeskURL);

      /* Set request content type */
      putMethod.addRequestHeader("Content-Type", "application/xml");
      putMethod.addRequestHeader("accept", "application/xml");

      /* Cookie is required to be set for session management */
      putMethod.addRequestHeader("Cookie", sessionId);

      /* Add request entity */
      final RequestEntity entity = new StringRequestEntity(ZDCRMMessageFormatUtils.getCreateRequestXML(cADCommandObject), null, null);
      putMethod.setRequestEntity(entity);

      final HttpClient httpclient = new HttpClient();

      /* Set credentials */
      httpclient.getState().setCredentials(new AuthScope(serviceURL, crmPort), new UsernamePasswordCredentials(userId, password));

      /* Execute method */
      int result = httpclient.executeMethod(putMethod);
      logger.debug("---Inside updateObject response code: " + result + " & body: " + putMethod.getResponseBodyAsString());

      /* Check if object is updated */
      if (result == HttpStatus.SC_OK || result == HttpStatus.SC_CREATED) {

        /* Return the original object */
        return cADCommandObject;
      } else if (result == HttpStatus.SC_BAD_REQUEST || result == HttpStatus.SC_METHOD_NOT_ALLOWED || result == HttpStatus.SC_NOT_ACCEPTABLE) {
        throw new CADException(CADResponseCodes._1003 + "Invalid request : "
            + ZDCRMMessageFormatUtils.getErrorFromResponse(putMethod.getResponseBodyAsStream()));
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {
        throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zendesk CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {
        throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zendesk CRM");
      } else if (result == HttpStatus.SC_MOVED_TEMPORARILY) {
        throw new CADException(CADResponseCodes._1004
            + "Requested data not found at Zendesk CRM : Seems like Zendesk Service URL/Protocol is not correct");
      }
    } catch (final CADException exception) {
      throw exception;
    } catch (final ParserConfigurationException exception) {
      throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
    } catch (final SAXException exception) {
      throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } finally {
      /* Release connection socket */
      if (putMethod != null) {
        putMethod.releaseConnection();
      }
    }
    return cADCommandObject;
  }

  @Override
  public final CADCommandObject createObject(final CADCommandObject cADCommandObject) throws Exception {
    logger.debug("---Inside createObject");
    PostMethod postMethod = null;
    try {

      String serviceURL = null;
      String serviceProtocol = null;
      String userId = null;
      String password = null;
      String sessionId = null;
      int crmPort = 80;

      /* Check if CrmUserId is present in request */
      if (cADCommandObject.getCrmUserId() != null) {

        /* Get agent from session manager */
        final CADUser agent = zDCRMSessionManager.getCrmUserIdWithCRMSessionInformation(cADCommandObject.getCrmUserId());

        /* Get CRM information from agent */
        serviceURL = agent.getCrmServiceURL();
        serviceProtocol = agent.getCrmServiceProtocol();
        userId = agent.getCrmUserId();
        password = agent.getCrmPassword();
        sessionId = agent.getCrmSessionId();
        crmPort = agent.getCrmPort();
      }

      /* Create Zen desk URL */
      final String zenDeskURL = serviceProtocol + "://" + serviceURL + "/" + cADCommandObject.getObjectType() + "s.xml";

      logger.debug("---Inside createObject zenDeskURL: " + zenDeskURL);

      /* Instantiate get method */
      postMethod = new PostMethod(zenDeskURL);

      /* Set request content type */
      postMethod.addRequestHeader("Content-Type", "application/xml");
      postMethod.addRequestHeader("accept", "application/xml");

      /* Cookie is required to be set for session management */
      postMethod.addRequestHeader("Cookie", sessionId);

      /* Add request entity */
      final RequestEntity entity = new StringRequestEntity(ZDCRMMessageFormatUtils.getCreateRequestXML(cADCommandObject), null, null);
      postMethod.setRequestEntity(entity);

      final HttpClient httpclient = new HttpClient();

      /* Set credentials */
      httpclient.getState().setCredentials(new AuthScope(serviceURL, crmPort), new UsernamePasswordCredentials(userId, password));

      /* Execute method */
      int result = httpclient.executeMethod(postMethod);
      logger.debug("---Inside createObject response code: " + result + " & body: " + postMethod.getResponseBodyAsString());

      /* Check if object is created */
      if (result == HttpStatus.SC_OK || result == HttpStatus.SC_CREATED) {

        /* Trick: ZD does not gives object in create response */
        /* TODO: Read the newly created ZD object using search */
        return cADCommandObject;
      } else if (result == HttpStatus.SC_BAD_REQUEST || result == HttpStatus.SC_METHOD_NOT_ALLOWED || result == HttpStatus.SC_NOT_ACCEPTABLE) {
        throw new CADException(CADResponseCodes._1003 + "Invalid request : "
            + ZDCRMMessageFormatUtils.getErrorFromResponse(postMethod.getResponseBodyAsStream()));
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {
        throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zendesk CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {
        throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zendesk CRM");
      } else if (result == HttpStatus.SC_MOVED_TEMPORARILY) {
        throw new CADException(CADResponseCodes._1004
            + "Requested data not found at Zendesk CRM : Seems like Zendesk Service URL/Protocol is not correct");
      }
    } catch (final CADException exception) {
      throw exception;
    } catch (final ParserConfigurationException exception) {
      throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
    } catch (final SAXException exception) {
      throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } finally {
      /* Release connection socket */
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
    return cADCommandObject;
  }

  @Override
  public final boolean deleteObject(final CADCommandObject cADCommandObject, final String idToBeDeleted) throws Exception {

    logger.debug("---Inside deleteObject");
    DeleteMethod deleteMethod = null;
    try {
      String serviceURL = null;
      String serviceProtocol = null;
      String userId = null;
      String password = null;
      String sessionId = null;
      int crmPort = 80;

      /* Check if CrmUserId is present in request */
      if (cADCommandObject.getCrmUserId() != null) {

        /* Get agent from session manager */
        final CADUser agent = zDCRMSessionManager.getCrmUserIdWithCRMSessionInformation(cADCommandObject.getCrmUserId());

        /* Get CRM information from agent */
        serviceURL = agent.getCrmServiceURL();
        serviceProtocol = agent.getCrmServiceProtocol();
        userId = agent.getCrmUserId();
        password = agent.getCrmPassword();
        sessionId = agent.getCrmSessionId();
        crmPort = agent.getCrmPort();
      }

      /* Check if id is available in request */
      if (idToBeDeleted == null) {
        /* Inform user about invalid request */
        throw new CADException(CADResponseCodes._1008 + "CRM object id for deletion purpose is not present in request");
      }

      /* Create Zen desk URL */
      final String zenDeskURL = serviceProtocol + "://" + serviceURL + "/" + cADCommandObject.getObjectType() + "s/" + idToBeDeleted.trim() + ".xml";

      logger.debug("---Inside deleteObject zenDeskURL: " + zenDeskURL);

      /* Instantiate delete method */
      deleteMethod = new DeleteMethod(zenDeskURL);

      /* Set request content type */
      deleteMethod.addRequestHeader("Content-Type", "application/xml");
      deleteMethod.addRequestHeader("accept", "application/xml");

      /* Cookie is required to be set for session management */
      deleteMethod.addRequestHeader("Cookie", sessionId);

      final HttpClient httpclient = new HttpClient();

      /* Set credentials */
      httpclient.getState().setCredentials(new AuthScope(serviceURL, crmPort), new UsernamePasswordCredentials(userId, password));

      /* Execute method */
      int result = httpclient.executeMethod(deleteMethod);
      logger.debug("---Inside deleteObject response code: " + result + " & body: " + deleteMethod.getResponseBodyAsString());

      /* Check if object is updated */
      if (result == HttpStatus.SC_OK || result == HttpStatus.SC_CREATED) {

        /* Return the original object */
        return true;
      } else if (result == HttpStatus.SC_BAD_REQUEST || result == HttpStatus.SC_METHOD_NOT_ALLOWED || result == HttpStatus.SC_NOT_ACCEPTABLE) {
        throw new CADException(CADResponseCodes._1003 + "Invalid request : "
            + ZDCRMMessageFormatUtils.getErrorFromResponse(deleteMethod.getResponseBodyAsStream()));
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {
        throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zendesk CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {
        throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zendesk CRM");
      } else if (result == HttpStatus.SC_MOVED_TEMPORARILY) {
        throw new CADException(CADResponseCodes._1004
            + "Requested data not found at Zendesk CRM : Seems like Zendesk Service URL/Protocol is not correct");
      }
    } catch (final CADException exception) {
      throw exception;
    } catch (final ParserConfigurationException exception) {
      throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
    } catch (final SAXException exception) {
      throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } finally {
      /* Release connection socket */
      if (deleteMethod != null) {
        deleteMethod.releaseConnection();
      }
    }
    return false;
  }

  /**
   * 
   * @param cADCommandObject
   * @param query
   * @param select
   * @param order
   * @return
   * @throws Exception
   */
  private final CADCommandObject searchAllTypeOfObjects(final CADCommandObject cADCommandObject, final String query, final String select,
      final String order) throws Exception {
    logger.debug("---Inside searchAllTypeOfObjects query: " + query + " & select: " + select + " & order: " + order);
    GetMethod getMethod = null;
    try {

      String serviceURL = null;
      String serviceProtocol = null;
      String userId = null;
      String password = null;
      String sessionId = null;
      int crmPort = 80;

      /* Check if CrmUserId is present in request */
      if (cADCommandObject.getCrmUserId() != null) {

        /* Get agent from session manager */
        final CADUser agent = zDCRMSessionManager.getCrmUserIdWithCRMSessionInformation(cADCommandObject.getCrmUserId());

        /* Get CRM information from agent */
        serviceURL = agent.getCrmServiceURL();
        serviceProtocol = agent.getCrmServiceProtocol();
        userId = agent.getCrmUserId();
        password = agent.getCrmPassword();
        sessionId = agent.getCrmSessionId();
        crmPort = agent.getCrmPort();
      }

      /* Create Zen desk URL */
      final String zenDeskURL = serviceProtocol + "://" + serviceURL + "/search.xml";

      logger.debug("---Inside searchAllTypeOfObjects zenDeskURL: " + zenDeskURL + " & sessionId: " + sessionId);

      /* Instantiate get method */
      getMethod = new GetMethod(zenDeskURL);

      /* Set request content type */
      getMethod.addRequestHeader("Content-Type", "application/xml");

      /* Cookie is required to be set in case of search */
      getMethod.addRequestHeader("Cookie", sessionId);

      final HttpClient httpclient = new HttpClient();

      /* Set credentials */
      httpclient.getState().setCredentials(new AuthScope(serviceURL, crmPort), new UsernamePasswordCredentials(userId, password));

      /* Check if user has not provided a valid query */
      if (ZDCRMMessageFormatUtils.validateQuery(query)) {
        getMethod.setQueryString(new NameValuePair[] {new NameValuePair("query", ZDCRMMessageFormatUtils.createZDQuery(query))});
      }

      /* Execute method */
      int result = httpclient.executeMethod(getMethod);
      logger.debug("---Inside searchAllTypeOfObjects response code: " + result + " & body: " + getMethod.getResponseBodyAsString());

      if (result == HttpStatus.SC_OK) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(getMethod.getResponseBodyAsStream());

        /* Create new XPath object to query XML document */
        final XPath xpath = XPathFactory.newInstance().newXPath();

        /* XPath Query for showing all nodes value */
        final XPathExpression expr = xpath.compile("/records/record");

        /* Get node list from resposne document */
        final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

        final List<CADObject> cADbjectList = new ArrayList<CADObject>();

        /* Get select field list */
        List<String> selectFieldList = null;

        /* Check if user is asking for all fields */
        if (select != null && !select.equalsIgnoreCase(HTTPConstants.allCRMObjectFields)) {
          selectFieldList = ZDCRMMessageFormatUtils.createSelectFieldList(select);
        }

        /* Iterate over node list */
        for (int i = 0; i < nodeList.getLength(); i++) {

          /* Get node from node list */
          final Node node = nodeList.item(i);

          /* Create new CAD object */
          final CADObject cADbject = new CADObject();

          /* Check if node has child nodes */
          if (node.hasChildNodes()) {

            final NodeList subNodeList = node.getChildNodes();

            /* Create list of elements */
            final List<Element> elementList = new ArrayList<Element>();

            for (int j = 0; j < subNodeList.getLength(); j++) {

              /* Get all subnodes */
              final Node subNode = subNodeList.item(j);

              /* This trick is to avoid empty nodes */
              if (!subNode.getNodeName().contains("#text")) {
                if (selectFieldList != null) {
                  /* Add only user requested fields */
                  if (selectFieldList.contains(subNode.getNodeName().trim().toUpperCase())) {
                    /* Create element from response */
                    final Element element = ZDCRMMessageFormatUtils.createMessageElement(subNode.getNodeName(), subNode.getTextContent());

                    /* Add element in list */
                    elementList.add(element);
                  }
                } else {
                  /* Create element from response */
                  final Element element = ZDCRMMessageFormatUtils.createMessageElement(subNode.getNodeName(), subNode.getTextContent());

                  /* Add element in list */
                  elementList.add(element);
                }
              }
            }

            /* Add all CRM fields */
            cADbject.setXmlContent(elementList);

            /* Add CAD object in list */
            cADbjectList.add(cADbject);
          }
        }
        /* Check if no record found */
        if (cADbjectList.size() == 0) {
          throw new CADException(CADResponseCodes._1004 + "No records found at Zendesk");
        }

        /* Set the final object in command object */
        cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));
      } else if (result == HttpStatus.SC_BAD_REQUEST || result == HttpStatus.SC_METHOD_NOT_ALLOWED || result == HttpStatus.SC_NOT_ACCEPTABLE) {
        throw new CADException(CADResponseCodes._1003 + "Invalid request : "
            + ZDCRMMessageFormatUtils.getErrorFromResponse(getMethod.getResponseBodyAsStream()));
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {
        throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zendesk CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {
        throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zendesk CRM");
      } else if (result == HttpStatus.SC_MOVED_TEMPORARILY) {
        throw new CADException(CADResponseCodes._1004
            + "Requested data not found at Zendesk CRM : Seems like Zendesk Service URL/Protocol is not correct");
      }
    } catch (final CADException exception) {
      throw exception;
    } catch (final ParserConfigurationException exception) {
      throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
    } catch (final SAXException exception) {
      throw new CADException(CADResponseCodes._1020 + "Recieved an invalid XML from Zendesk CRM", exception);
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } catch (final Exception e) {
      throw new CADException(CADResponseCodes._1000 + "Problem while communicating with Zendesk CRM", e);
    } finally {
      /* Release connection socket */
      if (getMethod != null) {
        getMethod.releaseConnection();
      }
    }
    return cADCommandObject;
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

  public final ZDCRMSessionManager getzDCRMSessionManager() {
    return zDCRMSessionManager;
  }

  public final void setzDCRMSessionManager(final ZDCRMSessionManager zDCRMSessionManager) {
    this.zDCRMSessionManager = zDCRMSessionManager;
  }
}
