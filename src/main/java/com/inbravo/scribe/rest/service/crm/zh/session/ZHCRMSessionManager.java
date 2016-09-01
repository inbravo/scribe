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

package com.inbravo.scribe.rest.service.crm.zh.session;

import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.service.crm.cache.BasicObject;
import com.inbravo.scribe.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.scribe.rest.service.crm.cache.ScribeCacheObject;
import com.inbravo.scribe.rest.service.crm.session.CRMSessionManager;
import com.inbravo.scribe.rest.service.crm.zh.auth.ZHAuthManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZHCRMSessionManager implements CRMSessionManager {

  private final Logger logger = Logger.getLogger(ZHCRMSessionManager.class.getName());

  /* Authentication manager */
  private ZHAuthManager zHAuthManager;

  /* Agent id special character */
  private String agentIdSplitCharacter;

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  @Override
  public final BasicObject getSessionInfo(final String id) throws Exception {

    logger.debug("----Inside getSessionInfo, id: " + id);

    /* Check if session is already available at cache */
    final ScribeCacheObject cacheObject = (ScribeCacheObject) cRMSessionCache.recover(id);

    logger.debug("----Inside getSessionInfo, agent: " + id);

    /* Validate crm service params */
    this.validateCacheObject(cacheObject);

    /* Get session information from ZH */
    final String sessionId =
        zHAuthManager.getSessionId(cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword(), cacheObject
            .getScribeMetaObject().getCrmServiceURL(), cacheObject.getScribeMetaObject().getCrmServiceProtocol());

    if (sessionId != null) {

      /* Set CRM API token information */
      cacheObject.getScribeMetaObject().setCrmSessionId(sessionId);

      /* Save this agent in session cache */
      cRMSessionCache.admit(id, cacheObject);

      /* If everything is fine return true */
      return cacheObject;
    } else {
      /* Inform user about absent header value */
      throw new ScribeException(ScribeResponseCodes._1012 + "Login attempt at ZH is failed. Check credentials.");
    }
  }

  @Override
  public final boolean login(final String crmUserId, final String crmPassword) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + "Following operation is not supported by the Scribe");
  }

  @Override
  public final boolean reset(final String crmUserId, final String crmPassword) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + "Following operation is not supported by the Scribe");
  }

  public final ZHAuthManager getzHAuthManager() {
    return zHAuthManager;
  }

  public final void setzHAuthManager(final ZHAuthManager zHAuthManager) {
    this.zHAuthManager = zHAuthManager;
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

  private final void validateCacheObject(final ScribeCacheObject cacheObject) {

    /* Service URL is a must for ZH */
    if (cacheObject.getScribeMetaObject().getCrmServiceURL() == null) {
      throw new ScribeException(ScribeResponseCodes._1008 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for ZH */
    if (cacheObject.getScribeMetaObject().getCrmServiceProtocol() == null) {
      throw new ScribeException(ScribeResponseCodes._1008 + "CRM integration information is missing: CRM service Protocol");
    }
  }
}
