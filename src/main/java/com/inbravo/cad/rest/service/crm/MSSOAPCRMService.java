package com.inbravo.cad.rest.service.crm;

import java.util.Map;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.CADUserInfoService;
import com.inbravo.cad.internal.service.SuperUserInfoService;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.Tenant;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.cad.rest.service.crm.ms.MSCRMObjectService;
import com.inbravo.cad.rest.service.crm.ms.auth.MSAuthManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSSOAPCRMService extends CRMService {

  private final Logger logger = Logger.getLogger(MSSOAPCRMService.class.getName());

  /* Decesion maker class to know about customer CRM info */
  private MSAuthManager mSAuthManager;

  /* Agent id special character */
  private String agentIdSplitCharacter;

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* Agent information service */
  private CADUserInfoService agentInfoService;

  /* MS CRM V4 CR service */
  private MSCRMObjectService mSCRMV4ObjectService;

  /* MS CRM V5 CR service */
  private MSCRMObjectService mSCRMV5ObjectService;

  @Override
  public final CADCommandObject createObject(final CADCommandObject eDSACommandObject) throws Exception {
    logger.debug("---Inside createObject");
    return this.getmSCRMObjectService(eDSACommandObject).createObject(eDSACommandObject);
  }

  @Override
  public final boolean deleteObject(final CADCommandObject eDSACommandObject, final String idToBeDeleted) throws Exception {
    logger.debug("---Inside deleteObject idToBeDeleted: " + idToBeDeleted);
    return this.getmSCRMObjectService(eDSACommandObject).deleteObject(eDSACommandObject, idToBeDeleted);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject) throws Exception {
    logger.debug("---Inside getObjects");
    return this.getmSCRMObjectService(eDSACommandObject).getObjects(eDSACommandObject);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query) throws Exception {
    logger.debug("---Inside getObjects query: " + query);
    return this.getmSCRMObjectService(eDSACommandObject).getObjects(eDSACommandObject, query);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select) throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select);
    return this.getmSCRMObjectService(eDSACommandObject).getObjects(eDSACommandObject, query, select);
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select, final String order)
      throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select + " & order: " + order);
    return this.getmSCRMObjectService(eDSACommandObject).getObjects(eDSACommandObject, query, select, order);
  }

  @Override
  public CADCommandObject getObjectsCount(CADCommandObject eDSACommandObject) throws Exception {
    return this.getmSCRMObjectService(eDSACommandObject).getObjectsCount(eDSACommandObject);
  }

  @Override
  public final CADCommandObject getObjectsCount(CADCommandObject eDSACommandObject, final String query) throws Exception {
    return this.getmSCRMObjectService(eDSACommandObject).getObjectsCount(eDSACommandObject, query);
  }

  @Override
  public final CADCommandObject updateObject(final CADCommandObject eDSACommandObject) throws Exception {
    return this.getmSCRMObjectService(eDSACommandObject).updateObject(eDSACommandObject);
  }

  /**
   * 
   * @param eDSACommandObject
   * @return
   */
  private final MSCRMObjectService getmSCRMObjectService(final CADCommandObject eDSACommandObject) throws Exception {

    /* CRM service params */
    CADUser agent = null;
    String serviceURL = null;
    String servicePrototol = "https";
    Map<String, String> nodeMap = null;
    String STSEnpoint = null;

    /* Check if tenant or agent request */
    if (eDSACommandObject.getAgent() != null && !"".equals(eDSACommandObject.getAgent())) {

      logger.debug("---Inside getmSCRMObjectService for agent: " + eDSACommandObject.getAgent());

      /* Check if session is already available at cache */
      agent = (CADUser) cRMSessionCache.recover(eDSACommandObject.getAgent().trim());

      /* Check if agent is found in cache */
      if (agent == null) {

        /* Get agent information from LDAP(CRM info is stored at LDAP) */
        agent = agentInfoService.getAgentInformation(eDSACommandObject.getAgent());
      }

      /* Get crm service URL */
      serviceURL = agent.getCrmServiceURL();
      servicePrototol = agent.getCrmServiceProtocol();
      nodeMap = agent.getAdditionalInfo();
    }

    /* Validate CRM URL */
    if (serviceURL == null && "".equals(serviceURL)) {

      /* Send user error */
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
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

        logger.debug("---Inside getmSCRMObjectService, adding MS login additonal info at agent: " + eDSACommandObject.getAgent());

        if (agent.getAdditionalInfo() != null) {

          /* Update agent additonal information */
          agent.getAdditionalInfo().putAll(nodeMap);
        } else {

          /* Update agent additonal information */
          agent.setAdditionalInfo(nodeMap);
        }

        /* Put back the agent back to cache */
        cRMSessionCache.admit(eDSACommandObject.getAgent().trim(), agent);
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
   * @return the agentIdSplitCharacter
   */
  public final String getAgentIdSplitCharacter() {
    return this.agentIdSplitCharacter;
  }

  /**
   * @param agentIdSplitCharacter the agentIdSplitCharacter to set
   */
  public final void setAgentIdSplitCharacter(final String agentIdSplitCharacter) {
    this.agentIdSplitCharacter = agentIdSplitCharacter;
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
   * @return the agentInfoService
   */
  public final CADUserInfoService getAgentInfoService() {
    return this.agentInfoService;
  }

  /**
   * @param agentInfoService the agentInfoService to set
   */
  public final void setAgentInfoService(final CADUserInfoService agentInfoService) {
    this.agentInfoService = agentInfoService;
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
