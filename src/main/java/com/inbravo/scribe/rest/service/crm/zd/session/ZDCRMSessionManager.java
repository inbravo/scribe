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

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.service.crm.cache.BasicObject;
import com.inbravo.scribe.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.scribe.rest.service.crm.cache.ScribeCacheObject;
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
    final ScribeCacheObject cacheObject = (ScribeCacheObject) cRMSessionCache.recover(crmUserId);

    /* Validate crm service params */
    this.validateUser(cacheObject);

    /* Login at Zen desk */
    if (zDAuthManager.login(cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword(), cacheObject
        .getScribeMetaObject().getCrmServiceURL(), cacheObject.getScribeMetaObject().getCrmServiceProtocol(), cacheObject.getScribeMetaObject().getCrmPort())) {

      /* Save this agent in session cache */
      cRMSessionCache.admit(crmUserId, cacheObject);

      /* If everything is fine return true */
      return true;
    } else {
      /* Inform user about absent header value */
      throw new ScribeException(ScribeResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
    }
  }

  @Override
  public final boolean reset(final String crmUserId, final String crmPassword) throws Exception {

    logger.debug("----Inside reset id: " + crmUserId);

    /* Check if session is already available at cache */
    return this.login(crmUserId, crmPassword);
  }

  @Override
  public final BasicObject getSessionInfo(final String id) throws Exception {

    logger.debug("----Inside getSessionInfo id: " + id);

    /* Check if session is already available at cache */
    final ScribeCacheObject user = (ScribeCacheObject) cRMSessionCache.recover(id);

    /* Validate crm service params */
    this.validateUser(user);

    /* Login at Zen desk */
    String crmUserId = user.getScribeMetaObject().getCrmUserId();
    String crmPassword = user.getScribeMetaObject().getCrmPassword();

    /* Check if CRM API token is present */
    if (user.getScribeMetaObject().getCrmSecurityToken() != null && !user.getScribeMetaObject().getCrmSecurityToken()[0].equals("")) {

      crmUserId += "/token";
      crmPassword = user.getScribeMetaObject().getCrmSecurityToken()[0];
    }

    /* Get session information from ZD */
    final Map<String, String> additionalInfoMap =
        zDAuthManager.getSessionInfoAfterValidLogin(crmUserId, crmPassword, user.getScribeMetaObject().getCrmServiceURL(), user.getScribeMetaObject()
            .getCrmServiceProtocol(), user.getScribeMetaObject().getCrmPort());

    if (additionalInfoMap != null) {

      /* Set additonal information */
      user.setAdditionalInfo(additionalInfoMap);

      /* Save this agent in session cache */
      cRMSessionCache.admit(id, user);

      /* If everything is fine return true */
      return user;
    } else {
      /* Inform user about absent header value */
      throw new ScribeException(ScribeResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
    }
  }

  public final synchronized ScribeCacheObject getCrmUserIdWithCRMSessionInformation(final String agentId) throws Exception {
    logger.debug("----Inside getCrmUserIdWithCRMSessionInformation");

    /* Recover agent from cache */
    final ScribeCacheObject cacheObject = (ScribeCacheObject) cRMSessionCache.recover(agentId);

    /* Validate crm service params */
    this.validateUser(cacheObject);

    String crmUserId = cacheObject.getScribeMetaObject().getCrmUserId();
    String crmPassword = cacheObject.getScribeMetaObject().getCrmPassword();

    /* Check if CRM API token is present */
    if (cacheObject.getScribeMetaObject().getCrmSecurityToken() != null && !cacheObject.getScribeMetaObject().getCrmSecurityToken()[0].equals("")) {

      crmUserId += "/token";
      crmPassword = cacheObject.getScribeMetaObject().getCrmSecurityToken()[0];
    }

    /* Check for ZD session id */
    if (cacheObject.getScribeMetaObject().getCrmSessionId() == null) {

      /* Set CRM session id in cache object */
      cacheObject.getScribeMetaObject().setCrmSessionId(
          zDAuthManager.getSessionId(crmUserId, crmPassword, cacheObject.getScribeMetaObject().getCrmServiceURL(), cacheObject.getScribeMetaObject()
              .getCrmServiceProtocol(), cacheObject.getScribeMetaObject().getCrmPort()));
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(agentId, cacheObject);

    return cacheObject;
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

  private final void validateUser(final ScribeCacheObject cacheObject) {

    /* Service URL is a must for ZD */
    if (cacheObject.getScribeMetaObject().getCrmServiceURL() == null) {
      throw new ScribeException(ScribeResponseCodes._1008 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for ZD */
    if (cacheObject.getScribeMetaObject().getCrmServiceProtocol() == null) {
      throw new ScribeException(ScribeResponseCodes._1008 + "CRM integration information is missing: CRM service Protocol");
    }
  }
}
