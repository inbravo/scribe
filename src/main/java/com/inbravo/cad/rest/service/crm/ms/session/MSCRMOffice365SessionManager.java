package com.inbravo.cad.rest.service.crm.ms.session;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.BasicObject;
import com.inbravo.cad.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.cad.rest.service.crm.ms.auth.MSOffice365AuthManager;
import com.inbravo.cad.rest.service.crm.session.CRMSessionManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMOffice365SessionManager implements CRMSessionManager {

  private final Logger logger = Logger.getLogger(MSCRMOffice365SessionManager.class.getName());

  /* Agent id special character */
  private String agentIdSplitCharacter;

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* Office 365 authentication manager */
  private MSOffice365AuthManager mSOffice365AuthManager;

  @Override
  public final boolean login(final String crmUserId, final String crmPassword) throws Exception {

    /* Check if session is already available at cache */
    final BasicObject basicObject = (BasicObject) cRMSessionCache.recover(crmUserId);

    logger.debug("---Inside login agent from cache: " + crmUserId);

    /* Get agent information from cache */
    final CADUser user = (CADUser) basicObject;

    /* Validate crm service params */
    this.validateUser(user);

    /* Get user information from Microsoft CRM */
    final String[] crmSecurityToken = mSOffice365AuthManager.getCRMAuthToken(user);

    if (crmSecurityToken != null) {

      /* Add token in agent */
      user.setCrmSecurityToken(crmSecurityToken);

      /* Save this agent in session cache */
      cRMSessionCache.admit(crmUserId, user);

      /* If everything is fine return true */
      return true;
    } else {

      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
    }
  }

  @Override
  public final boolean reset(final String crmUserId, final String crmPassword) throws Exception {

    logger.debug("---Inside reset crmUserId: " + crmUserId);

    /* Check if session is already available at cache */
    return this.login(crmUserId, crmPassword);
  }

  @Override
  public final BasicObject getSessionInfo(final String crmUserId) throws Exception {

    logger.debug("---Inside getSessionInfo id: " + crmUserId);

    /* Check if session is already available at cache */
    final BasicObject basicObject = (BasicObject) cRMSessionCache.recover(crmUserId);

    /* Get agent information from cache */
    final CADUser user = (CADUser) basicObject;

    /* Validate crm service params */
    this.validateUser(user);

    /* Get user information from Microsoft CRM */
    final String[] crmSecurityToken = mSOffice365AuthManager.getCRMAuthToken(user);

    if (crmSecurityToken != null) {

      /* Add token in agent */
      user.setCrmSecurityToken(crmSecurityToken);

      /* Save this agent in session cache */
      cRMSessionCache.admit(crmUserId, user);

      /* If everything is fine return true */
      return basicObject;
    } else {

      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
    }
  }



  /**
   * Get agent CRM information
   * 
   * @param agentId
   * @return
   * @throws Exception
   */
  public final synchronized CADUser getCrmUserInfoWithCRMSessionInformation(final String agentId) throws Exception {

    logger.debug("---Inside getCrmUserInfoWithCRMSessionInformation agent: " + agentId);

    /* Recover agent from cache */
    final CADUser agent = (CADUser) cRMSessionCache.recover(agentId);

    /* Service URL is must for MS-CRM */
    if (agent.getCrmServiceURL() == null) {

      /* Inform user about missing credentials */
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: MS CRM service URL");
    }

    if (agent.getCrmSecurityToken() == null) {

      logger.debug("---Inside getTenantWithCRMSessionInformation agent's CRM session is not found; Going to fetch session information");

      /* Get user information from Microsoft CRM */
      final String[] crmSecurityToken = mSOffice365AuthManager.getCRMAuthToken(agent);

      /* Set CRM security token in agent */
      agent.setCrmSecurityToken(crmSecurityToken);
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(agentId, agent);

    return agent;
  }

  public final String getCrmUserIdIdSplitCharacter() {
    return agentIdSplitCharacter;
  }

  public final void setCrmUserIdIdSplitCharacter(final String agentIdSplitCharacter) {
    this.agentIdSplitCharacter = agentIdSplitCharacter;
  }

  public final CRMSessionCache getcRMSessionCache() {
    return cRMSessionCache;
  }

  public final void setcRMSessionCache(final CRMSessionCache cRMSessionCache) {
    this.cRMSessionCache = cRMSessionCache;
  }

  private final void validateUser(final CADUser agent) {

    /* Service URL is a must for MS-CRM */
    if (agent.getCrmServiceURL() == null) {

      /* Inform user about missing credentials */
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for MS-CRM */
    if (agent.getCrmServiceProtocol() == null) {

      /* Inform user about missing credentials */
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service Protocol");
    }
  }

  /**
   * @return the mSOffice365AuthManager
   */
  public final MSOffice365AuthManager getmSOffice365AuthManager() {
    return this.mSOffice365AuthManager;
  }

  /**
   * @param mSOffice365AuthManager the mSOffice365AuthManager to set
   */
  public final void setmSOffice365AuthManager(final MSOffice365AuthManager mSOffice365AuthManager) {
    this.mSOffice365AuthManager = mSOffice365AuthManager;
  }
}
