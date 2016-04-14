package com.inbravo.cad.rest.service.crm.ms.session;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.CADUserInfoService;
import com.inbravo.cad.internal.service.SuperUserInfoService;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.BasicObject;
import com.inbravo.cad.internal.service.dto.Tenant;
import com.inbravo.cad.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.cad.rest.service.crm.ms.auth.MSCRMDiscoveryManager;
import com.inbravo.cad.rest.service.crm.ms.dto.MSCRMUserInformation;
import com.inbravo.cad.rest.service.crm.session.CRMSessionManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMLiveIdSessionManager implements CRMSessionManager {

  private final Logger logger = Logger.getLogger(MSCRMLiveIdSessionManager.class.getName());

  /* Agent id special character */
  private String agentIdSplitCharacter;

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* Agent information service */
  private CADUserInfoService agentInfoService;

  /* Tenant information service */
  private SuperUserInfoService tenantInfoService;

  /* MS CRM discovery service */
  private MSCRMDiscoveryManager mSCRMDiscoveryManager;

  @SuppressWarnings("unused")
  @Override
  public final boolean login(final String id) throws Exception {

    /* Check if session is already available at cache */
    final BasicObject basicObject = (BasicObject) cRMSessionCache.recover(id);

    /* Check if basic object is null or not */
    if (basicObject == null) {

      if (id.contains(agentIdSplitCharacter)) {

        logger.debug("---Inside login agent: " + id);

        /* Get agent information from LDAP(CRM info is stored at LDAP) */
        final CADUser agent = agentInfoService.getAgentInformation(id);

        if (agent == null) {
          /* Inform user about unauthorized agent */
          throw new CADException(CADResponseCodes._1012 + "Agent");
        }

        /* Validate crm service params */
        this.validateAgent(agent);

        /* Get user information from Microsoft CRM */
        final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(agent);

        if (mSCRMUserInformation != null) {

          /* Save this agent in session cache */
          cRMSessionCache.admit(id, agent);

          /* If everything is fine return true */
          return true;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
        }
      } else {
        logger.debug("---Inside login tenant: " + id);

        /* Get tenant information from LDAP(CRM info is stored at DB) */
        final Tenant tenant = tenantInfoService.getTenantInformation(id);

        if (tenant == null) {
          /* Inform user about unauthorized agent */
          throw new CADException(CADResponseCodes._1012 + "Tenant");
        }

        /* Validate crm service params */
        this.validateTenant(tenant);

        /* Get user information from Microsoft CRM */
        final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(tenant);

        if (mSCRMUserInformation != null) {

          /* Save this tenant in session cache */
          cRMSessionCache.admit(id, tenant);

          /* If everything is fine return true */
          return true;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
        }
      }
    } else {
      /* Check the type of user */
      if (basicObject instanceof CADUser) {

        logger.debug("---Inside login agent from cache: " + id);

        /* Get agent information from cache */
        CADUser agent = (CADUser) basicObject;

        if (agent == null) {

          /* Get fresh agent from LDAP */
          agent = agentInfoService.getAgentInformation(id);

          /* If tenant is still not found; tenant is not valid */
          if (agent == null) {

            /* Inform user about unauthorized agent */
            throw new CADException(CADResponseCodes._1012 + "Agent");
          }
        }

        /* Validate crm service params */
        this.validateAgent(agent);

        /* Get user information from Microsoft CRM */
        final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(agent);

        if (mSCRMUserInformation != null) {

          /* Save this agent in session cache */
          cRMSessionCache.admit(id, agent);

          /* If everything is fine return true */
          return true;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
        }
      } else if (basicObject instanceof Tenant) {

        logger.debug("---Inside login tenant from cache: " + id);

        /* Get tenant information from cache */
        Tenant tenant = (Tenant) basicObject;

        if (tenant == null) {

          /* Get fresh tenant from LDAP */
          tenant = tenantInfoService.getTenantInformation(id);

          /* If tenant is still not found; tenant is not valid */
          if (tenant == null) {

            /* Inform user about unauthorized agent */
            throw new CADException(CADResponseCodes._1012 + "Tenant");
          }
        }

        /* Validate crm service params */
        this.validateTenant(tenant);

        /* Get user information from Microsoft CRM */
        final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(tenant);

        if (mSCRMUserInformation != null) {

          /* Save this tenant in session cache */
          cRMSessionCache.admit(id, tenant);

          /* If everything is fine return true */
          return true;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
        }
      } else {
        /* Inform user about absent header value */
        throw new CADException(CADResponseCodes._1008 + " Agent/Tenant information is not valid");
      }
    }
  }

  @Override
  public final boolean reset(final String id) throws Exception {
    logger.debug("---Inside reset id: " + id);
    /* Check if session is already available at cache */
    return this.login(id);
  }

  @SuppressWarnings("unused")
  @Override
  public final BasicObject getSessionInfo(final String id) throws Exception {
    logger.debug("---Inside getSessionInfo id: " + id);

    /* Check if session is already available at cache */
    final BasicObject basicObject = (BasicObject) cRMSessionCache.recover(id);

    /* Check if basic object is null or not */
    if (basicObject == null) {

      if (id.contains(agentIdSplitCharacter)) {

        logger.debug("---Inside login agent: " + id);

        /* Get agent information from LDAP(CRM info is stored at LDAP) */
        CADUser agent = agentInfoService.getAgentInformation(id);

        if (agent == null) {
          /* Inform user about unauthorized agent */
          throw new CADException(CADResponseCodes._1012 + "Agent");
        }

        /* Validate crm service params */
        this.validateAgent(agent);

        /* Get user information from Microsoft CRM */
        final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(agent);

        /* Login at Zen desk */
        if (mSCRMUserInformation != null) {

          /* Save this agent in session cache */
          cRMSessionCache.admit(id, agent);

          /* If everything is fine return true */
          return agent;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
        }
      } else {
        logger.debug("---Inside login tenant: " + id);

        /* Get tenant information from LDAP(CRM info is stored at DB) */
        final Tenant tenant = tenantInfoService.getTenantInformation(id);

        if (tenant == null) {
          /* Inform user about unauthorized tenant */
          throw new CADException(CADResponseCodes._1012 + "Tenant");
        }

        /* Validate crm service params */
        this.validateTenant(tenant);

        /* Get user information from Microsoft CRM */
        final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(tenant);

        if (mSCRMUserInformation != null) {

          /* Save this tenant in session cache */
          cRMSessionCache.admit(id, tenant);

          /* If everything is fine return true */
          return tenant;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
        }
      }
    } else {
      /* Check the type of user */
      if (basicObject instanceof CADUser) {

        logger.debug("---Inside login agent from cache: " + id);

        /* Get agent information from cache */
        CADUser agent = (CADUser) basicObject;

        if (agent == null) {

          /* Get fresh agent from LDAP */
          agent = agentInfoService.getAgentInformation(id);

          /* If tenant is still not found; tenant is not valid */
          if (agent == null) {

            /* Inform user about unauthorized agent */
            throw new CADException(CADResponseCodes._1012 + "Agent");
          }
        }

        /* Validate crm service params */
        this.validateAgent(agent);

        /* Get user information from Microsoft CRM */
        final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(agent);

        /* Login at Zen desk */
        if (mSCRMUserInformation != null) {

          /* Save this agent in session cache */
          cRMSessionCache.admit(id, agent);

          /* If everything is fine return true */
          return agent;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
        }
      } else if (basicObject instanceof Tenant) {

        logger.debug("---Inside login tenant from cache: " + id);

        /* Get tenant information from cache */
        Tenant tenant = (Tenant) basicObject;

        if (tenant == null) {

          /* Get fresh tenant from LDAP */
          tenant = tenantInfoService.getTenantInformation(id);

          /* If tenant is still not found; tenant is not valid */
          if (tenant == null) {

            /* Inform user about unauthorized agent */
            throw new CADException(CADResponseCodes._1012 + "Tenant");
          }
        }

        /* Validate crm service params */
        this.validateTenant(tenant);

        /* Get user information from Microsoft CRM */
        final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(tenant);

        if (mSCRMUserInformation != null) {

          /* Save this tenant in session cache */
          cRMSessionCache.admit(id, tenant);

          /* If everything is fine return true */
          return tenant;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at MS-CRM is failed. Check credentials");
        }
      } else {
        /* Inform user about absent header value */
        throw new CADException(CADResponseCodes._1008 + " Agent/Tenant information is not valid");
      }
    }
  }

  public final synchronized Tenant getTenantWithCRMSessionInformation(final String tenantId) throws Exception {

    logger.debug("---Inside getTenantWithCRMSessionInformation tenant: " + tenantId);

    /* Recover Tenant from cache */
    Tenant tenant = (Tenant) cRMSessionCache.recover(tenantId);

    /* This code block will be use full if cache size limit is reached */
    if (tenant == null) {
      logger
          .debug("---Inside getTenantWithCRMSessionInformation tenant not found in cache; going for fresh fetch. Seems like cache limit is reached");
      tenant = tenantInfoService.getTenantInformation(tenantId);
    } else {
      logger.debug("---Inside getTenantWithCRMSessionInformation tenant found in cache");
    }

    /* Service URL is must for MS-CRM */
    if (tenant.getCrmServiceURL() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
    }

    if (tenant.getCrmSessionId() == null) {

      logger.debug("---Inside getTenantWithCRMSessionInformation tenant's CRM session is not found; Going to fetch session information");

      /* Get CRM service information from CRM */
      final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(tenant);

      /* Set CRM ticket as session id at tenant */
      tenant.setCrmSessionId(mSCRMUserInformation.getCrmTicket());

      /* Set CRM organization information at tenant */
      tenant.setCrmOrgName(mSCRMUserInformation.getOrganizationName());
    }

    /* Re-admit this tenant with CRM session information */
    cRMSessionCache.admit(tenantId, tenant);

    return tenant;
  }

  public final synchronized CADUser getAgentWithCRMSessionInformation(final String agentId) throws Exception {

    logger.debug("---Inside getAgentWithCRMSessionInformation agent: " + agentId);
    /* Recover agent from cache */
    CADUser agent = (CADUser) cRMSessionCache.recover(agentId);

    /* This code block will be use full if cache size limit is reached */
    if (agent == null) {
      logger.debug("---Inside getAgentWithCRMSessionInformation tenant not found in cache; going for fresh fetch. Seems like cache limit is reached");
      agent = agentInfoService.getAgentInformation(agentId);
    } else {
      logger.debug("---Inside getAgentWithCRMSessionInformation agent found in cache");
    }

    /* Service URL is must for MS-CRM */
    if (agent.getCrmServiceURL() == null) {

      /* Inform the user about error */
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
    }

    if (agent.getCrmSessionId() == null) {

      logger.debug("---Inside getAgentWithCRMSessionInformation agent's CRM session is not found; Going to fetch session information");

      /* Get CRM service information from CRM */
      final MSCRMUserInformation mSCRMUserInformation = mSCRMDiscoveryManager.getMSCRMUserInformation(agent);

      logger.debug("---Inside getAgentWithCRMSessionInformation : CrmSessionId: " + mSCRMUserInformation.getCrmTicket());

      /* Set CRM ticket as session id at agent */
      agent.setCrmSessionId(mSCRMUserInformation.getCrmTicket());

      /* Set CRM organization information at agent */
      agent.setCrmOrgName(mSCRMUserInformation.getOrganizationName());
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(agentId, agent);

    return agent;
  }

  public final String getAgentIdSplitCharacter() {
    return agentIdSplitCharacter;
  }

  public final void setAgentIdSplitCharacter(final String agentIdSplitCharacter) {
    this.agentIdSplitCharacter = agentIdSplitCharacter;
  }

  public final CRMSessionCache getcRMSessionCache() {
    return cRMSessionCache;
  }

  public final void setcRMSessionCache(final CRMSessionCache cRMSessionCache) {
    this.cRMSessionCache = cRMSessionCache;
  }

  public final CADUserInfoService getAgentInfoService() {
    return agentInfoService;
  }

  public final void setAgentInfoService(final CADUserInfoService agentInfoService) {
    this.agentInfoService = agentInfoService;
  }

  public final SuperUserInfoService getTenantInfoService() {
    return tenantInfoService;
  }

  public final void setTenantInfoService(final SuperUserInfoService tenantInfoService) {
    this.tenantInfoService = tenantInfoService;
  }

  public final MSCRMDiscoveryManager getmSCRMDiscoveryManager() {
    return mSCRMDiscoveryManager;
  }

  public final void setmSCRMDiscoveryManager(final MSCRMDiscoveryManager mSCRMDiscoveryManager) {
    this.mSCRMDiscoveryManager = mSCRMDiscoveryManager;
  }

  private final void validateTenant(final Tenant tenant) {

    /* Service URL is a must for MS-CRM */
    if (tenant.getCrmServiceURL() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for MS-CRM */
    if (tenant.getCrmServiceProtocol() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service Protocol");
    }
  }

  private final void validateAgent(final CADUser agent) {

    /* Service URL is a must for MS-CRM */
    if (agent.getCrmServiceURL() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for MS-CRM */
    if (agent.getCrmServiceProtocol() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service Protocol");
    }
  }
}
