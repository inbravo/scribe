package com.inbravo.cad.rest.service.crm.ms.auth;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.jaxen.XPath;

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
  public final String[] getCRMAuthToken(final BasicObject basicObject) throws Exception {

    String userName = null;
    String password = null;
    String crmServiceURL = null;

    /* Check if agent/tenant */
    if (basicObject instanceof CADUser) {

      /* Type cast it to agent */
      final CADUser agent = (CADUser) basicObject;

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getCRMAuthToken for agent: " + agent.getName());
      }

      /* get CRM credentials */
      userName = agent.getCrmUserid();
      password = agent.getCrmPassword();
      crmServiceURL = agent.getCrmServiceURL();

    } else /* Check if agent/tenant */
    if (basicObject instanceof Tenant) {

      /* Type cast it to tenant */
      final Tenant tenant = (Tenant) basicObject;

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside getCRMAuthToken for tenant: " + tenant.getName());
      }

      /* get CRM credentials */
      userName = tenant.getCrmUserid();
      password = tenant.getCrmPassword();
      crmServiceURL = tenant.getCrmServiceURL();
    }

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
