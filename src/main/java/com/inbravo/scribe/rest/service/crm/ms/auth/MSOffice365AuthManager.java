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
import java.util.UUID;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.service.crm.CRMMessageFormatUtils;
import com.inbravo.scribe.rest.service.crm.cache.ScribeCacheObject;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMMessageFormatUtils;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSOffice365AuthManager extends MSAuthManager {

  private final Logger logger = Logger.getLogger(MSOffice365AuthManager.class.getName());

  private static String samlForMSLogin;

  private SOAPExecutor sOAPExecutor;

  private int loginExpirationInMinutes = 5;

  private String loginFileName = "MSOffice365Login.xml";

  private String msOffice365RequestDateFormat = SOAPExecutor.OFFICE_365_REQUEST_DATE_FORMAT;

  private String msOffice365RequestTimeZone = SOAPExecutor.OFFICE_365_REQUEST_TZ;

  @Override
  public final String[] getCRMAuthToken(final ScribeCacheObject cacheObject) throws Exception {

    String stsEndpoint = null;
    String urnAddress = null;
    String userName = null;
    String password = null;
    String crmServiceURL = null;
    String crmServiceProtocal = null;

    logger.debug("---Inside getCRMAuthToken for agent: " + cacheObject.getScribeMetaObject().getCrmUserId());

    /* Check if additonal info is present */
    if (cacheObject.getAdditionalInfo() != null) {

      /* Parse the reponse */
      stsEndpoint = cacheObject.getAdditionalInfo().get("STSEnpoint");
      urnAddress = cacheObject.getAdditionalInfo().get("URNAddress");
    }

    /* get CRM credentials */
    userName = cacheObject.getScribeMetaObject().getCrmUserId();
    password = cacheObject.getScribeMetaObject().getCrmPassword();
    crmServiceURL = cacheObject.getScribeMetaObject().getCrmServiceURL();
    crmServiceProtocal = cacheObject.getScribeMetaObject().getCrmServiceProtocol();

    logger.debug("---Inside getCRMAuthToken userName: " + userName + " & password: " + password + " & stsEndpoint: " + stsEndpoint
        + " & urnAddress: " + urnAddress + " & crmServiceURL: " + crmServiceURL + " & crmServiceProtocal: " + crmServiceProtocal);

    final URL fileURL = CRMMessageFormatUtils.getFileURL(loginFileName);

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

      /* This is a trick to avoid error if password contains '$' */
      userName = Matcher.quoteReplacement(userName);
      password = Matcher.quoteReplacement(password);

      logger.debug("---Inside getCRMAuthToken userName: " + userName + " & password: " + password + " & stsEndpoint: " + stsEndpoint);

      /* Get DB specific formatter */
      final DateTimeFormatter isoDateFormat = DateTimeFormat.forPattern(msOffice365RequestDateFormat);

      /* Get current time */
      final String currentDateTime = isoDateFormat.print(DateTime.now(DateTimeZone.forID((msOffice365RequestTimeZone))));

      /* Add 5 minutes expiry time from now */
      final String expireDateTime =
          isoDateFormat.print(DateTime.now(DateTimeZone.forID((msOffice365RequestTimeZone))).plusMinutes(loginExpirationInMinutes));

      /* The final customer specific security header */
      msg = String.format(msg, UUID.randomUUID().toString(), "ACQA", stsEndpoint, currentDateTime, expireDateTime, userName, password, urnAddress);

      logger.debug("---Inside getCRMAuthToken, login request: " + msg);

      /* Send SOAP message */
      final String response = sOAPExecutor.getSOAPResponse(stsEndpoint, msg);

      logger.debug("---Inside getCRMAuthToken, login response: " + response);

      /* If a valid response */
      if (response != null && !response.contains("internalerror")) {

        /* Extract all the values from response */
        final String securityToken0 = MSCRMMessageFormatUtils.getValueFromXML(response, "//*[local-name()='CipherValue']/text()");
        final String securityToken1 = MSCRMMessageFormatUtils.getValueFromXML(response, "//*[local-name()='CipherValue']/text()", 1);
        final String keyIdentifier = MSCRMMessageFormatUtils.getValueFromXML(response, "//*[local-name()='KeyIdentifier']/text()");

        logger.debug("---Inside getCRMAuthToken securityToken0: " + securityToken0 + " & securityToken1: " + securityToken1 + " & keyIdentifier: "
            + keyIdentifier);
        return new String[] {securityToken0, securityToken1, keyIdentifier};
      } else {

        /* Extract all the values from response */
        final String error =
            MSCRMMessageFormatUtils
                .getValueFromXML(
                    response,
                    "//*[local-name()='Reason' and namespace-uri()='http://www.w3.org/2003/05/soap-envelope']/*[local-name()='Text' and namespace-uri()='http://www.w3.org/2003/05/soap-envelope']/text()");

        throw new ScribeException(ScribeResponseCodes._1012 + " MS Login request failed : " + error);
      }

    } catch (final IOException e) {
      throw new ScribeException(ScribeResponseCodes._1015 + " Not able to connect to office 365 login server: " + stsEndpoint);
    }
  }

  /**
   * @return the samlForMSLogin
   */
  public static final String getSamlForMSLogin() {
    return samlForMSLogin;
  }

  /**
   * @param samlForMSLogin the samlForMSLogin to set
   */
  public static final void setSamlForMSLogin(final String samlForMSLogin) {
    MSOffice365AuthManager.samlForMSLogin = samlForMSLogin;
  }

  /**
   * @return the loginEspirationInMinutes
   */
  public final int getLoginExpirationInMinutes() {
    return this.loginExpirationInMinutes;
  }

  /**
   * @param loginEspirationInMinutes the loginEspirationInMinutes to set
   */
  public final void setLoginExpirationInMinutes(final int loginExpirationInMinutes) {
    this.loginExpirationInMinutes = loginExpirationInMinutes;
  }

  /**
   * @return the loginFileName
   */
  public final String getLoginFileName() {
    return this.loginFileName;
  }

  /**
   * @param loginFileName the loginFileName to set
   */
  public final void setLoginFileName(final String loginFileName) {
    this.loginFileName = loginFileName;
  }

  /**
   * @return the sOAPExecutor
   */
  public final SOAPExecutor getsOAPExecutor() {
    return this.sOAPExecutor;
  }

  /**
   * @param sOAPExecutor the sOAPExecutor to set
   */
  public final void setsOAPExecutor(final SOAPExecutor sOAPExecutor) {
    this.sOAPExecutor = sOAPExecutor;
  }

  /**
   * @return the msOffice365RequestTimeZone
   */
  public final String getMsOffice365RequestTimeZone() {
    return this.msOffice365RequestTimeZone;
  }

  /**
   * @param msOffice365RequestTimeZone the msOffice365RequestTimeZone to set
   */
  public final void setMsOffice365RequestTimeZone(final String msOffice365RequestTimeZone) {
    this.msOffice365RequestTimeZone = msOffice365RequestTimeZone;
  }

  /**
   * @return the msOffice365RequestDateFormat
   */
  public final String getMsOffice365RequestDateFormat() {
    return this.msOffice365RequestDateFormat;
  }

  /**
   * @param msOffice365RequestDateFormat the msOffice365RequestDateFormat to set
   */
  public final void setMsOffice365RequestDateFormat(final String msOffice365RequestDateFormat) {
    this.msOffice365RequestDateFormat = msOffice365RequestDateFormat;
  }
}
