package com.inbravo.cad.exception;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;

import com.sforce.soap.partner.fault.InvalidFieldFault;
import com.sforce.soap.partner.fault.InvalidIdFault;
import com.sforce.soap.partner.fault.InvalidQueryLocatorFault;
import com.sforce.soap.partner.fault.InvalidSObjectFault;
import com.sforce.soap.partner.fault.MalformedQueryFault;
import com.sforce.soap.partner.fault.MalformedSearchFault;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CADExceptionMapper implements ExceptionMapper<Exception> {

  private final Logger logger = Logger.getLogger(CADExceptionMapper.class.getName());

  private final String SAXParseExceptionString = "SAXParseException";

  /**
   * Following API is called by CXF internally to map the error to respective message
   */
  public final Response toResponse(final Exception exception) {

    final String errorConstant = "=**=User error reason=**= : ";

    if (exception instanceof InvalidFieldFault || exception instanceof InvalidIdFault || exception instanceof UnexpectedErrorFault
        || exception instanceof InvalidSObjectFault || exception instanceof MalformedQueryFault || exception instanceof MalformedSearchFault
        || exception instanceof InvalidQueryLocatorFault) {
      logger.debug("---Inside toResponse an exception is found: " + exception);

      /* Inform the user about invalid data errors from sales force */
      if (exception instanceof InvalidQueryLocatorFault) {

        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_XML)
            .entity(getErrorXML(exception.toString() + ": Batch information in request is invalid")).build();
      } else {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.toString())).build();
      }
    } else if (exception instanceof IOException || exception instanceof UnknownHostException || exception instanceof RemoteException) {
      logger.error("||||Internal Server Error||||Reason||||CRM(SalesForce/NetSuite/Custom) connectvity error||||" + exception, exception);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.toString())).build();
    } else if (exception instanceof AxisFault) {
      logger.error("||||Server Error||||Reason||||CRM webservice error||||" + exception, exception);
      return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity(exception.toString()).build();

    } else if (exception instanceof IllegalArgumentException) {
      logger.error("||||Server Error||||Reason||||" + exception, exception);
      return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_XML)
          .entity(exception.toString() + " : Please provide json/xml as _type only").build();
    } else if (exception instanceof CADException) {

      /* Tell user about original error */
      if (exception.getMessage().contains(CADResponseCodes._1000)) {
        /* This is to find some internal configuration error */
        logger.error("||||Internal Server Error||||Reason||||" + exception.getMessage(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();

      } else if (exception.getMessage().contains(CADResponseCodes._1001)) {
        /* This is to find some internal configuration error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();

      } else if (exception.getMessage().contains(CADResponseCodes._1002)) {
        /* Inform user about the error */
        logger.error("||||Internal Server Error||||Reason||||" + exception.getMessage(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML)
            .entity(getErrorXML("Configuration problem. Please contact customer care")).build();

      } else if (exception.getMessage().contains(CADResponseCodes._1003)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();

      } else if (exception.getMessage().contains(CADResponseCodes._1004)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage())).build();

      } else if (exception.getMessage().contains(CADResponseCodes._1005)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();

      } else if (exception.getMessage().contains(CADResponseCodes._1006)) {
        /* This is to find some internal configuration error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage())).build();

      } else if (exception.getMessage().contains(CADResponseCodes._1007)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML)
            .entity(getErrorXML("Configuration problem. Please contact customer care")).build();

      } else if (exception.getMessage().contains(CADResponseCodes._1008)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage())).build();

      } else if (exception.getMessage().contains(CADResponseCodes._1009)) {
        return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage())).build();

      } else if (exception.getMessage().contains(CADResponseCodes._1010)) {
        logger.error("||||Internal Server Error||||Reason||||" + exception.getMessage(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML)
            .entity(getErrorXML("Configuration problem. Please contact customer care")).build();

      } else if (exception.getMessage().contains(CADResponseCodes._1011)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();

      } else if (exception.getMessage().contains(CADResponseCodes._1012)) {
        /* Inform user about the error */
        logger.info("=**=User error reason=**= " + exception.getMessage());
        return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage())).build();

      } else if (exception.getMessage().contains(CADResponseCodes._1013)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage())).build();

      } else if (exception.getMessage().contains(CADResponseCodes._1015)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();

      } else if (exception.getMessage().contains(CADResponseCodes._1020)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();

      } else if (exception.getMessage().contains(CADResponseCodes._1021)) {
        /* Inform user about the error */
        logger.info(errorConstant + exception.getMessage());
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();

      } else {
        logger.error("||||Internal Server Error||||Reason|||| " + exception.getMessage(), exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();

      }
    } else if (exception instanceof WebApplicationException) {
      /* Inform user about the error */
      logger.info("=**=Web application error reason=**= " + exception.getMessage());
      /* Check if cause is present */
      if (exception.getCause() != null) {
        if (exception.getCause().toString().contains(SAXParseExceptionString)) {
          return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).type(MediaType.APPLICATION_XML)
              .entity(getErrorXML("Found invalid XML in request")).build();
        } else {
          return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_XML)
              .entity(getErrorXML(CADResponseCodes._1008 + "No REST resource is available at following path. Please provide valid URL")).build();
        }
      } else {
        return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_XML)
            .entity(getErrorXML(CADResponseCodes._1008 + "No Resource available at following path. Please provide valid URL")).build();
      }
    } else {
      logger.error("||||Unknown Server Error||||Reason|||| " + exception, exception);
      if (exception.getMessage() != null) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML).entity(getErrorXML(exception.getMessage()))
            .build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML).entity(getErrorXML("Internal Server Error"))
            .build();
      }
    }
  }

  public final static String getErrorXML(final String error) {
    return "<CAD>" + "<Error>" + error + "</Error>" + "</CAD>";
  }
}
