package com.inbravo.cad.rest.service.validator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.resource.CADCommandObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CADRequestValidator {

  private final Logger logger = Logger.getLogger(CADRequestValidator.class.getName());

  private String urlEncodingType;

  private String agentIdSplitCharacter;

  private String enableQueryParamDecoding;

  private String tenantSchemaConst = "$tenantDBSchema$";

  /**
   * 
   * @param eDSACommandObject
   * @param ObjectType
   * @param checkInputObject
   * @throws CADException
   */
  public final void validateRequestObject(final CADCommandObject eDSACommandObject, final String ObjectType, final boolean checkInputObject)
      throws Exception {

    if (eDSACommandObject != null) {

      logger.debug("---Inside validateRequestObject Object type: " + ObjectType);

      /* Create Exception if both Tenant/Agent are null */
      if (eDSACommandObject.getCrmUserId() == null && eDSACommandObject.getCrmPassword() == null && eDSACommandObject.getCrmType() == null) {

        /* Inform user about invalid request */
        throw new CADException(CADResponseCodes._1008 + "CRM information is not present in request");
      }

      /* Create Exception if agent is in perfect shape */
      if (eDSACommandObject.getCrmUserId() != null) {

        /* Inform user about invalid request */
        throw new CADException(CADResponseCodes._1008 + "CRM User information is not valid");
      }

      /* Check for a valid input Object */
      if (checkInputObject) {
        logger.debug("---Inside validateRequestObject checking the input CAD object");

        /* Check is user id is valid */
        if (eDSACommandObject.getcADObject() == null) {
          logger.debug("---Inside validateRequestObject no CAD object in request");

          /* Inform user about invalid request */
          throw new CADException(CADResponseCodes._1008 + "CRM object information is not present in request");
        }

        /* Check is user id is valid */
        if (eDSACommandObject.getcADObject()[0] == null) {
          logger.debug("---Inside validateRequestObject invalid CAD object in request");

          /* Inform user about invalid request */
          throw new CADException(CADResponseCodes._1008 + "CRM object information is not valid");
        }

        /* Check is user id is valid */
        if (eDSACommandObject.getcADObject()[0].getXmlContent() == null) {
          logger.debug("---Inside validateRequestObject invalid XML content in request");

          /* Inform user about invalid request */
          throw new CADException(CADResponseCodes._1008 + "CRM object information is not valid");
        }
      }
    } else {
      /* Inform user about invalid request */
      throw new CADException(CADResponseCodes._1008 + "Invalid request");
    }
  }

  /**
   * This is required for double decoding of request param
   * 
   * @param requestString
   * @return
   * @throws Exception
   */
  public final String decodeRequestParam(final String requestString) throws Exception {

    logger.debug("---Inside decodeRequestParam input requestString: " + requestString + " & enableQueryParamDecoding: " + enableQueryParamDecoding);

    String outRequestString = null;

    /* Decode query/select/order params if permitted by system settings */
    if (this.getQueryParamDecodingStatus()) {

      try {
        /* Encode query */
        outRequestString = URLDecoder.decode(requestString, urlEncodingType);
      } catch (final UnsupportedEncodingException e) {
        throw new CADException(CADResponseCodes._1003 + "URL encoding is not supported. Please provide format: " + urlEncodingType);
      }
    } else {
      outRequestString = requestString;
    }

    logger.debug("---Inside decodeRequestParam output requestString: " + outRequestString);

    return outRequestString;
  }

  public final void validateSessionRequest(final String id) {
    logger.debug("---Inside validateSessionRequest session object id: " + id);

    /* Check if id is not null or emptry string */
    if (id == null || "".equalsIgnoreCase(id)) {

      /* Inform user about invalid request */
      throw new CADException(CADResponseCodes._1008 + "Agent/Tenant id is not found in request");
    }
  }

  public final String getAgentIdSplitCharacter() {
    return agentIdSplitCharacter;
  }

  public final void setAgentIdSplitCharacter(final String agentIdSplitCharacter) {
    this.agentIdSplitCharacter = agentIdSplitCharacter;
  }

  public final String getEnableQueryParamDecoding() {
    return enableQueryParamDecoding;
  }

  public final void setEnableQueryParamDecoding(final String enableQueryParamDecoding) {
    this.enableQueryParamDecoding = enableQueryParamDecoding;
  }

  public final String getUrlEncodingType() {
    return urlEncodingType;
  }

  public final void setUrlEncodingType(final String urlEncodingType) {
    this.urlEncodingType = urlEncodingType;
  }

  private final boolean getQueryParamDecodingStatus() {

    /* Check if this is enabled */
    if (enableQueryParamDecoding != null) {

      /* Check if this is yes */
      if (enableQueryParamDecoding.equalsIgnoreCase("YES") || enableQueryParamDecoding.equalsIgnoreCase("TRUE")) {
        return true;
      } else {

        /* If not enabled return false */
        return false;
      }
    } else {

      /* Return false, if value not set */
      return false;
    }
  }

  /**
   * @return the tenantSchemaConst
   */
  public final String getTenantSchemaConst() {
    return this.tenantSchemaConst;
  }

  /**
   * @param tenantSchemaConst the tenantSchemaConst to set
   */
  public final void setTenantSchemaConst(final String tenantSchemaConst) {
    this.tenantSchemaConst = tenantSchemaConst;
  }
}
