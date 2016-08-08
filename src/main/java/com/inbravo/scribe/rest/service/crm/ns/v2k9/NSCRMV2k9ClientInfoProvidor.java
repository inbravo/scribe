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

package com.inbravo.scribe.rest.service.crm.ns.v2k9;

import java.net.MalformedURLException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;

/**
 * The purpose of this class is to provide data center URLS for each customer
 * 
 * @author amit.dixit
 * 
 */
public final class NSCRMV2k9ClientInfoProvidor {

  private final Logger logger = Logger.getLogger(NSCRMV2k9ClientInfoProvidor.class.getName());

  /* NS URLs for retrieval of user roles */
  private String nsGetRoleURLs = "https://system.netsuite.com/rest/roles,https://system.sandbox.netsuite.com/rest/roles";

  /**
   * This API will give the URL of web service, assigned for the user
   * 
   * @param userEmail
   * @param password
   * @return
   * @throws Exception
   */
  public final NSCRMV2k9ClientInfo getNSCRMV2k9ClientInfo(final String userEmail, final String password) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getWebServiceURL: userEmail: " + userEmail + " & password: " + password + " & NS REST urls: " + nsGetRoleURLs);
    }
    try {
      /* Start from zero retry counts */
      return this.getNSCRMV2k9ClientInfo(userEmail, password, this.getNsGetRoleURLs(0));
    } catch (final Exception exception) {

      try {
        /* log this error */
        logger.info("---Inside getNSCRMV2k9ClientInfo, found error while trying first NS REST URL, going to retry second NS REST URL");

        /* Start from 2+ retry counts */
        return this.getNSCRMV2k9ClientInfo(userEmail, password, this.getNsGetRoleURLs(1));
      } catch (final Exception anotherException) {

        /* log this error */
        logger.error("=*=Inside getNSCRMV2k9ClientInfo, going to use default URL ", anotherException);

        /* Use default configuration */
        return new NSCRMV2k9ClientInfo();
      }
    }
  }

  /**
   * This API will give the URL of web service, assigned for the user
   * 
   * @param userEmail
   * @param password
   * @param nsGetRoleURL
   * @return
   * @throws Exception
   */
  private final NSCRMV2k9ClientInfo getNSCRMV2k9ClientInfo(final String userEmail, final String password, final String nsGetRoleURL) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getNSCRMV2k9ClientInfo: userEmail: " + userEmail + " & password: " + password + " & ns role url: " + nsGetRoleURL);
    }

    /* Validate credentials validity */
    if (userEmail == null || "".equals(userEmail) || password == null || "".equals(password)) {

      /* Inform user invalid credentials */
      throw new CADException(CADResponseCodes._1012 + "Invalid credentials");
    }

    /* Create a HTTP Get type request */
    final GetMethod getM = new GetMethod(nsGetRoleURL);

    /* Add authentication header */
    getM.addRequestHeader("Authorization", "NLAuth nlauth_email=" + userEmail + ", nlauth_signature=" + password);

    try {
      /* Create instance of http client in lazy fashion */
      final HttpClient httpclient = new HttpClient();

      /* Call http url */
      int result = httpclient.executeMethod(getM);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getNSCRMV2k9ClientInfo: response status code: " + result);
      }

      if (result == HttpStatus.SC_OK) {

        /* Check if valid response */
        if (getM.getResponseBodyAsString() != null && !"".equals(getM.getResponseBodyAsString())) {

          return this.get2K9ServiceURls(getM.getResponseBodyAsString().trim());
        } else {
          if (logger.isDebugEnabled()) {
            logger.debug("---Inside getNSCRMV2k9ClientInfo: found no response, going to use default" + result);
          }
          return new NSCRMV2k9ClientInfo();
        }
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("---Inside getNSCRMV2k9ClientInfo: found error from NS: " + result);
        }
        /* Inform user about service exception */
        throw new CADException(CADResponseCodes._1012 + "Service error from NetSuite : " + result);
      }

    } catch (final Exception exception) {

      /* Inform user about service exception */
      throw new CADException(CADResponseCodes._1021 + "Service exception while finding web service url for NetSuite", exception);
    } finally {

      /* Release connection */
      if (getM != null) {
        getM.releaseConnection();
      }
    }
  }

  /**
   * 
   * @param nsRespString
   * @return
   * @throws JSONException
   * @throws XMLStreamException
   * @throws MalformedURLException
   */
  private final NSCRMV2k9ClientInfo get2K9ServiceURls(final String nsResp) throws JSONException, XMLStreamException, MalformedURLException {

    /* Create new JSON parsing object */
    final JSONObject allAccs = new JSONObject(nsResp.replace("[{", "{").replace("}]", "}"));

    /* Create a sub json object on dataCenterURLs */
    final JSONObject dataCenterURL = new JSONObject(allAccs.getString("dataCenterURLs"));

    /* All service urls */
    final String restDomain = dataCenterURL.getString("restDomain");
    final String systemDomain = dataCenterURL.getString("systemDomain");
    final String webservicesDomain = dataCenterURL.getString("webservicesDomain");

    /* Create URL of domain string */
    if ((webservicesDomain != null && !"".equals(webservicesDomain)) || (systemDomain != null && !"".equals(systemDomain))) {

      return new NSCRMV2k9ClientInfo(restDomain, systemDomain, webservicesDomain);
    } else {
      /* Inform user about service exception */
      throw new CADException(CADResponseCodes._1021 + "Service exception while finding web service url from NetSuite using rest url");
    }
  }

  /**
   * @return the nsGetRoleURLs
   */
  private final String getNsGetRoleURLs(final int tryCount) throws Exception {

    /* Check if comma seperated URLs are provided */
    if (nsGetRoleURLs.contains(",")) {

      /* Split on the basis of comma */
      final String[] nsGetRoleURLArr = nsGetRoleURLs.split(",");

      /* Validate if array has element at location */
      if (nsGetRoleURLArr[tryCount] != null) {

        /* Get respective URL */
        return nsGetRoleURLArr[tryCount];
      } else {

        /* Inform user about service exception */
        throw new CADException(CADResponseCodes._1021 + "No more NS REST URLs for finding web service location");
      }
    } else {

      /* Consider it as single URL */
      return nsGetRoleURLs;
    }
  }

  /**
   * @return the nsGetRoleURLs
   */
  public final String getNsGetRoleURLs() {
    return this.nsGetRoleURLs;
  }

  /**
   * @param nsGetRoleURLs the nsGetRoleURLs to set
   */
  public final void setNsGetRoleURLs(final String nsGetRoleURLs) {
    this.nsGetRoleURLs = nsGetRoleURLs;
  }

  public final class NSCRMV2k9ClientInfo {

    private String restDomain = "https://rest.netsuite.com";
    private String systemDomain = "https://system.netsuite.com";
    private String webservicesDomain = "https://webservices.netsuite.com";

    public NSCRMV2k9ClientInfo() {

    }

    public NSCRMV2k9ClientInfo(final String restDomain, final String systemDomain, final String webservicesDomain) {
      super();
      this.restDomain = restDomain;
      this.systemDomain = systemDomain;
      this.webservicesDomain = webservicesDomain;
    }

    /**
     * @return the restDomain
     */
    public final String getRestDomain() {
      return this.restDomain;
    }

    /**
     * @param restDomain the restDomain to set
     */
    public final void setRestDomain(final String restDomain) {
      this.restDomain = restDomain;
    }

    /**
     * @return the systemDomain
     */
    public final String getSystemDomain() {
      return this.systemDomain;
    }

    /**
     * @param systemDomain the systemDomain to set
     */
    public final void setSystemDomain(final String systemDomain) {
      this.systemDomain = systemDomain;
    }

    /**
     * @return the webservicesDomain
     */
    public final String getWebservicesDomain() {
      return this.webservicesDomain;
    }

    /**
     * @param webservicesDomain the webservicesDomain to set
     */
    public final void setWebservicesDomain(final String webservicesDomain) {
      this.webservicesDomain = webservicesDomain;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "NSCRMV2k9ClientInfo [restDomain=" + this.restDomain + ", systemDomain=" + this.systemDomain + ", webservicesDomain="
          + this.webservicesDomain + "]";
    }
  }

  public static void main(String[] args) throws Exception {

    PropertyConfigurator.configure("D:/work/contactual_source/trunk/WEB/CAD/Properties/scribe-log4j.properties");
    final NSCRMV2k9ClientInfoProvidor prov = new NSCRMV2k9ClientInfoProvidor();

    System.out.println(prov.getNSCRMV2k9ClientInfo("thomas.wu@8x8.com", "welcome789"));
  }
}
