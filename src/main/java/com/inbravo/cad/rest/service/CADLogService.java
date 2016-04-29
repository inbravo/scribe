package com.inbravo.cad.rest.service;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.inbravo.cad.rest.constants.HTTPConstants;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CADLogService {

  private final Logger logger = Logger.getLogger(CADLogService.class.getName());

  @PUT
  @Path(HTTPConstants.PathForProcessLogLevelChangeRequest)
  @Produces({HTTPConstants.mimeTypeJSON, HTTPConstants.mimeTypeXML})
  public final Response update(final @QueryParam(HTTPConstants.logLevel) String logLevel) throws Exception {

    logger.debug("---Inside update logLevel: " + logLevel);

    /* Change log */
    final String newLogLevel = this.updateLogLevel(logLevel);

    /* Return the success response */
    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_XML)
        .entity("<CAD>Log level is successfully updated to: " + newLogLevel + "</CAD>").build();
  }

  /**
   * 
   * @param logLevel
   * @return
   * @throws Exception
   */
  private final String updateLogLevel(final String logLevel) throws Exception {
    logger.debug("---Inside updateLogLevel logLevel: " + logLevel);

    /* Get the root logger of logging system */
    Logger rootLogger = Logger.getLogger("com.inbravo.cad");

    String newLogLevel = logLevel;

    /* Check for log relevant log4j level */
    if ("DEBUG".equalsIgnoreCase(logLevel)) {
      rootLogger.setLevel(org.apache.log4j.Level.DEBUG);
    } else if ("INFO".equalsIgnoreCase(logLevel)) {
      rootLogger.setLevel(org.apache.log4j.Level.INFO);
    } else if ("ERROR".equalsIgnoreCase(logLevel)) {
      rootLogger.setLevel(org.apache.log4j.Level.ERROR);
    } else if ("FATAL".equalsIgnoreCase(logLevel)) {
      rootLogger.setLevel(org.apache.log4j.Level.FATAL);
    } else if ("OFF".equalsIgnoreCase(logLevel)) {
      rootLogger.setLevel(org.apache.log4j.Level.OFF);
    } else if ("WARN".equalsIgnoreCase(logLevel)) {
      rootLogger.setLevel(org.apache.log4j.Level.WARN);
    } else if ("ALL".equalsIgnoreCase(logLevel)) {
      rootLogger.setLevel(org.apache.log4j.Level.ALL);
    } else {
      rootLogger.setLevel(org.apache.log4j.Level.ALL);
      newLogLevel = "ALL";
    }
    logger.debug("---Inside updateLogLevel, logLevel changed successfully to: " + newLogLevel);
    return newLogLevel;
  }
}
