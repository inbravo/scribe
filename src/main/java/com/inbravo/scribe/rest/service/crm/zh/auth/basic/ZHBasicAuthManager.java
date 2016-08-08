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

package com.inbravo.scribe.rest.service.crm.zh.auth.basic;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.rest.service.crm.zh.auth.ZHAuthManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZHBasicAuthManager implements ZHAuthManager {

  private final Logger logger = Logger.getLogger(ZHBasicAuthManager.class.getName());

  @Override
  public final String getSessionId(final String userId, final String password, final String crmURL, final String crmProtocol) throws Exception {
    logger
        .debug("---Inside getSessionId, userId: " + userId + " & password: " + password + " & crmURL: " + crmURL + " & crmProtocol: " + crmProtocol);

    /* Validate protocol */
    if (crmProtocol == null || "".equalsIgnoreCase(crmProtocol)) {

      /* Send user error */
      throw new CADException(CADResponseCodes._1003 + "Invalid protocol to communicate with Zoho: " + crmProtocol);
    }

    String loginURL = crmURL;

    /* Change CRM service URL to CRM login URL */
    if (crmURL.toLowerCase().startsWith("crm")) {

      /* If CRM URL contains . */
      if (crmURL.toLowerCase().contains(".")) {

        /* Split the URL */
        final String[] loginURLArr = crmURL.split("\\.");

        /* Create login URL */
        loginURL = "accounts." + loginURLArr[1] + "." + loginURLArr[2];
      }
    }

    /* Create CRM URL */
    final String zohoCRMURL =
        crmProtocol + "://" + loginURL + "/apiauthtoken/nb/create?SCOPE=ZohoCRM/crmapi&EMAIL_ID=" + userId + "&PASSWORD=" + password;

    logger.debug("---Inside getSessionId zohoCRMURL: " + zohoCRMURL);

    /* Get CRM session id */
    final GetMethod getMethod = new GetMethod(zohoCRMURL);

    /* Create new http client instance */
    final HttpClient httpclient = new HttpClient();
    try {

      /* Execute get command */
      int result = httpclient.executeMethod(getMethod);
      logger.debug("---Inside getSessionId, response code: " + result + " & body: " + getMethod.getResponseBodyAsString());

      /* Check for authentication */
      if (result == HttpStatus.SC_UNAUTHORIZED) {
        logger.debug("---Inside getSessionId found unauthorized user");
        throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zoho CRM");
      } else {
        /* Response string */
        final String response = getMethod.getResponseBodyAsString();

        /* If vailid Zoho CRM response */
        if (response != null && response.contains("RESULT=TRUE")) {
          final String sessionId = response.substring(response.indexOf("=") + 1, response.lastIndexOf("=") - 6);

          logger.debug("---Inside getSessionId, sessionId: " + sessionId);
          return sessionId;
        } else {
          final String reason = response.substring(response.indexOf("=") + 1, response.lastIndexOf("=") - 6);

          throw new CADException(CADResponseCodes._1022 + "No session id is found in response from Zoho CRM : " + reason);
        }
      }
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1022 + "Communication error while communicating with Zoho CRM", exception);
    } finally {
      /* Release connection socket */
      if (getMethod != null) {
        getMethod.releaseConnection();
      }
    }
  }

  @Override
  public final boolean login(final String userId, final String password, final String crmURL, final String crmProtocol) throws Exception {
    logger.debug("---Inside login userId: " + userId + " & password: " + password + " & crmURL: " + crmURL + " & crmProtocol: " + crmProtocol);

    /* Validate protocol */
    if (crmProtocol == null || "".equalsIgnoreCase(crmProtocol)) {

      /* Send user error */
      throw new CADException(CADResponseCodes._1003 + "Invalid protocol to communicate with Zoho CRM: " + crmProtocol);
    }

    String loginURL = crmURL;

    /* Change CRM service URL to CRM login URL */
    if (crmURL.toLowerCase().startsWith("crm")) {

      /* If CRM URL contains . */
      if (crmURL.toLowerCase().contains(".")) {

        /* Split the URL */
        final String[] loginURLArr = crmURL.split("\\.");

        /* Create login URL */
        loginURL = "accounts." + loginURLArr[1] + "." + loginURLArr[2];
      }
    }

    /* Create CRM URL */
    final String zohoCRMURL =
        crmProtocol + "://" + loginURL + "/apiauthtoken/nb/create?SCOPE=ZohoCRM/crmapi&EMAIL_ID=" + userId + "&PASSWORD=" + password;
    logger.debug("---Inside login zohoCRMURL: " + zohoCRMURL);

    /* Get CRM session id */
    final GetMethod getMethod = new GetMethod(zohoCRMURL);

    getMethod.addRequestHeader("Content-Type", "application/xml");
    final HttpClient httpclient = new HttpClient();

    try {
      int result = httpclient.executeMethod(getMethod);
      logger.debug("---Inside login, response code: " + result + " & body: " + getMethod.getResponseBodyAsString());

      /* Check for authentication */
      if (result == HttpStatus.SC_UNAUTHORIZED) {
        return false;
      } else {

        /* Response string */
        final String response = getMethod.getResponseBodyAsString();

        /* If vailid Zoho CRM response */
        if (response != null && response.contains("RESULT=TRUE")) {
          final String sessionId = response.substring(response.indexOf("=") + 1, response.lastIndexOf("=") - 6);

          logger.debug("---Inside login, sessionId: " + sessionId);
          return true;
        } else {
          final String reason = response.substring(response.indexOf("=") + 1, response.lastIndexOf("=") - 6);

          throw new CADException(CADResponseCodes._1022 + "No session id is found in response from Zoho CRM : " + reason);
        }
      }
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1022 + "Communication error while communicating with Zoho CRM", exception);
    } finally {
      /* Release connection socket */
      if (getMethod != null) {
        getMethod.releaseConnection();
      }
    }
  }
}
