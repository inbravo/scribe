package com.inbravo.cad.rest.service.crm.sf.session;

import org.apache.axis.message.SOAPHeaderElement;
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
import com.inbravo.cad.rest.service.crm.sf.SalesForceSOAPClient;
import com.sforce.soap.partner.SforceServiceLocator;
import com.sforce.soap.partner.SoapBindingStub;

/**
 * 
 * @author amit.dixit
 * 
 */
public class SalesForceCRMSessionManager implements CRMSessionManager {

  private final Logger logger = Logger.getLogger(SalesForceCRMSessionManager.class.getName());

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* Sales Force SOAP client */
  private SalesForceSOAPClient salesForceSOAPClient;

  /* Agent information service */
  private CADUserInfoService agentInfoService;

  /* Tenant information service */
  private SuperUserInfoService tenantInfoService;

  private String agentIdSplitCharacter;

  public final synchronized SoapBindingStub getSalesForceSoapBindingStubForAgent(final String agentId) throws Exception {

    logger.debug("---Inside getSalesForceSoapBindingStubForAgent: " + agentId);

    /* Recover agent from cache */
    CADUser agent = (CADUser) cRMSessionCache.recover(agentId);

    /* This code block will be usefull if cache size limit is reached */
    if (agent == null) {
      logger.debug("---Inside tenant not found in cache hence going for fresh fetch. Seems like cache limit is reached");
      agent = agentInfoService.getAgentInformation(agentId);
    }

    /* Login at Sales Force */
    final SoapBindingStub soapBindingStub = salesForceSOAPClient.login(agent.getCrmUserid(), agent.getCrmPassword());

    /* Take session information from agent */
    final SOAPHeaderElement sOAPHeaderElement =
        soapBindingStub.getHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader");
    if (sOAPHeaderElement != null) {
      logger.debug("---Inside getSalesForceSoapBindingStubForAgent agent's sessionId : "
          + sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());

      /* Set session information at agent */
      /* Following session will be used for pagination */
      agent.setCrmSessionId(sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());
    } else {
      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1008 + "CRM session id not set with cache object");
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(agentId, agent);

    return soapBindingStub;
  }

  public final synchronized SoapBindingStub getSalesForceSoapBindingStubForTenant(final String tenantName) throws Exception {

    logger.debug("---Inside getSalesForceSoapBindingStubForTenant: " + tenantName);

    Tenant tenant = (Tenant) cRMSessionCache.recover(tenantName);

    /* This code block will be usefull if cache size limit is reached */
    if (tenant == null) {
      logger.debug("---Inside tenant not found in cache hence going for fresh fetch. Seems like cache limit is reached");
      /* Get tenant information from LDAP(CRM info is stored at DB) */
      tenant = tenantInfoService.getTenantInformation(tenantName);
    }

    /* Login at Sales Force */
    final SoapBindingStub soapBindingStub = salesForceSOAPClient.login(tenant.getCrmUserid(), tenant.getCrmPassword());

    /* Take session information from tenant */
    final SOAPHeaderElement sOAPHeaderElement =
        soapBindingStub.getHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader");
    if (sOAPHeaderElement != null) {
      logger.debug("---Inside getSalesForceSoapBindingStubForTenant tenant's sessionId : "
          + sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());

      /* Set session information at agent */
      /* Following session will be used for pagination */
      tenant.setCrmSessionId(sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());
    } else {
      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1008 + "CRM session id not set with cache object");
    }

    /* Re-admit this tenant with CRM session information */
    cRMSessionCache.admit(tenantName, tenant);
    return soapBindingStub;
  }

  /**
   * 
   * @param id
   * @return
   * @throws Exception
   */
  public final boolean reset(final String id) throws Exception {

    final BasicObject basicObject = (BasicObject) cRMSessionCache.recover(id);

    if (basicObject == null) {
      /* If no object in cache return false */
      return false;
    }
    if (basicObject instanceof CADUser) {

      logger.debug("---Inside resetCRMSession agent: " + id);

      /* Get agent information from LDAP(CRM info is stored at LDAP) */
      final CADUser agent = agentInfoService.getAgentInformation(id);

      if (agent == null) {
        /* Inform user about unauthorized agent */
        throw new CADException(CADResponseCodes._1012 + "Agent");
      }

      /* Login at Sales Force */
      final SoapBindingStub soapBindingStub = salesForceSOAPClient.login(agent.getCrmUserid(), agent.getCrmPassword());

      /* Take session information from agent */
      final SOAPHeaderElement sOAPHeaderElement =
          soapBindingStub.getHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader");
      if (sOAPHeaderElement != null) {
        logger.debug("---Inside resetCRMSession agent's sessionId : " + sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());

        /* Set session information at agent */
        agent.setCrmSessionId(sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());
      } else {
        /* Inform user about absent header value */
        throw new CADException(CADResponseCodes._1008 + "CRM session id not set with cache object");
      }

      /* Save freshly updated agent in session cache */
      cRMSessionCache.admit(id, agent);

      /* If everything is fine return true */
      return true;
    } else if (basicObject instanceof Tenant) {

      logger.debug("---Inside resetCRMSession tenant: " + id);

      /* Get tenant information from LDAP(CRM info is stored at DB) */
      final Tenant tenant = tenantInfoService.getTenantInformation(id);

      if (tenant == null) {
        /* Inform user about unauthorized tenant */
        throw new CADException(CADResponseCodes._1012 + "Tenant");
      }

      /* Login at Sales Force */
      final SoapBindingStub soapBindingStub = salesForceSOAPClient.login(tenant.getCrmUserid(), tenant.getCrmPassword());

      /* Take session information from tenant */
      final SOAPHeaderElement sOAPHeaderElement =
          soapBindingStub.getHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader");
      if (sOAPHeaderElement != null) {
        logger.debug("---Inside resetCRMSession tenant's sessionId : " + sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());

        /* Set session information at agent */
        tenant.setCrmSessionId(sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());
      } else {
        /* Inform user about absent header value */
        throw new CADException(CADResponseCodes._1008 + "CRM session id not set with cache object");
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

  /**
   * 
   * @param id
   * @return
   * @throws Exception
   */

  @SuppressWarnings("unused")
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

        /* Login at Sales Force */
        final SoapBindingStub soapBindingStub = salesForceSOAPClient.login(agent.getCrmUserid(), agent.getCrmPassword());

        /* Take session information from agent */
        final SOAPHeaderElement sOAPHeaderElement =
            soapBindingStub.getHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader");
        if (sOAPHeaderElement != null) {
          logger.debug("---Inside login agent's sessionId : " + sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());

          /* Set session information at agent */
          agent.setCrmSessionId(sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1008 + "CRM session id not set with cache object");
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

        /* Login at Sales Force */
        final SoapBindingStub soapBindingStub = salesForceSOAPClient.login(tenant.getCrmUserid(), tenant.getCrmPassword());

        /* Take session information from tenant */
        final SOAPHeaderElement sOAPHeaderElement =
            soapBindingStub.getHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader");
        if (sOAPHeaderElement != null) {
          logger.debug("---Inside login tenant's sessionId : " + sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());

          /* Set session information at tenant */
          tenant.setCrmSessionId(sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1008 + "CRM session id not set with cache object");
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

        /* If agent is found from LDAP */
        if (agent == null) {

          /* Get fresh agent from LDAP */
          agent = agentInfoService.getAgentInformation(id);

          /* If agent is still not found; agent is not valid */
          if (agent == null) {

            /* Inform user about unauthorized agent */
            throw new CADException(CADResponseCodes._1012 + "Agent");
          }
        }

        /* Login at Sales Force */
        final SoapBindingStub soapBindingStub = salesForceSOAPClient.login(agent.getCrmUserid(), agent.getCrmPassword());

        /* Take session information from agent */
        final SOAPHeaderElement sOAPHeaderElement =
            soapBindingStub.getHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader");
        if (sOAPHeaderElement != null) {
          logger.debug("---Inside login agent's sessionId : " + sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());

          /* Set session information at agent */
          agent.setCrmSessionId(sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1008 + "CRM session id not set with cache object");
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

        /* Login at Sales Force */
        final SoapBindingStub soapBindingStub = salesForceSOAPClient.login(tenant.getCrmUserid(), tenant.getCrmPassword());

        /* Take session information from tenant */
        final SOAPHeaderElement sOAPHeaderElement =
            soapBindingStub.getHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader");
        if (sOAPHeaderElement != null) {
          logger.debug("---Inside login tenant's sessionId : " + sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());

          /* Set session information at tenant */
          tenant.setCrmSessionId(sOAPHeaderElement.getAsDOM().getFirstChild().getTextContent());
        } else {
          /* Inform user about absent header value */
          throw new CADException(CADResponseCodes._1008 + "CRM session id not set with cache object");
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
  public final BasicObject getSessionInfo(final String id) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the CRM");
  }

  public final CRMSessionCache getcRMSessionCache() {
    return cRMSessionCache;
  }

  public final void setcRMSessionCache(final CRMSessionCache cRMSessionCache) {
    this.cRMSessionCache = cRMSessionCache;
  }

  public final void setSalesForceSOAPClient(final SalesForceSOAPClient salesForceSOAPClient) {
    this.salesForceSOAPClient = salesForceSOAPClient;
  }

  public final SalesForceSOAPClient getSalesForceSOAPClient() {
    return salesForceSOAPClient;
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
}
