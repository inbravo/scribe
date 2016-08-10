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

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.internal.service.dto.CADUser;
import com.inbravo.scribe.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.scribe.rest.service.crm.ms.auth.MSCRMDiscoveryManager;
import com.inbravo.scribe.rest.service.crm.ms.dto.MSCRMUserInformation;
import com.inbravo.scribe.rest.service.crm.session.CRMSessionManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMLiveIdSessionManager implements CRMSessionManager {

  private final Logger logger = Logger.getLogger(MSCRMLiveIdSessionManager.class.getName());

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* MS CRM discovery service */
  private MSCRMDiscoveryManager mSCRMDiscoveryManager;

  @Override
  public final boolean login(final String crmUserId, final String crmPassword) throws Exception {

    /* Check if session is already available at cache */
    final CADUser user = (CADUser) cRMSessionCache.recover(crmUserId);
    logger.debug("---Inside login crmUserId: " + crmUserId);

    /* Get user information from Microsoft CRM */
    final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(user);

    if (mSCRMUserInformation != null) {

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
  public final CADUser getSessionInfo(final String crmUserId) throws Exception {
    logger.debug("---Inside getSessionInfo crmUserId: " + crmUserId);

    /* Check if session is already available at cache */
    final CADUser user = (CADUser) cRMSessionCache.recover(crmUserId);

    logger.debug("---Inside login crmUserId: " + crmUserId);

    if (user == null) {

      /* Inform user about unauthorized agent */
      throw new CADException(CADResponseCodes._1012 + "User");
    }

    /* Get user information from Microsoft CRM */
    final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(user);

    /* Login at Zen desk */
    if (mSCRMUserInformation != null) {

      /* Save this agent in session cache */
      cRMSessionCache.admit(crmUserId, user);

      /* If everything is fine return user */
      return user;
    } else {
      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
    }
  }


  public final synchronized CADUser getCrmUserInfoWithCRMSessionInformation(final String agentId) throws Exception {

    logger.debug("---Inside getCrmUserInfoWithCRMSessionInformation agent: " + agentId);

    /* Recover agent from cache */
    final CADUser agent = (CADUser) cRMSessionCache.recover(agentId);

    /* Service URL is must for MS-CRM */
    if (agent.getCrmServiceURL() == null) {

      /* Inform the user about error */
      throw new CADException(CADResponseCodes._1008 + "CRM integration information is missing: CRM service URL");
    }

    if (agent.getCrmSessionId() == null) {

      logger.debug("---Inside getCrmUserInfoWithCRMSessionInformation agent's CRM session is not found; Going to fetch session information");

      /* Get CRM service information from CRM */
      final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(agent);

      logger.debug("---Inside getCrmUserInfoWithCRMSessionInformation : CrmSessionId: " + mSCRMUserInformation.getCrmTicket());

      /* Set CRM ticket as session id at agent */
      agent.setCrmSessionId(mSCRMUserInformation.getCrmTicket());

      /* Set CRM organization information at agent */
      agent.setCrmOrgName(mSCRMUserInformation.getOrganizationName());
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(agentId, agent);

    return agent;
  }

  public final CRMSessionCache getcRMSessionCache() {
    return cRMSessionCache;
  }

  public final void setcRMSessionCache(final CRMSessionCache cRMSessionCache) {
    this.cRMSessionCache = cRMSessionCache;
  }

  public final MSCRMDiscoveryManager getmSCRMDiscoveryManager() {
    return mSCRMDiscoveryManager;
  }

  public final void setmSCRMDiscoveryManager(final MSCRMDiscoveryManager mSCRMDiscoveryManager) {
    this.mSCRMDiscoveryManager = mSCRMDiscoveryManager;
  }
}
