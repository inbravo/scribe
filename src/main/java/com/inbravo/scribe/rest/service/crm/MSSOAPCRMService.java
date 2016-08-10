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

package com.inbravo.scribe.rest.service.crm;

import java.util.Map;

import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.internal.service.dto.CADUser;
import com.inbravo.scribe.rest.resource.CADCommandObject;
import com.inbravo.scribe.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMObjectService;
import com.inbravo.scribe.rest.service.crm.ms.auth.MSAuthManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSSOAPCRMService extends CRMService {

  private final Logger logger = Logger.getLogger(MSSOAPCRMService.class.getName());

  /* Decesion maker class to know about customer CRM info */
  private MSAuthManager mSAuthManager;

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* MS CRM V4 CR service */
  private MSCRMObjectService mSCRMV4ObjectService;

  /* MS CRM V5 CR service */
  private MSCRMObjectService mSCRMV5ObjectService;

  @Override
  public final CADCommandObject createObject(final CADCommandObject cADCommandObject) throws Exception {
    logger.debug("---Inside createObject");
    return this.getmSCRMObjectService(cADCommandObject).createObject(cADCommandObject);
  }

  @Override
  public final boolean deleteObject(final CADCommandObject cADCommandObject, final String idToBeDeleted) throws Exception {
    logger.debug("---Inside deleteObject idToBeDeleted: " + idToBeDeleted);
    return this.getmSCRMObjectService(cADCommandObject).deleteObject(cADCommandObject, idToBeDeleted);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject) throws Exception {
    logger.debug("---Inside getObjects");
    return this.getmSCRMObjectService(cADCommandObject).getObjects(cADCommandObject);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query) throws Exception {
    logger.debug("---Inside getObjects query: " + query);
    return this.getmSCRMObjectService(cADCommandObject).getObjects(cADCommandObject, query);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select) throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select);
    return this.getmSCRMObjectService(cADCommandObject).getObjects(cADCommandObject, query, select);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select, final String order)
      throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select + " & order: " + order);
    return this.getmSCRMObjectService(cADCommandObject).getObjects(cADCommandObject, query, select, order);
  }

  @Override
  public CADCommandObject getObjectsCount(CADCommandObject cADCommandObject) throws Exception {
    return this.getmSCRMObjectService(cADCommandObject).getObjectsCount(cADCommandObject);
  }

  @Override
  public final CADCommandObject getObjectsCount(CADCommandObject cADCommandObject, final String query) throws Exception {
    return this.getmSCRMObjectService(cADCommandObject).getObjectsCount(cADCommandObject, query);
  }

  @Override
  public final CADCommandObject updateObject(final CADCommandObject cADCommandObject) throws Exception {
    return this.getmSCRMObjectService(cADCommandObject).updateObject(cADCommandObject);
  }

  /**
   * 
   * @param cADCommandObject
   * @return
   */
  private final MSCRMObjectService getmSCRMObjectService(final CADCommandObject cADCommandObject) throws Exception {

    /* CRM service params */
    CADUser agent = null;
    String serviceURL = null;
    String servicePrototol = "https";
    Map<String, String> nodeMap = null;
    String STSEnpoint = null;

    /* Check if CRM user id is found */
    if (cADCommandObject.getCrmUserId() != null && !"".equals(cADCommandObject.getCrmUserId())) {

      logger.debug("---Inside getmSCRMObjectService for user: " + cADCommandObject.getCrmUserId());

      /* Check if session is already available at cache */
      agent = (CADUser) cRMSessionCache.recover(cADCommandObject.getCrmUserId().trim());

      /* Get crm service URL */
      serviceURL = agent.getCrmServiceURL();
      servicePrototol = agent.getCrmServiceProtocol();
      nodeMap = agent.getAdditionalInfo();
    }

    /* Validate CRM URL */
    if (serviceURL == null && "".equals(serviceURL)) {

      /* Send user error */
      throw new CADException(CADResponseCodes._1008 + "CRM integration information is missing: CRM service URL");
    }

    /* if additional info is available */
    if (nodeMap != null && nodeMap.get("STSEnpoint") != null) {

      logger.debug("---Inside getmSCRMObjectService, STSEnpoint information is found");

      /* Parse the reponse */
      STSEnpoint = nodeMap.get("STSEnpoint");
    } else {

      logger.debug("---Inside getmSCRMObjectService, going to MS CRM for STSEnpoint information");

      /* Create additonal information map for next level usage */
      nodeMap = mSAuthManager.getMSCRMOrganizationInfo(serviceURL, servicePrototol);

      /* Parse the reponse */
      STSEnpoint = nodeMap.get("STSEnpoint");

      /* Check if tenant or agent request */
      if (agent != null) {

        logger.debug("---Inside getmSCRMObjectService, adding MS login additonal info at agent: " + cADCommandObject.getCrmUserId());

        if (agent.getAdditionalInfo() != null) {

          /* Update agent additonal information */
          agent.getAdditionalInfo().putAll(nodeMap);
        } else {

          /* Update agent additonal information */
          agent.setAdditionalInfo(nodeMap);
        }

        /* Put back the agent back to cache */
        cRMSessionCache.admit(cADCommandObject.getCrmUserId().trim(), agent);
      }
    }

    /* Check if Live Id authentication is desired */
    if ((STSEnpoint != null) && (STSEnpoint.startsWith("https://login.live.com"))) {

      logger.debug("---Inside getmSCRMObjectService, found Live Id based service, Will use V4 WSDL");
      /* Return Live id based V4 service */
      return mSCRMV4ObjectService;
    } else {

      logger.debug("---Inside getmSCRMObjectService, found Office 365 based service, Will use V5 WSDL");
      /* Return Office 365 based V5 service */
      return mSCRMV5ObjectService;
    }
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
   * @return the mSCRMV4ObjectService
   */
  public final MSCRMObjectService getmSCRMV4ObjectService() {
    return this.mSCRMV4ObjectService;
  }

  /**
   * @param mSCRMV4ObjectService the mSCRMV4ObjectService to set
   */
  public final void setmSCRMV4ObjectService(final MSCRMObjectService mSCRMV4ObjectService) {
    this.mSCRMV4ObjectService = mSCRMV4ObjectService;
  }

  /**
   * @return the mSCRMV5ObjectService
   */
  public final MSCRMObjectService getmSCRMV5ObjectService() {
    return this.mSCRMV5ObjectService;
  }

  /**
   * @param mSCRMV5ObjectService the mSCRMV5ObjectService to set
   */
  public final void setmSCRMV5ObjectService(final MSCRMObjectService mSCRMV5ObjectService) {
    this.mSCRMV5ObjectService = mSCRMV5ObjectService;
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
}
