package com.inbravo.cad.rest.service;

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

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.constants.CRMConstants.CTLCRMObjectType;
import com.inbravo.cad.rest.constants.CRMConstants.UserType;
import com.inbravo.cad.rest.constants.HTTPConstants;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.service.basic.CADInternalService;
import com.inbravo.cad.rest.service.crm.CADAttachmentUtils;
import com.inbravo.cad.rest.service.msg.type.FileAttachments;
import com.inbravo.cad.rest.service.validator.CADRequestValidator;

/**
 * 
 * @author amit.dixit
 * 
 */
@Path(HTTPConstants.object)
@Produces(HTTPConstants.mimeTypeXML)
public final class CADObjectService {

  private final Logger logger = Logger.getLogger(CADObjectService.class.getName());

  @Resource
  private HttpServletRequest httpServletRequest;

  private CADInternalService eDSAInternalService;

  private CADRequestValidator eDSARequestValidator;

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
  public final Response getObjects(@QueryParam("") CADCommandObject eDSACommandObject, final @PathParam(HTTPConstants.query) String query,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType, final @PathParam(HTTPConstants.select) String select) throws Exception {

    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, EDSA-TransId : [" + transactionId
        + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjects select specific CRM fields with dynamic query. Object type: " + ObjectType);

    /* Set transaction id */
    eDSACommandObject.seteDSATransId("" + transactionId);

    /* Check if attachment request */
    if (ObjectType != null && ObjectType.equalsIgnoreCase(CTLCRMObjectType.CtlAttachments.toString())) {
      logger.debug("---Inside getObjects, forwarding to attachment service");

      /* Set object type in command object */
      eDSACommandObject.setObjectType(CTLCRMObjectType.CtlAttachments.toString());

      /* Call attachment service */
      return this.getAttachments(eDSACommandObject, select, query);
    } else {

      /* Update the request object for requestObject from URL */
      eDSACommandObject.setObjectType(ObjectType);

      /* Validate the request */
      eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, false);

      if (eDSACommandObject.getAgent() != null) {
        eDSACommandObject =
            eDSAInternalService
                .getServiceFactory(eDSACommandObject)
                .getService(eDSACommandObject, UserType.Agent)
                .getObjects(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(query),
                    eDSARequestValidator.decodeEDSARequestParam(select));
      } else if (eDSACommandObject.getTenant() != null) {
        eDSACommandObject =
            eDSAInternalService
                .getServiceFactory(eDSACommandObject)
                .getService(eDSACommandObject, UserType.Tenant)
                .getObjects(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(query),
                    eDSARequestValidator.decodeEDSARequestParam(select));
      }

      if (eDSACommandObject == null) {
        /* Inform user that object not found */
        throw new CADException(CADResponseCodes._1004 + ObjectType);
      }

      /* Update the object for removing requestObject */
      eDSACommandObject.setObjectType(null);

      logger.info("==**== Transaction completed, EDSA-TransId : [" + transactionId + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId()
          + "] ==**== ");

      return Response.ok(eDSACommandObject).build();
    }
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
  public final Response getObjects(@QueryParam("") CADCommandObject eDSACommandObject, final @PathParam(HTTPConstants.query) String query,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType, final @PathParam(HTTPConstants.select) String select,
      final @PathParam(HTTPConstants.order) String order) throws Exception {

    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, EDSA-TransId : [" + transactionId
        + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjects select specific CRM fields with dynamic query and order. Object type: " + ObjectType);

    /* Set transaction id */
    eDSACommandObject.seteDSATransId("" + transactionId);

    /* Update the request object for requestObject from URL */
    eDSACommandObject.setObjectType(ObjectType);

    /* Validate the request */
    eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, false);

    if (eDSACommandObject.getAgent() != null) {
      eDSACommandObject =
          eDSAInternalService
              .getServiceFactory(eDSACommandObject)
              .getService(eDSACommandObject, UserType.Agent)
              .getObjects(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(query), eDSARequestValidator.decodeEDSARequestParam(select),
                  eDSARequestValidator.decodeEDSARequestParam(order));
    } else if (eDSACommandObject.getTenant() != null) {
      eDSACommandObject =
          eDSAInternalService
              .getServiceFactory(eDSACommandObject)
              .getService(eDSACommandObject, UserType.Tenant)
              .getObjects(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(query), eDSARequestValidator.decodeEDSARequestParam(select),
                  eDSARequestValidator.decodeEDSARequestParam(order));
    }

    if (eDSACommandObject == null) {
      /* Inform user that object not found */
      throw new CADException(CADResponseCodes._1004 + ObjectType);
    }

    /* Update the object for removing requestObject */
    eDSACommandObject.setObjectType(null);

    logger.info("==**== Transaction completed, EDSA-TransId : [" + transactionId + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId()
        + "] ==**== ");

    return Response.ok(eDSACommandObject).build();
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
  public final Response getObjects(@QueryParam("") CADCommandObject eDSACommandObject, final @PathParam(HTTPConstants.ObjectType) String ObjectType)
      throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, EDSA-TransId : [" + transactionId
        + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjects select all CRM fields. Object type: " + ObjectType);

    /* Set transaction id */
    eDSACommandObject.seteDSATransId("" + transactionId);

    /* Update the request object for requestObject from URL */
    eDSACommandObject.setObjectType(ObjectType);

    /* Validate the request */
    eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, false);

    if (eDSACommandObject.getAgent() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Agent).getObjects(eDSACommandObject);
    } else if (eDSACommandObject.getTenant() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Tenant).getObjects(eDSACommandObject);
    }

    if (eDSACommandObject == null) {
      /* Inform user that object not found */
      throw new CADException(CADResponseCodes._1004 + ObjectType);
    }

    /* Update the object for removing requestObject */
    eDSACommandObject.setObjectType(null);

    logger.info("==**== Transaction completed, EDSA-TransId : [" + transactionId + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId()
        + "] ==**== ");

    return Response.ok(eDSACommandObject).build();
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
  public final Response getObjectsCount(@QueryParam("") CADCommandObject eDSACommandObject,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType) throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, EDSA-TransId : [" + transactionId
        + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getAllObjectsCount Object type: " + ObjectType);

    /* Set transaction id */
    eDSACommandObject.seteDSATransId("" + transactionId);

    /* Update the request object for requestObject from URL */
    eDSACommandObject.setObjectType(ObjectType);

    /* Validate the request */
    eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, false);

    if (eDSACommandObject.getAgent() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Agent).getObjectsCount(eDSACommandObject);
    } else if (eDSACommandObject.getTenant() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Tenant).getObjectsCount(eDSACommandObject);
    }

    if (eDSACommandObject == null) {
      /* Inform user that object not found */
      throw new CADException(CADResponseCodes._1004 + ObjectType);
    }

    /* Update the object for removing requestObject */
    eDSACommandObject.setObjectType(null);

    logger.info("==**== Transaction completed, EDSA-TransId : [" + transactionId + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId()
        + "] ==**== ");

    return Response.ok(eDSACommandObject).build();
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
  public final Response getObjects(@QueryParam("") CADCommandObject eDSACommandObject, final @PathParam(HTTPConstants.query) String query,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType) throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, EDSA-TransId : [" + transactionId
        + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjects select all CRM fields by dynamic query. Object type: " + ObjectType);

    /* Set transaction id */
    eDSACommandObject.seteDSATransId("" + transactionId);

    /* Update the request object for requestObject from URL */
    eDSACommandObject.setObjectType(ObjectType);

    /* Validate the request */
    eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, false);

    /* Call direct DB service */
    if (eDSACommandObject.getAgent() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Agent)
              .getObjects(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(query));
    } else if (eDSACommandObject.getTenant() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Tenant)
              .getObjects(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(query));
    }

    if (eDSACommandObject == null) {
      /* Inform user that object not found */
      throw new CADException(CADResponseCodes._1004 + ObjectType);
    }

    /* Update the object for removing requestObject */
    eDSACommandObject.setObjectType(null);

    logger.info("==**== Transaction completed, EDSA-TransId : [" + transactionId + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId()
        + "] ==**== ");

    return Response.ok(eDSACommandObject).build();
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
  public final Response getObjectsCount(@QueryParam("") CADCommandObject eDSACommandObject, final @PathParam(HTTPConstants.query) String query,
      final @PathParam(HTTPConstants.ObjectType) String ObjectType) throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, EDSA-TransId : [" + transactionId
        + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside getObjectsCount Object type: " + ObjectType);

    /* Set transaction id */
    eDSACommandObject.seteDSATransId("" + transactionId);

    /* Update the request object for requestObject from URL */
    eDSACommandObject.setObjectType(ObjectType);

    /* Validate the request */
    eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, false);

    if (eDSACommandObject.getAgent() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Agent)
              .getObjectsCount(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(query));
    } else if (eDSACommandObject.getTenant() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Tenant)
              .getObjectsCount(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(query));
    }

    if (eDSACommandObject == null) {
      /* Inform user that object not found */
      throw new CADException(CADResponseCodes._1004 + ObjectType);
    }

    /* Update the object for removing requestObject */
    eDSACommandObject.setObjectType(null);

    logger.info("==**== Transaction completed, EDSA-TransId : [" + transactionId + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId()
        + "] ==**== ");

    return Response.ok(eDSACommandObject).build();
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
  public final Response createObject(CADCommandObject eDSACommandObject, final @PathParam(HTTPConstants.ObjectType) String ObjectType)
      throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, EDSA-TransId : [" + transactionId
        + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside createObject object type: " + ObjectType);

    /* Set transaction id */
    eDSACommandObject.seteDSATransId("" + transactionId);

    /* Update the request object for requestObject from URL */
    eDSACommandObject.setObjectType(ObjectType);

    /* Validate the request */
    eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, true);

    if (eDSACommandObject.getAgent() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Agent).createObject(eDSACommandObject);
    } else if (eDSACommandObject.getTenant() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Tenant).createObject(eDSACommandObject);
    }
    /* Update the object for removing requestObject */
    eDSACommandObject.setObjectType(null);

    logger.info("==**== Transaction completed, EDSA-TransId : [" + transactionId + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId()
        + "] ==**== ");

    return Response.ok(eDSACommandObject).build();
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
  public final Response updateObject(CADCommandObject eDSACommandObject, final @PathParam(HTTPConstants.ObjectType) String ObjectType)
      throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started, EDSA-TransId : [" + transactionId
        + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId() + "] ==**== ");
    logger.debug("---Inside updateObject object type: " + ObjectType);

    /* Set transaction id */
    eDSACommandObject.seteDSATransId("" + transactionId);

    /* Update the request object for requestObject from URL */
    eDSACommandObject.setObjectType(ObjectType);

    /* Validate the request */
    eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, true);

    if (eDSACommandObject.getAgent() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Agent).updateObject(eDSACommandObject);
    } else if (eDSACommandObject.getTenant() != null) {
      eDSACommandObject =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Tenant).updateObject(eDSACommandObject);
    }

    /* Update the object for removing requestObject */
    eDSACommandObject.setObjectType(null);

    logger.info("==**== Transaction completed, EDSA-TransId : [" + transactionId + "]; Ext-TransId : [" + eDSACommandObject.getExtTransId()
        + "] ==**== ");

    return Response.ok(eDSACommandObject).build();
  }

  /**
   * 
   * @return
   * @throws Exception
   */
  @DELETE
  @Path(HTTPConstants.PahForObjectTypeById)
  public final Response deleteObject(@PathParam(HTTPConstants.id) String idToBeDeleted, final @QueryParam(HTTPConstants.agent) String agentId,
      final @QueryParam(HTTPConstants.tenant) String tenant, final @PathParam(HTTPConstants.ObjectType) String ObjectType) throws Exception {
    final long transactionId = System.currentTimeMillis();
    logger.info("==**== Request received from " + httpServletRequest.getRemoteHost() + "; Transaction started : [" + transactionId + "] ==**==");
    logger.debug("---Inside deleteObject id to be deleted: " + idToBeDeleted);

    /* Create eDSACommandObject */
    final CADCommandObject eDSACommandObject = new CADCommandObject();

    /* Set transaction id */
    eDSACommandObject.seteDSATransId("" + transactionId);

    /* Set agent/tenant information */
    eDSACommandObject.setTenant(tenant);
    eDSACommandObject.setAgent(agentId);

    /* Update the request object for requestObject from URL */
    eDSACommandObject.setObjectType(ObjectType);

    /* Validate the request */
    eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, false);

    boolean status = false;
    if (eDSACommandObject.getAgent() != null) {
      status =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Agent)
              .deleteObject(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(idToBeDeleted));
    } else if (eDSACommandObject.getTenant() != null) {
      status =
          eDSAInternalService.getServiceFactory(eDSACommandObject).getService(eDSACommandObject, UserType.Tenant)
              .deleteObject(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(idToBeDeleted));
    }

    /* Update the object for removing requestObject */
    eDSACommandObject.setObjectType(null);

    logger.info("==**== Transaction completed: [" + transactionId + "] ==**==");

    if (status) {
      return Response.status(Response.Status.OK).type(MediaType.APPLICATION_XML).entity("<EDSA>CRM Object is deleted</EDSA>").build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_XML).entity("<EDSA>CRM Object is not deleted</EDSA>").build();
    }
  }

  /**
   * 
   * @param eDSACommandObject
   * @param objectId
   * @param ObjectType
   * @return
   * @throws Exception
   */
  private final Response getAttachments(CADCommandObject eDSACommandObject, final String objectId, final String ObjectType) throws Exception {

    logger.debug("---Inside getAttachments, Object type: " + ObjectType + " & objectId: " + objectId);

    /* Validate the request */
    eDSARequestValidator.validateRequestObject(eDSACommandObject, ObjectType, false);

    /* Pass objectid as query param */
    if (eDSACommandObject.getAgent() != null) {
      eDSACommandObject =
          eDSAInternalService
              .getServiceFactory(eDSACommandObject)
              .getService(eDSACommandObject, UserType.Agent)
              .getObjects(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(objectId),
                  eDSARequestValidator.decodeEDSARequestParam(ObjectType));
    } else if (eDSACommandObject.getTenant() != null) {
      eDSACommandObject =
          eDSAInternalService
              .getServiceFactory(eDSACommandObject)
              .getService(eDSACommandObject, UserType.Tenant)
              .getObjects(eDSACommandObject, eDSARequestValidator.decodeEDSARequestParam(objectId),
                  eDSARequestValidator.decodeEDSARequestParam(ObjectType));
    }

    if (eDSACommandObject == null) {

      /* Inform user that object not found */
      throw new CADException(CADResponseCodes._1004 + ObjectType);
    }

    /* Create file attachments */
    final FileAttachments attachments = CADAttachmentUtils.createFileAttachments(eDSACommandObject);

    /* Update the object for removing requestObject */
    eDSACommandObject.setObjectType(null);

    logger.info("==**== Transaction completed, EDSA-TransId : [" + eDSACommandObject.geteDSATransId() + "]; Ext-TransId : ["
        + eDSACommandObject.getExtTransId() + "] ==**== ");

    /* If file map has some content */
    if (attachments != null && attachments.size() == 1) {

      /* Return single file in attachment */
      return Response.ok(attachments.getFileAttachments().get(0)).build();
    } else if (attachments != null && attachments.size() > 1) {

      /* Return zip in attachment */
      return Response.ok(attachments).build();
    } else {

      /* Inform user that object not found */
      throw new CADException(CADResponseCodes._1004 + ObjectType + " with id: " + objectId);
    }
  }

  public final HttpServletRequest getHttpServletRequest() {
    return httpServletRequest;
  }

  public final void setHttpServletRequest(final HttpServletRequest httpServletRequest) {
    this.httpServletRequest = httpServletRequest;
  }

  public final CADRequestValidator geteDSARequestValidator() {
    return eDSARequestValidator;
  }

  public final void seteDSARequestValidator(final CADRequestValidator eDSARequestValidator) {
    this.eDSARequestValidator = eDSARequestValidator;
  }

  public final CADInternalService geteDSAInternalService() {
    return eDSAInternalService;
  }

  public final void seteDSAInternalService(final CADInternalService eDSAInternalService) {
    this.eDSAInternalService = eDSAInternalService;
  }
}
