package com.inbravo.cad.rest.service.crm.ns.session;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.CADUserInfoService;
import com.inbravo.cad.internal.service.SuperUserInfoService;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.BasicObject;
import com.inbravo.cad.internal.service.dto.Tenant;
import com.inbravo.cad.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.cad.rest.service.crm.ns.NetSuiteSOAPClient;
import com.inbravo.cad.rest.service.crm.ns.v2k9.NSCRMV2k9ClientInfoProvidor;
import com.inbravo.cad.rest.service.crm.ns.v2k9.NSCRMV2k9ClientInfoProvidor.NSCRMV2k9ClientInfo;
import com.inbravo.cad.rest.service.crm.session.CRMSessionManager;
import com.netsuite.webservices.platform.NetSuiteBindingStub;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class NetSuiteCRMSessionManager implements CRMSessionManager {

  private final Logger logger = Logger.getLogger(NetSuiteCRMSessionManager.class.getName());

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* NetSuite SOAP client */
  private NetSuiteSOAPClient netSuiteSOAPClient;

  /* Agent information service */
  private CADUserInfoService agentInfoService;

  /* Tenant information service */
  private SuperUserInfoService tenantInfoService;

  /* Agent/tenant id seperator */
  private String agentIdSplitCharacter;

  /* REST based web service URL info provider */
  private NSCRMV2k9ClientInfoProvidor clientInfoProvidor;

  /**
   * API to get web service stub for Agent
   * 
   * @param agentId
   * @return
   * @throws Exception
   */
  public final synchronized NetSuiteBindingStub getSoapBindingStubForAgent(final String agentId) throws Exception {

    logger.debug("---Inside getSoapBindingStubForAgent: " + agentId);

    /* Recover agent from cache */
    CADUser agent = (CADUser) cRMSessionCache.recover(agentId);

    /* This code block will be usefull if cache size limit is reached */
    if (agent == null) {
      logger.debug("---Inside getSoapBindingStubForAgent, agent not found in cache hence going for fresh fetch. Seems like cache limit is reached");
      agent = agentInfoService.getAgentInformation(agentId);
    } else {
      logger.debug("---Inside getSoapBindingStubForAgent, agent is found in cache");
    }

    NetSuiteBindingStub soapBindingStub = null;

    /* Get SOAP stub */
    if (agent.getSoapStub() != null) {

      logger.debug("---Inside getSoapBindingStubForAgent, using stub from cache");

      /* Get stub from cache */
      soapBindingStub = (NetSuiteBindingStub) agent.getSoapStub();
    } else {

      /* Get service URL information */
      final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(agent.getCrmUserid(), agent.getCrmPassword());

      logger.debug("---Inside getSoapBindingStubForAgent, creating fresh stub, client info: " + clientInfo);

      /* TODO : Login at NetSuite : pass role id as 3 for admin role */
      soapBindingStub =
          netSuiteSOAPClient.login(agent.getCrmUserid(), agent.getCrmPassword(), agent.getCrmAccountId(), clientInfo.getWebservicesDomain());

      /* Set this stub in agent */
      agent.setSoapStub(soapBindingStub);
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(agentId, agent);

    return soapBindingStub;
  }

  /**
   * API to get web service stub for Tenant
   * 
   * @param tenantId
   * @return
   * @throws Exception
   */
  public final synchronized NetSuiteBindingStub getSoapBindingStubForTenant(final String tenantId) throws Exception {

    logger.debug("---Inside getSoapBindingStubForTenant: " + tenantId);

    /* Recover tenant from cache */
    Tenant tenant = (Tenant) cRMSessionCache.recover(tenantId);

    /* This code block will be usefull if cache size limit is reached */
    if (tenant == null) {
      logger.debug("---Inside getSoapBindingStubForTenant, tenant not found in cache hence going for fresh fetch. Seems like cache limit is reached");
      tenant = tenantInfoService.getTenantInformation(tenantId);
    } else {
      logger.debug("---Inside getSoapBindingStubForAgent, tenant is found in cache");
    }

    NetSuiteBindingStub soapBindingStub = null;

    /* Get SOAP stub */
    if (tenant.getSoapStub() != null) {

      logger.debug("---Inside getSoapBindingStubForTenant, using stub from cache");

      /* Get stub from cache */
      soapBindingStub = (NetSuiteBindingStub) tenant.getSoapStub();
    } else {

      /* Get service URL information */
      final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(tenant.getCrmUserid(), tenant.getCrmPassword());

      logger.debug("---Inside getSoapBindingStubForTenant, creating fresh stub, client info: " + clientInfo);

      /* Login at NetSuite */
      soapBindingStub =
          netSuiteSOAPClient.login(tenant.getCrmUserid(), tenant.getCrmPassword(), tenant.getCrmAccountId(), clientInfo.getWebservicesDomain());

      /* Set this stub in tenant */
      tenant.setSoapStub(soapBindingStub);
    }

    /* Re-admit this tenant with CRM session information */
    cRMSessionCache.admit(tenantId, tenant);

    return soapBindingStub;
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
        final CADUser agent = agentInfoService.getAgentInformation(id);

        if (agent == null) {
          /* Inform user about unauthorized agent */
          throw new CADException(CADResponseCodes._1012 + "Agent");
        }

        /* Get service URL information */
        final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(agent.getCrmUserid(), agent.getCrmPassword());

        logger.debug("---Inside getSessionInfo, clientInfo: " + clientInfo);

        /* Login at NetSuite : pass role id as 3 for admin role */
        final NetSuiteBindingStub soapBindingStub =
            netSuiteSOAPClient.login(agent.getCrmUserid(), agent.getCrmPassword(), agent.getCrmAccountId(), clientInfo.getWebservicesDomain());

        /* Set this stub in agent */
        agent.setSoapStub(soapBindingStub);

        /* Set service URL */
        if (agent.getCrmServiceURL() == null) {

          agent.setCrmServiceURL(clientInfo.getSystemDomain());
          agent.setCrmServiceProtocol(null);
        }

        /* Save this agent in session cache */
        cRMSessionCache.admit(id, agent);

        /* If everything is fine return agent */
        return agent;
      } else {
        logger.debug("---Inside getSessionInfo tenant: " + id);

        /* Get tenant information from LDAP(CRM info is stored at DB) */
        final Tenant tenant = tenantInfoService.getTenantInformation(id);

        if (tenant == null) {
          /* Inform user about unauthorized agent */
          throw new CADException(CADResponseCodes._1012 + "Tenant");
        }

        /* Get service URL information */
        final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(tenant.getCrmUserid(), tenant.getCrmPassword());

        logger.debug("---Inside getSessionInfo, clientInfo: " + clientInfo);

        /* Login at NetSuite : pass role id as 3 for admin role */
        final NetSuiteBindingStub soapBindingStub =
            netSuiteSOAPClient.login(tenant.getCrmUserid(), tenant.getCrmPassword(), tenant.getCrmAccountId(), clientInfo.getWebservicesDomain());

        /* Set this stub in tenant */
        tenant.setSoapStub(soapBindingStub);

        /* Set service URL */
        if (tenant.getCrmServiceURL() == null) {

          tenant.setCrmServiceURL(clientInfo.getSystemDomain());
          tenant.setCrmServiceProtocol(null);
        }

        /* Save this tenant in cache */
        cRMSessionCache.admit(id, tenant);

        /* If everything is fine return tenant */
        return tenant;
      }
    } else {
      /* Check the type of user */
      if (basicObject instanceof CADUser) {

        logger.debug("---Inside getSessionInfo agent: " + id);

        /* Get agent information from cache */
        CADUser agent = (CADUser) basicObject;

        if (agent == null) {

          /* Get fresh agent from LDAP */
          agent = agentInfoService.getAgentInformation(id);

          /* If agent is still not found; tenant is not valid */
          if (agent == null) {

            /* Inform user about unauthorized agent */
            throw new CADException(CADResponseCodes._1012 + "Agent");
          }
        }

        /* Get service URL information */
        final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(agent.getCrmUserid(), agent.getCrmPassword());

        logger.debug("---Inside getSessionInfo, clientInfo: " + clientInfo);

        /* Login at NetSuite : pass role id as 3 for admin role */
        final NetSuiteBindingStub soapBindingStub =
            netSuiteSOAPClient.login(agent.getCrmUserid(), agent.getCrmPassword(), agent.getCrmAccountId(), clientInfo.getWebservicesDomain());

        /* Set this stub in agent */
        agent.setSoapStub(soapBindingStub);

        /* Set service URL */
        if (agent.getCrmServiceURL() == null) {

          agent.setCrmServiceURL(clientInfo.getSystemDomain());
          agent.setCrmServiceProtocol(null);
        }

        /* Save this freshly updated agent in session cache */
        cRMSessionCache.admit(id, agent);

        /* If everything is fine return true */
        return agent;
      } else if (basicObject instanceof Tenant) {

        logger.debug("---Inside getSessionInfo tenant from cache: " + id);

        /* Get tenant information from cache */
        Tenant tenant = (Tenant) basicObject;

        if (tenant == null) {

          /* Get fresh tenant from LDAP */
          tenant = tenantInfoService.getTenantInformation(id);

          /* If tenant is still not found; tenant is not valid */
          if (tenant == null) {

            /* Inform user about unauthorized tenant */
            throw new CADException(CADResponseCodes._1012 + "Tenant");
          }
        }

        /* Get service URL information */
        final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(tenant.getCrmUserid(), tenant.getCrmPassword());

        logger.debug("---Inside getSessionInfo tenant from clientInfo: " + clientInfo);

        /* Login at NetSuite : pass role id as 3 for admin role */
        final NetSuiteBindingStub soapBindingStub =
            netSuiteSOAPClient.login(tenant.getCrmUserid(), tenant.getCrmPassword(), tenant.getCrmAccountId(), clientInfo.getWebservicesDomain());

        /* Set this stub in tenant */
        tenant.setSoapStub(soapBindingStub);

        /* Set service URL */
        if (tenant.getCrmServiceURL() == null) {

          tenant.setCrmServiceURL(clientInfo.getSystemDomain());
          tenant.setCrmServiceProtocol(null);
        }

        /* Save this freshly updated tenant in tenant */
        cRMSessionCache.admit(id, tenant);

        /* If everything is fine return true */
        return tenant;
      } else {
        /* Inform user about absent header value */
        throw new CADException(CADResponseCodes._1008 + " Agent/Tenant information is not valid");
      }
    }
  }

  @SuppressWarnings("unused")
  @Override
  public final boolean login(final String id) throws Exception {
    logger.debug("---Inside login id: " + id);

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

        /* Get service URL information */
        final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(agent.getCrmUserid(), agent.getCrmPassword());

        /* Login at NetSuite : pass role id as 3 for admin role */
        final NetSuiteBindingStub soapBindingStub =
            netSuiteSOAPClient.login(agent.getCrmUserid(), agent.getCrmPassword(), agent.getCrmAccountId(), clientInfo.getWebservicesDomain());

        /* Set this stub in agent */
        agent.setSoapStub(soapBindingStub);

        /* Set service URL */
        if (agent.getCrmServiceURL() == null) {

          agent.setCrmServiceURL(clientInfo.getSystemDomain());
          agent.setCrmServiceProtocol(null);
        }

        /* Save this agent in session cache */
        cRMSessionCache.admit(id, agent);

        /* If everything is fine return true */
        return true;
      } else {
        logger.debug("---Inside login tenant: " + id);

        /* Get tenant information from LDAP(CRM info is stored at DB) */
        final Tenant tenant = tenantInfoService.getTenantInformation(id);

        if (tenant == null) {
          /* Inform user about unauthorized agent */
          throw new CADException(CADResponseCodes._1012 + "Tenant");
        }

        /* Get service URL information */
        final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(tenant.getCrmUserid(), tenant.getCrmPassword());

        logger.debug("---Inside login, clientInfo: " + clientInfo);

        /* Login at NetSuite : pass role id as 3 for admin role */
        final NetSuiteBindingStub soapBindingStub =
            netSuiteSOAPClient.login(tenant.getCrmUserid(), tenant.getCrmPassword(), tenant.getCrmAccountId(), clientInfo.getWebservicesDomain());

        /* Set this stub in tenant */
        tenant.setSoapStub(soapBindingStub);

        /* Set service URL */
        if (tenant.getCrmServiceURL() == null) {

          tenant.setCrmServiceURL(clientInfo.getSystemDomain());
          tenant.setCrmServiceProtocol(null);
        }

        /* Save this tenant in cache */
        cRMSessionCache.admit(id, tenant);

        /* If everything is fine return true */
        return true;
      }
    } else {
      /* Check the type of user */
      if (basicObject instanceof CADUser) {

        logger.debug("---Inside login agent: " + id);

        /* Get agent information from cache */
        CADUser agent = (CADUser) basicObject;

        if (agent == null) {

          /* Get fresh agent from LDAP */
          agent = agentInfoService.getAgentInformation(id);

          /* If agent is still not found; tenant is not valid */
          if (agent == null) {

            /* Inform user about unauthorized agent */
            throw new CADException(CADResponseCodes._1012 + "Agent");
          }
        }

        /* Get service URL information */
        final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(agent.getCrmUserid(), agent.getCrmPassword());

        logger.debug("---Inside login clientInfo: " + clientInfo);

        /* Login at NetSuite : pass role id as 3 for admin role */
        final NetSuiteBindingStub soapBindingStub =
            netSuiteSOAPClient.login(agent.getCrmUserid(), agent.getCrmPassword(), agent.getCrmAccountId(), clientInfo.getWebservicesDomain());

        /* Set this stub in agent */
        agent.setSoapStub(soapBindingStub);

        /* Set service URL */
        if (agent.getCrmServiceURL() == null) {

          agent.setCrmServiceURL(clientInfo.getSystemDomain());
          agent.setCrmServiceProtocol(null);
        }

        /* Save this freshly updated agent in session cache */
        cRMSessionCache.admit(id, agent);

        /* If everything is fine return true */
        return true;
      } else if (basicObject instanceof Tenant) {

        logger.debug("---Inside login tenant from cache: " + id);

        /* Get tenant information from cache */
        Tenant tenant = (Tenant) basicObject;

        if (tenant == null) {

          /* Get fresh tenant from LDAP */
          tenant = tenantInfoService.getTenantInformation(id);

          /* If tenant is still not found; tenant is not valid */
          if (tenant == null) {

            /* Inform user about unauthorized tenant */
            throw new CADException(CADResponseCodes._1012 + "Tenant");
          }
        }

        /* Get service URL information */
        final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(tenant.getCrmUserid(), tenant.getCrmPassword());

        logger.debug("---Inside login, clientInfo: " + clientInfo);

        /* Login at NetSuite : pass role id as 3 for admin role */
        final NetSuiteBindingStub soapBindingStub =
            netSuiteSOAPClient.login(tenant.getCrmUserid(), tenant.getCrmPassword(), tenant.getCrmAccountId(), clientInfo.getWebservicesDomain());

        /* Set this stub in tenant */
        tenant.setSoapStub(soapBindingStub);

        /* Set service URL */
        if (tenant.getCrmServiceURL() == null) {

          tenant.setCrmServiceURL(clientInfo.getSystemDomain());
          tenant.setCrmServiceProtocol(null);
        }

        /* Save this freshly updated tenant in cache */
        cRMSessionCache.admit(id, tenant);

        /* If everything is fine return true */
        return true;
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

  public final void setNetSuiteSOAPClient(final NetSuiteSOAPClient netSuiteSOAPClient) {
    this.netSuiteSOAPClient = netSuiteSOAPClient;
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

  public final String getAgentIdSplitCharacter() {
    return agentIdSplitCharacter;
  }

  public final void setAgentIdSplitCharacter(final String agentIdSplitCharacter) {
    this.agentIdSplitCharacter = agentIdSplitCharacter;
  }

  /**
   * @return the clientInfoProvidor
   */
  public final NSCRMV2k9ClientInfoProvidor getClientInfoProvidor() {
    return this.clientInfoProvidor;
  }

  /**
   * @param clientInfoProvidor the clientInfoProvidor to set
   */
  public final void setClientInfoProvidor(final NSCRMV2k9ClientInfoProvidor clientInfoProvidor) {
    this.clientInfoProvidor = clientInfoProvidor;
  }
}
