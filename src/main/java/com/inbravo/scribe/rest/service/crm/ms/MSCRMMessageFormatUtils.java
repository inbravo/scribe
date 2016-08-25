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

package com.inbravo.scribe.rest.service.crm.ms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlFloat;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.datacontract.schemas._2004._07.system_collections_generic.KeyValuePairOfstringanyType;
import org.joda.time.DateTime;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.constants.HTTPConstants;
import com.inbravo.scribe.rest.constants.CRMConstants.MSCRMObjectType;
import com.inbravo.scribe.rest.constants.CRMConstants.MSCRMSchemaType;
import com.inbravo.scribe.rest.resource.ScribeObject;
import com.inbravo.scribe.rest.service.crm.CRMMessageFormatUtils;
import com.microsoft.schemas.crm._2006.query.ArrayOfAnyType;
import com.microsoft.schemas.crm._2006.query.ArrayOfConditionExpression;
import com.microsoft.schemas.crm._2006.query.ArrayOfOrderExpression;
import com.microsoft.schemas.crm._2006.query.ConditionExpression;
import com.microsoft.schemas.crm._2006.query.ConditionOperator;
import com.microsoft.schemas.crm._2006.query.FilterExpression;
import com.microsoft.schemas.crm._2006.query.OrderExpression;
import com.microsoft.schemas.crm._2006.query.OrderType;
import com.microsoft.schemas.crm._2006.query.QueryExpression;
import com.microsoft.schemas.crm._2006.webservices.BusinessEntity;
import com.microsoft.schemas.crm._2007.webservices.Account;
import com.microsoft.schemas.crm._2007.webservices.Contact;
import com.microsoft.schemas.crm._2007.webservices.Incident;
import com.microsoft.schemas.crm._2007.webservices.Lead;
import com.microsoft.schemas.crm._2007.webservices.Opportunity;
import com.microsoft.schemas.crm._2007.webservices.Task;
import com.microsoft.schemas.xrm._2011.contracts.AttributeCollection;
import com.microsoft.schemas.xrm._2011.contracts.Entity;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMMessageFormatUtils extends CRMMessageFormatUtils {

  private static final Logger logger = Logger.getLogger(MSCRMMessageFormatUtils.class.getName());

  private MSCRMMessageFormatUtils() {
    super();
  }

  private static final String regardingObjectidConst = "RegardingObjectid";

  /**
   * 
   * @param fileName
   * @return
   * @throws IOException
   */
  public static final String readStringFromFile(final String fileName) throws IOException {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside readStringFromFile: " + fileName);
    }
    return readStringFromBufferedReader(new BufferedReader(new FileReader(fileName)));
  }

  /**
   * This API will generate logs in general logs file. This is helpfull to see the xml content
   */
  public static final void debugSOAPMessage() {
    if (logger.isDebugEnabled()) {
      System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
      System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
      System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
      System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
    }
  }

  public static final List<Element> createEntityFromBusinessObject(final BusinessEntity businessEntity) throws Exception {

    /* Create list of elements */
    final List<Element> elementList = new ArrayList<Element>();

    if (businessEntity != null) {
      final Node node = businessEntity.getDomNode();
      if (node.hasChildNodes()) {
        final NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {

          /* To avoid : 'DOM Level 3 Not implemented' error */
          final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          final Document document = builder.newDocument();
          final Element element = (Element) document.importNode(nodeList.item(i), true);

          if (element.getNodeName() != null && element.getNodeName().contains(":")) {
            final String nodeName = element.getNodeName().split(":")[1];

            /* Check for attributes */
            final NamedNodeMap attributes = element.getAttributes();

            if (attributes != null && attributes.getLength() != 0) {

              /* Create new map for attributes */
              final Map<String, String> attributeMap = new HashMap<String, String>();

              for (int j = 0; j < attributes.getLength(); j++) {
                final Attr attr = (Attr) attributes.item(j);

                /* Set node name and value in map */
                attributeMap.put(attr.getNodeName(), attr.getNodeValue());
              }

              /* Create node with attributes */
              elementList.add(MSCRMMessageFormatUtils.createMessageElement(nodeName, element.getTextContent(), attributeMap));
            } else {

              /* Create node without attributes */
              elementList.add(MSCRMMessageFormatUtils.createMessageElement(nodeName, element.getTextContent()));
            }
          }
        }
      }
      return elementList;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param entity
   * @return
   * @throws Exception
   */
  public static final List<Element> createV5EntityFromBusinessObject(final Entity entity) throws Exception {

    /* Create list of elements */
    final List<Element> elementList = new ArrayList<Element>();

    /* Set entity id */
    elementList.add(createMessageElement("id", entity.getId()));

    /* Step 2: get all node attributes */
    final AttributeCollection attCol = entity.getAttributes();

    /* Check if entity is not null */
    if (attCol != null) {

      /* Get all attributes for the CRM field */
      final KeyValuePairOfstringanyType[] kvpsatArr = attCol.getKeyValuePairOfstringanyTypeArray();

      /* This is to avoid : 'DOM Level 3 Not implemented' error */
      final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final Document document = builder.newDocument();

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside createV5EntityFromBusinessObject: no of crm fields: " + kvpsatArr.length);
      }

      /* Iterate over all attributes */
      for (final KeyValuePairOfstringanyType kvpsat : kvpsatArr) {

        /* Get field name */
        final String fieldName = kvpsat.getKey();

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside createV5EntityFromBusinessObject: crm field name: " + fieldName);
        }

        /* Get field value */
        String fieldValue = null;

        /* Get field value node */
        final XmlObject xo = kvpsat.getValue();

        /* If object is valid */
        if (xo != null) {

          /* Get DOM node from Xo */
          final Node node = xo.getDomNode();

          /* Check if node is not null */
          if (node != null && node.hasChildNodes()) {

            /* Get all child nodes */
            final NodeList nodeList = node.getChildNodes();

            /* Create new map for attributes */
            final Map<String, String> attributeMap = new HashMap<String, String>();

            /* If more than 1 elements in node list */
            if (nodeList.getLength() > 1) {

              /* Iterate on all child node list */
              for (int i = 0; i < nodeList.getLength(); i++) {

                if (nodeList.item(i) instanceof Element) {

                  final Element childElement = (Element) document.importNode(nodeList.item(i), true);

                  if (childElement.getNodeName() != null && childElement.getNodeName().contains(":")) {

                    /* Get attribute name */
                    final String attName = childElement.getNodeName().split(":")[1];

                    /* Get attribute value */
                    final String attValue = childElement.getTextContent();

                    if ("id".equalsIgnoreCase(attName)) {

                      fieldValue = attValue;
                    } else {

                      /* Put values in map */
                      attributeMap.put(attName, attValue);
                    }
                  }
                }
              }

              /* Create node with attributes */
              elementList.add(createMessageElement(fieldName, fieldValue, attributeMap));

            } else if (nodeList.getLength() == 1) {

              /* Iterate on all child node list */
              if (nodeList.item(0) instanceof Element) {

                final Element childElement = (Element) document.importNode(nodeList.item(0), true);

                /* Get attribute value */
                fieldValue = childElement.getTextContent();

                /* Create node with attributes */
                elementList.add(createMessageElement(fieldName, fieldValue));
              } else {

                /* Create node with attributes */
                elementList.add(createMessageElement(fieldName, nodeList.item(0).getNodeValue()));
              }
            }
          } else {

            /* Create node with attributes */
            elementList.add(createMessageElement(fieldName, node.getTextContent()));
          }
        }
      }
    }

    return elementList;
  }

  public static final QueryExpression createFilterInQuery(final String query, final String order, final QueryExpression queryExpression,
      final String crmFieldsSeparator, final String orderFieldsSeparator) throws Exception {

    if (query != null) {
      /* Tokenize the where clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(query, HTTPConstants.andClause + HTTPConstants.orClause, true);

      /* Create filter expression */
      final FilterExpression filterExpression = queryExpression.addNewCriteria();

      /* Create array of condition expression object */
      final ArrayOfConditionExpression arrayOfConditionExpression = filterExpression.addNewConditions();

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {
        String tempElement = stringTokenizer.nextToken();

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside createFilterExpression: tempElement: " + tempElement);
        }

        /* Replace 'like' operator case */
        tempElement = replaceLikeOpCase(tempElement);

        /* Check for '!=' operator */
        if (tempElement.contains(notEqualOperator)) {

          /* Validate the element */
          if (tempElement.split(notEqualOperator).length < 2) {
            throw new ScribeException(ScribeResponseCodes._1008 + "Query criteria is invalid: '" + tempElement + "'");
          }

          /* Create condition expression */
          final ConditionExpression conditionExpression = arrayOfConditionExpression.addNewCondition();

          /* Set attribute name */
          conditionExpression.setAttributeName(tempElement.split(notEqualOperator)[0].trim());

          /* Check for 'NULL' values */
          if (tempElement.split(notEqualOperator)[1].trim().toUpperCase().equalsIgnoreCase(nullConstant)) {

            /* Set Nill value */
            conditionExpression.setOperator(ConditionOperator.NOT_NULL);
          } else {

            /* Set operator as 'NOT_EQUAL' */
            conditionExpression.setOperator(ConditionOperator.NOT_EQUAL);

            /* Set value */
            final ArrayOfAnyType arrayOfAnyType = ArrayOfAnyType.Factory.newInstance();
            arrayOfAnyType.addNewValue();
            arrayOfAnyType.setValueArray(0, XmlString.Factory.newValue(tempElement.split(notEqualOperator)[1].trim()));
            conditionExpression.setValues(arrayOfAnyType);
          }
        } else /* Check for '=' operator */
        if (tempElement.contains(equalOperator)) {

          /* Validate the element */
          if (tempElement.split(equalOperator).length < 2) {
            throw new ScribeException(ScribeResponseCodes._1008 + "Query criteria is invalid: '" + tempElement + "'");
          }

          /* Create condition expression */
          final ConditionExpression conditionExpression = arrayOfConditionExpression.addNewCondition();

          /* Set attribute name */
          conditionExpression.setAttributeName(tempElement.split(equalOperator)[0].trim());

          /* Check for 'NULL' values */
          if (tempElement.split(equalOperator)[1].trim().toUpperCase().equalsIgnoreCase(nullConstant)) {

            /* Set Nill value */
            conditionExpression.setOperator(ConditionOperator.NULL);
          } else {

            /* Set operator as 'EQUAL' */
            conditionExpression.setOperator(ConditionOperator.EQUAL);

            /* Set value */
            final ArrayOfAnyType arrayOfAnyType = ArrayOfAnyType.Factory.newInstance();
            arrayOfAnyType.addNewValue();
            arrayOfAnyType.setValueArray(0, XmlString.Factory.newValue(tempElement.split(equalOperator)[1].trim()));
            conditionExpression.setValues(arrayOfAnyType);
          }
        } else /* Check for 'LIKE' operator */
        if (tempElement.contains(likeOperator)) {

          /* Validate the element */
          if (tempElement.split(likeOperator).length < 2) {
            throw new ScribeException(ScribeResponseCodes._1008 + "Query criteria is invalid: '" + tempElement + "'");
          }

          /* Create condition expression */
          final ConditionExpression conditionExpression = arrayOfConditionExpression.addNewCondition();

          /* Set attribute name */
          conditionExpression.setAttributeName(tempElement.split(likeOperator)[0].trim());

          /* Check for 'NULL' values */
          if (tempElement.split(likeOperator)[1].trim().toUpperCase().equalsIgnoreCase(nullConstant)) {

            /* Set Nill value */
            conditionExpression.setOperator(ConditionOperator.NULL);
          } else {

            /* Set operator as 'EQUAL' */
            conditionExpression.setOperator(ConditionOperator.LIKE);

            /* Set value */
            final ArrayOfAnyType arrayOfAnyType = ArrayOfAnyType.Factory.newInstance();
            arrayOfAnyType.addNewValue();
            arrayOfAnyType.setValueArray(0, XmlString.Factory.newValue(tempElement.split(likeOperator)[1].trim()));
            conditionExpression.setValues(arrayOfAnyType);
          }
        } else {
          throw new ScribeException(ScribeResponseCodes._1008 + "Query criteria is invalid: '" + tempElement + "'");
        }
      }
    }

    /* If a valid order */
    if (order != null && !"".equals(order)) {

      /* Create order expression */
      final ArrayOfOrderExpression arrayOfOrderExpression = queryExpression.addNewOrders();

      /* Tokenize the order clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(order, crmFieldsSeparator);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {

        /* Create new order expression */
        final OrderExpression orderExpression = arrayOfOrderExpression.addNewOrder();

        /* Validate order clause */
        final String[] updatedOrderArray = parseAndValidateOrderClauseForMSCRM(stringTokenizer.nextToken(), orderFieldsSeparator);

        /* Set attribute name */
        orderExpression.setAttributeName(updatedOrderArray[0]);

        /* Set order type */
        if ("ASC".equalsIgnoreCase(updatedOrderArray[1])) {

          orderExpression.setOrderType(OrderType.ASCENDING);
        } else if ("DESC".equalsIgnoreCase(updatedOrderArray[1])) {

          orderExpression.setOrderType(OrderType.DESCENDING);
        }
      }
    }
    return queryExpression;
  }

  /**
   * 
   * @param query
   * @param order
   * @param queryExpression
   * @param crmFieldsSeparator
   * @param orderFieldsSeparator
   * @return
   * @throws Exception
   */
  public static final com.microsoft.schemas.xrm._2011.contracts.QueryExpression createV5FilterInQuery(final String query, final String order,
      final com.microsoft.schemas.xrm._2011.contracts.QueryExpression queryExpression, final String crmFieldsSeparator,
      final String orderFieldsSeparator) throws Exception {

    if (query != null) {

      /* Tokenize the where clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(query, HTTPConstants.andClause + HTTPConstants.orClause, true);

      /* Create filter expression */
      final com.microsoft.schemas.xrm._2011.contracts.FilterExpression filterExpression = queryExpression.addNewCriteria();

      /* Create array of condition expression object */
      final com.microsoft.schemas.xrm._2011.contracts.ArrayOfConditionExpression arrayOfConditionExpression = filterExpression.addNewConditions();

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {
        String tempElement = stringTokenizer.nextToken();

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside createFilterExpression: tempElement: " + tempElement);
        }

        /* Replace 'like' operator case */
        tempElement = replaceLikeOpCase(tempElement);

        /* Check for '!=' operator */
        if (tempElement.contains(notEqualOperator)) {

          /* Validate the element */
          if (tempElement.split(notEqualOperator).length < 2) {
            throw new ScribeException(ScribeResponseCodes._1008 + "Query criteria is invalid: '" + tempElement + "'");
          }

          /* Create condition expression */
          final com.microsoft.schemas.xrm._2011.contracts.ConditionExpression conditionExpression =
              arrayOfConditionExpression.addNewConditionExpression();

          /* Set attribute name */
          conditionExpression.setAttributeName(tempElement.split(notEqualOperator)[0].trim());

          /* Check for 'NULL' values */
          if (tempElement.split(notEqualOperator)[1].trim().equalsIgnoreCase(nullConstant)) {

            /* Set Nill value */
            conditionExpression.setOperator(com.microsoft.schemas.xrm._2011.contracts.ConditionOperator.NOT_NULL);
          } else {

            /* Set operator as 'NOT_EQUAL' */
            conditionExpression.setOperator(com.microsoft.schemas.xrm._2011.contracts.ConditionOperator.NOT_EQUAL);

            /* Set value */
            final com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfanyType arrayOfAnyType =
                com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfanyType.Factory.newInstance();
            arrayOfAnyType.addNewAnyType();
            arrayOfAnyType.setAnyTypeArray(0, XmlString.Factory.newValue(tempElement.split(notEqualOperator)[1].trim()));

            /* Add type information to avoid MS error */
            addMSSpecificNS(arrayOfAnyType);

            conditionExpression.setValues(arrayOfAnyType);
          }
        } else /* Check for '=' operator */
        if (tempElement.contains(equalOperator)) {

          /* Validate the element */
          if (tempElement.split(equalOperator).length < 2) {
            throw new ScribeException(ScribeResponseCodes._1008 + "Query criteria is invalid: '" + tempElement + "'");
          }

          /* Create condition expression */
          final com.microsoft.schemas.xrm._2011.contracts.ConditionExpression conditionExpression =
              arrayOfConditionExpression.addNewConditionExpression();

          /* Set attribute name */
          conditionExpression.setAttributeName(tempElement.split(equalOperator)[0].trim());

          /* Check for 'NULL' values */
          if (tempElement.split(equalOperator)[1].trim().toUpperCase().equalsIgnoreCase(nullConstant)) {

            /* Set Nill value */
            conditionExpression.setOperator(com.microsoft.schemas.xrm._2011.contracts.ConditionOperator.NULL);
          } else {

            /* Set operator as 'EQUAL' */
            conditionExpression.setOperator(com.microsoft.schemas.xrm._2011.contracts.ConditionOperator.EQUAL);

            /* Set value */
            final com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfanyType aoat =
                com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfanyType.Factory.newInstance();
            aoat.addNewAnyType();
            aoat.setAnyTypeArray(0, XmlString.Factory.newValue(tempElement.split(equalOperator)[1].trim()));

            /* Add type information to avoid MS error */
            addMSSpecificNS(aoat);

            /* Set values in condition */
            conditionExpression.setValues(aoat);
          }
        } else /* Check for 'LIKE' operator */
        if (tempElement.contains(likeOperator)) {

          logger.debug("---Inside createFilterExpression: tempElement length: " + tempElement.split(likeOperator).length);

          /* Validate the element */
          if (tempElement.split(likeOperator).length < 2) {
            throw new ScribeException(ScribeResponseCodes._1008 + "Query criteria is invalid: '" + tempElement + "'");
          }

          /* Create condition expression */
          final com.microsoft.schemas.xrm._2011.contracts.ConditionExpression conditionExpression =
              arrayOfConditionExpression.addNewConditionExpression();

          /* Set attribute name */
          conditionExpression.setAttributeName(tempElement.split(likeOperator)[0].trim());

          /* Check for 'NULL' values */
          if (tempElement.split(likeOperator)[1].trim().toUpperCase().equalsIgnoreCase(nullConstant)) {

            /* Set Nill value */
            conditionExpression.setOperator(com.microsoft.schemas.xrm._2011.contracts.ConditionOperator.NULL);
          } else {

            /* Set operator as 'EQUAL' */
            conditionExpression.setOperator(com.microsoft.schemas.xrm._2011.contracts.ConditionOperator.LIKE);

            /* Set value */
            final com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfanyType arrayOfAnyType =
                com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfanyType.Factory.newInstance();
            arrayOfAnyType.addNewAnyType();
            arrayOfAnyType.setAnyTypeArray(0, XmlString.Factory.newValue(tempElement.split(likeOperator)[1].trim()));

            /* Add type information to avoid MS error */
            addMSSpecificNS(arrayOfAnyType);

            conditionExpression.setValues(arrayOfAnyType);
          }
        } else {
          throw new ScribeException(ScribeResponseCodes._1008 + "Query criteria is invalid: '" + tempElement + "'");
        }
      }
    }

    /* If a valid order */
    if (order != null && !"".equals(order)) {

      /* Create order expression */
      final com.microsoft.schemas.xrm._2011.contracts.ArrayOfOrderExpression arrayOfOrderExpression = queryExpression.addNewOrders();

      /* Tokenize the order clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(order, crmFieldsSeparator);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {

        /* Create new order expression */
        final com.microsoft.schemas.xrm._2011.contracts.OrderExpression oe = arrayOfOrderExpression.addNewOrderExpression();

        /* Validate order clause */
        final String[] updatedOrderArray = parseAndValidateOrderClauseForMSCRM(stringTokenizer.nextToken(), orderFieldsSeparator);

        /* Set attribute name */
        oe.setAttributeName(updatedOrderArray[0]);

        /* Set order type */
        if ("ASC".equalsIgnoreCase(updatedOrderArray[1])) {

          oe.setOrderType(com.microsoft.schemas.xrm._2011.contracts.OrderType.ASCENDING);
        } else if ("DESC".equalsIgnoreCase(updatedOrderArray[1])) {

          oe.setOrderType(com.microsoft.schemas.xrm._2011.contracts.OrderType.DESCENDING);
        }
      }
    }
    return queryExpression;
  }

  /**
   * This method is to avoid error from MS about missing NS
   * 
   * @param aoat
   */
  private static final void addMSSpecificNS(final com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfanyType aoat) {

    /* Create new cursor to add type information */
    final XmlCursor xc = aoat.getAnyTypeArray(0).newCursor();

    /* Go to first element */
    xc.toFirstContentToken();

    /* Add attribute with namespace */
    xc.insertNamespace("xs", "http://www.w3.org/2001/XMLSchema");
    xc.insertAttributeWithValue(new QName("http://www.w3.org/2001/XMLSchema-instance", "type"), "xs:string");
    xc.dispose();
  }

  /**
   * 
   * @param br
   * @return
   * @throws IOException
   */
  private static final String readStringFromBufferedReader(final BufferedReader br) throws IOException {

    /* New string buffer */
    final StringBuffer sbr = new StringBuffer();

    /* Read buffer and create string */
    for (String ln = br.readLine(); ln != null; ln = br.readLine()) {
      sbr.append(ln + "\n");
    }

    /* Close the buffer */
    br.close();
    return sbr.toString();
  }

  /**
   * 
   * @param orderClause
   * @param orderFieldsSeparator
   * @return
   */
  private static final String[] parseAndValidateOrderClauseForMSCRM(final String orderClause, final String orderFieldsSeparator) throws Exception {

    final String[] orderByClauseArray = orderClause.split(orderFieldsSeparator);

    if (orderByClauseArray == null) {
      if (logger.isDebugEnabled()) {
        logger.debug("---Inside parseAndValidateOrderClauseForMSCRM; Order by criteria is invalid");
      }
      throw new ScribeException(ScribeResponseCodes._1008 + "Order by criteria is invalid");
    } else if (orderByClauseArray.length == 0 || orderByClauseArray.length == 1) {
      if (logger.isDebugEnabled()) {
        logger.debug("---Inside parseAndValidateOrderClauseForMSCRM; Order by criteria is invalid");
      }
      throw new ScribeException(ScribeResponseCodes._1008 + "Order by criteria is invalid");
    } else if (orderByClauseArray.length != 2) {
      if (logger.isDebugEnabled()) {
        logger.debug("---Inside parseAndValidateOrderClauseForMSCRM; Order by criteria is invalid");
      }
      throw new ScribeException(ScribeResponseCodes._1008 + "Order by criteria is invalid");
    } else {
      if (orderByClauseArray[0] == null) {
        if (logger.isDebugEnabled()) {
          logger.debug("---Inside parseAndValidateOrderClauseForMSCRM; Order by criteria is invalid");
        }
        throw new ScribeException(ScribeResponseCodes._1008 + "Order by criteria is invalid");
      }
      if (orderByClauseArray[1] == null) {
        if (logger.isDebugEnabled()) {
          logger.debug("---Inside parseAndValidateOrderClauseForMSCRM; Order by criteria is invalid");
        }
        throw new ScribeException(ScribeResponseCodes._1008 + "Order by criteria is invalid");
      } else {

        if (!("ASC".equalsIgnoreCase(orderByClauseArray[1]) || "DESC".equalsIgnoreCase(orderByClauseArray[1]))) {
          if (logger.isDebugEnabled()) {
            logger
                .debug("---Inside parseAndValidateOrderClauseForMSCRM; Order by criteria is invalid. Please provide ASC/DESC clause with the fields");
          }
          throw new ScribeException(ScribeResponseCodes._1008 + "Order by criteria is invalid. Please provide ASC/DESC clause with the fields");
        }
      }
    }

    /* Create order clause */
    final String[] arrayOfOrderClause = new String[] {orderByClauseArray[0], orderByClauseArray[1]};
    return arrayOfOrderClause;
  }

  /**
   * 
   * @param account
   * @param accountId
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final Entity createV5RetrieveCRMObjectReq(final ScribeObject cADbject, final String crmFieldIntraSeparator,
      final String permittedDateFormats) throws Exception {

    /* Check for supported objects */
    if (cADbject.getObjectType().equalsIgnoreCase(MSCRMObjectType.Account.toString())
        || (cADbject.getObjectType().equalsIgnoreCase(MSCRMObjectType.Task.toString()))
        || (cADbject.getObjectType().equalsIgnoreCase(MSCRMObjectType.Contact.toString()))
        || (cADbject.getObjectType().equalsIgnoreCase(MSCRMObjectType.Lead.toString()))
        || (cADbject.getObjectType().equalsIgnoreCase(MSCRMObjectType.Opportunity.toString()))
        || (cADbject.getObjectType().equalsIgnoreCase(MSCRMObjectType.Incident.toString()))
        || (cADbject.getObjectType().equalsIgnoreCase(MSCRMObjectType.Case.toString()))) {

      /* Create new V5 MS CRM object */
      return createV5CRMObject(cADbject, crmFieldIntraSeparator, permittedDateFormats);
    } else {

      /* Else send not supported error to user */
      throw new ScribeException(ScribeResponseCodes._1003 + "Following object type is not supported by the Scribe");
    }
  }

  /**
   * This method is to support custom fields in CRM objects
   * 
   * @param account
   * @param accountId
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final Entity createV5CRMObject(final ScribeObject cADbject, final String crmFieldIntraSeparator, final String permittedDateFormats)
      throws Exception {

    /* Create xml beans object */
    final Entity entity = Entity.Factory.newInstance();

    /* Set target name */
    entity.setLogicalName(cADbject.getObjectType().trim());

    /* Step 2: get all node attributes */
    final AttributeCollection attCol = entity.addNewAttributes();

    /* Iterate on the Element list and create SOAP object */
    for (final Element element : cADbject.getXmlContent()) {

      /* Avoid if its local purpose regardingobjectid node */
      if (element.getNodeName().equalsIgnoreCase(regardingObjectidConst)) {

        /* Ignore this element */
        continue;
      } else {

        /* Get element name */
        final String crmField = element.getNodeName();

        /* Break the field name using field/type seperator */
        if (crmField.contains(crmFieldIntraSeparator)) {

          /* Get field name */
          final String crmFieldName = crmField.split(crmFieldIntraSeparator)[0];

          /* Get field type */
          final String crmFieldtype = crmField.split(crmFieldIntraSeparator)[1];

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside createV5CRMObject: crmFieldName: '" + crmFieldName + "' & crmFieldtype : '" + crmFieldtype + "'");
          }

          /* Add new key value pair */
          final KeyValuePairOfstringanyType kvpsat = attCol.addNewKeyValuePairOfstringanyType();

          /* Set CRM field name */
          kvpsat.setKey(crmFieldName);

          /* If type is OptionSet; map it to boolean */
          if (crmFieldtype.equalsIgnoreCase(MSV5DataTypes.OPTION_SET) || crmFieldtype.equalsIgnoreCase(MSV5DataTypes.TWO_OPTIONS)) {

            /* Create new string value */
            final XmlBoolean xb = org.apache.xmlbeans.XmlBoolean.Factory.newInstance();

            try {

              if (element.getTextContent() != null) {

                /* Set node value */
                xb.setStringValue(element.getTextContent());
              }

              /* Set CRM field value */
              kvpsat.setValue(xb);

            } catch (final XmlValueOutOfRangeException e) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM field value is not valid: " + element.getTextContent()
                  + ": type should be : " + MSV5DataTypes.OPTION_SET + " or " + MSV5DataTypes.TWO_OPTIONS);
            }
          } else
          /* If type is datetime */
          if (crmFieldtype.equalsIgnoreCase(MSV5DataTypes.DATE_TIME)) {

            /* Create new date value */
            final XmlDateTime xdt = org.apache.xmlbeans.XmlDateTime.Factory.newInstance();

            try {

              if (element.getTextContent() != null) {

                /* Set node value */
                xdt.setDateValue(validateDate(element.getTextContent(), permittedDateFormats));
              }

              /* Set CRM field value */
              kvpsat.setValue(xdt);

            } catch (final XmlValueOutOfRangeException e) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM field value is not valid: " + element.getTextContent()
                  + ": type should be : " + MSV5DataTypes.DATE_TIME);
            }

          } else
          /* If type is number/whole number */
          if (crmFieldtype.equalsIgnoreCase(MSV5DataTypes.NUMBER) || crmFieldtype.equalsIgnoreCase(MSV5DataTypes.WHOLE_NUMBER)) {

            /* Create new string value */
            final XmlInt xi = org.apache.xmlbeans.XmlInt.Factory.newInstance();

            try {

              if (element.getTextContent() != null) {

                /* Set node value */
                xi.setStringValue(element.getTextContent());
              }

              /* Set CRM field value */
              kvpsat.setValue(xi);

            } catch (final XmlValueOutOfRangeException e) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM field value is not valid: " + element.getTextContent()
                  + ": type should be : " + MSV5DataTypes.NUMBER + " or " + MSV5DataTypes.WHOLE_NUMBER);
            }

          } else
          /* If type is text */
          if (crmFieldtype.equalsIgnoreCase(MSV5DataTypes.TEXT)) {

            /* Create new string value */
            final XmlString xs = org.apache.xmlbeans.XmlString.Factory.newInstance();

            try {

              if (element.getTextContent() != null) {

                /* Set node value */
                xs.setStringValue(element.getTextContent());
              }

              /* Set CRM field value */
              kvpsat.setValue(xs);

            } catch (final XmlValueOutOfRangeException e) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM field value is not valid: " + element.getTextContent()
                  + ": type should be : " + MSV5DataTypes.TEXT);
            }

          } else
          /* If type is float */
          if (crmFieldtype.equalsIgnoreCase(MSV5DataTypes.FLOATING_POINT)) {

            /* Create new float value */
            final XmlFloat xf = org.apache.xmlbeans.XmlFloat.Factory.newInstance();

            try {

              if (element.getTextContent() != null) {

                /* Set node value */
                xf.setStringValue(element.getTextContent());
              }

              /* Set CRM field value */
              kvpsat.setValue(xf);

            } catch (final XmlValueOutOfRangeException e) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM field value is not valid: " + element.getTextContent()
                  + ": type should be : " + MSV5DataTypes.FLOATING_POINT);
            }

          } else
          /* If type is multiple list */
          if (crmFieldtype.equalsIgnoreCase(MSV5DataTypes.MULTIPLE_LIST)) {

            /* Create new string value */
            final XmlString xs = org.apache.xmlbeans.XmlString.Factory.newInstance();

            try {

              if (element.getTextContent() != null) {

                /* Set node value */
                xs.setStringValue(element.getTextContent());
              }

              /* Set CRM field value */
              kvpsat.setValue(xs);

            } catch (final XmlValueOutOfRangeException e) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM field value is not valid: " + element.getTextContent()
                  + ": type should be : " + MSV5DataTypes.MULTIPLE_LIST);
            }

          } else
          /* If type is currency */
          if (crmFieldtype.equalsIgnoreCase(MSV5DataTypes.CURRENCY)) {

            /* Create new string value */
            final XmlString xs = org.apache.xmlbeans.XmlString.Factory.newInstance();

            try {

              if (element.getTextContent() != null) {

                /* Set node value */
                xs.setStringValue(element.getTextContent());
              }

              /* Set CRM field value */
              kvpsat.setValue(xs);

            } catch (final XmlValueOutOfRangeException e) {

              /* Throw user error */
              throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM field value is not valid: " + element.getTextContent()
                  + ": type should be : " + MSV5DataTypes.CURRENCY);
            }

          } else {

            /* Throw user error */
            throw new ScribeException(ScribeResponseCodes._1003 + "Following MS CRM field type is not supported: " + crmFieldtype);

          }
        } else {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM field name is not valid: " + element.getTextContent()
              + ": It should contain type information seperated by: " + crmFieldIntraSeparator);
        }
      }
    }

    return entity;
  }

  private static Date validateDate(final String dateStr, final String permittedDateFormats) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside validateDate: date input: '" + dateStr + "'");
    }

    /* Format the date */
    if (dateStr != null && !"".equals(dateStr)) {

      final DateTime dateTime = validateInputDate(dateStr, permittedDateFormats);

      return dateTime.toDate();

    } else {
      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM date value is not accepted by Scribe: " + dateStr);
    }
  }

  public static final String[] getRegardingObjectInfo(final ScribeObject cADbject) {

    /* Iterate on the Element list and create SOAP object */
    for (final Element element : cADbject.getXmlContent()) {

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getRegardingObjectInfo: node: '" + element.getNodeName() + "' with value : '" + element.getTextContent()
            + "' & Type: '" + element.getAttribute("type") + "'");
      }

      /* Check for regardingobjectid node */
      if (element.getNodeName().equalsIgnoreCase(regardingObjectidConst)) {

        String schema = null;

        /* Find schema */
        if (MSCRMObjectType.Account.toString().equalsIgnoreCase(element.getAttribute("type"))) {

          schema = MSCRMSchemaType.Account_Tasks.toString();

        } else if (MSCRMObjectType.Contact.toString().equalsIgnoreCase(element.getAttribute("type"))) {

          schema = MSCRMSchemaType.Contact_Tasks.toString();

        } else if (MSCRMObjectType.Lead.toString().equalsIgnoreCase(element.getAttribute("type"))) {

          schema = MSCRMSchemaType.Lead_Tasks.toString();

        } else if (MSCRMObjectType.Opportunity.toString().equalsIgnoreCase(element.getAttribute("type"))) {

          schema = MSCRMSchemaType.Opportunity_Tasks.toString();

        } else {
          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1003 + "Following object type is not supported for object association by Scribe");
        }
        return new String[] {element.getTextContent(), element.getAttribute("type"), schema};

      } else {

        /* Ignore other elements */
        continue;
      }
    }

    return null;
  }

  /**
   * 
   * @param account
   * @param accountId
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final BusinessEntity createCRMObject(final String mSCRMObjectType, final ScribeObject cADbject, final String crmFieldIntraSeparator)
      throws Exception {

    if (mSCRMObjectType.equalsIgnoreCase(MSCRMObjectType.Account.toString())) {
      return createCRMObjectTypeAccount(cADbject, crmFieldIntraSeparator);
    } else if (mSCRMObjectType.equalsIgnoreCase(MSCRMObjectType.Task.toString())) {
      return createCRMObjectTypeTask(cADbject, crmFieldIntraSeparator);
    } else if (mSCRMObjectType.equalsIgnoreCase(MSCRMObjectType.Contact.toString())) {
      return createCRMObjectTypeContact(cADbject, crmFieldIntraSeparator);
    } else if (mSCRMObjectType.equalsIgnoreCase(MSCRMObjectType.Lead.toString())) {
      return createCRMObjectTypeLead(cADbject, crmFieldIntraSeparator);
    } else if (mSCRMObjectType.equalsIgnoreCase(MSCRMObjectType.Opportunity.toString())) {
      return createCRMObjectTypeOpportunity(cADbject, crmFieldIntraSeparator);
    } else if (mSCRMObjectType.equalsIgnoreCase(MSCRMObjectType.Incident.toString())) {
      return createCRMObjectTypeIncident(cADbject, crmFieldIntraSeparator);
    } else {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1003 + "Following object type is not supported by the Scribe");
    }
  }

  /**
   * 
   * @param account
   * @param accountId
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final Account createCRMObjectTypeAccount(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    /* Create xml beans object */
    final Account account = Account.Factory.newInstance();

    /* Create new cursor */
    XmlCursor cursor = null;
    try {
      cursor = account.newCursor();
      cursor.toFirstContentToken();
      final Node node = cursor.getDomNode();

      /* Get main namespace URl */
      if (node != null) {
        final String nsURL = node.getNamespaceURI();

        /* Iterate on the Element list and create SOAP object */
        for (final Element element : cADbject.getXmlContent()) {

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside createCRMObject_Account: adding node: " + element.getNodeName() + " with value : " + element.getTextContent());
          }

          /* Get crm field */
          final String crmField = element.getNodeName();

          String crmFieldName = null;

          /* If field contains the type information */
          if (crmField != null && crmField.contains(crmFieldIntraSeparator)) {

            /* Split the field and get name */
            crmFieldName = crmField.split(crmFieldIntraSeparator)[0];

          } else {
            crmFieldName = crmField;
          }

          /* Add all new nodes */
          cursor.beginElement(new QName(nsURL, crmFieldName));

          /* Set node attributes */
          if (element.getAttributes() != null) {

            /* Get attribute map */
            final NamedNodeMap namedNodeMap = element.getAttributes();

            /* Interate over map */
            for (int i = 0; i < namedNodeMap.getLength(); i++) {

              /* Get respective item */
              final Node namedNode = namedNodeMap.item(i);

              if (logger.isDebugEnabled()) {
                logger.debug("---Inside createCRMObject_Account: adding attribute name : " + namedNode.getNodeName() + " with value : "
                    + namedNode.getNodeValue());
              }

              /* Set node attribute */
              cursor.insertAttributeWithValue(namedNode.getNodeName(), namedNode.getNodeValue());
            }
          }

          /* Set node value */
          cursor.insertChars(element.getTextContent());

          /* Jump to next token */
          cursor.toNextToken();
        }
      }
    } finally {
      if (cursor != null) {
        cursor.dispose();
      }
    }
    return account;
  }

  /**
   * 
   * @param account
   * @param accountId
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final Task createCRMObjectTypeTask(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    /* Create xml beans object */
    final Task task = Task.Factory.newInstance();

    /* Create new cursor */
    XmlCursor cursor = null;
    try {
      cursor = task.newCursor();
      cursor.toFirstContentToken();
      final Node node = cursor.getDomNode();

      /* Get main namespace URl */
      if (node != null) {
        final String nsURL = node.getNamespaceURI();

        /* Iterate on the Element list and create SOAP object */
        for (final Element element : cADbject.getXmlContent()) {

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside createCRMObject_Task: adding node : " + element.getNodeName() + " with value : " + element.getTextContent());
          }

          /* Get crm field */
          final String crmField = element.getNodeName();

          String crmFieldName = null;

          /* If field contains the type information */
          if (crmField != null && crmField.contains(crmFieldIntraSeparator)) {

            /* Split the field and get name */
            crmFieldName = crmField.split(crmFieldIntraSeparator)[0];
          } else {
            crmFieldName = crmField;
          }

          /* Add all new nodes */
          cursor.beginElement(new QName(nsURL, crmFieldName));

          /* Set node attributes */
          if (element.getAttributes() != null) {

            /* Get attribute map */
            final NamedNodeMap namedNodeMap = element.getAttributes();

            /* Interate over map */
            for (int i = 0; i < namedNodeMap.getLength(); i++) {

              /* Get respective item */
              final Node namedNode = namedNodeMap.item(i);

              if (logger.isDebugEnabled()) {
                logger.debug("---Inside createCRMObject_Task: adding attribute name : " + namedNode.getNodeName() + " with value : "
                    + namedNode.getNodeValue());
              }

              /* Set node attribute */
              cursor.insertAttributeWithValue(namedNode.getNodeName(), namedNode.getNodeValue());
            }
          }

          /* Set node value */
          cursor.insertChars(element.getTextContent());

          /* Jump to next token */
          cursor.toNextToken();
        }
      }
    } finally {
      if (cursor != null) {
        cursor.dispose();
      }
    }
    return task;
  }

  /**
   * 
   * @param account
   * @param accountId
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final Contact createCRMObjectTypeContact(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    /* Create xml beans object */
    final Contact contact = Contact.Factory.newInstance();

    /* Create new cursor */
    XmlCursor cursor = null;
    try {
      cursor = contact.newCursor();
      cursor.toFirstContentToken();
      final Node node = cursor.getDomNode();

      /* Get main namespace URl */
      if (node != null) {
        final String nsURL = node.getNamespaceURI();

        /* Iterate on the Element list and create SOAP object */
        for (final Element element : cADbject.getXmlContent()) {

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside createCRMObject_Contact: adding node : " + element.getNodeName() + " with value : " + element.getTextContent());
          }

          /* Get crm field */
          final String crmField = element.getNodeName();

          String crmFieldName = null;

          /* If field contains the type information */
          if (crmField != null && crmField.contains(crmFieldIntraSeparator)) {

            /* Split the field and get name */
            crmFieldName = crmField.split(crmFieldIntraSeparator)[0];
          } else {
            crmFieldName = crmField;
          }

          /* Add all new nodes */
          cursor.beginElement(new QName(nsURL, crmFieldName));

          /* Set node attributes */
          if (element.getAttributes() != null) {

            /* Get attribute map */
            final NamedNodeMap namedNodeMap = element.getAttributes();

            /* Interate over map */
            for (int i = 0; i < namedNodeMap.getLength(); i++) {

              /* Get respective item */
              final Node namedNode = namedNodeMap.item(i);

              if (logger.isDebugEnabled()) {
                logger.debug("---Inside createCRMObject_Contact: adding attribute name : " + namedNode.getNodeName() + " with value: "
                    + namedNode.getNodeValue());
              }

              /* Set node attribute */
              cursor.insertAttributeWithValue(namedNode.getNodeName(), namedNode.getNodeValue());
            }
          }

          /* Set node value */
          cursor.insertChars(element.getTextContent());

          /* Jump to next token */
          cursor.toNextToken();
        }
      }
    } finally {
      if (cursor != null) {
        cursor.dispose();
      }
    }
    return contact;
  }

  /**
   * 
   * @param account
   * @param accountId
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final Lead createCRMObjectTypeLead(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    /* Create xml beans object */
    final Lead lead = Lead.Factory.newInstance();

    /* Create new cursor */
    XmlCursor cursor = null;
    try {
      cursor = lead.newCursor();
      cursor.toFirstContentToken();
      final Node node = cursor.getDomNode();

      /* Get main namespace URl */
      if (node != null) {

        final String nsURL = node.getNamespaceURI();

        /* Iterate on the Element list and create SOAP object */
        for (final Element element : cADbject.getXmlContent()) {
          if (logger.isDebugEnabled()) {
            logger.debug("---Inside createCRMObject_Lead: adding node : " + element.getNodeName() + " with value : " + element.getTextContent());
          }

          /* Get crm field */
          final String crmField = element.getNodeName();

          String crmFieldName = null;

          /* If field contains the type information */
          if (crmField != null && crmField.contains(crmFieldIntraSeparator)) {

            /* Split the field and get name */
            crmFieldName = crmField.split(crmFieldIntraSeparator)[0];
          } else {
            crmFieldName = crmField;
          }

          /* Add all new nodes */
          cursor.beginElement(new QName(nsURL, crmFieldName));

          /* Set node attributes */
          if (element.getAttributes() != null) {

            /* Get attribute map */
            final NamedNodeMap namedNodeMap = element.getAttributes();

            /* Interate over map */
            for (int i = 0; i < namedNodeMap.getLength(); i++) {

              /* Get respective item */
              final Node namedNode = namedNodeMap.item(i);

              if (logger.isDebugEnabled()) {
                logger.debug("---Inside createCRMObject_Lead: adding attribute name : " + namedNode.getNodeName() + " with value: "
                    + namedNode.getNodeValue());
              }

              /* Set node attribute */
              cursor.insertAttributeWithValue(namedNode.getNodeName(), namedNode.getNodeValue());
            }
          }

          /* Set node value */
          cursor.insertChars(element.getTextContent());

          /* Jump to next token */
          cursor.toNextToken();
        }
      }
    } finally {
      if (cursor != null) {
        cursor.dispose();
      }
    }
    return lead;
  }

  /**
   * 
   * @param account
   * @param accountId
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final Opportunity createCRMObjectTypeOpportunity(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    /* Create xml beans object */
    final Opportunity opportunity = Opportunity.Factory.newInstance();

    /* Create new cursor */
    XmlCursor cursor = null;
    try {
      cursor = opportunity.newCursor();
      cursor.toFirstContentToken();
      final Node node = cursor.getDomNode();

      /* Get main namespace URl */
      if (node != null) {

        final String nsURL = node.getNamespaceURI();

        /* Iterate on the Element list and create SOAP object */
        for (final Element element : cADbject.getXmlContent()) {

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside createCRMObject_Opportunity: adding node : " + element.getNodeName() + " with value : "
                + element.getTextContent());
          }

          /* Get crm field */
          final String crmField = element.getNodeName();

          String crmFieldName = null;

          /* If field contains the type information */
          if (crmField != null && crmField.contains(crmFieldIntraSeparator)) {

            /* Split the field and get name */
            crmFieldName = crmField.split(crmFieldIntraSeparator)[0];
          } else {
            crmFieldName = crmField;
          }

          /* Add all new nodes */
          cursor.beginElement(new QName(nsURL, crmFieldName));

          /* Set node attributes */
          if (element.getAttributes() != null) {

            /* Get attribute map */
            final NamedNodeMap namedNodeMap = element.getAttributes();

            /* Interate over map */
            for (int i = 0; i < namedNodeMap.getLength(); i++) {

              /* Get respective item */
              final Node namedNode = namedNodeMap.item(i);

              if (logger.isDebugEnabled()) {
                logger.debug("---Inside createCRMObject_Opportunity: adding attribute name : " + namedNode.getNodeName() + " with value: "
                    + namedNode.getNodeValue());
              }

              /* Set node attribute */
              cursor.insertAttributeWithValue(namedNode.getNodeName(), namedNode.getNodeValue());
            }
          }

          /* Set node value */
          cursor.insertChars(element.getTextContent());

          /* Jump to next token */
          cursor.toNextToken();
        }
      }
    } finally {
      if (cursor != null) {
        cursor.dispose();
      }
    }
    return opportunity;
  }

  /**
   * 
   * @param account
   * @param accountId
   * @param cADbject
   * @return
   * @throws Exception
   */
  public static final Incident createCRMObjectTypeIncident(final ScribeObject cADbject, final String crmFieldIntraSeparator) throws Exception {

    /* Create xml beans object */
    /* In MS CRM 'Incident' is actually a 'Case' */
    final Incident caseObject = Incident.Factory.newInstance();

    /* Create new cursor */
    XmlCursor cursor = null;
    try {
      cursor = caseObject.newCursor();
      cursor.toFirstContentToken();
      final Node node = cursor.getDomNode();

      /* Get main namespace URl */
      if (node != null) {
        final String nsURL = node.getNamespaceURI();

        /* Iterate on the Element list and create SOAP object */
        for (final Element element : cADbject.getXmlContent()) {

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside createCRMObject_Incident: adding node : " + element.getNodeName() + " with value : " + element.getTextContent());
          }

          /* Get crm field */
          final String crmField = element.getNodeName();

          String crmFieldName = null;

          /* If field contains the type information */
          if (crmField != null && crmField.contains(crmFieldIntraSeparator)) {

            /* Split the field and get name */
            crmFieldName = crmField.split(crmFieldIntraSeparator)[0];
          } else {
            crmFieldName = crmField;
          }

          /* Add all new nodes */
          cursor.beginElement(new QName(nsURL, crmFieldName));

          /* Set node attributes */
          if (element.getAttributes() != null) {

            /* Get attribute map */
            final NamedNodeMap namedNodeMap = element.getAttributes();

            /* Interate over map */
            for (int i = 0; i < namedNodeMap.getLength(); i++) {

              /* Get respective item */
              final Node namedNode = namedNodeMap.item(i);

              if (logger.isDebugEnabled()) {
                logger.debug("---Inside createCRMObject_Incident: adding attribute name : " + namedNode.getNodeName() + " with value: "
                    + namedNode.getNodeValue());
              }

              /* Set node attribute */
              cursor.insertAttributeWithValue(namedNode.getNodeName(), namedNode.getNodeValue());
            }
          }

          /* Set node value */
          cursor.insertChars(element.getTextContent());

          /* Jump to next token */
          cursor.toNextToken();
        }
      }
    } finally {
      if (cursor != null) {
        cursor.dispose();
      }
    }
    return caseObject;
  }

  public static interface MSV5DataTypes {

    public static final String WHOLE_NUMBER = "WholeNumber";
    public static final String TEXT = "Text";
    public static final String NUMBER = "Number";
    public static final String MULTIPLE_LIST = "MultipleList";
    public static final String OPTION_SET = "OptionSet";
    public static final String DATE_TIME = "DateTime";
    public static final String TWO_OPTIONS = "TwoOptions";
    public static final String FLOATING_POINT = "FloatingPoint";
    public static final String CURRENCY = "Currency";

    /* CRM object Task fields */
    public static final String regardingObjectId = "REGARDINGOBJECTID";
  }

  public static interface MSV5NodeTypes {

    /* CRM object Task fields */
    public static final String regardingObjectId = "REGARDINGOBJECTID";
  }
}
