package com.inbravo.cad.rest.service.crm.ms.session;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.CADUserInfoService;
import com.inbravo.cad.internal.service.SuperUserInfoService;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.BasicObject;
import com.inbravo.cad.internal.service.dto.Tenant;
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

  /* Agent information service */
  private CADUserInfoService agentInfoService;

  /* Tenant information service */
  private SuperUserInfoService tenantInfoService;

  /* Agent id special character */
  private String agentIdSplitCharacter;

  /**
   * 
   * @param id
   * @return
   * @throws Exception
   */
  public final MSCRMVersionType checkMSCRMVersion(final String id) throws Exception {

    /* CRM service params */
    CADUser agent = null;
    Tenant tenant = null;
    String serviceURL = null;
    String servicePrototol = "https";
    Map<String, String> nodeMap = null;
    String STSEnpoint = null;

    /* Check if session is already available at cache */
    final BasicObject basicObject = (BasicObject) cRMSessionCache.recover(id);

    /* Check if basic object is null or not */
    if (basicObject == null) {

      /* If agent */
      if (id.contains(agentIdSplitCharacter)) {

        /* Get agent information from LDAP(CRM info is stored at LDAP) */
        agent = agentInfoService.getAgentInformation(id);

        /* Get crm service URL */
        serviceURL = agent.getCrmServiceURL();
        servicePrototol = agent.getCrmServiceProtocol();
        nodeMap = agent.getAdditionalInfo();
      } else {

        /* Get tenant information from LDAP(CRM info is stored at LDAP) */
        tenant = tenantInfoService.getTenantInformation(id);
      }

      /* Get crm service URL */
      serviceURL = tenant.getCrmServiceURL();
      servicePrototol = tenant.getCrmServiceProtocol();
      nodeMap = tenant.getAdditionalInfo();

    } else if (basicObject instanceof CADUser) {

      /* Else get agent from cache */
      agent = (CADUser) cRMSessionCache.recover(id.trim());

      /* Get crm service URL */
      serviceURL = agent.getCrmServiceURL();
      servicePrototol = agent.getCrmServiceProtocol();
      nodeMap = agent.getAdditionalInfo();
    } else if (basicObject instanceof Tenant) {

      /* Else get tenant from cache */
      tenant = (Tenant) cRMSessionCache.recover(id.trim());

      /* Get crm service URL */
      serviceURL = tenant.getCrmServiceURL();
      servicePrototol = tenant.getCrmServiceProtocol();
      nodeMap = tenant.getAdditionalInfo();
    } else {
      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1008 + "Agent/Tenant information is not valid");
    }

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

      /* Check if tenant or agent request */
      if (agent != null) {

        logger.debug("---Inside checkMSCRMVersion, adding MS login additonal info at agent: " + id);

        if (agent.getAdditionalInfo() != null) {

          /* Update agent additonal information */
          agent.getAdditionalInfo().putAll(nodeMap);
        } else {

          /* Update agent additonal information */
          agent.setAdditionalInfo(nodeMap);
        }

        /* Put the agent back to cache */
        cRMSessionCache.admit(id.trim(), agent);
      } else {

        if (tenant.getAdditionalInfo() != null) {

          /* Update tenant additonal information */
          tenant.getAdditionalInfo().putAll(nodeMap);
        } else {

          /* Update tenant additonal information */
          tenant.setAdditionalInfo(nodeMap);
        }

        /* Put the tenant back to cache */
        cRMSessionCache.admit(id.trim(), tenant);
      }
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
   * @return the tenantInfoService
   */
  public final SuperUserInfoService getTenantInfoService() {
    return this.tenantInfoService;
  }

  /**
   * @param tenantInfoService the tenantInfoService to set
   */
  public final void setTenantInfoService(final SuperUserInfoService tenantInfoService) {
    this.tenantInfoService = tenantInfoService;
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
}
