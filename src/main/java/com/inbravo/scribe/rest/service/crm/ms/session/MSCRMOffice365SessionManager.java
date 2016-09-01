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

package com.inbravo.scribe.rest.service.crm.ms.session;

import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.service.crm.cache.BasicObject;
import com.inbravo.scribe.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.scribe.rest.service.crm.cache.ScribeCacheObject;
import com.inbravo.scribe.rest.service.crm.ms.auth.MSOffice365AuthManager;
import com.inbravo.scribe.rest.service.crm.session.CRMSessionManager;

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

    logger.debug("----Inside login agent from cache: " + crmUserId);

    /* Get agent information from cache */
    final ScribeCacheObject user = (ScribeCacheObject) basicObject;

    /* Validate crm service params */
    this.validateUser(user);

    /* Get user information from Microsoft CRM */
    final String[] crmSecurityToken = mSOffice365AuthManager.getCRMAuthToken(user);

    if (crmSecurityToken != null) {

      /* Add token in agent */
      user.getScribeMetaObject().setCrmSecurityToken(crmSecurityToken);

      /* Save this agent in session cache */
      cRMSessionCache.admit(crmUserId, user);

      /* If everything is fine return true */
      return true;
    } else {

      /* Inform user about absent header value */
      throw new ScribeException(ScribeResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
    }
  }

  @Override
  public final boolean reset(final String crmUserId, final String crmPassword) throws Exception {

    logger.debug("----Inside reset crmUserId: " + crmUserId);

    /* Check if session is already available at cache */
    return this.login(crmUserId, crmPassword);
  }

  @Override
  public final BasicObject getSessionInfo(final String crmUserId) throws Exception {

    logger.debug("----Inside getSessionInfo id: " + crmUserId);

    /* Check if session is already available at cache */
    final BasicObject basicObject = (BasicObject) cRMSessionCache.recover(crmUserId);

    /* Get agent information from cache */
    final ScribeCacheObject user = (ScribeCacheObject) basicObject;

    /* Validate crm service params */
    this.validateUser(user);

    /* Get user information from Microsoft CRM */
    final String[] crmSecurityToken = mSOffice365AuthManager.getCRMAuthToken(user);

    if (crmSecurityToken != null) {

      /* Add token in agent */
      user.getScribeMetaObject().setCrmSecurityToken(crmSecurityToken);

      /* Save this agent in session cache */
      cRMSessionCache.admit(crmUserId, user);

      /* If everything is fine return true */
      return basicObject;
    } else {

      /* Inform user about absent header value */
      throw new ScribeException(ScribeResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
    }
  }



  /**
   * Get agent CRM information
   * 
   * @param agentId
   * @return
   * @throws Exception
   */
  public final synchronized ScribeCacheObject getCrmUserInfoWithCRMSessionInformation(final String agentId) throws Exception {

    logger.debug("----Inside getCrmUserInfoWithCRMSessionInformation agent: " + agentId);

    /* Recover cacheObject from cache */
    final ScribeCacheObject cacheObject = (ScribeCacheObject) cRMSessionCache.recover(agentId);

    /* Service URL is must for MS-CRM */
    if (cacheObject.getScribeMetaObject().getCrmServiceURL() == null) {

      /* Inform user about missing credentials */
      throw new ScribeException(ScribeResponseCodes._1008 + "CRM integration information is missing: MS CRM service URL");
    }

    if (cacheObject.getScribeMetaObject().getCrmSecurityToken() == null) {

      logger.debug("----Inside getTenantWithCRMSessionInformation agent's CRM session is not found; Going to fetch session information");

      /* Get user information from Microsoft CRM */
      final String[] crmSecurityToken = mSOffice365AuthManager.getCRMAuthToken(cacheObject);

      /* Set CRM security token in agent */
      cacheObject.getScribeMetaObject().setCrmSecurityToken(crmSecurityToken);
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(agentId, cacheObject);

    return cacheObject;
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

  private final void validateUser(final ScribeCacheObject agent) {

    /* Service URL is a must for MS-CRM */
    if (agent.getScribeMetaObject().getCrmServiceURL() == null) {

      /* Inform user about missing credentials */
      throw new ScribeException(ScribeResponseCodes._1008 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for MS-CRM */
    if (agent.getScribeMetaObject().getCrmServiceProtocol() == null) {

      /* Inform user about missing credentials */
      throw new ScribeException(ScribeResponseCodes._1008 + "CRM integration information is missing: CRM service Protocol");
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
