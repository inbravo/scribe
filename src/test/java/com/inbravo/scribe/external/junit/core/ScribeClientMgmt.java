package com.inbravo.scribe.external.junit.core;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.client.WebClient;

import com.inbravo.scribe.rest.resource.ScribeCommandObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ScribeClientMgmt {

  private String cadURL;

  private String objectUrl = "";

  public ScribeClientMgmt(final String cadURL, final String objectUrl) throws Exception {
    this.cadURL = cadURL;
    this.objectUrl = objectUrl;
  }

  public final ScribeCommandObject getScribeCommandObjectByCRMObjectField(final String fieldName, final String fieldValue) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    ScribeCommandObject accounts = null;
    try {
      /* Call Scribe */
      accounts = client.path(objectUrl).query(fieldName, fieldValue).get(ScribeCommandObject.class);
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

  public final ScribeCommandObject getScribeCommandObjectByIndustry(final String industry) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    ScribeCommandObject accounts = null;
    try {
      /* Call Scribe */
      accounts = client.path(objectUrl).query("Industry", industry).get(ScribeCommandObject.class);
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

  public final ScribeCommandObject getScribeCommandObjectByIsDeleted(final boolean isDeleted, final String agent) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    ScribeCommandObject accounts = null;
    try {
      /* Call Scribe */
      accounts = client.path(objectUrl).query("IsDeleted", isDeleted).query("agent", agent).get(ScribeCommandObject.class);
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

  public final ScribeCommandObject getScribeCommandObjectForAgent(final String agent) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    ScribeCommandObject accounts = null;
    try {
      /* Call Scribe */
      accounts = client.path(objectUrl).query("agent", agent).get(ScribeCommandObject.class);
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

  public final ScribeCommandObject getScribeCommandObjectForTenant(final String tenant) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    ScribeCommandObject accounts = null;
    try {
      /* Call Scribe */
      accounts = client.path(objectUrl).query("tenant", tenant).get(ScribeCommandObject.class);
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

  public final ScribeCommandObject getScribeCommandObjectForTenantWithDynamicQuery(final String tenant) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    ScribeCommandObject accounts = null;
    try {
      /* Call Scribe */
      accounts = client.path(objectUrl).query("tenant", tenant).query("isdeleted", "false").get(ScribeCommandObject.class);
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

  public final ScribeCommandObject getScribeCommandObjectForAgentWithDynamicQuery(final String agent) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create account object */
    ScribeCommandObject accounts = null;
    try {
      /* Call Scribe */
      accounts = client.path(objectUrl).query("agent", agent).query("isdeleted", "false").get(ScribeCommandObject.class);
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

  public final ScribeCommandObject getScribeCommandObject() throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create Response object */
    final ScribeCommandObject cADCommandObject = client.path(objectUrl).get(ScribeCommandObject.class);
    return cADCommandObject;
  }

  public final Response createObject(final ScribeCommandObject cADCommandObject) throws Exception {

    /* Create a web client */
    final WebClient client = WebClient.create(cadURL);

    /* Create Response object */
    final Response response = client.path(objectUrl).encoding("UTF-8").post(cADCommandObject);
    return response;
  }

  public final Response updateObject(final ScribeCommandObject cADCommandObject) throws Exception {

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
