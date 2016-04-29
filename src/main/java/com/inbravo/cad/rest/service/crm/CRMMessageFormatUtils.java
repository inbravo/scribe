package com.inbravo.cad.rest.service.crm;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.axis.message.MessageElement;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.constants.HTTPConstants;
import com.inbravo.cad.rest.resource.CADObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public abstract class CRMMessageFormatUtils {

  private static final Logger logger = Logger.getLogger(CRMMessageFormatUtils.class.getName());

  public static final String equalOperator = "=";
  public static final String notEqualOperator = "!=";
  public static final String parenthesisStartOperator = "(";
  public static final String parenthesisEndOperator = ")";
  public static final String likeOperator = " LIKE ";
  public static final String andOperator = " AND ";
  public static final String nullConstant = "NULL";

  /* This is a solution to avoid out of memory error */
  private static MessageElement TEMPLATE_MESSAGE_ELEMENT = new MessageElement("", "temp");

  private static Element TEMPLATE_XML_ELEMENT;

  static {
    try {
      /* Create and cache this org.w3c.dom.Element instance */
      TEMPLATE_XML_ELEMENT = TEMPLATE_MESSAGE_ELEMENT.getAsDOM();

      /* Set other values */
      TEMPLATE_XML_ELEMENT.removeAttribute("xsi:type");
      TEMPLATE_XML_ELEMENT.removeAttribute("xmlns:ns1");
      TEMPLATE_XML_ELEMENT.removeAttribute("xmlns:xsd");
      TEMPLATE_XML_ELEMENT.removeAttribute("xmlns:xsi");

    } catch (final Exception e) {
      throw new CADException(CADResponseCodes._1000 + "Problem in initializing the system resources");
    }
  }

  /**
   * This API is for avoiding OutOfMemoryError
   * 
   * @param name
   * @param value
   * @return
   * @throws Exception
   */
  public static final MessageElement createMessageElement(final String name, final Object value) throws Exception {

    if (name != null) {

      /* Use the TEMPLATE org.w3c.dom.Element to create new element */
      final MessageElement element = new MessageElement(TEMPLATE_XML_ELEMENT);

      /* Set name of element */
      element.setName(name);

      /* Set value of element */
      if (value != null) {

        if (value instanceof String) {

          /* Type cast the value to String */
          element.setValue((String) value);
        } else if (value instanceof BigDecimal) {

          /* Type cast the value to String */
          element.setValue(((BigDecimal) value).toString());
        } else {

          /* Type cast the value to String */
          element.setValue("" + value);
        }
      }
      return element;
    } else {
      return null;
    }
  }

  /**
   * This API is for avoiding OutOfMemoryError
   * 
   * @param name
   * @param value
   * @return
   * @throws Exception
   */
  public static final MessageElement createMessageElement(final String name, final Object value, final Map<String, String> attributeMap)
      throws Exception {

    if (name != null) {
      /* Use the TEMPLATE org.w3c.dom.Element to create new element */
      final MessageElement element = new MessageElement(TEMPLATE_XML_ELEMENT);

      /* Set value of element */
      element.setName(name);

      /* Set value of element */
      if (value != null) {

        if (value instanceof String) {

          /* Type cast the value to String */
          element.setValue((String) value);
        } else if (value instanceof BigDecimal) {

          /* Type cast the value to String */
          element.setValue(((BigDecimal) value).toString());
        } else {

          /* Type cast the value to String */
          element.setValue("" + value);
        }
      }

      /* Add attribute map to element */
      if (attributeMap != null) {
        for (final Entry<String, String> entry : attributeMap.entrySet()) {
          /* Add attribute in element */
          element.setAttribute(entry.getKey(), entry.getValue());
        }
      }
      return element;
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
  public static final String createWhereClause(final String query) throws Exception {

    String whereClause = query;

    if (whereClause != null) {

      /* Take the main query and add where clause */
      whereClause = " where " + query;

      /* Replace all '&' with 'and' parameters */
      whereClause = whereClause.replace(HTTPConstants.andClause, " and ");

      /* Replace all '|' with 'or' parameters */
      whereClause = whereClause.replace(HTTPConstants.orClause, " or ");
    }
    return whereClause;
  }

  /**
   * 
   * @param query
   * @return
   * @throws Exception
   */
  public static final String replaceMathOperator(final String query) throws Exception {

    String whereClause = query;

    if (whereClause != null) {

      /* Replace all '&' with 'and' parameters */
      whereClause = whereClause.replace(HTTPConstants.andClause, " and ");

      /* Replace all '|' with 'or' parameters */
      whereClause = whereClause.replace(HTTPConstants.orClause, " or ");
    }
    return whereClause;
  }

  /**
   * 
   * @param orderClause
   * @param orderFieldsSeparator
   * @return
   */
  public static final String parseAndValidateOrderClause(final String orderClause, final String orderFieldsSeparator) throws Exception {
    logger.debug("---Inside parseAndValidateOrderClause orderClause: " + orderClause + " & orderFieldsSeparator: " + orderFieldsSeparator);

    final String[] orderByClauseArray = orderClause.trim().split(orderFieldsSeparator.trim());

    if (orderByClauseArray == null) {
      logger.debug("---Inside parseAndValidateOrderClause Order by criteria is invalid");
      throw new CADException(CADResponseCodes._1008 + "Order by criteria is invalid");
    } else if (orderByClauseArray.length == 0 && orderByClauseArray.length == 1) {
      logger.debug("---Inside parseAndValidateOrderClause Order by criteria is invalid");
      throw new CADException(CADResponseCodes._1008 + "Order by criteria is invalid");
    } else if (orderByClauseArray.length != 2) {
      logger.debug("---Inside parseAndValidateOrderClause Order by criteria is invalid");
      throw new CADException(CADResponseCodes._1008 + "Order by criteria is invalid");
    } else {
      if (orderByClauseArray[0] == null) {
        logger.debug("---Inside parseAndValidateOrderClause Order by criteria is invalid");
        throw new CADException(CADResponseCodes._1008 + "Order by criteria is invalid");
      }
      if (orderByClauseArray[1] == null) {
        logger.debug("---Inside parseAndValidateOrderClause Order by criteria is invalid");
        throw new CADException(CADResponseCodes._1008 + "Order by criteria is invalid");
      } else {
        if (!("ASC".equalsIgnoreCase(orderByClauseArray[1].trim()) || "DESC".equalsIgnoreCase(orderByClauseArray[1].trim()))) {
          logger.debug("---Inside parseAndValidateOrderClause Order by criteria is invalid. Please provide ASC/DESC clause with the fields");
          throw new CADException(CADResponseCodes._1008 + "Order by criteria is invalid. Please provide ASC/DESC clause with the fields");
        }
      }
    }

    /* Create order clause */
    final String returnOrderClause = orderByClauseArray[0].trim() + " " + orderByClauseArray[1].trim();
    logger.debug("---Inside parseAndValidateOrderClause orderClause: " + returnOrderClause);
    return returnOrderClause;
  }

  /**
   * 
   * @param fileName
   * @return
   */
  public final static URL getFileURL(final String fileName) {
    final URL fileURL = CRMMessageFormatUtils.class.getClassLoader().getResource(fileName);
    if (fileURL == null) {
      throw new CADException(CADResponseCodes._1002 + "Following file is not found on system: " + fileName);
    } else {
      return fileURL;
    }
  }

  /**
   * 
   * @param query
   * @return
   */
  public final static String replaceLikeOpCase(final String query) {
    String updatedQuery = query;

    /* Check if query contains 'like' */
    if (query.toUpperCase().contains(likeOperator)) {
      updatedQuery = updatedQuery.replaceAll(" (?i)like ", likeOperator);
    }
    return updatedQuery;
  }

  /**
   * 
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final String getNodeValue(final String nodeName, final CADObject cADbject) throws Exception {

    String idNodeValue = null;

    /* Iterate on the Element list and get id node value */
    for (final Element element : cADbject.getXmlContent()) {
      if (element.getNodeName().equalsIgnoreCase(nodeName)) {
        idNodeValue = element.getTextContent();
      }
    }
    return idNodeValue;
  }

  /**
   * 
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final CADObject setNodeValue(final String nodeName, final String nodeValue, final CADObject cADbject) throws Exception {

    /* Iterate on the Element list and set id node value */
    for (final Element element : cADbject.getXmlContent()) {
      if (element.getNodeName().equalsIgnoreCase(nodeName)) {
        element.setTextContent(nodeValue);
      }
    }
    return cADbject;
  }

  /**
   * 
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final CADObject addNode(final String nodeName, final String nodeValue, final CADObject cADbject) throws Exception {

    /* Create new node */
    final Element newElement = createMessageElement(nodeName, nodeValue);

    /* Add new node in list */
    final List<Element> elementList = cADbject.getXmlContent();

    /* Add element in list */
    elementList.add(newElement);

    /* Add updated list */
    cADbject.setXmlContent(elementList);

    return cADbject;
  }

  public static final CADObject createCADObject(final Map<String, String> additionalInfo) throws Exception {

    /* Get additional information */
    if (additionalInfo != null) {

      /* Create list of elements */
      final List<Element> elementList = new ArrayList<Element>();

      /* Create new CAD object */
      final CADObject cADbject = new CADObject();

      for (final Entry<String, String> entry : additionalInfo.entrySet()) {

        /* Create element from response */
        elementList.add(createMessageElement(entry.getKey(), entry.getValue()));

      }

      /* Set element list */
      cADbject.setXmlContent(elementList);

      return cADbject;
    } else {
      return null;
    }
  }

  public static final String getValueFromXML(final String inputXML, final String xPathQuery) throws Exception {

    /* Call another method */
    return getValueFromXML(inputXML, xPathQuery, 0);
  }

  public static final String getValueFromXML(final String inputXML, final String xPathQuery, final int index) throws Exception {

    /* Call another method */
    return getValueFromXML(inputXML, xPathQuery, index, null);
  }

  private static final String getValueFromXML(final String inputXML, final String xPathQuery, final int index, final String[] namespaces)
      throws Exception {

    /* new document builder factory */
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    /* Factory is name space aware */
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
   * @param finishDate
   * @return
   * @throws Exception
   */
  public static final DateTime validateInputDate(final String date, final String permittedDateFormats) throws Exception {

    logger.debug("---Inside validateInputDate, date: " + date + " & permittedDateFormats: " + permittedDateFormats);

    /* Seperate all the formats */
    final String[] defaultDateFormats = permittedDateFormats.split(",");

    /* Create array for all date parsing formats */
    final DateTimeParser[] dateTimeParser = new DateTimeParser[defaultDateFormats.length];

    /* Parse with individual formats */
    for (int i = 0; i < defaultDateFormats.length; i++) {

      /* If format is valid */
      if (defaultDateFormats[i] != null && !"".equals(defaultDateFormats[i])) {

        /* Create new parser for each format */
        dateTimeParser[i] = DateTimeFormat.forPattern(defaultDateFormats[i].trim()).getParser();
      }
    }

    /* Final date formater builder */
    final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().append(null, dateTimeParser).toFormatter();

    /* Parse user supplied date */
    final DateTime updatedDate = dateTimeFormatter.parseDateTime(date);

    logger.debug("---Inside validateInputDate, updated date: " + updatedDate);

    /* Return updated date */
    return updatedDate;
  }
}
