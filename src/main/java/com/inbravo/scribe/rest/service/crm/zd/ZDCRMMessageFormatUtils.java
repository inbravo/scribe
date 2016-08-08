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

package com.inbravo.scribe.rest.service.crm.zd;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.rest.constants.HTTPConstants;
import com.inbravo.scribe.rest.constants.CRMConstants.ZDCRMFieldType;
import com.inbravo.scribe.rest.constants.CRMConstants.ZDCRMObjectType;
import com.inbravo.scribe.rest.resource.CADCommandObject;
import com.inbravo.scribe.rest.resource.CADObject;
import com.inbravo.scribe.rest.service.crm.CRMMessageFormatUtils;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZDCRMMessageFormatUtils extends CRMMessageFormatUtils {

  private static final Logger logger = Logger.getLogger(ZDCRMMessageFormatUtils.class.getName());

  private static final String zdEqualOperator = ":";
  private static final String zdAndOperator = ",";

  private ZDCRMMessageFormatUtils() {
    super();
  }

  public static final String createZDQuery(final String cadQuery) throws Exception {

    String zdQuery = "";

    if (cadQuery != null) {

      /* Tokenize the where clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(cadQuery, HTTPConstants.andClause + HTTPConstants.orClause, true);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {
        String tempElement = stringTokenizer.nextToken();
        logger.debug("---Inside createZDQuery: tempElement: " + tempElement);

        /* Replace 'like' operator case */
        tempElement = replaceLikeOpCase(tempElement);

        /* Check for '!=' operator */
        if (tempElement.contains(notEqualOperator)) {
          throw new CADException(CADResponseCodes._1003 + notEqualOperator + " operator is not supported by Zendesk CRM");
        } else /* Check for '=' operator */
        if (tempElement.contains(equalOperator)) {

          if (tempElement.split(equalOperator)[1].trim().equalsIgnoreCase("NULL")) {
            throw new CADException(CADResponseCodes._1003 + " 'NULL' value is not supported by Zendesk CRM");
          } else {
            zdQuery = zdQuery + tempElement.split(equalOperator)[0].trim() + zdEqualOperator + tempElement.split(equalOperator)[1].trim();
          }
        } else /* Check for 'like' operator */
        if (tempElement.contains(likeOperator)) {
          throw new CADException(CADResponseCodes._1003 + likeOperator + " operator is not supported by Zendesk CRM");
        } else /* Check for '&' */
        if (tempElement.contains(HTTPConstants.andClause)) {

          zdQuery = zdQuery + zdAndOperator;
        } else /* Check for '|' operator */
        if (tempElement.contains(HTTPConstants.orClause)) {

          throw new CADException(CADResponseCodes._1003 + "'" + HTTPConstants.orClause + "' is not supported by Zendesk CRM");
        } /* Check for anything else */
        else {
          zdQuery = zdQuery + tempElement;
        }
      }
    }
    logger.debug("---Inside createZDQuery: zdQuery: " + zdQuery);
    return zdQuery;
  }

  /**
   * 
   * @param select
   * @return
   */
  public static final List<String> createSelectFieldList(final String select) {
    if (select != null) {
      /* Tokenize the select clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(select, ",", false);

      /* Create new select list */
      final List<String> selectFieldList = new ArrayList<String>();

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {
        final String tempElement = stringTokenizer.nextToken();

        /* Add select field in list */
        if (tempElement != null) {
          selectFieldList.add(tempElement.toUpperCase().trim());
        }
      }
      logger.debug("---Inside createSelectFieldList list: " + selectFieldList);
      return selectFieldList;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param query
   */
  public static final boolean validateQuery(final String query) {

    /* Check various types of queries */
    if (query == null) {

      /* Inform user about invalid input */
      throw new CADException(CADResponseCodes._1008 + "Please provide valid query filter. Filter cant't be 'null' in case of object type 'ANY'");
    } else if (query.equalsIgnoreCase(HTTPConstants.noCRMFilter)) {

      /* Inform user about invalid input */
      throw new CADException(CADResponseCodes._1008 + "Please provide valid query filter. Filter can't be 'NONE' in case of object type 'ANY'");
    } else {
      return true;
    }
  }

  /**
   * 
   * @param cADCommandObject
   * @return
   */
  public static final String getCreateRequestXML(final CADCommandObject cADCommandObject) {

    /* Iterate over CAD object and create XML request */
    if (cADCommandObject.getcADObject() != null && cADCommandObject.getcADObject().length == 1) {
      final CADObject cADbject = cADCommandObject.getcADObject()[0];
      final List<Element> elementList = cADbject.getXmlContent();

      /* Add start tag */
      String reqString = "<" + cADCommandObject.getObjectType() + ">";

      /* Iterate over element list */
      for (final Element element : elementList) {
        reqString = reqString + "<" + element.getNodeName() + ">" + element.getTextContent() + "</" + element.getNodeName() + ">";
      }

      /* Add end tag */
      reqString = reqString + "</" + cADCommandObject.getObjectType() + ">";
      logger.debug("---Inside getCreateRequestXML xml : " + reqString);
      return reqString;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param cADCommandObject
   * @return
   * @throws JAXBException
   */
  public static final String getCreateRequestJSON(final CADCommandObject cADCommandObject) throws JAXBException {

    /* Iterate over CAD object and create JSON request */
    if (cADCommandObject.getcADObject() != null && cADCommandObject.getcADObject().length == 1) {

      final CADObject cADbject = cADCommandObject.getcADObject()[0];
      String requestJSONStr = null;

      /* If user type of object */
      if ("USER".equalsIgnoreCase(cADbject.getObjectType())) {

        /* Get JSON string */
        requestJSONStr = getCreateUserRequestJSON(cADbject.getXmlContent());

      } else
      /* If ticket type of object */
      if ("TICKET".equalsIgnoreCase(cADbject.getObjectType())) {

        /* Get JSON string */
        requestJSONStr = getCreateTicketRequestJSON(cADbject.getXmlContent());
      } else
      /* If ticket type of object */
      if ("FORUM".equalsIgnoreCase(cADbject.getObjectType())) {

        /* Get JSON string */
        requestJSONStr = getCreateForumRequestJSON(cADbject.getXmlContent());
      } else {

        /* Throw user error */
        throw new CADException(CADResponseCodes._1003 + " Object type : " + cADbject.getObjectType() + " is not supported by CAD");
      }

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getCreateRequestJSON json : " + requestJSONStr);
      }

      return requestJSONStr;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param cADCommandObject
   * @return
   * @throws JAXBException
   */
  private static final String getCreateTicketRequestJSON(final List<Element> elementList) throws JAXBException {

    /* Create new ticket type of object */
    final ZDTicketObject zdObj = new ZDTicketObject(elementList);

    /* Create new JAXB context */
    final JAXBContext jc = JAXBContext.newInstance(ZDTicketObject.class);

    /* Create new marshaller */
    final Marshaller marshaller = jc.createMarshaller();

    /* Set for JAXB type */
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    /* Create a stringWriter to hold the XML */
    final StringWriter stringWriter = new StringWriter();

    /* Create new namespace configuration */
    final MappedNamespaceConvention mnc = new MappedNamespaceConvention(new Configuration());

    /* Create new XML string */
    final XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(mnc, stringWriter);

    /* Marshal the object */
    marshaller.marshal(zdObj, xmlStreamWriter);

    return stringWriter.toString();
  }

  /**
   * 
   * @param cADCommandObject
   * @return
   * @throws JAXBException
   */
  private static final String getCreateForumRequestJSON(final List<Element> elementList) throws JAXBException {

    /* Create new forum type of object */
    final ZDForumObject zdObj = new ZDForumObject(elementList);

    /* Create new JAXB context */
    final JAXBContext jc = JAXBContext.newInstance(ZDForumObject.class);

    /* Create new marshaller */
    final Marshaller marshaller = jc.createMarshaller();

    /* Set for JAXB type */
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    /* Create a stringWriter to hold the XML */
    final StringWriter stringWriter = new StringWriter();

    /* Create new namespace configuration */
    final MappedNamespaceConvention mnc = new MappedNamespaceConvention(new Configuration());

    /* Create new XML string */
    final XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(mnc, stringWriter);

    /* Marshal the object */
    marshaller.marshal(zdObj, xmlStreamWriter);

    return stringWriter.toString();
  }

  /**
   * 
   * @param cADCommandObject
   * @return
   * @throws JAXBException
   */
  private static final String getCreateUserRequestJSON(final List<Element> elementList) throws JAXBException {

    /* Create new ticket type of object */
    final ZDUserObject zdObj = new ZDUserObject(elementList);

    /* Create new JAXB context */
    final JAXBContext jc = JAXBContext.newInstance(ZDUserObject.class);

    /* Create new marshaller */
    final Marshaller marshaller = jc.createMarshaller();

    /* Set for JAXB type */
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    /* Create a stringWriter to hold the XML */
    final StringWriter stringWriter = new StringWriter();

    /* Create new namespace configuration */
    final MappedNamespaceConvention mnc = new MappedNamespaceConvention(new Configuration());

    /* Create new XML string */
    final XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(mnc, stringWriter);

    /* Marshal the object */
    marshaller.marshal(zdObj, xmlStreamWriter);

    return stringWriter.toString();
  }

  /**
   * 
   * @param cADCommandObject
   * @return
   */
  public static final String getCreateQueryFromCommandObject(final CADCommandObject cADCommandObject) {

    if (cADCommandObject.getcADObject() != null && cADCommandObject.getcADObject().length == 1) {
      final CADObject cADbject = cADCommandObject.getcADObject()[0];
      final List<Element> elementList = cADbject.getXmlContent();

      /* Create request string */
      String reqString = "";

      logger.debug("---Inside getCreateRequestString objectType: " + cADCommandObject.getObjectType());

      /* Find the respective object type */
      if (cADCommandObject.getObjectType().equalsIgnoreCase(ZDCRMObjectType.User.toString())) {

        /* Iterate over element list */
        for (final Element element : elementList) {
          if (element.getNodeName().equalsIgnoreCase(ZDCRMFieldType.Email.toString())) {
            reqString = reqString + element.getNodeName() + zdEqualOperator + element.getTextContent();

            /* Add comma : ',' */
            reqString = reqString + zdAndOperator;
          }
        }
      } else if (cADCommandObject.getObjectType().equalsIgnoreCase(ZDCRMObjectType.Ticket.toString())) {

        /* Iterate over element list */
        for (final Element element : elementList) {
          if (element.getNodeName().equalsIgnoreCase(ZDCRMFieldType.Description.toString())) {
            reqString = reqString + element.getNodeName() + zdEqualOperator + element.getTextContent();

            /* Add comma : ',' */
            reqString = reqString + zdAndOperator;
          }
        }
      }

      /* Remove extra comma: ',' */
      if (reqString != null && !"".equalsIgnoreCase(reqString)) {

        reqString = reqString.substring(0, reqString.length() - 1);
      }
      logger.debug("---Inside getCreateRequestString reqString: " + reqString);
      return reqString;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param zdResponseString
   * @return
   * @throws XPathExpressionException
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   */
  public static final String getErrorFromResponse(final InputStream zdResponseStream) {

    if (zdResponseStream != null) {
      try {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(zdResponseStream);

        /* Create new XPath object to query XML document */
        final XPath xpath = XPathFactory.newInstance().newXPath();

        /* XPath Query for showing all nodes value */
        final XPathExpression expr = xpath.compile("/errors/error");

        /* Get node list from response document */
        final Node node = (Node) expr.evaluate(document, XPathConstants.NODE);

        logger.debug("---Inside getErrorFromResponse: node: " + node);

        /* Return error description */
        if (node.getNodeValue() != null) {
          logger.debug("---Inside getErrorFromResponse: node: " + node.getNodeValue());
          return node.getNodeValue();
        } else {
          logger.debug("---Inside getErrorFromResponse: node: " + node.getTextContent());
          return node.getTextContent();
        }
      } catch (final Exception e) {
        logger.debug("---Inside getErrorFromResponse: ignoring error while parsing error : " + e);
        /* Return empty string to avoid any error */
        return "";
      }
    } else {
      /* Return empty string to avoid any error */
      return "";
    }
  }

  /**
   * 
   * @param zdResponseString
   * @return
   * @throws XPathExpressionException
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   */
  public static final String getErrorFromResponse(final String errorResponse) {

    if (errorResponse != null) {

      String errorString = "";
      try {

        /* Create JSON object from response */
        final JSONObject jObj = new JSONObject(errorResponse);

        /* Get all keys */
        @SuppressWarnings("rawtypes")
        final Iterator itr = jObj.keys();

        /* Iterate over user json object list */
        while (itr.hasNext()) {

          /* Get key name */
          final String key = (String) itr.next();

          /* Get array value */
          if (jObj.get(key).getClass().isAssignableFrom(JSONArray.class)) {

            /* Get array object */
            final JSONArray arrObj = (JSONArray) jObj.get(key);

            /* Run over all JSON objects */
            for (int i = 0; i < arrObj.length(); i++) {

              /* Get individual object */
              final JSONObject subObj = (JSONObject) arrObj.get(i);

              /* Get all keys */
              @SuppressWarnings("rawtypes")
              final Iterator subItr = subObj.keys();

              /* Loop over each user in list */
              while (subItr.hasNext()) {

                /* Get key name */
                final String subKey = (String) subItr.next();

                /* Get value */
                final Object value = subObj.get(subKey);

                if (logger.isDebugEnabled()) {
                  logger.debug("---Inside getErrorFromResponse key : " + subKey + " & value: " + value);

                }

                /* Update error string */
                errorString = errorString + " " + subKey + " : " + value;
              }
            }
          }

          /* If string value */
          else if (jObj.get(key).getClass().isAssignableFrom(String.class)) {

            /* Get string value */
            final String value = (String) jObj.get(key);

            /* Update error string */
            errorString = errorString + " " + key + " : " + value;

          } else {

            if (logger.isDebugEnabled()) {
              logger.debug("---Inside getErrorFromResponse unexpected JSON object type in response : " + jObj);
            }
          }
        }
        return errorString;

      } catch (final Exception e) {
        logger.debug("---Inside getErrorFromResponse: ignoring error while parsing error : " + e);

        /* Return empty string to avoid any error */
        return errorString;
      }
    } else {

      /* Return empty string to avoid any error */
      return "";
    }
  }

  public static void main(String[] args) throws Exception {

    final CADCommandObject cADCommandObject = new CADCommandObject();

    /* Create new CAD object list */
    final List<CADObject> cADbjectList = new ArrayList<CADObject>();

    /* Create list of elements */
    final List<Element> elementList = new ArrayList<Element>();

    /* Add element in list */
    elementList.add(ZDCRMMessageFormatUtils.createMessageElement("name", "amit"));

    /* Add element in list */
    elementList.add(ZDCRMMessageFormatUtils.createMessageElement("address", "pilkhuwa"));

    /* Create new CAD object */
    final CADObject cADbject = new CADObject();

    /* Set object type */
    cADbject.setObjectType("ticket");

    /* Add all CRM fields */
    cADbject.setXmlContent(elementList);

    /* Add CAD object in list */
    cADbjectList.add(cADbject);

    /* Set the final object in command object */
    cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));

    System.out.println("" + getCreateRequestJSON(cADCommandObject));
  }
}
