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

package com.inbravo.scribe.rest.service.crm.zd.session;

import java.util.Map;

import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.internal.service.dto.BasicObject;
import com.inbravo.scribe.internal.service.dto.CADUser;
import com.inbravo.scribe.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.scribe.rest.service.crm.session.CRMSessionManager;
import com.inbravo.scribe.rest.service.crm.zd.auth.ZDAuthManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZDCRMSessionManager implements CRMSessionManager {

  private final Logger logger = Logger.getLogger(ZDCRMSessionManager.class.getName());

  /* Authentication manager */
  private ZDAuthManager zDAuthManager;

  /* Agent id special character */
  private String agentIdSplitCharacter;

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  @Override
  public final boolean login(final String crmUserId, final String crmPassword) throws Exception {

    /* Check if session is already available at cache */
    final CADUser user = (CADUser) cRMSessionCache.recover(crmUserId);

    /* Validate crm service params */
    this.validateAgent(user);

    /* Login at Zen desk */
    if (zDAuthManager.login(user.getCrmAPIUserId(), user.getCrmAPIPassword(), user.getCrmServiceURL(), user.getCrmServiceProtocol(),
        user.getCrmPort())) {

      /* Save this agent in session cache */
      cRMSessionCache.admit(crmUserId, user);

      /* If everything is fine return true */
      return true;
    } else {
      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
    }
  }

  @Override
  public final boolean reset(final String crmUserId, final String crmPassword) throws Exception {

    logger.debug("---Inside reset id: " + crmUserId);

    /* Check if session is already available at cache */
    return this.login(crmUserId, crmPassword);
  }

  @Override
  public final BasicObject getSessionInfo(final String id) throws Exception {

    logger.debug("---Inside getSessionInfo id: " + id);

    /* Check if session is already available at cache */
    final CADUser user = (CADUser) cRMSessionCache.recover(id);

    /* Validate crm service params */
    this.validateAgent(user);

    /* Login at Zen desk */
    String crmUserId = user.getCrmUserId();
    String crmPassword = user.getCrmPassword();

    /* Check if CRM API token is present */
    if (user.getCrmAPIToken() != null && !user.getCrmAPIToken().equals("")) {

      crmUserId += "/token";
      crmPassword = user.getCrmAPIToken();
    }

    /* Get session information from ZD */
    final Map<String, String> additionalInfoMap =
        zDAuthManager.getSessionInfoAfterValidLogin(crmUserId, crmPassword, user.getCrmServiceURL(), user.getCrmServiceProtocol(), user.getCrmPort());

    if (additionalInfoMap != null) {

      /* Set additonal information */
      user.setAdditionalInfo(additionalInfoMap);

      /* Save this agent in session cache */
      cRMSessionCache.admit(id, user);

      /* If everything is fine return true */
      return user;
    } else {
      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
    }
  }

  public final synchronized CADUser getCrmUserIdWithCRMSessionInformation(final String agentId) throws Exception {
    logger.debug("---Inside getCrmUserIdWithCRMSessionInformation");

    /* Recover agent from cache */
    final CADUser user = (CADUser) cRMSessionCache.recover(agentId);

    /* Validate crm service params */
    this.validateAgent(user);

    String crmUserId = user.getCrmUserId();
    String crmPassword = user.getCrmPassword();
    if (user.getCrmAPIToken() != null && !user.getCrmAPIToken().equals("")) {
      crmUserId += "/token";
      crmPassword = user.getCrmAPIToken();
    }
    /* Check for ZD session id */
    if (user.getCrmSessionId() == null) {
      user.setCrmSessionId(zDAuthManager.getSessionId(crmUserId, crmPassword, user.getCrmServiceURL(), user.getCrmServiceProtocol(),
          user.getCrmPort()));
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(agentId, user);

    return user;
  }

  public final ZDAuthManager getzDAuthManager() {
    return zDAuthManager;
  }

  public final void setzDAuthManager(final ZDAuthManager zDAuthManager) {
    this.zDAuthManager = zDAuthManager;
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

  private final void validateAgent(final CADUser agent) {

    /* Service URL is a must for ZD */
    if (agent.getCrmServiceURL() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for ZD */
    if (agent.getCrmServiceProtocol() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service Protocol");
    }
  }
}
