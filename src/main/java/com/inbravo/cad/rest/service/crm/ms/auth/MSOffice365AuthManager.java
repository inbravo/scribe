package com.inbravo.cad.rest.service.crm.ms.auth;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.BasicObject;
import com.inbravo.cad.internal.service.dto.Tenant;
import com.inbravo.cad.rest.service.crm.CRMMessageFormatUtils;
import com.inbravo.cad.rest.service.crm.ms.MSCRMMessageFormatUtils;

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
  public final String[] getCRMAuthToken(final BasicObject basicObject) throws Exception {

    String stsEndpoint = null;
    String urnAddress = null;
    String userName = null;
    String password = null;
    String crmServiceURL = null;
    String crmServiceProtocal = null;

    /* Check if agent/tenant */
    if (basicObject instanceof CADUser) {

      /* Type cast it to agent */
      final CADUser agent = (CADUser) basicObject;

      logger.debug("---Inside getCRMAuthToken for agent: " + agent.getName());

      /* Check if additonal info is present */
      if (agent.getAdditionalInfo() != null) {

        /* Parse the reponse */
        stsEndpoint = agent.getAdditionalInfo().get("STSEnpoint");
        urnAddress = agent.getAdditionalInfo().get("URNAddress");
      }

      /* get CRM credentials */
      userName = agent.getCrmUserid();
      password = agent.getCrmPassword();
      crmServiceURL = agent.getCrmServiceURL();
      crmServiceProtocal = agent.getCrmServiceProtocol();

    } else /* Check if agent/tenant */
    if (basicObject instanceof Tenant) {

      /* Type cast it to tenant */
      final Tenant tenant = (Tenant) basicObject;

      logger.debug("---Inside getCRMAuthToken for tenant: " + tenant.getName());

      /* Check if additonal info is present */
      if (tenant.getAdditionalInfo() != null) {

        /* Parse the reponse */
        stsEndpoint = tenant.getAdditionalInfo().get("STSEnpoint");
        urnAddress = tenant.getAdditionalInfo().get("URNAddress");
      }

      /* get CRM credentials */
      userName = tenant.getCrmUserid();
      password = tenant.getCrmPassword();
      crmServiceURL = tenant.getCrmServiceURL();
      crmServiceProtocal = tenant.getCrmServiceProtocol();
    }

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

        throw new CADException(CADResponseCodes._1012 + " MS Login request failed : " + error);
      }

    } catch (final IOException e) {
      throw new CADException(CADResponseCodes._1015 + " Not able to connect to office 365 login server: " + stsEndpoint);
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
