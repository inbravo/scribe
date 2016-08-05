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

package com.inbravo.cad.rest.service.crm.ms.session;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.rest.constants.CRMConstants.MSCRMVersionType;
import com.inbravo.cad.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.cad.rest.service.crm.ms.auth.MSAuthManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMSessionManagerFactory implements ApplicationContextAware {

  private final Logger logger = Logger.getLogger(MSCRMSessionManagerFactory.class.getName());

  /* Decesion maker class to know about customer CRM info */
  private MSAuthManager mSAuthManager;

  /* Spring app context */
  private ApplicationContext applicationContext;

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* Agent id special character */
  private String agentIdSplitCharacter;

  /**
   * 
   * @param id
   * @return
   * @throws Exception
   */
  public final MSCRMVersionType checkMSCRMVersion(final String id) throws Exception {

    /* Get agent from cache */
    final CADUser user = (CADUser) cRMSessionCache.recover(id.trim());

    /* Get crm service URL */
    String STSEnpoint = null;
    final String serviceURL = user.getCrmServiceURL();
    final String servicePrototol = user.getCrmServiceProtocol();
    Map<String, String> nodeMap = user.getAdditionalInfo();

    /* Validate CRM service URL */
    if (serviceURL == null || "".equals(serviceURL)) {

      /* Send user error */
      throw new CADException(CADResponseCodes._1009 + "CRM integration info is missing: CRM service URL");
    }

    /* if additional info is available */
    if (nodeMap != null && nodeMap.get("STSEnpoint") != null) {

      logger.debug("---Inside checkMSCRMVersion, STSEnpoint information is found");

      /* Parse the reponse */
      STSEnpoint = nodeMap.get("STSEnpoint");
    } else {

      logger.debug("---Inside checkMSCRMVersion, going to MS CRM for STSEnpoint information");

      /* Create additonal information map for next level usage */
      nodeMap = mSAuthManager.getMSCRMOrganizationInfo(serviceURL, servicePrototol);

      /* Parse the reponse */
      STSEnpoint = nodeMap.get("STSEnpoint");



      logger.debug("---Inside checkMSCRMVersion, adding MS login additonal info at agent: " + id);

      if (user.getAdditionalInfo() != null) {

        /* Update agent additonal information */
        user.getAdditionalInfo().putAll(nodeMap);
      } else {

        /* Update agent additonal information */
        user.setAdditionalInfo(nodeMap);
      }

      /* Put the agent back to cache */
      cRMSessionCache.admit(id.trim(), user);

    }

    /* Check if Live Id authentication is desired */
    if ((STSEnpoint != null) && (STSEnpoint.startsWith("https://login.live.com"))) {

      logger.debug("---Inside checkMSCRMVersion, found Live Id based service, Will use V4 WSDL");
      /* Return Live id based V4 service */
      return MSCRMVersionType.V4;
    } else {

      logger.debug("---Inside checkMSCRMVersion, found Office 365 based service, Will use V5 WSDL");
      /* Return Office 365 based V5 service */
      return MSCRMVersionType.V5;
    }

  }

  @Override
  public final void setApplicationContext(final ApplicationContext ApplicationContext) throws BeansException {
    this.applicationContext = ApplicationContext;
  }

  public final ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /**
   * @return the mSAuthManager
   */
  public final MSAuthManager getmSAuthManager() {
    return this.mSAuthManager;
  }

  /**
   * @param mSAuthManager the mSAuthManager to set
   */
  public final void setmSAuthManager(final MSAuthManager mSAuthManager) {
    this.mSAuthManager = mSAuthManager;
  }

  /**
   * @return the cRMSessionCache
   */
  public final CRMSessionCache getcRMSessionCache() {
    return this.cRMSessionCache;
  }

  /**
   * @param cRMSessionCache the cRMSessionCache to set
   */
  public final void setcRMSessionCache(final CRMSessionCache cRMSessionCache) {
    this.cRMSessionCache = cRMSessionCache;
  }

  /**
   * @return the agentIdSplitCharacter
   */
  public final String getCrmUserIdIdSplitCharacter() {
    return this.agentIdSplitCharacter;
  }

  /**
   * @param agentIdSplitCharacter the agentIdSplitCharacter to set
   */
  public final void setCrmUserIdIdSplitCharacter(final String agentIdSplitCharacter) {
    this.agentIdSplitCharacter = agentIdSplitCharacter;
  }
}
