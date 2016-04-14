package com.inbravo.cad.external.junit.core;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.client.WebClient;

import com.inbravo.cad.rest.resource.CADCommandObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CADClientMgmt {

  private String cadURL;

  private String objectUrl = "";

  public CADClientMgmt(final String cadURL, final String objectUrl) throws Exception {
    this.cadURL = cadURL;
    this.objectUrl = objectUrl;
  }

  public final CADCommandObject getCADCommandObjectByCRMObjectField(final String fieldName, final String fieldValue) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    CADCommandObject accounts = null;
    try {
      /* Call CAD */
      accounts = client.path(objectUrl).query(fieldName, fieldValue).get(CADCommandObject.class);
    } catch (final Exception e) {
      if (e instanceof WebApplicationException) {
        final WebApplicationException webApplicationException = (WebApplicationException) e;
        System.err.println(webApplicationException.getResponse().getStatus());
        if (webApplicationException.getResponse().getStatus() != Status.OK.getStatusCode()) {
          return null;
        }
      }
    }
    return accounts;
  }

  public final CADCommandObject getCADCommandObjectByIndustry(final String industry) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    CADCommandObject accounts = null;
    try {
      /* Call CAD */
      accounts = client.path(objectUrl).query("Industry", industry).get(CADCommandObject.class);
    } catch (final Exception e) {
      if (e instanceof WebApplicationException) {
        final WebApplicationException webApplicationException = (WebApplicationException) e;
        if (webApplicationException.getResponse().getStatus() != Status.OK.getStatusCode()) {
          return null;
        }
      }
    }
    return accounts;
  }

  public final CADCommandObject getCADCommandObjectByIsDeleted(final boolean isDeleted, final String agent) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    CADCommandObject accounts = null;
    try {
      /* Call CAD */
      accounts = client.path(objectUrl).query("IsDeleted", isDeleted).query("agent", agent).get(CADCommandObject.class);
    } catch (final Exception e) {
      if (e instanceof WebApplicationException) {
        final WebApplicationException webApplicationException = (WebApplicationException) e;
        if (webApplicationException.getResponse().getStatus() != Status.OK.getStatusCode()) {
          return null;
        }
      }
    }
    return accounts;
  }

  public final CADCommandObject getCADCommandObjectForAgent(final String agent) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    CADCommandObject accounts = null;
    try {
      /* Call CAD */
      accounts = client.path(objectUrl).query("agent", agent).get(CADCommandObject.class);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        final WebApplicationException webApplicationException = (WebApplicationException) e;
        if (webApplicationException.getResponse().getStatus() != Status.OK.getStatusCode()) {
          return null;
        }
      }
    }
    return accounts;
  }

  public final CADCommandObject getCADCommandObjectForTenant(final String tenant) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    CADCommandObject accounts = null;
    try {
      /* Call CAD */
      accounts = client.path(objectUrl).query("tenant", tenant).get(CADCommandObject.class);
    } catch (final Exception e) {
      if (e instanceof WebApplicationException) {
        final WebApplicationException webApplicationException = (WebApplicationException) e;
        if (webApplicationException.getResponse().getStatus() != Status.OK.getStatusCode()) {
          return null;
        }
      }
    }
    return accounts;
  }

  public final CADCommandObject getCADCommandObjectForTenantWithDynamicQuery(final String tenant) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    CADCommandObject accounts = null;
    try {
      /* Call CAD */
      accounts = client.path(objectUrl).query("tenant", tenant).query("isdeleted", "false").get(CADCommandObject.class);
    } catch (final Exception e) {
      if (e instanceof WebApplicationException) {
        final WebApplicationException webApplicationException = (WebApplicationException) e;
        if (webApplicationException.getResponse().getStatus() != Status.OK.getStatusCode()) {
          return null;
        }
      }
    }
    return accounts;
  }

  public final CADCommandObject getCADCommandObjectForAgentWithDynamicQuery(final String agent) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    CADCommandObject accounts = null;
    try {
      /* Call CAD */
      accounts = client.path(objectUrl).query("agent", agent).query("isdeleted", "false").get(CADCommandObject.class);
    } catch (final Exception e) {
      if (e instanceof WebApplicationException) {
        final WebApplicationException webApplicationException = (WebApplicationException) e;
        if (webApplicationException.getResponse().getStatus() != Status.OK.getStatusCode()) {
          return null;
        }
      }
    }
    return accounts;
  }

  public final CADCommandObject getCADCommandObject() throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create Response object */
    final CADCommandObject cADCommandObject = client.path(objectUrl).get(CADCommandObject.class);
    return cADCommandObject;
  }

  public final Response createObject(final CADCommandObject cADCommandObject) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create Response object */
    final Response response = client.path(objectUrl).encoding("UTF-8").post(cADCommandObject);
    return response;
  }

  public final Response updateObject(final CADCommandObject cADCommandObject) throws Exception {

    /* Create a web client */
    WebClient client = WebClient.create(cadURL);

    /* Create Response object */
    final Response response = client.path(objectUrl).encoding("UTF-8").put(cADCommandObject);

    return response;
  }

  public final Response deleteObject(final String accountId, final String agentOrtenant, final String agentOrTenantName) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create Response object */
    final Response response = client.path(objectUrl + "/" + accountId).query(agentOrtenant, agentOrTenantName).encoding("UTF-8").delete();

    return response;
  }

}
