package com.inbravo.cad.rest.service.crm;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.axis2.util.XMLChar;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.Tenant;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.resource.CADObject;
import com.inbravo.cad.rest.service.crm.session.CRMSessionManager;
import com.inbravo.cad.rest.service.crm.zh.ZHCRMMessageFormatUtils;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZHRESTCRMService extends CRMService {

  private final Logger logger = Logger.getLogger(ZHRESTCRMService.class.getName());

  private String crmFieldsSeparator;

  private String orderFieldsSeparator;

  private CRMSessionManager cRMSessionManager;

  private String spaceCharReplacement = "___";

  private String queryPhoneFieldConst = "ALL_PHONE_FIELD";

  private String zohoInputDateFormat = "yyyy-MM-dd HH:mm:ss";

  private String permittedDateFormats;

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject) throws Exception {
    logger.debug("---Inside getObjects");

    PostMethod postMethod = null;
    try {

      String serviceURL = null;
      String serviceProtocol = null;
      String sessionId = null;

      /* Check if tenant is present in request */
      if (eDSACommandObject.getTenant() != null) {

        /* Get tenant from session manager */
        final Tenant tenant = (Tenant) cRMSessionManager.getSessionInfo(eDSACommandObject.getTenant());

        /* Get CRM information from tenant */
        serviceURL = tenant.getCrmServiceURL();
        serviceProtocol = tenant.getCrmServiceProtocol();
        sessionId = tenant.getCrmSessionId();
      } else {
        /* Get agent from session manager */
        final CADUser agent = (CADUser) cRMSessionManager.getSessionInfo(eDSACommandObject.getAgent());

        /* Get CRM information from agent */
        serviceURL = agent.getCrmServiceURL();
        serviceProtocol = agent.getCrmServiceProtocol();
        sessionId = agent.getCrmSessionId();
      }

      /* Create Zoho URL */
      final String zohoURL = serviceProtocol + "://" + serviceURL + "/crm/private/xml/" + eDSACommandObject.getObjectType() + "s/getRecords";

      logger.debug("---Inside getObjects zohoURL: " + zohoURL + " & sessionId: " + sessionId);

      /* Instantiate post method */
      postMethod = new PostMethod(zohoURL);

      /* Set request parameters */
      postMethod.setParameter("authtoken", sessionId.trim());
      postMethod.setParameter("scope", "crmapi");

      final HttpClient httpclient = new HttpClient();

      /* Execute method */
      int result = httpclient.executeMethod(postMethod);
      logger.debug("---Inside getObjects response code: " + result + " & body: " + postMethod.getResponseBodyAsString());

      /* Check if response if SUCCESS */
      if (result == HttpStatus.SC_OK) {

        /* Create XML document from response */
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(postMethod.getResponseBodyAsStream());

        /* Create new XPath object to query XML document */
        final XPath xpath = XPathFactory.newInstance().newXPath();

        /* XPath Query for showing all nodes value */
        final XPathExpression expr = xpath.compile("/response/result/" + eDSACommandObject.getObjectType() + "s/row");

        /* Get node list from response document */
        final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

        /* Check if records founds */
        if (nodeList != null && nodeList.getLength() == 0) {

          /* XPath Query for showing error message */
          XPathExpression errorExpression = xpath.compile("/response/error/message");

          /* Get erroe message from response document */
          Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

          /* Check if error message is found */
          if (errorMessage != null) {

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
          } else {

            /* XPath Query for showing error message */
            errorExpression = xpath.compile("/response/nodata/message");

            /* Get erroe message from response document */
            errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
          }
        } else {

          /* Create new EDSA object list */
          final List<CADObject> eDSAObjectList = new ArrayList<CADObject>();

          /* Iterate over node list */
          for (int i = 0; i < nodeList.getLength(); i++) {

            /* Create list of elements */
            final List<Element> elementList = new ArrayList<Element>();

            /* Get node from node list */
            final Node node = nodeList.item(i);

            /* Create new EDSA object */
            final CADObject eDSAObject = new CADObject();

            /* Check if node has child nodes */
            if (node.hasChildNodes()) {

              final NodeList subNodeList = node.getChildNodes();

              /* Create new map for attributes */
              final Map<String, String> attributeMap = new HashMap<String, String>();

              /* Iterate over sub node list and create elements */
              for (int j = 0; j < subNodeList.getLength(); j++) {

                final Node subNode = subNodeList.item(j);

                /* This trick is to avoid empty nodes */
                if (!subNode.getNodeName().contains("#text")) {

                  /* Create element from response */
                  final Element element = (Element) subNode;

                  /* Populate label map */
                  attributeMap.put("label", element.getAttribute("val"));

                  /* Get node name */
                  final String nodeName = element.getAttribute("val").replace(" ", spaceCharReplacement);

                  /* Validate the node name */
                  if (XMLChar.isValidName(nodeName)) {

                    /* Add element in list */
                    elementList.add(ZHCRMMessageFormatUtils.createMessageElement(nodeName, element.getTextContent(), attributeMap));
                  } else {
                    logger.debug("---Inside getObjects, found invalid XML node; ignoring field: " + element.getAttribute("val"));
                  }
                }
              }
            }
            /* Add all CRM fields */
            eDSAObject.setXmlContent(elementList);

            /* Set type information in object */
            eDSAObject.setObjectType(eDSACommandObject.getObjectType());

            /* Add EDSA object in list */
            eDSAObjectList.add(eDSAObject);
          }

          /* Check if no record found */
          if (eDSAObjectList.size() == 0) {
            throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM");
          }

          /* Set the final object in command object */
          eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
        }
      } else if (result == HttpStatus.SC_FORBIDDEN) {
        throw new CADException(CADResponseCodes._1022 + "Query is forbidden by Zoho CRM");
      } else if (result == HttpStatus.SC_BAD_REQUEST) {
        throw new CADException(CADResponseCodes._1003 + "Invalid request content");
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {
        throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zoho CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {
        throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
      } else if (result == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

        /* Create XML document from response */
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(postMethod.getResponseBodyAsStream());

        /* Create new XPath object to query XML document */
        final XPath xpath = XPathFactory.newInstance().newXPath();

        /* XPath Query for showing error message */
        final XPathExpression errorExpression = xpath.compile("/response/error/message");

        /* Get erroe message from response document */
        final Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

        if (errorMessage != null) {

          /* Send user error */
          throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM : " + errorMessage.getTextContent());
        } else {

          /* Send user error */
          throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
        }
      }
    } catch (final CADException exception) {
      throw exception;
    } catch (final ParserConfigurationException exception) {
      throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
    } catch (final SAXException exception) {
      throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1022 + "Communication error while communicating with Zoho CRM");
    } finally {
      /* Release connection socket */
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
    return eDSACommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query) throws Exception {
    logger.debug("---Inside getObjects, query: " + query);

    /* Transfer the call to second method */
    if (query != null && query.toUpperCase().startsWith(queryPhoneFieldConst.toUpperCase())) {

      CADCommandObject returnObject = null;

      try {
        /* Query CRM object by Phone field */
        returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, null, null, "Phone");

      } catch (final CADException firstE) {

        /* Check if record is not found */
        if (firstE.getMessage().contains(CADResponseCodes._1004)) {

          try {
            /* Query CRM object by Mobile field */
            returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, null, null, "Mobile");
          } catch (final CADException secondE) {

            /* Check if record is again not found */
            if (secondE.getMessage().contains(CADResponseCodes._1004)) {

              try {
                /* Query CRM object by Home Phone field */
                returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, null, null, "Home Phone");
              } catch (final CADException thirdE) {

                /* Check if record is again not found */
                if (thirdE.getMessage().contains(CADResponseCodes._1004)) {

                  try {
                    /* Query CRM object by Other Phone field */
                    returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, null, null, "Other Phone");
                  } catch (final CADException fourthE) {

                    /* Throw the error to user */
                    throw fourthE;
                  }
                }
              }
            }
          }
        }
      }

      return returnObject;
    } else {

      PostMethod postMethod = null;
      try {

        String serviceURL = null;
        String serviceProtocol = null;
        String sessionId = null;

        /* Check if tenant is present in request */
        if (eDSACommandObject.getTenant() != null) {

          /* Get tenant from session manager */
          final Tenant tenant = (Tenant) cRMSessionManager.getSessionInfo(eDSACommandObject.getTenant());

          /* Get CRM information from tenant */
          serviceURL = tenant.getCrmServiceURL();
          serviceProtocol = tenant.getCrmServiceProtocol();
          sessionId = tenant.getCrmSessionId();
        } else {
          /* Get agent from session manager */
          final CADUser agent = (CADUser) cRMSessionManager.getSessionInfo(eDSACommandObject.getAgent());

          /* Get CRM information from agent */
          serviceURL = agent.getCrmServiceURL();
          serviceProtocol = agent.getCrmServiceProtocol();
          sessionId = agent.getCrmSessionId();
        }

        /* Create Zoho URL */
        final String zohoURL = serviceProtocol + "://" + serviceURL + "/crm/private/xml/" + eDSACommandObject.getObjectType() + "s/getSearchRecords";

        logger.debug("---Inside getObjects zohoURL: " + zohoURL + " & sessionId: " + sessionId);

        /* Instantiate post method */
        postMethod = new PostMethod(zohoURL);

        /* Set request parameters */
        postMethod.setParameter("authtoken", sessionId.trim());
        postMethod.setParameter("scope", "crmapi");

        if (!query.equalsIgnoreCase("NONE")) {

          /* Create ZH query */
          final String zhQuery = ZHCRMMessageFormatUtils.createZHQuery(query);

          if (zhQuery != null && !"".equals(zhQuery)) {

            /* Set search parameter in request */
            postMethod.setParameter("searchCondition", "(" + zhQuery + ")");
          }
        } else {

          /* Without query param this method is not applicable */
          return this.getObjects(eDSACommandObject);
        }

        /* Set request param to select all fields */
        postMethod.setParameter("selectColumns", "All");

        final HttpClient httpclient = new HttpClient();

        /* Execute method */
        int result = httpclient.executeMethod(postMethod);
        logger.debug("---Inside getObjects response code: " + result + " & body: " + postMethod.getResponseBodyAsString());

        /* Check if response if SUCCESS */
        if (result == HttpStatus.SC_OK) {

          /* Create XML document from response */
          final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          final DocumentBuilder builder = factory.newDocumentBuilder();
          final Document document = builder.parse(postMethod.getResponseBodyAsStream());

          /* Create new XPath object to query XML document */
          final XPath xpath = XPathFactory.newInstance().newXPath();

          /* XPath Query for showing all nodes value */
          final XPathExpression expr = xpath.compile("/response/result/" + eDSACommandObject.getObjectType() + "s/row");

          /* Get node list from response document */
          final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

          /* Check if records founds */
          if (nodeList != null && nodeList.getLength() == 0) {

            /* XPath Query for showing error message */
            XPathExpression errorExpression = xpath.compile("/response/error/message");

            /* Get erroe message from response document */
            Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

            /* Check if error message is found */
            if (errorMessage != null) {

              /* Send user error */
              throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
            } else {

              /* XPath Query for showing error message */
              errorExpression = xpath.compile("/response/nodata/message");

              /* Get erroe message from response document */
              errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

              /* Send user error */
              throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
            }

          } else {

            /* Create new EDSA object list */
            final List<CADObject> eDSAObjectList = new ArrayList<CADObject>();

            /* Iterate over node list */
            for (int i = 0; i < nodeList.getLength(); i++) {

              /* Create list of elements */
              final List<Element> elementList = new ArrayList<Element>();

              /* Get node from node list */
              final Node node = nodeList.item(i);

              /* Create new EDSA object */
              final CADObject eDSAObject = new CADObject();

              /* Check if node has child nodes */
              if (node.hasChildNodes()) {

                final NodeList subNodeList = node.getChildNodes();

                /* Create new map for attributes */
                final Map<String, String> attributeMap = new HashMap<String, String>();

                /*
                 * Iterate over sub node list and create elements
                 */
                for (int j = 0; j < subNodeList.getLength(); j++) {

                  final Node subNode = subNodeList.item(j);

                  /* This trick is to avoid empty nodes */
                  if (!subNode.getNodeName().contains("#text")) {

                    /* Create element from response */
                    final Element element = (Element) subNode;

                    /* Populate label map */
                    attributeMap.put("label", element.getAttribute("val"));

                    /* Get node name */
                    final String nodeName = element.getAttribute("val").replace(" ", spaceCharReplacement);

                    /* Validate the node name */
                    if (XMLChar.isValidName(nodeName)) {

                      /* Add element in list */
                      elementList.add(ZHCRMMessageFormatUtils.createMessageElement(nodeName, element.getTextContent(), attributeMap));
                    } else {
                      logger.debug("---Inside getObjects, found invalid XML node; ignoring field: " + element.getAttribute("val"));
                    }
                  }
                }
              }
              /* Add all CRM fields */
              eDSAObject.setXmlContent(elementList);

              /* Set type information in object */
              eDSAObject.setObjectType(eDSACommandObject.getObjectType());

              /* Add EDSA object in list */
              eDSAObjectList.add(eDSAObject);
            }

            /* Check if no record found */
            if (eDSAObjectList.size() == 0) {
              throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM");
            }

            /* Set the final object in command object */
            eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
          }
        } else if (result == HttpStatus.SC_FORBIDDEN) {
          throw new CADException(CADResponseCodes._1022 + "Query is forbidden by Zoho CRM");
        } else if (result == HttpStatus.SC_BAD_REQUEST) {
          throw new CADException(CADResponseCodes._1003 + "Invalid request content");
        } else if (result == HttpStatus.SC_UNAUTHORIZED) {
          throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zoho CRM");
        } else if (result == HttpStatus.SC_NOT_FOUND) {
          throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
        } else if (result == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

          /* Create XML document from response */
          final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          final DocumentBuilder builder = factory.newDocumentBuilder();
          final Document document = builder.parse(postMethod.getResponseBodyAsStream());

          /* Create new XPath object to query XML document */
          final XPath xpath = XPathFactory.newInstance().newXPath();

          /* XPath Query for showing error message */
          final XPathExpression errorExpression = xpath.compile("/response/error/message");

          /* Get erroe message from response document */
          final Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

          if (errorMessage != null) {

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM : " + errorMessage.getTextContent());
          } else {

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
          }
        }
      } catch (final CADException exception) {
        throw exception;
      } catch (final ParserConfigurationException exception) {
        throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
      } catch (final SAXException exception) {
        throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
      } catch (final IOException exception) {
        throw new CADException(CADResponseCodes._1022 + "Communication error while communicating with Zoho CRM");
      } finally {
        /* Release connection socket */
        if (postMethod != null) {
          postMethod.releaseConnection();
        }
      }
      return eDSACommandObject;
    }
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select) throws Exception {
    logger.debug("---Inside getObjects, query: " + query + " & select: " + select);

    /* Transfer the call to second method */
    if (query != null && query.toUpperCase().startsWith(queryPhoneFieldConst.toUpperCase())) {

      CADCommandObject returnObject = null;

      try {
        /* Query CRM object by Phone field */
        returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, select, null, "Phone");

      } catch (final CADException firstE) {

        /* Check if record is not found */
        if (firstE.getMessage().contains(CADResponseCodes._1004)) {

          try {
            /* Query CRM object by Mobile field */
            returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, select, null, "Mobile");
          } catch (final CADException secondE) {

            /* Check if record is again not found */
            if (secondE.getMessage().contains(CADResponseCodes._1004)) {

              try {
                /* Query CRM object by Home Phone field */
                returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, select, null, "Home Phone");
              } catch (final CADException thirdE) {

                /* Check if record is again not found */
                if (thirdE.getMessage().contains(CADResponseCodes._1004)) {

                  try {
                    /* Query CRM object by Other Phone field */
                    returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, select, null, "Other Phone");
                  } catch (final CADException fourthE) {

                    /* Throw the error to user */
                    throw fourthE;
                  }
                }
              }
            }
          }
        }
      }

      return returnObject;
    } else {

      PostMethod postMethod = null;
      try {

        String serviceURL = null;
        String serviceProtocol = null;
        String sessionId = null;

        /* Check if tenant is present in request */
        if (eDSACommandObject.getTenant() != null) {

          /* Get tenant from session manager */
          final Tenant tenant = (Tenant) cRMSessionManager.getSessionInfo(eDSACommandObject.getTenant());

          /* Get CRM information from tenant */
          serviceURL = tenant.getCrmServiceURL();
          serviceProtocol = tenant.getCrmServiceProtocol();
          sessionId = tenant.getCrmSessionId();
        } else {
          /* Get agent from session manager */
          final CADUser agent = (CADUser) cRMSessionManager.getSessionInfo(eDSACommandObject.getAgent());

          /* Get CRM information from agent */
          serviceURL = agent.getCrmServiceURL();
          serviceProtocol = agent.getCrmServiceProtocol();
          sessionId = agent.getCrmSessionId();
        }

        /* Create Zoho URL */
        final String zohoURL = serviceProtocol + "://" + serviceURL + "/crm/private/xml/" + eDSACommandObject.getObjectType() + "s/getSearchRecords";

        logger.debug("---Inside getObjects zohoURL: " + zohoURL + " & sessionId: " + sessionId);

        /* Instantiate post method */
        postMethod = new PostMethod(zohoURL);

        /* Set request parameters */
        postMethod.setParameter("authtoken", sessionId.trim());
        postMethod.setParameter("scope", "crmapi");

        if (!query.equalsIgnoreCase("NONE")) {

          /* Create ZH query */
          final String zhQuery = ZHCRMMessageFormatUtils.createZHQuery(query);

          if (zhQuery != null && !"".equals(zhQuery)) {

            /* Set search parameter in request */
            postMethod.setParameter("searchCondition", "(" + zhQuery + ")");
          }
        } else {

          /* Without query param this method is not applicable */
          return this.getObjects(eDSACommandObject);
        }

        if (!select.equalsIgnoreCase("ALL")) {

          /* Create ZH select CRM fields information */
          final String zhSelect = ZHCRMMessageFormatUtils.createZHSelect(eDSACommandObject, select);

          /* Validate query */
          if (zhSelect != null && !"".equals(zhSelect)) {

            /* Set request param to select fields */
            postMethod.setParameter("selectColumns", zhSelect);
          }
        } else {

          /* Set request param to select all fields */
          postMethod.setParameter("selectColumns", "All");
        }

        final HttpClient httpclient = new HttpClient();

        /* Execute method */
        int result = httpclient.executeMethod(postMethod);
        logger.debug("---Inside getObjects response code: " + result + " & body: " + postMethod.getResponseBodyAsString());

        /* Check if response if SUCCESS */
        if (result == HttpStatus.SC_OK) {

          /* Create XML document from response */
          final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          final DocumentBuilder builder = factory.newDocumentBuilder();
          final Document document = builder.parse(postMethod.getResponseBodyAsStream());

          /* Create new XPath object to query XML document */
          final XPath xpath = XPathFactory.newInstance().newXPath();

          /* XPath Query for showing all nodes value */
          final XPathExpression expr = xpath.compile("/response/result/" + eDSACommandObject.getObjectType() + "s/row");

          /* Get node list from response document */
          final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

          /* Check if records founds */
          if (nodeList != null && nodeList.getLength() == 0) {

            /* XPath Query for showing error message */
            XPathExpression errorExpression = xpath.compile("/response/error/message");

            /* Get erroe message from response document */
            Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

            /* Check if error message is found */
            if (errorMessage != null) {

              /* Send user error */
              throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
            } else {

              /* XPath Query for showing error message */
              errorExpression = xpath.compile("/response/nodata/message");

              /* Get erroe message from response document */
              errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

              /* Send user error */
              throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
            }
          } else {
            /* Create new EDSA object list */
            final List<CADObject> eDSAObjectList = new ArrayList<CADObject>();

            /* Iterate over node list */
            for (int i = 0; i < nodeList.getLength(); i++) {

              /* Create list of elements */
              final List<Element> elementList = new ArrayList<Element>();

              /* Get node from node list */
              final Node node = nodeList.item(i);

              /* Create new EDSA object */
              final CADObject eDSAObject = new CADObject();

              /* Check if node has child nodes */
              if (node.hasChildNodes()) {

                final NodeList subNodeList = node.getChildNodes();

                /* Create new map for attributes */
                final Map<String, String> attributeMap = new HashMap<String, String>();

                /*
                 * Iterate over sub node list and create elements
                 */
                for (int j = 0; j < subNodeList.getLength(); j++) {

                  final Node subNode = subNodeList.item(j);

                  /* This trick is to avoid empty nodes */
                  if (!subNode.getNodeName().contains("#text")) {

                    /* Create element from response */
                    final Element element = (Element) subNode;

                    /* Populate label map */
                    attributeMap.put("label", element.getAttribute("val"));

                    /* Get node name */
                    final String nodeName = element.getAttribute("val").replace(" ", spaceCharReplacement);

                    /* Validate the node name */
                    if (XMLChar.isValidName(nodeName)) {

                      /* Add element in list */
                      elementList.add(ZHCRMMessageFormatUtils.createMessageElement(nodeName, element.getTextContent(), attributeMap));
                    } else {
                      logger.debug("---Inside getObjects, found invalid XML node; ignoring field: " + element.getAttribute("val"));
                    }
                  }
                }
              }
              /* Add all CRM fields */
              eDSAObject.setXmlContent(elementList);

              /* Set type information in object */
              eDSAObject.setObjectType(eDSACommandObject.getObjectType());

              /* Add EDSA object in list */
              eDSAObjectList.add(eDSAObject);
            }

            /* Check if no record found */
            if (eDSAObjectList.size() == 0) {
              throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM");
            }

            /* Set the final object in command object */
            eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
          }
        } else if (result == HttpStatus.SC_FORBIDDEN) {
          throw new CADException(CADResponseCodes._1022 + "Query is forbidden by Zoho CRM");
        } else if (result == HttpStatus.SC_BAD_REQUEST) {
          throw new CADException(CADResponseCodes._1003 + "Invalid request content");
        } else if (result == HttpStatus.SC_UNAUTHORIZED) {
          throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zoho CRM");
        } else if (result == HttpStatus.SC_NOT_FOUND) {
          throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
        } else if (result == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

          /* Create XML document from response */
          final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          final DocumentBuilder builder = factory.newDocumentBuilder();
          final Document document = builder.parse(postMethod.getResponseBodyAsStream());

          /* Create new XPath object to query XML document */
          final XPath xpath = XPathFactory.newInstance().newXPath();

          /* XPath Query for showing error message */
          final XPathExpression errorExpression = xpath.compile("/response/error/message");

          /* Get erroe message from response document */
          final Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

          if (errorMessage != null) {

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM : " + errorMessage.getTextContent());
          } else {

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
          }
        }
      } catch (final CADException exception) {
        throw exception;
      } catch (final ParserConfigurationException exception) {
        throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
      } catch (final SAXException exception) {
        throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
      } catch (final IOException exception) {
        throw new CADException(CADResponseCodes._1022 + "Communication error while communicating with Zoho CRM");
      } finally {
        /* Release connection socket */
        if (postMethod != null) {
          postMethod.releaseConnection();
        }
      }

      return eDSACommandObject;
    }
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select, final String order)
      throws Exception {
    logger.debug("---Inside getObjects, query: " + query + " & select: " + select + " & order: " + order);

    /* Transfer the call to second method */
    if (query != null && query.toUpperCase().startsWith(queryPhoneFieldConst.toUpperCase())) {

      CADCommandObject returnObject = null;

      try {
        /* Query CRM object by Phone field */
        returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, select, order, "Phone");

      } catch (final CADException firstE) {

        /* Check if record is not found */
        if (firstE.getMessage().contains(CADResponseCodes._1004)) {

          try {
            /* Query CRM object by Home Phone field */
            returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, select, order, "Mobile Phone");
          } catch (final CADException secondE) {

            /* Check if record is again not found */
            if (secondE.getMessage().contains(CADResponseCodes._1004)) {

              try {
                /* Query CRM object by Home Phone field */
                returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, select, order, "Home Phone");
              } catch (final CADException thirdE) {

                /* Check if record is again not found */
                if (thirdE.getMessage().contains(CADResponseCodes._1004)) {

                  try {
                    /* Query CRM object by Home Phone field */
                    returnObject = this.getObjectsByPhoneField(eDSACommandObject, query, select, order, "Other Phone");
                  } catch (final CADException fourthE) {

                    /* Throw the error to user */
                    throw fourthE;
                  }
                }
              }
            }
          }
        }
      }

      return returnObject;
    } else {

      PostMethod postMethod = null;
      try {

        String serviceURL = null;
        String serviceProtocol = null;
        String sessionId = null;

        /* Check if tenant is present in request */
        if (eDSACommandObject.getTenant() != null) {

          /* Get tenant from session manager */
          final Tenant tenant = (Tenant) cRMSessionManager.getSessionInfo(eDSACommandObject.getTenant());

          /* Get CRM information from tenant */
          serviceURL = tenant.getCrmServiceURL();
          serviceProtocol = tenant.getCrmServiceProtocol();
          sessionId = tenant.getCrmSessionId();
        } else {
          /* Get agent from session manager */
          final CADUser agent = (CADUser) cRMSessionManager.getSessionInfo(eDSACommandObject.getAgent());

          /* Get CRM information from agent */
          serviceURL = agent.getCrmServiceURL();
          serviceProtocol = agent.getCrmServiceProtocol();
          sessionId = agent.getCrmSessionId();
        }

        /* Create Zoho URL */
        final String zohoURL = serviceProtocol + "://" + serviceURL + "/crm/private/xml/" + eDSACommandObject.getObjectType() + "s/getSearchRecords";

        logger.debug("---Inside getObjects zohoURL: " + zohoURL + " & sessionId: " + sessionId);

        /* Instantiate post method */
        postMethod = new PostMethod(zohoURL);

        /* Set request parameters */
        postMethod.setParameter("authtoken", sessionId.trim());
        postMethod.setParameter("scope", "crmapi");

        if (!query.equalsIgnoreCase("NONE")) {

          /* Create ZH query */
          final String zhQuery = ZHCRMMessageFormatUtils.createZHQuery(query);

          if (zhQuery != null && !"".equals(zhQuery)) {

            /* Set search parameter in request */
            postMethod.setParameter("searchCondition", "(" + zhQuery + ")");
          }
        } else {

          /* Without query param this method is not applicable */
          return this.getObjects(eDSACommandObject);
        }

        if (!select.equalsIgnoreCase("ALL")) {

          /* Create ZH select CRM fields information */
          final String zhSelect = ZHCRMMessageFormatUtils.createZHSelect(eDSACommandObject, select);

          /* Validate query */
          if (zhSelect != null && !"".equals(zhSelect)) {

            /* Set request param to select fields */
            postMethod.setParameter("selectColumns", zhSelect);
          }
        } else {

          /* Set request param to select all fields */
          postMethod.setParameter("selectColumns", "All");
        }

        /* Validate query */
        if (order != null && !"".equals(order)) {

          /* Validate ordering information */
          ZHCRMMessageFormatUtils.parseAndValidateOrderClause(order, orderFieldsSeparator);

          /* Set request param to select fields */
          postMethod.setParameter("sortColumnString", ZHCRMMessageFormatUtils.createZHSortColumnString(order, orderFieldsSeparator));

          /* Set request param to select fields */
          postMethod.setParameter("sortOrderString", ZHCRMMessageFormatUtils.createZHSortOrderString(order, orderFieldsSeparator));
        }

        final HttpClient httpclient = new HttpClient();

        /* Execute method */
        int result = httpclient.executeMethod(postMethod);
        logger.debug("---Inside getObjects response code: " + result + " & body: " + postMethod.getResponseBodyAsString());

        /* Check if response if SUCCESS */
        if (result == HttpStatus.SC_OK) {

          /* Create XML document from response */
          final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          final DocumentBuilder builder = factory.newDocumentBuilder();
          final Document document = builder.parse(postMethod.getResponseBodyAsStream());

          /* Create new XPath object to query XML document */
          final XPath xpath = XPathFactory.newInstance().newXPath();

          /* XPath Query for showing all nodes value */
          final XPathExpression expr = xpath.compile("/response/result/" + eDSACommandObject.getObjectType() + "s/row");

          /* Get node list from response document */
          final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

          /* Check if records founds */
          if (nodeList != null && nodeList.getLength() == 0) {

            /* XPath Query for showing error message */
            XPathExpression errorExpression = xpath.compile("/response/error/message");

            /* Get erroe message from response document */
            Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

            /* Check if error message is found */
            if (errorMessage != null) {

              /* Send user error */
              throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
            } else {

              /* XPath Query for showing error message */
              errorExpression = xpath.compile("/response/nodata/message");

              /* Get erroe message from response document */
              errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

              /* Send user error */
              throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
            }
          } else {

            /* Create new EDSA object list */
            final List<CADObject> eDSAObjectList = new ArrayList<CADObject>();

            /* Iterate over node list */
            for (int i = 0; i < nodeList.getLength(); i++) {

              /* Create list of elements */
              final List<Element> elementList = new ArrayList<Element>();

              /* Get node from node list */
              final Node node = nodeList.item(i);

              /* Create new EDSA object */
              final CADObject eDSAObject = new CADObject();

              /* Check if node has child nodes */
              if (node.hasChildNodes()) {

                final NodeList subNodeList = node.getChildNodes();

                /* Create new map for attributes */
                final Map<String, String> attributeMap = new HashMap<String, String>();

                /*
                 * Iterate over sub node list and create elements
                 */
                for (int j = 0; j < subNodeList.getLength(); j++) {

                  final Node subNode = subNodeList.item(j);

                  /* This trick is to avoid empty nodes */
                  if (!subNode.getNodeName().contains("#text")) {

                    /* Create element from response */
                    final Element element = (Element) subNode;

                    /* Populate label map */
                    attributeMap.put("label", element.getAttribute("val"));

                    /* Get node name */
                    final String nodeName = element.getAttribute("val").replace(" ", spaceCharReplacement);

                    /* Validate the node name */
                    if (XMLChar.isValidName(nodeName)) {

                      /* Add element in list */
                      elementList.add(ZHCRMMessageFormatUtils.createMessageElement(nodeName, element.getTextContent(), attributeMap));
                    } else {
                      logger.debug("---Inside getObjects, found invalid XML node; ignoring field: " + element.getAttribute("val"));
                    }
                  }
                }
              }

              /* Add all CRM fields */
              eDSAObject.setXmlContent(elementList);

              /* Set type information in object */
              eDSAObject.setObjectType(eDSACommandObject.getObjectType());

              /* Add EDSA object in list */
              eDSAObjectList.add(eDSAObject);
            }

            /* Check if no record found */
            if (eDSAObjectList.size() == 0) {
              throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM");
            }

            /* Set the final object in command object */
            eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
          }
        } else if (result == HttpStatus.SC_FORBIDDEN) {
          throw new CADException(CADResponseCodes._1022 + "Query is forbidden by Zoho CRM");
        } else if (result == HttpStatus.SC_BAD_REQUEST) {
          throw new CADException(CADResponseCodes._1003 + "Invalid request content");
        } else if (result == HttpStatus.SC_UNAUTHORIZED) {
          throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zoho CRM");
        } else if (result == HttpStatus.SC_NOT_FOUND) {
          throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
        } else if (result == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

          /* Create XML document from response */
          final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          final DocumentBuilder builder = factory.newDocumentBuilder();
          final Document document = builder.parse(postMethod.getResponseBodyAsStream());

          /* Create new XPath object to query XML document */
          final XPath xpath = XPathFactory.newInstance().newXPath();

          /* XPath Query for showing error message */
          final XPathExpression errorExpression = xpath.compile("/response/error/message");

          /* Get erroe message from response document */
          final Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

          if (errorMessage != null) {

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM : " + errorMessage.getTextContent());
          } else {

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
          }
        }
      } catch (final CADException exception) {
        throw exception;
      } catch (final ParserConfigurationException exception) {
        throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
      } catch (final SAXException exception) {
        throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
      } catch (final IOException exception) {
        throw new CADException(CADResponseCodes._1022 + "Communication error while communicating with Zoho CRM");
      } finally {
        /* Release connection socket */
        if (postMethod != null) {
          postMethod.releaseConnection();
        }
      }

      return eDSACommandObject;
    }
  }

  /**
   * 
   * @param eDSACommandObject
   * @param query
   * @param select
   * @param order
   * @return
   * @throws Exception
   */
  private final CADCommandObject getObjectsByPhoneField(final CADCommandObject eDSACommandObject, final String query, final String select,
      final String order, final String phoneFieldName) throws Exception {
    logger.debug("---Inside getObjectsByAllPhoneNumbers, query: " + query + " & select: " + select + " & order: " + order + " & phoneFieldName: "
        + phoneFieldName);

    PostMethod postMethod = null;
    try {

      String serviceURL = null;
      String serviceProtocol = null;
      String sessionId = null;

      /* Check if tenant is present in request */
      if (eDSACommandObject.getTenant() != null) {

        /* Get tenant from session manager */
        final Tenant tenant = (Tenant) cRMSessionManager.getSessionInfo(eDSACommandObject.getTenant());

        /* Get CRM information from tenant */
        serviceURL = tenant.getCrmServiceURL();
        serviceProtocol = tenant.getCrmServiceProtocol();
        sessionId = tenant.getCrmSessionId();
      } else {
        /* Get agent from session manager */
        final CADUser agent = (CADUser) cRMSessionManager.getSessionInfo(eDSACommandObject.getAgent());

        /* Get CRM information from agent */
        serviceURL = agent.getCrmServiceURL();
        serviceProtocol = agent.getCrmServiceProtocol();
        sessionId = agent.getCrmSessionId();
      }

      /* Create Zoho URL */
      final String zohoURL = serviceProtocol + "://" + serviceURL + "/crm/private/xml/" + eDSACommandObject.getObjectType() + "s/getSearchRecords";

      logger.debug("---Inside getObjectsByAllPhoneNumbers zohoURL: " + zohoURL + " & sessionId: " + sessionId);

      /* Instantiate post method */
      postMethod = new PostMethod(zohoURL);

      /* Set request parameters */
      postMethod.setParameter("authtoken", sessionId.trim());
      postMethod.setParameter("scope", "crmapi");

      if (!query.equalsIgnoreCase("NONE")) {

        /* Create ZH query */
        final String zhQuery = ZHCRMMessageFormatUtils.createZHQueryForPhoneFields(query, phoneFieldName);

        if (zhQuery != null && !"".equals(zhQuery)) {

          /* Set search parameter in request */
          postMethod.setParameter("searchCondition", "(" + zhQuery + ")");
        }
      } else {

        /* Without query param this method is not applicable */
        return this.getObjects(eDSACommandObject);
      }

      if (select != null && !select.equalsIgnoreCase("ALL")) {

        /* Create ZH select CRM fields information */
        final String zhSelect = ZHCRMMessageFormatUtils.createZHSelect(eDSACommandObject, select);

        /* Validate query */
        if (zhSelect != null && !"".equals(zhSelect)) {

          /* Set request param to select fields */
          postMethod.setParameter("selectColumns", zhSelect);
        }
      } else {

        /* Set request param to select all fields */
        postMethod.setParameter("selectColumns", "All");
      }

      /* Validate query */
      if (order != null && !"".equals(order)) {

        /* Validate ordering information */
        ZHCRMMessageFormatUtils.parseAndValidateOrderClause(order, orderFieldsSeparator);

        /* Set request param to select fields */
        postMethod.setParameter("sortColumnString", ZHCRMMessageFormatUtils.createZHSortColumnString(order, orderFieldsSeparator));

        /* Set request param to select fields */
        postMethod.setParameter("sortOrderString", ZHCRMMessageFormatUtils.createZHSortOrderString(order, orderFieldsSeparator));
      }

      final HttpClient httpclient = new HttpClient();

      /* Execute method */
      int result = httpclient.executeMethod(postMethod);
      logger.debug("---Inside getObjectsByAllPhoneNumbers response code: " + result + " & body: " + postMethod.getResponseBodyAsString());

      /* Check if response if SUCCESS */
      if (result == HttpStatus.SC_OK) {

        /* Create XML document from response */
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(postMethod.getResponseBodyAsStream());

        /* Create new XPath object to query XML document */
        final XPath xpath = XPathFactory.newInstance().newXPath();

        /* XPath Query for showing all nodes value */
        final XPathExpression expr = xpath.compile("/response/result/" + eDSACommandObject.getObjectType() + "s/row");

        /* Get node list from response document */
        final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

        /* Check if records founds */
        if (nodeList != null && nodeList.getLength() == 0) {

          /* XPath Query for showing error message */
          XPathExpression errorExpression = xpath.compile("/response/error/message");

          /* Get erroe message from response document */
          Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

          /* Check if error message is found */
          if (errorMessage != null) {

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
          } else {

            /* XPath Query for showing error message */
            errorExpression = xpath.compile("/response/nodata/message");

            /* Get erroe message from response document */
            errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

            /* Send user error */
            throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM : " + errorMessage.getTextContent());
          }
        } else {

          /* Create new EDSA object list */
          final List<CADObject> eDSAObjectList = new ArrayList<CADObject>();

          /* Iterate over node list */
          for (int i = 0; i < nodeList.getLength(); i++) {

            /* Create list of elements */
            final List<Element> elementList = new ArrayList<Element>();

            /* Get node from node list */
            final Node node = nodeList.item(i);

            /* Create new EDSA object */
            final CADObject eDSAObject = new CADObject();

            /* Check if node has child nodes */
            if (node.hasChildNodes()) {

              final NodeList subNodeList = node.getChildNodes();

              /* Create new map for attributes */
              final Map<String, String> attributeMap = new HashMap<String, String>();

              /* Iterate over sub node list and create elements */
              for (int j = 0; j < subNodeList.getLength(); j++) {

                final Node subNode = subNodeList.item(j);

                /* This trick is to avoid empty nodes */
                if (!subNode.getNodeName().contains("#text")) {

                  /* Create element from response */
                  final Element element = (Element) subNode;

                  /* Populate label map */
                  attributeMap.put("label", element.getAttribute("val"));

                  /* Get node name */
                  final String nodeName = element.getAttribute("val").replace(" ", spaceCharReplacement);

                  /* Validate the node name */
                  if (XMLChar.isValidName(nodeName)) {

                    /* Add element in list */
                    elementList.add(ZHCRMMessageFormatUtils.createMessageElement(nodeName, element.getTextContent(), attributeMap));
                  } else {
                    logger.debug("---Inside getObjectsByAllPhoneNumbers, found invalid XML node; ignoring field: " + element.getAttribute("val"));
                  }
                }
              }
            }

            /* Add all CRM fields */
            eDSAObject.setXmlContent(elementList);

            /* Set type information in object */
            eDSAObject.setObjectType(eDSACommandObject.getObjectType());

            /* Add EDSA object in list */
            eDSAObjectList.add(eDSAObject);
          }

          /* Check if no record found */
          if (eDSAObjectList.size() == 0) {
            throw new CADException(CADResponseCodes._1004 + "No records found at Zoho CRM");
          }

          /* Set the final object in command object */
          eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
        }
      } else if (result == HttpStatus.SC_FORBIDDEN) {
        throw new CADException(CADResponseCodes._1022 + "Query is forbidden by Zoho CRM");
      } else if (result == HttpStatus.SC_BAD_REQUEST) {
        throw new CADException(CADResponseCodes._1003 + "Invalid request content");
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {
        throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zoho CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {
        throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
      } else if (result == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

        /* Create XML document from response */
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(postMethod.getResponseBodyAsStream());

        /* Create new XPath object to query XML document */
        final XPath xpath = XPathFactory.newInstance().newXPath();

        /* XPath Query for showing error message */
        final XPathExpression errorExpression = xpath.compile("/response/error/message");

        /* Get erroe message from response document */
        final Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

        if (errorMessage != null) {

          /* Send user error */
          throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM : " + errorMessage.getTextContent());
        } else {

          /* Send user error */
          throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
        }
      }
    } catch (final CADException exception) {
      throw exception;
    } catch (final ParserConfigurationException exception) {
      throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
    } catch (final SAXException exception) {
      throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM");
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1022 + "Communication error while communicating with Zoho CRM");
    } finally {
      /* Release connection socket */
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
    return eDSACommandObject;
  }

  @Override
  public final CADCommandObject createObject(final CADCommandObject eDSACommandObject) throws Exception {
    logger.debug("---Inside createObject");

    PostMethod postMethod = null;
    try {

      String serviceURL = null;
      String serviceProtocol = null;
      String sessionId = null;

      /* Check if tenant is present in request */
      if (eDSACommandObject.getTenant() != null) {

        /* Get tenant from session manager */
        final Tenant tenant = (Tenant) cRMSessionManager.getSessionInfo(eDSACommandObject.getTenant());

        /* Get CRM information from tenant */
        serviceURL = tenant.getCrmServiceURL();
        serviceProtocol = tenant.getCrmServiceProtocol();
        sessionId = tenant.getCrmSessionId();
      } else {
        /* Get agent from session manager */
        final CADUser agent = (CADUser) cRMSessionManager.getSessionInfo(eDSACommandObject.getAgent());

        /* Get CRM information from agent */
        serviceURL = agent.getCrmServiceURL();
        serviceProtocol = agent.getCrmServiceProtocol();
        sessionId = agent.getCrmSessionId();
      }

      /* Create Zoho URL */
      final String zohoURL = serviceProtocol + "://" + serviceURL + "/crm/private/xml/" + eDSACommandObject.getObjectType() + "s/insertRecords";

      logger.debug("---Inside createObject, zohoURL: " + zohoURL + " & sessionId: " + sessionId);

      /* Instantiate post method */
      postMethod = new PostMethod(zohoURL);

      final String xmlData =
          ZHCRMMessageFormatUtils.createRequestString(eDSACommandObject, spaceCharReplacement, permittedDateFormats, zohoInputDateFormat);

      /* Validate xmlData */
      if (xmlData != null && !"".equals(xmlData)) {

        /* Set request param to send request data */
        postMethod.setParameter("xmlData", xmlData);
      }

      /* Set request parameters */
      postMethod.setParameter("authtoken", sessionId.trim());
      postMethod.setParameter("scope", "crmapi");

      final HttpClient httpclient = new HttpClient();

      /* Execute method */
      int result = httpclient.executeMethod(postMethod);
      logger.debug("---Inside createObject response code: " + result + " & body: " + postMethod.getResponseBodyAsString());

      /* Check if response if SUCCESS */
      if (result == HttpStatus.SC_OK) {

        /* Create XML document from response */
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(postMethod.getResponseBodyAsStream());

        /* Create new XPath object to query XML document */
        final XPath xpath = XPathFactory.newInstance().newXPath();

        /* XPath Query for showing all nodes value */
        final XPathExpression expr = xpath.compile("/response/result/recorddetail");

        /* Get node list from response document */
        final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

        /* Check if records founds */
        if (nodeList.getLength() == 0) {

          /* XPath Query for showing error message */
          final XPathExpression errorExpression = xpath.compile("/response/error/message");

          /* Get erroe message from response document */
          final Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

          /* Send user error */
          throw new CADException(CADResponseCodes._1004 + "Not able to create record at Zoho CRM : " + errorMessage.getTextContent());

        } else {

          /* Iterate over node list */
          for (int i = 0; i < nodeList.getLength(); i++) {

            /* Get node from node list */
            final Node node = nodeList.item(i);

            /* Check if node has child nodes */
            if (node.hasChildNodes()) {

              final NodeList subNodeList = node.getChildNodes();

              /* Iterate over sub node list and create elements */
              for (int j = 0; j < subNodeList.getLength(); j++) {

                final Node subNode = subNodeList.item(j);

                /* This trick is to avoid empty nodes */
                if (!subNode.getNodeName().contains("#text")) {

                  /* Create element from response */
                  final Element element = (Element) subNode;

                  if (element.getAttribute("val").equalsIgnoreCase(("ID"))) {

                    /* Set object id in request object */
                    eDSACommandObject.geteDSAObject()[0] =
                        ZHCRMMessageFormatUtils.addNode("Id", element.getTextContent(), eDSACommandObject.geteDSAObject()[0]);
                  }
                }
              }
            }
          }
        }
      } else if (result == HttpStatus.SC_FORBIDDEN) {
        throw new CADException(CADResponseCodes._1022 + "Query is forbidden by Zoho CRM");
      } else if (result == HttpStatus.SC_BAD_REQUEST) {
        throw new CADException(CADResponseCodes._1003 + "Invalid request content");
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {
        throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zoho CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {
        throw new CADException(CADResponseCodes._1004 + "Requested data not found at Zoho CRM");
      } else if (result == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

        /* Create XML document from response */
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(postMethod.getResponseBodyAsStream());

        /* Create new XPath object to query XML document */
        final XPath xpath = XPathFactory.newInstance().newXPath();

        /* XPath Query for showing error message */
        final XPathExpression errorExpression = xpath.compile("/response/error/message");

        /* Get erroe message from response document */
        final Node errorMessage = (Node) errorExpression.evaluate(document, XPathConstants.NODE);

        if (errorMessage != null) {

          /* Send user error */
          throw new CADException(CADResponseCodes._1004 + "Not able to create record at Zoho CRM : " + errorMessage.getTextContent());
        } else {

          /* Send user error */
          throw new CADException(CADResponseCodes._1004 + "Not able to create record at Zoho CRM");
        }
      }
    } catch (final CADException exception) {
      throw exception;
    } catch (final ParserConfigurationException exception) {
      throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM", exception);
    } catch (final SAXException exception) {
      throw new CADException(CADResponseCodes._1022 + "Recieved an invalid XML from Zoho CRM", exception);
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1022 + "Communication error while communicating with Zoho CRM", exception);
    } finally {
      /* Release connection socket */
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
    return eDSACommandObject;
  }

  @Override
  public CADCommandObject getObjectsCount(final CADCommandObject eDSACommandObject) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the EDSA");
  }

  @Override
  public CADCommandObject getObjectsCount(final CADCommandObject eDSACommandObject, final String query) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the EDSA");
  }

  @Override
  public CADCommandObject updateObject(final CADCommandObject eDSACommandObject) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the EDSA");
  }

  @Override
  public final boolean deleteObject(final CADCommandObject eDSACommandObject, final String idToBeDeleted) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the EDSA");
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

  public final CRMSessionManager getcRMSessionManager() {
    return cRMSessionManager;
  }

  public final void setcRMSessionManager(final CRMSessionManager cRMSessionManager) {
    this.cRMSessionManager = cRMSessionManager;
  }

  public final String getSpaceCharReplacement() {
    return spaceCharReplacement;
  }

  public final void setSpaceCharReplacement(final String spaceCharReplacement) {
    this.spaceCharReplacement = spaceCharReplacement;
  }

  /**
   * @return the queryPhoneFieldConst
   */
  public final String getQueryPhoneFieldConst() {
    return this.queryPhoneFieldConst;
  }

  /**
   * @param queryPhoneFieldConst the queryPhoneFieldConst to set
   */
  public final void setQueryPhoneFieldConst(final String queryPhoneFieldConst) {
    this.queryPhoneFieldConst = queryPhoneFieldConst;
  }

  /**
   * @return the zohoInputDateFormat
   */
  public final String getZohoInputDateFormat() {
    return this.zohoInputDateFormat;
  }

  /**
   * @param zohoInputDateFormat the zohoInputDateFormat to set
   */
  public final void setZohoInputDateFormat(final String zohoInputDateFormat) {
    this.zohoInputDateFormat = zohoInputDateFormat;
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

}
