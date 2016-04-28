package com.inbravo.cad.rest.service.crm.zd.session;

import java.util.Map;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.CADUserInfoService;
import com.inbravo.cad.internal.service.SuperUserInfoService;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.BasicObject;
import com.inbravo.cad.internal.service.dto.Tenant;
import com.inbravo.cad.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.cad.rest.service.crm.session.CRMSessionManager;
import com.inbravo.cad.rest.service.crm.zd.auth.ZDAuthManager;

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

  /* Agent information service */
  private CADUserInfoService agentInfoService;

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
        CADUser agent = agentInfoService.getAgentInformation(id);

        if (agent == null) {
          /* Inform user about unauthorized agent */
          throw new CADException(CADResponseCodes._1012 + "Agent");
        }

        /* Validate crm service params */
        this.validateAgent(agent);

        /* Login at Zen desk */
        if (zDAuthManager.login(agent.getCrmAPIUserId(), agent.getCrmAPIPassword(), agent.getCrmServiceURL(), agent.getCrmServiceProtocol(),
            agent.getCrmPort())) {

          /* Save this agent in session cache */
          cRMSessionCache.admit(id, agent);

          /* If everything is fine return true */
          return true;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
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

        /* Login at Zen desk */
        if (zDAuthManager.login(tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmServiceURL(), tenant.getCrmServiceProtocol(),
            tenant.getCrmPort())) {

          /* Save this tenant in session cache */
          cRMSessionCache.admit(id, tenant);

          /* If everything is fine return true */
          return true;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
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

        /* Login at Zen desk */
        if (zDAuthManager.login(agent.getCrmAPIUserId(), agent.getCrmAPIPassword(), agent.getCrmServiceURL(), agent.getCrmServiceProtocol(),
            agent.getCrmPort())) {

          /* Save this agent in session cache */
          cRMSessionCache.admit(id, agent);

          /* If everything is fine return true */
          return true;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
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

        /* Login at Zen desk */
        if (zDAuthManager.login(tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmServiceURL(), tenant.getCrmServiceProtocol(),
            tenant.getCrmPort())) {

          /* Save this tenant in session cache */
          cRMSessionCache.admit(id, tenant);

          /* If everything is fine return true */
          return true;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
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

        logger.debug("---Inside getSessionInfo agent: " + id);

        /* Get agent information from LDAP(CRM info is stored at LDAP) */
        CADUser agent = agentInfoService.getAgentInformation(id);

        /* Check if agent is not null */
        if (agent == null) {

          /* Inform user about unauthorized agent */
          throw new CADException(CADResponseCodes._1012 + "Agent");
        }

        /* Validate crm service params */
        this.validateAgent(agent);

        /* Login at Zen desk */
        String crmUserId = agent.getCrmUserId();
        String crmPassword = agent.getCrmPassword();

        /* Check if CRM API token is present */
        if (agent.getCrmAPIToken() != null && !agent.getCrmAPIToken().equals("")) {

          crmUserId += "/token";
          crmPassword = agent.getCrmAPIToken();
        }

        /* Get session information from ZD */
        final Map<String, String> additionalInfoMap =
            zDAuthManager.getSessionInfoAfterValidLogin(crmUserId, crmPassword, agent.getCrmServiceURL(), agent.getCrmServiceProtocol(),
                agent.getCrmPort());

        if (additionalInfoMap != null) {

          /* Set additonal information */
          agent.setAdditionalInfo(additionalInfoMap);

          /* Save this agent in session cache */
          cRMSessionCache.admit(id, agent);

          /* If everything is fine return true */
          return agent;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
        }
      } else {
        logger.debug("---Inside getSessionInfo tenant: " + id);

        /* Get tenant information from LDAP(CRM info is stored at DB) */
        final Tenant tenant = tenantInfoService.getTenantInformation(id);

        if (tenant == null) {
          /* Inform user about unauthorized agent */
          throw new CADException(CADResponseCodes._1012 + "Tenant");
        }

        /* Validate crm service params */
        this.validateTenant(tenant);

        /* Get session information from ZD */
        final Map<String, String> additionalInfoMap =
            zDAuthManager.getSessionInfoAfterValidLogin(tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmServiceURL(),
                tenant.getCrmServiceProtocol(), tenant.getCrmPort());

        /* Get session information from ZD */
        if (additionalInfoMap != null) {

          /* Set additonal information */
          tenant.setAdditionalInfo(additionalInfoMap);

          /* Save this tenant in session cache */
          cRMSessionCache.admit(id, tenant);

          /* If everything is fine return true */
          return tenant;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
        }
      }
    } else {
      /* Check the type of user */
      if (basicObject instanceof CADUser) {

        logger.debug("---Inside getSessionInfo agent from cache: " + id);

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

        /* Login at Zen desk */
        String crmUserId = agent.getCrmUserId();
        String crmPassword = agent.getCrmPassword();

        /* Check if CRM API token is present */
        if (agent.getCrmAPIToken() != null && !agent.getCrmAPIToken().equals("")) {

          crmUserId += "/token";
          crmPassword = agent.getCrmAPIToken();
        }

        /* Get session information from ZD */
        final Map<String, String> additionalInfoMap =
            zDAuthManager.getSessionInfoAfterValidLogin(crmUserId, crmPassword, agent.getCrmServiceURL(), agent.getCrmServiceProtocol(),
                agent.getCrmPort());

        /* additionalInfoMap will be null if authentication is failed */
        if (additionalInfoMap != null) {

          /* Set additonal information */
          agent.setAdditionalInfo(additionalInfoMap);

          /* Save this agent in session cache */
          cRMSessionCache.admit(id, agent);

          /* If everything is fine return true */
          return agent;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
        }
      } else if (basicObject instanceof Tenant) {

        logger.debug("---Inside getSessionInfo tenant from cache: " + id);

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

        /* Get session information from ZD */
        final Map<String, String> additionalInfoMap =
            zDAuthManager.getSessionInfoAfterValidLogin(tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmServiceURL(),
                tenant.getCrmServiceProtocol(), tenant.getCrmPort());

        /* Get session information from ZD */
        if (additionalInfoMap != null) {

          /* Set additonal information */
          tenant.setAdditionalInfo(additionalInfoMap);

          /* Save this tenant in session cache */
          cRMSessionCache.admit(id, tenant);

          /* If everything is fine return true */
          return tenant;
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
        }
      } else {
        /* Inform user about absent header value */
        throw new CADException(CADResponseCodes._1008 + " Agent/Tenant information is not valid");
      }
    }
  }

  public final synchronized Tenant getTenantWithCRMSessionInformation(final String tenantId) throws Exception {
    logger.debug("---Inside getTenantWithCRMSessionInformation");

    /* Recover tenant from cache */
    Tenant tenant = (Tenant) cRMSessionCache.recover(tenantId);

    /* This code block will be use full if cache size limit is reached */
    if (tenant == null) {
      logger
          .debug("---Inside getTenantWithCRMSessionInformation tenant not found in cache; going for fresh fetch. Seems like cache limit is reached");
      tenant = tenantInfoService.getTenantInformation(tenantId);
    } else {
      logger.debug("---Inside getTenantWithCRMSessionInformation tenant found in cache: tenant : " + tenant);
    }

    /* Validate crm service params */
    this.validateTenant(tenant);

    /* Check for ZD session id */
    if (tenant.getCrmSessionId() == null) {
      /* Get new session id from ZD */
      tenant.setCrmSessionId(zDAuthManager.getSessionId(tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmServiceURL(),
          tenant.getCrmServiceProtocol(), tenant.getCrmPort()));
    }

    /* Re-admit this tenant with CRM session information */
    cRMSessionCache.admit(tenantId, tenant);

    return tenant;
  }

  public final synchronized CADUser getAgentWithCRMSessionInformation(final String agentId) throws Exception {
    logger.debug("---Inside getAgentWithCRMSessionInformation");

    /* Recover agent from cache */
    CADUser agent = (CADUser) cRMSessionCache.recover(agentId);

    /* This code block will be use full if cache size limit is reached */
    if (agent == null) {
      logger.debug("---Inside getAgentWithCRMSessionInformation agent not found in cache; going for fresh fetch. Seems like cache limit is reached");
      agent = agentInfoService.getAgentInformation(agentId);
    } else {
      logger.debug("---Inside getAgentWithCRMSessionInformation agent found in cache: agent : " + agent);
    }

    /* Validate crm service params */
    this.validateAgent(agent);
    String crmUserId = agent.getCrmUserId();
    String crmPassword = agent.getCrmPassword();
    if (agent.getCrmAPIToken() != null && !agent.getCrmAPIToken().equals("")) {
      crmUserId += "/token";
      crmPassword = agent.getCrmAPIToken();
    }
    /* Check for ZD session id */
    if (agent.getCrmSessionId() == null) {
      agent.setCrmSessionId(zDAuthManager.getSessionId(crmUserId, crmPassword, agent.getCrmServiceURL(), agent.getCrmServiceProtocol(),
          agent.getCrmPort()));
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(agentId, agent);

    return agent;
  }

  public final ZDAuthManager getzDAuthManager() {
    return zDAuthManager;
  }

  public final void setzDAuthManager(final ZDAuthManager zDAuthManager) {
    this.zDAuthManager = zDAuthManager;
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

  public CADUserInfoService getAgentInfoService() {
    return agentInfoService;
  }

  public final void setAgentInfoService(final CADUserInfoService agentInfoService) {
    this.agentInfoService = agentInfoService;
  }

  public final SuperUserInfoService getTenantInfoService() {
    return tenantInfoService;
  }

  public void setTenantInfoService(final SuperUserInfoService tenantInfoService) {
    this.tenantInfoService = tenantInfoService;
  }

  private final void validateTenant(final Tenant tenant) {

    /* Service URL is a must for ZD */
    if (tenant.getCrmServiceURL() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for ZD */
    if (tenant.getCrmServiceProtocol() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service Protocol");
    }
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
