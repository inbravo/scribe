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

package com.inbravo.scribe.rest.service.crm.ms.auth;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.jaxen.XPath;

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.internal.service.dto.CADUser;
import com.inbravo.scribe.rest.service.crm.CRMMessageFormatUtils;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMMessageFormatUtils;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSLiveIdManager extends MSAuthManager {

  private final Logger logger = Logger.getLogger(MSLiveIdManager.class.getName());

  private static String samlForMSLogin;

  private String liveIdHost = SOAPExecutor.LIVE_ID_HOST;

  private String liveIdEndpoint = SOAPExecutor.LIVE_ID_ENDPOINT;

  private String liveIdLoginFileName = "LiveIdLogin.xml";

  private String userPolicy = SOAPExecutor.WSSE_XMLNS;

  private SOAPExecutor sOAPExecutor;

  /**
	 * 
	 */
  public final String[] getCRMAuthToken(final CADUser user) throws Exception {

    String userName = null;
    String password = null;
    String crmServiceURL = null;

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getCRMAuthToken for user: " + user.getCrmUserId());
    }

    /* get CRM credentials */
    userName = user.getCrmUserId();
    password = user.getCrmPassword();
    crmServiceURL = user.getCrmServiceURL();

    if (logger.isDebugEnabled()) {

      logger.debug("---Inside getCRMAuthToken loginURL: https://" + liveIdHost + liveIdEndpoint + " & userName: " + userName + " & password: "
          + password + " & crmServiceURL: " + crmServiceURL);
    }

    /* Create final url */
    final URL fileURL = CRMMessageFormatUtils.getFileURL(liveIdLoginFileName);

    String msg = null;
    try {

      /* Get SAML from login */
      if (samlForMSLogin == null) {

        logger.debug("---Inside getCRMAuthToken reading security template from file");

        /* This logic is for reading the file once in lifetime only */
        samlForMSLogin = MSCRMMessageFormatUtils.readStringFromFile(fileURL.getPath());

        /* Convert for local usage */
        msg = samlForMSLogin;
      } else {
        logger.debug("---Inside getCRMAuthToken reading security template from memory");

        /* Convert for local usage */
        msg = samlForMSLogin;
      }

      /* Replace all constants from document */
      msg = msg.replaceAll(SOAPExecutor.LIVE_ID_SERVER_PLACEHOLDER, crmServiceURL);

      /* This is a trick to avoid error if password contains '$' */
      userName = Matcher.quoteReplacement(userName);
      password = Matcher.quoteReplacement(password);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getCRMAuthToken userName: " + userName + " & password: " + password);
      }

      msg = msg.replaceAll(SOAPExecutor.LIVE_ID_USERNAME_PLACEHOLDER, userName);
      msg = msg.replaceAll(SOAPExecutor.LIVE_ID_PASSWORD_PLACEHOLDER, password);
      msg = msg.replaceAll(SOAPExecutor.LIVE_ID_POLICY_PLACEHOLDER, userPolicy);

      /* Send SOAP message */
      final SOAPMessage response = sOAPExecutor.execute("https://" + liveIdHost + liveIdEndpoint, msg);

      /* If response is valid */
      if (response != null) {

        final XPath xp = sOAPExecutor.createXPath("//wsse:BinarySecurityToken/text()", response);

        xp.addNamespace("wsse", SOAPExecutor.WSSE_XMLNS);
        final Node result = (Node) xp.selectSingleNode(response.getSOAPBody());

        /* If invalid security token */
        if (result == null || (result.getValue() == null && !"".equals(result.getValue()))) {

          /* Report error to user */
          throw new CADException(CADResponseCodes._1009 + "Security token not recieved from Live Id server");
        } else {
          return new String[] {result.getValue()};
        }
      } else {

        /* Report error to user */
        throw new CADException(CADResponseCodes._1009 + "Invalid response from MS Live Id server");
      }

    } catch (final IOException e) {

      /* Report error to user */
      throw new CADException(CADResponseCodes._1015 + "Not able to connect to MS Live Id server: " + "https://" + liveIdHost + liveIdEndpoint);
    } catch (final SOAPException e) {

      /* Report error to user */
      throw new CADException(CADResponseCodes._1012 + "SOAP error from Microsoft Live Id server, Error : " + e.getMessage());
    } catch (final Exception e) {

      /* Report error to user and system */
      logger.error("=*=Exception at getCRMAuthToken ", e);
      throw new CADException(CADResponseCodes._1012 + "Error during MS Live Id authentication");
    }
  }

  public final String getLiveIdLoginFileName() {
    return liveIdLoginFileName;
  }

  public final void setLiveIdLoginFileName(final String liveIdLoginFileName) {
    this.liveIdLoginFileName = liveIdLoginFileName;
  }

  public final String getLiveIdHost() {
    return liveIdHost;
  }

  public final void setLiveIdHost(final String liveIdHost) {
    this.liveIdHost = liveIdHost;
  }

  public final String getLiveIdEndpoint() {
    return liveIdEndpoint;
  }

  public final void setLiveIdEndpoint(final String liveIdEndpoint) {
    this.liveIdEndpoint = liveIdEndpoint;
  }

  public final String getUserPolicy() {
    return userPolicy;
  }

  public final void setUserPolicy(final String userPolicy) {
    this.userPolicy = userPolicy;
  }

  public final SOAPExecutor getsOAPExecutor() {
    return sOAPExecutor;
  }

  public final void setsOAPExecutor(final SOAPExecutor sOAPExecutor) {
    this.sOAPExecutor = sOAPExecutor;
  }
}
