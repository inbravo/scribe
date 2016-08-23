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

package com.inbravo.scribe.rest.service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.constants.HTTPConstants;
import com.inbravo.scribe.rest.resource.ScribeCommandObject;
import com.inbravo.scribe.rest.service.basic.ScribeInternalService;
import com.inbravo.scribe.rest.service.log.MutualDiagnosticLogService;
import com.inbravo.scribe.rest.service.validator.ScribeRequestValidator;

/**
 * The core CRM adaptor service
 * 
 * @author amit.dixit
 * 
 */
@Path(HTTPConstants.object)
@Produces(HTTPConstants.mimeTypeXML)
public final class ScribeObjectService {

  private final Logger logger = Logger.getLogger(ScribeObjectService.class.getName());

  @Resource
  private HttpServletRequest httpServletRequest;

  private ScribeInternalService scribeInternalService;

  private ScribeRequestValidator scribeRequestValidator;

  private MutualDiagnosticLogService mutualDiagnosticLogService;

  /**
   * This API will do dynamic query based on the select criteria obtained from user. User will also
   * send the where clause of CRM query. Following method will be applicable in the case of Sales
   * Force only
   * 
   * @return
   * @throws Exception
   */
  @GET
  @Path(HTTPConstants.PahForObjectTypeByQueryAndSelect)
  @Produces({HTTPConstants.mimeTypeJSON, HTTPConstants.mimeTypeXML, HTTPConstants.mimeTypeOctetStream})
  public final Response getObjects(@QueryParam("") ScribeCommandObject cADCommandObject, final @PathParam(HTTPConstants.query) String query,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType, final @PathParam(HTTPConstants.select) String select) throws Exception {

    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, Scribe-TransId : [" + transactionId
        + "]; Ext-TransId : [" + cADCommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjects select specific CRM fields with dynamic query. Object type: " + ObjectType);

    try {

      /* Set transaction id */
      cADCommandObject.setIntTansId("" + transactionId);

      /* Set transaction info in logs */
      mutualDiagnosticLogService.addTransactionInfo(cADCommandObject);

      /* Update the request object for requestObject from URL */
      cADCommandObject.setObjectType(ObjectType);

      /* Validate the request */
      scribeRequestValidator.validateRequestObject(cADCommandObject, ObjectType, false);

      cADCommandObject =
          scribeInternalService.getServiceFactory(cADCommandObject).getService(cADCommandObject)
              .getObjects(cADCommandObject, scribeRequestValidator.decodeRequestParam(query), scribeRequestValidator.decodeRequestParam(select));


      if (cADCommandObject == null) {

        /* Inform user that object not found */
        throw new ScribeException(ScribeResponseCodes._1004 + ObjectType);
      }

      /* Update the object for removing requestObject */
      cADCommandObject.setObjectType(null);

      logger.info("==**== Transaction completed, Scribe-TransId : [" + transactionId + "]; Ext-TransId : [" + cADCommandObject.getExtTransId()
          + "] ==**== ");

    } finally {

      /* Set transaction info in logs */
      mutualDiagnosticLogService.discardTransactionInfo();
    }

    return Response.ok(cADCommandObject).build();
  }

  /**
   * This API will do dynamic query based on the select criteria obtained from user. User will also
   * send the where clause of CRM query. Following method will be applicable in the case of Sales
   * Force only
   * 
   * @return
   * @throws Exception
   */
  @GET
  @Path(HTTPConstants.PahForObjectTypeByQueryAndSelectAndOrder)
  @Produces({HTTPConstants.mimeTypeJSON, HTTPConstants.mimeTypeXML})
  public final Response getObjects(@QueryParam("") ScribeCommandObject cADCommandObject, final @PathParam(HTTPConstants.query) String query,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType, final @PathParam(HTTPConstants.select) String select,
      final @PathParam(HTTPConstants.order) String order) throws Exception {

    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, Scribe-TransId : [" + transactionId
        + "]; Ext-TransId : [" + cADCommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjects select specific CRM fields with dynamic query and order. Object type: " + ObjectType);

    try {

      /* Set transaction id */
      cADCommandObject.setIntTansId("" + transactionId);

      /* Set transaction info in logs */
      mutualDiagnosticLogService.addTransactionInfo(cADCommandObject);

      /* Update the request object for requestObject from URL */
      cADCommandObject.setObjectType(ObjectType);

      /* Validate the request */
      scribeRequestValidator.validateRequestObject(cADCommandObject, ObjectType, false);

      cADCommandObject =
          scribeInternalService
              .getServiceFactory(cADCommandObject)
              .getService(cADCommandObject)
              .getObjects(cADCommandObject, scribeRequestValidator.decodeRequestParam(query), scribeRequestValidator.decodeRequestParam(select),
                  scribeRequestValidator.decodeRequestParam(order));


      if (cADCommandObject == null) {
        /* Inform user that object not found */
        throw new ScribeException(ScribeResponseCodes._1004 + ObjectType);
      }

      /* Update the object for removing requestObject */
      cADCommandObject.setObjectType(null);

      logger.info("==**== Transaction completed, Scribe-TransId : [" + transactionId + "]; Ext-TransId : [" + cADCommandObject.getExtTransId()
          + "] ==**== ");

    } finally {

      /* Set transaction info in logs */
      mutualDiagnosticLogService.discardTransactionInfo();
    }

    return Response.ok(cADCommandObject).build();
  }

  /**
   * This API will do dynamic query. User will also send the where clause of CRM query. Following
   * method will be applicable in the case of Sales Force only
   * 
   * @return
   * @throws Exception
   */
  @GET
  @Path(HTTPConstants.PahForObjectType)
  @Produces({HTTPConstants.mimeTypeJSON, HTTPConstants.mimeTypeXML})
  public final Response getObjects(@QueryParam("") ScribeCommandObject cADCommandObject, final @PathParam(HTTPConstants.ObjectType) String ObjectType)
      throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, Scribe-TransId : [" + transactionId
        + "]; Ext-TransId : [" + cADCommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjects select all CRM fields. Object type: " + ObjectType);

    try {

      /* Set transaction id */
      cADCommandObject.setIntTansId("" + transactionId);

      /* Set transaction info in logs */
      mutualDiagnosticLogService.addTransactionInfo(cADCommandObject);

      /* Update the request object for requestObject from URL */
      cADCommandObject.setObjectType(ObjectType);

      /* Validate the request */
      scribeRequestValidator.validateRequestObject(cADCommandObject, ObjectType, false);

      cADCommandObject = scribeInternalService.getServiceFactory(cADCommandObject).getService(cADCommandObject).getObjects(cADCommandObject);

      if (cADCommandObject == null) {
        /* Inform user that object not found */
        throw new ScribeException(ScribeResponseCodes._1004 + ObjectType);
      }

      /* Update the object for removing requestObject */
      cADCommandObject.setObjectType(null);

      logger.info("==**== Transaction completed, Scribe-TransId : [" + transactionId + "]; Ext-TransId : [" + cADCommandObject.getExtTransId()
          + "] ==**== ");

    } finally {

      /* Set transaction info in logs */
      mutualDiagnosticLogService.discardTransactionInfo();
    }

    return Response.ok(cADCommandObject).build();
  }

  /**
   * This API will count number of objects in the dynamic query.
   * 
   * @return
   * @throws Exception
   */
  @GET
  @Path(HTTPConstants.PahForObjectTypesCount)
  @Produces({HTTPConstants.mimeTypeJSON, HTTPConstants.mimeTypeXML})
  public final Response getObjectsCount(@QueryParam("") ScribeCommandObject cADCommandObject,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType) throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, Scribe-TransId : [" + transactionId
        + "]; Ext-TransId : [" + cADCommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getAllObjectsCount Object type: " + ObjectType);

    try {

      /* Set transaction id */
      cADCommandObject.setIntTansId("" + transactionId);

      /* Set transaction info in logs */
      mutualDiagnosticLogService.addTransactionInfo(cADCommandObject);

      /* Update the request object for requestObject from URL */
      cADCommandObject.setObjectType(ObjectType);

      /* Validate the request */
      scribeRequestValidator.validateRequestObject(cADCommandObject, ObjectType, false);

      cADCommandObject = scribeInternalService.getServiceFactory(cADCommandObject).getService(cADCommandObject).getObjectsCount(cADCommandObject);

      if (cADCommandObject == null) {
        /* Inform user that object not found */
        throw new ScribeException(ScribeResponseCodes._1004 + ObjectType);
      }

      /* Update the object for removing requestObject */
      cADCommandObject.setObjectType(null);

      logger.info("==**== Transaction completed, Scribe-TransId : [" + transactionId + "]; Ext-TransId : [" + cADCommandObject.getExtTransId()
          + "] ==**== ");

    } finally {

      /* Set transaction info in logs */
      mutualDiagnosticLogService.discardTransactionInfo();
    }

    return Response.ok(cADCommandObject).build();
  }

  /**
   * This API will do CRM query. User can also send the where clause of CRM query.
   * 
   * @return
   * @throws Exception
   */
  @GET
  @Path(HTTPConstants.PahForObjectTypeByQuery)
  @Produces({HTTPConstants.mimeTypeJSON, HTTPConstants.mimeTypeXML})
  public final Response getObjects(@QueryParam("") ScribeCommandObject cADCommandObject, final @PathParam(HTTPConstants.query) String query,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType) throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, Scribe-TransId : [" + transactionId
        + "]; Ext-TransId : [" + cADCommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjects select all CRM fields by dynamic query. Object type: " + ObjectType);

    try {

      /* Set transaction id */
      cADCommandObject.setIntTansId("" + transactionId);

      /* Set transaction info in logs */
      mutualDiagnosticLogService.addTransactionInfo(cADCommandObject);

      /* Update the request object for requestObject from URL */
      cADCommandObject.setObjectType(ObjectType);

      /* Validate the request */
      scribeRequestValidator.validateRequestObject(cADCommandObject, ObjectType, false);

      cADCommandObject =
          scribeInternalService.getServiceFactory(cADCommandObject).getService(cADCommandObject)
              .getObjects(cADCommandObject, scribeRequestValidator.decodeRequestParam(query));

      if (cADCommandObject == null) {
        /* Inform user that object not found */
        throw new ScribeException(ScribeResponseCodes._1004 + ObjectType);
      }

      /* Update the object for removing requestObject */
      cADCommandObject.setObjectType(null);

      logger.info("==**== Transaction completed, Scribe-TransId : [" + transactionId + "]; Ext-TransId : [" + cADCommandObject.getExtTransId()
          + "] ==**== ");
    } finally {

      /* Set transaction info in logs */
      mutualDiagnosticLogService.discardTransactionInfo();
    }

    return Response.ok(cADCommandObject).build();
  }

  /**
   * This API will do count the number of objects in CRM query.
   * 
   * @return
   * @throws Exception
   */
  @GET
  @Path(HTTPConstants.PahForObjectCountByQuery)
  @Produces({HTTPConstants.mimeTypeJSON, HTTPConstants.mimeTypeXML})
  public final Response getObjectsCount(@QueryParam("") ScribeCommandObject cADCommandObject, final @PathParam(HTTPConstants.query) String query,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType) throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, Scribe-TransId : [" + transactionId
        + "]; Ext-TransId : [" + cADCommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjectsCount Object type: " + ObjectType);

    try {

      /* Set transaction id */
      cADCommandObject.setIntTansId("" + transactionId);

      /* Set transaction info in logs */
      mutualDiagnosticLogService.addTransactionInfo(cADCommandObject);

      /* Update the request object for requestObject from URL */
      cADCommandObject.setObjectType(ObjectType);

      /* Validate the request */
      scribeRequestValidator.validateRequestObject(cADCommandObject, ObjectType, false);

      cADCommandObject =
          scribeInternalService.getServiceFactory(cADCommandObject).getService(cADCommandObject)
              .getObjectsCount(cADCommandObject, scribeRequestValidator.decodeRequestParam(query));

      if (cADCommandObject == null) {
        /* Inform user that object not found */
        throw new ScribeException(ScribeResponseCodes._1004 + ObjectType);
      }

      /* Update the object for removing requestObject */
      cADCommandObject.setObjectType(null);

      logger.info("==**== Transaction completed, Scribe-TransId : [" + transactionId + "]; Ext-TransId : [" + cADCommandObject.getExtTransId()
          + "] ==**== ");

    } finally {

      /* Set transaction info in logs */
      mutualDiagnosticLogService.discardTransactionInfo();
    }

    return Response.ok(cADCommandObject).build();
  }

  /**
   * 
   * @return
   * @throws Exception
   */
  @POST
  @Path(HTTPConstants.PahForObjectType)
  @Consumes(HTTPConstants.mimeTypeXML)
  @Produces({HTTPConstants.mimeTypeXML, HTTPConstants.mimeTypeJSON})
  public final Response createObject(ScribeCommandObject cADCommandObject, final @PathParam(HTTPConstants.ObjectType) String ObjectType)
      throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, Scribe-TransId : [" + transactionId
        + "]; Ext-TransId : [" + cADCommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside createObject object type: " + ObjectType);

    try {

      /* Set transaction id */
      cADCommandObject.setIntTansId("" + transactionId);

      /* Set transaction info in logs */
      mutualDiagnosticLogService.addTransactionInfo(cADCommandObject);

      /* Update the request object for requestObject from URL */
      cADCommandObject.setObjectType(ObjectType);

      /* Validate the request */
      scribeRequestValidator.validateRequestObject(cADCommandObject, ObjectType, true);

      cADCommandObject = scribeInternalService.getServiceFactory(cADCommandObject).getService(cADCommandObject).createObject(cADCommandObject);

      /* Update the object for removing requestObject */
      cADCommandObject.setObjectType(null);

      logger.info("==**== Transaction completed, Scribe-TransId : [" + transactionId + "]; Ext-TransId : [" + cADCommandObject.getExtTransId()
          + "] ==**== ");

    } finally {

      /* Set transaction info in logs */
      mutualDiagnosticLogService.discardTransactionInfo();
    }

    return Response.ok(cADCommandObject).build();
  }

  /**
   * 
   * @return
   * @throws Exception
   */
  @PUT
  @Path(HTTPConstants.PahForObjectType)
  @Consumes(HTTPConstants.mimeTypeXML)
  @Produces({HTTPConstants.mimeTypeXML, HTTPConstants.mimeTypeJSON})
  public final Response updateObject(ScribeCommandObject cADCommandObject, final @PathParam(HTTPConstants.ObjectType) String ObjectType)
      throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, Scribe-TransId : [" + transactionId
        + "]; Ext-TransId : [" + cADCommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside updateObject object type: " + ObjectType);

    try {

      /* Set transaction id */
      cADCommandObject.setIntTansId("" + transactionId);

      /* Set transaction info in logs */
      mutualDiagnosticLogService.addTransactionInfo(cADCommandObject);

      /* Update the request object for requestObject from URL */
      cADCommandObject.setObjectType(ObjectType);

      /* Validate the request */
      scribeRequestValidator.validateRequestObject(cADCommandObject, ObjectType, true);

      cADCommandObject = scribeInternalService.getServiceFactory(cADCommandObject).getService(cADCommandObject).updateObject(cADCommandObject);

      /* Update the object for removing requestObject */
      cADCommandObject.setObjectType(null);

      logger.info("==**== Transaction completed, Scribe-TransId : [" + transactionId + "]; Ext-TransId : [" + cADCommandObject.getExtTransId()
          + "] ==**== ");

    } finally {

      /* Set transaction info in logs */
      mutualDiagnosticLogService.discardTransactionInfo();
    }

    return Response.ok(cADCommandObject).build();
  }

  /**
   * 
   * @return
   * @throws Exception
   */
  @DELETE
  @Path(HTTPConstants.PahForObjectTypeById)
  public final Response deleteObject(@PathParam(HTTPConstants.id) String idToBeDeleted, final @QueryParam(HTTPConstants.crmUserId) String agentId,
      final @QueryParam(HTTPConstants.crmUserId) String crmUserId, final @PathParam(HTTPConstants.ObjectType) String ObjectType) throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started : [" + transactionId + "] ==**==");
    logger.debug("---Inside deleteObject id to be deleted: " + idToBeDeleted);

    /* Create cADCommandObject */
    final ScribeCommandObject cADCommandObject = new ScribeCommandObject();

    /* Set transaction id */
    cADCommandObject.setIntTansId("" + transactionId);

    /* Set agent/tenant information */
    cADCommandObject.setCrmUserId(crmUserId);

    /* Update the request object for requestObject from URL */
    cADCommandObject.setObjectType(ObjectType);

    /* Validate the request */
    scribeRequestValidator.validateRequestObject(cADCommandObject, ObjectType, false);

    boolean status =
        scribeInternalService.getServiceFactory(cADCommandObject).getService(cADCommandObject)
            .deleteObject(cADCommandObject, scribeRequestValidator.decodeRequestParam(idToBeDeleted));

    /* Update the object for removing requestObject */
    cADCommandObject.setObjectType(null);

    logger.info("==**== Transaction completed: [" + transactionId + "] ==**==");

    if (status) {
      return Response.status(Response.Status.OK).type(MediaType.APPLICATION_XML).entity("<Scribe>CRM Object is deleted</Scribe>").build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_XML).entity("<Scribe>CRM Object is not deleted</Scribe>").build();
    }
  }

  public final HttpServletRequest getHttpServletRequest() {
    return httpServletRequest;
  }

  public final void setHttpServletRequest(final HttpServletRequest httpServletRequest) {
    this.httpServletRequest = httpServletRequest;
  }

  public final ScribeInternalService getScribeInternalService() {
    return scribeInternalService;
  }

  public final void setScribeInternalService(final ScribeInternalService scribeInternalService) {
    this.scribeInternalService = scribeInternalService;
  }

  public final ScribeRequestValidator getScribeRequestValidator() {
    return scribeRequestValidator;
  }

  public final void setScribeRequestValidator(final ScribeRequestValidator scribeRequestValidator) {
    this.scribeRequestValidator = scribeRequestValidator;
  }

  public final MutualDiagnosticLogService getMutualDiagnosticLogService() {
    return mutualDiagnosticLogService;
  }

  public final void setMutualDiagnosticLogService(final MutualDiagnosticLogService mutualDiagnosticLogService) {
    this.mutualDiagnosticLogService = mutualDiagnosticLogService;
  }
}
