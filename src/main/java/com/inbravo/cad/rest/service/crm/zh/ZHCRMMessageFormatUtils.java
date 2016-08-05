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

package com.inbravo.cad.rest.service.crm.zh;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Element;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.constants.HTTPConstants;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.resource.CADObject;
import com.inbravo.cad.rest.service.crm.CRMMessageFormatUtils;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZHCRMMessageFormatUtils extends CRMMessageFormatUtils {

  private static final Logger logger = Logger.getLogger(ZHCRMMessageFormatUtils.class.getName());

  private static final String zhQueryParamSeperator = "|";
  private static final String equalOperator = "=";
  private static final String notEqualOperator = "!=";
  private static final String lessThanOperator = "<";
  private static final String greaterThanOperator = ">";
  private static final String lessThanEqualOperator = "<=";
  private static final String greaterThanEqualOperator = ">=";
  private static final String parenthesisStartOperator = "(";
  private static final String parenthesisEndOperator = ")";
  private static final String zhNotEqualOperator = "<>";
  private static final String zhLikeOperator = "contains";
  private static final String zhGreaterThanEqualOperator = "=>";
  private static final String zHCallObject = "call";
  private static final String zHCallObjectFieldCallStartTime = "call start time";

  private ZHCRMMessageFormatUtils() {

  }

  /**
   * 
   * @param edsaQuery
   * @return
   */
  public final static String createZHQuery(final String edsaQuery) {

    String zhQuery = "";

    if (edsaQuery != null) {

      /* Tokenize the query clause */
      final StringTokenizer stringTokenizer =
          new StringTokenizer(edsaQuery, HTTPConstants.andClause + HTTPConstants.orClause + parenthesisStartOperator + parenthesisEndOperator, true);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {
        String tempElement = stringTokenizer.nextToken();
        logger.debug("---Inside createZHQuery: tempElement: " + tempElement);

        tempElement = tempElement.trim();

        /* Check for '<=' operator */
        if (tempElement.contains(lessThanEqualOperator)) {

          /* Convert it to upper case before compare */
          zhQuery =
              zhQuery + tempElement.split(lessThanEqualOperator)[0].trim() + zhQueryParamSeperator + "<=" + zhQueryParamSeperator
                  + tempElement.split(lessThanEqualOperator)[1];
        }

        /* Check for '<' operator */
        else if (tempElement.contains(lessThanOperator)) {

          zhQuery =
              zhQuery + tempElement.split(lessThanOperator)[0].trim() + zhQueryParamSeperator + "<" + zhQueryParamSeperator
                  + tempElement.split(lessThanOperator)[1];
        }

        /* Check for '>=' operator */
        else if (tempElement.contains(greaterThanEqualOperator)) {

          zhQuery =
              zhQuery + tempElement.split(greaterThanEqualOperator)[0].trim() + zhQueryParamSeperator + zhGreaterThanEqualOperator
                  + zhQueryParamSeperator + tempElement.split(greaterThanEqualOperator)[1];
        }

        /* Check for '>' operator */
        else if (tempElement.contains(greaterThanOperator)) {

          zhQuery =
              zhQuery + tempElement.split(greaterThanOperator)[0].trim() + zhQueryParamSeperator + ">" + zhQueryParamSeperator
                  + tempElement.split(greaterThanOperator)[1];
        }

        /* Check for '!=' operator */
        else if (tempElement.contains(notEqualOperator)) {

          zhQuery =
              zhQuery + tempElement.split(notEqualOperator)[0].trim() + zhQueryParamSeperator + zhNotEqualOperator + zhQueryParamSeperator
                  + tempElement.split(notEqualOperator)[1];
        }

        /* Check for '=' operator */
        else if (tempElement.contains(equalOperator)) {

          zhQuery =
              zhQuery + tempElement.split(equalOperator)[0].trim() + zhQueryParamSeperator + "=" + zhQueryParamSeperator
                  + tempElement.split(equalOperator)[1];
        }

        /* Check for ='like' operator */
        else if (tempElement.toUpperCase().contains(likeOperator)) {

          zhQuery =
              zhQuery + tempElement.split("(?i)" + likeOperator)[0].trim() + zhQueryParamSeperator + zhLikeOperator + zhQueryParamSeperator
                  + tempElement.split("(?i)" + likeOperator)[1];
        }

        /* Check for ')' */
        else if (tempElement.contains(parenthesisEndOperator)) {
          zhQuery = zhQuery + " ) ";
        }

        /* Check for '(' */
        else if (tempElement.contains(parenthesisStartOperator)) {
          zhQuery = zhQuery + " ( ";
        } else {
          zhQuery = zhQuery + tempElement;
        }

        /* Zoho CRM allows only one clause per query */
        break;
      }
    }
    logger.debug("---Inside createZHQuery, zhQuery: " + zhQuery);
    return zhQuery;
  }

  /**
   * 
   * @param edsaQuery
   * @return
   */
  public final static String createZHQueryForPhoneFields(final String edsaQuery, final String phoneFieldName) {

    String zhQuery = "";

    if (edsaQuery != null) {

      /* Tokenize the query clause */
      final StringTokenizer stringTokenizer =
          new StringTokenizer(edsaQuery, HTTPConstants.andClause + HTTPConstants.orClause + parenthesisStartOperator + parenthesisEndOperator, true);

      /* Iterate over tokenizer */
      while (stringTokenizer.hasMoreElements()) {
        String tempElement = stringTokenizer.nextToken();
        logger.debug("---Inside createZHQueryForPhoneFields: tempElement: " + tempElement);

        tempElement = tempElement.trim();

        /* Check for '<=' operator */
        if (tempElement.contains(lessThanEqualOperator)) {

          /* Convert it to upper case before compare */
          zhQuery =
              zhQuery + phoneFieldName.trim() + zhQueryParamSeperator + "<=" + zhQueryParamSeperator + tempElement.split(lessThanEqualOperator)[1];
        }

        /* Check for '<' operator */
        else if (tempElement.contains(lessThanOperator)) {

          zhQuery = zhQuery + phoneFieldName.trim() + zhQueryParamSeperator + "<" + zhQueryParamSeperator + tempElement.split(lessThanOperator)[1];
        }

        /* Check for '>=' operator */
        else if (tempElement.contains(greaterThanEqualOperator)) {

          zhQuery =
              zhQuery + phoneFieldName.trim() + zhQueryParamSeperator + zhGreaterThanEqualOperator + zhQueryParamSeperator
                  + tempElement.split(greaterThanEqualOperator)[1];
        }

        /* Check for '>' operator */
        else if (tempElement.contains(greaterThanOperator)) {

          zhQuery = zhQuery + phoneFieldName.trim() + zhQueryParamSeperator + ">" + zhQueryParamSeperator + tempElement.split(greaterThanOperator)[1];
        }

        /* Check for '!=' operator */
        else if (tempElement.contains(notEqualOperator)) {

          zhQuery =
              zhQuery + phoneFieldName.trim() + zhQueryParamSeperator + zhNotEqualOperator + zhQueryParamSeperator
                  + tempElement.split(notEqualOperator)[1];
        }

        /* Check for '=' operator */
        else if (tempElement.contains(equalOperator)) {

          zhQuery = zhQuery + phoneFieldName.trim() + zhQueryParamSeperator + "=" + zhQueryParamSeperator + tempElement.split(equalOperator)[1];
        }

        /* Check for ='like' operator */
        else if (tempElement.toUpperCase().contains(likeOperator)) {

          zhQuery =
              zhQuery + phoneFieldName.trim() + zhQueryParamSeperator + zhLikeOperator + zhQueryParamSeperator
                  + tempElement.split("(?i)" + likeOperator)[1];
        }

        /* Check for ')' */
        else if (tempElement.contains(parenthesisEndOperator)) {
          zhQuery = zhQuery + " ) ";
        }

        /* Check for '(' */
        else if (tempElement.contains(parenthesisStartOperator)) {
          zhQuery = zhQuery + " ( ";
        } else {
          zhQuery = zhQuery + tempElement;
        }

        /* Zoho CRM allows only one clause per query */
        break;
      }
    }
    logger.debug("---Inside createZHQueryForPhoneFields, zhQuery: " + zhQuery);
    return zhQuery;
  }

  /**
   * 
   * @param cADCommandObject
   * @param edsaSelect
   * @return
   */
  public final static String createZHSelect(final CADCommandObject cADCommandObject, final String edsaSelect) {

    String zhSelect = "";

    if (edsaSelect != null) {
      zhSelect = zhSelect + cADCommandObject.getObjectType() + "s(" + edsaSelect + ")";
    }

    logger.debug("---Inside createZHSelect, zhSelect: " + zhSelect);
    return zhSelect;
  }

  /**
   * 
   * @param orderClause
   * @param orderFieldsSeparator
   * @return
   */
  public final static String createZHSortColumnString(final String orderClause, final String orderFieldsSeparator) {

    String zhSortColumnString = "";

    if (orderClause != null) {
      zhSortColumnString = zhSortColumnString + orderClause.split(orderFieldsSeparator)[0];
    }

    logger.debug("---Inside createZHSortColumnString, sort field: " + zhSortColumnString);
    return zhSortColumnString;
  }

  /**
   * 
   * @param orderClause
   * @param orderFieldsSeparator
   * @return
   */
  public final static String createZHSortOrderString(final String orderClause, final String orderFieldsSeparator) {

    String zhSortOrderString = "";

    if (orderClause != null) {
      zhSortOrderString = zhSortOrderString + orderClause.split(orderFieldsSeparator)[1].toLowerCase();
    }

    logger.debug("---Inside createZHSortOrderString, order of sorting: " + zhSortOrderString);
    return zhSortOrderString;
  }

  /**
   * 
   * @param cADCommandObject
   * @return
   */
  public static final String createRequestString(final CADCommandObject cADCommandObject, final String spaceCharReplacement,
      final String permittedDateFormats, final String zHInputDateFormat) {

    if (cADCommandObject.getcADObject() != null && cADCommandObject.getcADObject().length == 1) {

      final CADObject cADbject = cADCommandObject.getcADObject()[0];
      final List<Element> elementList = cADbject.getXmlContent();

      /* Add start tag */
      String reqString = "<" + cADCommandObject.getObjectType() + "s><row no='1'>";

      /* Iterate over element list */
      for (final Element element : elementList) {

        /* Get node name/values */
        String nodeName = element.getNodeName().trim();
        String nodeValue = element.getTextContent();

        /* Replace space char */
        if (nodeName.contains(spaceCharReplacement)) {

          nodeName = nodeName.replace(spaceCharReplacement, " ");
        }

        /* Check if object is Calls and field is Call Start Time */
        if ((cADCommandObject.getObjectType() != null && cADCommandObject.getObjectType().trim().equalsIgnoreCase(zHCallObject))
            && (nodeName != null && nodeName.equalsIgnoreCase(zHCallObjectFieldCallStartTime))) {

          /* Update node value */
          nodeValue = changeToZHDateFormat(nodeValue, permittedDateFormats, zHInputDateFormat);
        }

        /* Add nodes */
        reqString = reqString + "<FL val='" + nodeName + "'>" + nodeValue + "</FL>";
      }

      /* Add end tag */
      reqString = reqString + "</row></" + cADCommandObject.getObjectType() + "s>";

      logger.debug("---Inside createRequestString reqString: " + reqString);
      return reqString;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param zHObjectType
   * @param iFieldName
   * @param iFieldValue
   * @param permittedDateFormats
   * @param zHInputDateFormat
   * @return
   * @throws Exception
   */
  private static final String changeToZHDateFormat(final String iFieldValue, final String permittedDateFormats, final String zHInputDateFormat) {

    /* Change to ZOHO specific date format */
    try {
      final DateTime iDT = validateInputDate(iFieldValue, permittedDateFormats);

      /* Create ZH specific date formatter */
      final DateTimeFormatter zhDateTimeFormatter = DateTimeFormat.forPattern(zHInputDateFormat);

      /* Format the date to ZH specific */
      return zhDateTimeFormatter.print(iDT);

    } catch (final Exception e) {
      throw new CADException(CADResponseCodes._1003 + "Following date input: " + iFieldValue + " is not acceptable at CAD", e);
    }
  }

  public static void main(String[] args) {

    final DateTimeFormatter zhDateTimeFormatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss Z");

    System.out.println(zhDateTimeFormatter.parseDateTime("2012/11/02 01:00:19 -0700"));

  }
}
