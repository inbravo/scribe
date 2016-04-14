package com.inbravo.cad.rest.service.crm.factory;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.CADUserInfoService;
import com.inbravo.cad.internal.service.SuperUserInfoService;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.Tenant;
import com.inbravo.cad.internal.service.factory.CADServiceFactory;
import com.inbravo.cad.rest.constants.CRMConstants;
import com.inbravo.cad.rest.constants.CRMConstants.UserType;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.service.crm.CRMService;
import com.inbravo.cad.rest.service.crm.cache.CRMSessionCache;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CRMServiceFactory implements ApplicationContextAware, CADServiceFactory {

  private final Logger logger = Logger.getLogger(CRMServiceFactory.class.getName());

  /* Boolean value to enable Zoho CRM */
  private boolean zohoCRMEnabled;

  /* Boolean value to enable SFDC CRM */
  private boolean sfdcCRMEnabled;

  /* Boolean value to enable WAPI CRM interface */
  private boolean wapiCRMEnabled;

  /* Boolean value to enable Zendesk CRM */
  private boolean zendeskCRMEnabled;

  /* Boolean value to enable NetSuite CRM */
  private boolean netsuiteCRMEnabled;

  /* Boolean value to enable MS CRM */
  private boolean microsoftCRMEnabled;

  /* Agent id special character */
  private String agentIdSplitCharacter;

  /* Boolean value to Contactual CRM */
  private boolean contactualCRMEnabled;

  /* SCRM session cache for holding tenant/agent */
  private CRMSessionCache cRMSessionCache;

  /* Agent information service */
  private CADUserInfoService agentInfoService;

  /* Tenant information service */
  private SuperUserInfoService tenantInfoService;

  private ApplicationContext applicationContext;

  /* Constant for holding SFDC CRM name */
  private String salesForceCRMConst = CRMConstants.salesForceCRM;

  /* Constant for holding SFDC CRM name */
  private String salesForceWCRMConst = CRMConstants.salesForceWCRM;

  /* Constant for Mashup URL */
  private String mashUpURLConst = CRMConstants.mashUpURL;

  /* Constant for Custom SFDC CRM name */
  private String customCRMConst = CRMConstants.customCRM;

  /* Constant for holding Contactual CRM name */
  private String contactualCRMConst = CRMConstants.contactualCRM;

  /* Constant for holding Contactual CRM name */
  private String eightByEightCRMConst = CRMConstants.eightByEightCRM;

  /* Constant for holding MS CRM name */
  private String microsoftCRMConst = CRMConstants.microsoftCRM;

  /* Constant for holding Zendesk CRM name */
  private String zendeskCRMConst = CRMConstants.zendeskCRM;

  /* Constant for holding Netsuite CRM name */
  private String netsuiteCRMConst = CRMConstants.netsuiteCRM;

  /* Constant for holding Zoho CRM name */
  private String zohoCRMConst = CRMConstants.zohoCRM;

  /**
   * This method will check the tenant/agent configuration and instantiate the relevant service
   * class
   * 
   * @return
   */
  public final synchronized CRMService getService(final CADCommandObject eDSACommandObject, final UserType userType) throws Exception {
    logger.debug("---Inside getService");
    CRMService cRMService = null;
    String id = null;
    switch (userType) {
      case Tenant:

        /* Get the tenant from request */
        id = eDSACommandObject.getTenant();

        /* Create an empty tenant object */
        Tenant tenant = null;

        /* Pick information from cache if batch request */
        if (eDSACommandObject.getBatch() != null) {
          logger.debug("---Inside getService: found a batch request. Going to fetch cached tenant information");

          /* Check if tenant in cache */
          tenant = (Tenant) cRMSessionCache.recover(id);

          if (tenant == null) {
            logger.debug("---Inside getService: tenant not found in cache. Going to fetch fresh tenant information");

            /* If tenant not in cache. Get tenant information */
            tenant = tenantInfoService.getTenantInformation(id);
          } else {
            logger.debug("---Inside getService: tenant found in cache tenant" + tenant);
          }
        } else {
          logger.debug("---Inside getService: found a non batch request. Going to fetch fresh tenant information");

          /* Get tenant information */
          tenant = tenantInfoService.getTenantInformation(id);
        }

        /* Validate tenant CRM information */
        if (tenant.getCrmName() == null) {

          /* Inform user about missing property */
          throw new CADException(CADResponseCodes._1012 + "CRM integration information is missing");
        }
        logger.debug("Inside getService: external service type " + eDSACommandObject.getExternalServiceType());

        /* Check if external service name is found in command object */
        if (eDSACommandObject.getExternalServiceType() != null
            && eDSACommandObject.getExternalServiceType().equalsIgnoreCase(CRMConstants.fbmcService)) {

          logger.debug("---Inside getService: found an external service type request: FBMC with username " + eDSACommandObject.getExternalUsername()
              + ":" + eDSACommandObject.getExternalPassword());

          /* Return FBMC CRM service */
          cRMService = (CRMService) getApplicationContext().getBean("fBMSOAPCCRMService");
        } else {

          /* Check target CRM service type */
          if (tenant.getCrmName().equalsIgnoreCase(salesForceCRMConst)) {

            /* Check if SFDC CRM is enabled */
            if (sfdcCRMEnabled) {

              /* Retrieve Sales Force CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("sFSOAPCRMService");
            } else {
              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + tenant.getCrmName() + " : integration is not enabled");
            }

          } else if (tenant.getCrmName().contains(customCRMConst)) {

            /* Check if WAPI is enabled */
            if (wapiCRMEnabled) {

              /* Retrieve WAPI implementation */
              cRMService = (CRMService) getApplicationContext().getBean("wAPICRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + tenant.getCrmName() + " : integration is not enabled");
            }
          } else if (tenant.getCrmName().contains(contactualCRMConst)) {

            /* Check if Contactual CRM is enabled */
            if (contactualCRMEnabled) {

              /* Retrieve Custom CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("cTLCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + tenant.getCrmName() + " : integration is not enabled");
            }
          } else if (tenant.getCrmName().contains(microsoftCRMConst)) {

            /* Check if Microsoft CRM is enabled */
            if (microsoftCRMEnabled) {

              /* Retrieve Microsoft CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("mSSOAPCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + tenant.getCrmName() + " : integration is not enabled");
            }
          } else if (tenant.getCrmName().contains(zendeskCRMConst)) {

            /* Check if Zendesk CRM is enabled */
            if (zendeskCRMEnabled) {

              /* Retrieve Zendesk CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("zDRESTCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + tenant.getCrmName() + " : integration is not enabled");
            }
          } else if (tenant.getCrmName().contains(netsuiteCRMConst)) {

            /* Check if Netsuite CRM is enabled */
            if (netsuiteCRMEnabled) {

              /* Retrieve Netsuite CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("nSSOAPCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + tenant.getCrmName() + " : integration is not enabled");
            }
          } else if (tenant.getCrmName().contains(zohoCRMConst)) {

            /* Check if Zoho CRM is enabled */
            if (zohoCRMEnabled) {

              /* Retrieve ZOHO CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("zHRESTCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + tenant.getCrmName() + " : integration is not enabled");
            }
          } else {

            /* Inform user about missing implementation */
            throw new CADException(CADResponseCodes._1003 + "Following CRM: " + tenant.getCrmName() + " : integration is not enabled");
          }
        }

        /* Save this tenant in cache */
        cRMSessionCache.admit(id, tenant);
        break;
      case Agent:

        /* Get the agent from request */
        id = eDSACommandObject.getAgent();

        /* Create an empty agent object */
        CADUser agent = null;

        /* Pick information from cache if batch request */
        if (eDSACommandObject.getBatch() != null) {
          logger.debug("---Inside getService: found a batch request. Going to fetch cached agent information");

          /* Check if agent in cache */
          agent = (CADUser) cRMSessionCache.recover(id);

          if (agent == null) {
            logger.debug("---Inside getService: agent not found in cache. Going to fetch fresh agent information");
            /* If agent not in cache. Get tenant information */
            agent = agentInfoService.getAgentInformation(id);
          } else {
            logger.debug("---Inside getService: agent found in cache agent: " + agent);
          }
        } else {
          logger.debug("---Inside getService: found a non batch request. Going to fetch fresh agent information");
          /* Get agent information */
          agent = agentInfoService.getAgentInformation(id);
        }

        /* Check if external service name is found in command object */
        if (eDSACommandObject.getExternalServiceType() != null
            && eDSACommandObject.getExternalServiceType().equalsIgnoreCase(CRMConstants.fbmcService)) {

          /* Inform user about missing implementation */
          throw new CADException(CADResponseCodes._1003 + "Following external service : " + eDSACommandObject.getExternalServiceType()
              + " : integration is not enabled for agents");
        } else {

          if (agent.getCrmName().contains(salesForceCRMConst)) {

            /* Check if SFDC CRM is enabled */
            if (sfdcCRMEnabled) {

              /* Retrieve Sales Force CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("sFSOAPCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + agent.getCrmName() + " : integration is not enabled");
            }
          } else if (agent.getCrmName().equalsIgnoreCase(salesForceWCRMConst)) {
            if (agent.getCrmURL() != null && agent.getCrmURL().contains(mashUpURLConst)) {

              /* Check if SFDC CRM is enabled */
              if (sfdcCRMEnabled) {

                /* Retrieve Sales Force CRM implementation */
                cRMService = (CRMService) getApplicationContext().getBean("sFSOAPCRMService");
              } else {

                /* Inform user about missing implementation */
                throw new CADException(CADResponseCodes._1003 + "Following CRM: " + agent.getCrmName() + " : integration is not enabled");
              }
            }
          } else if (agent.getCrmName().equalsIgnoreCase(microsoftCRMConst)) {

            /* Check if MS CRM is enabled */
            if (microsoftCRMEnabled) {

              /* Retrieve MS CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("mSSOAPCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + agent.getCrmName() + " : integration is not enabled");
            }
          } else if (agent.getCrmName().contains(customCRMConst)) {

            /* Check if WAPI is enabled */
            if (wapiCRMEnabled) {

              /* Retrieve WAPI implementation */
              cRMService = (CRMService) getApplicationContext().getBean("wAPICRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + agent.getCrmName() + " : integration is not enabled");
            }
          } else if (agent.getCrmName().contains(contactualCRMConst)) {

            /* Check if Contactual CRM is enabled */
            if (contactualCRMEnabled) {

              /* Retrieve Contactual CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("cTLCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + agent.getCrmName() + " : integration is not enabled");
            }
          } else if (agent.getCrmName().contains(zendeskCRMConst)) {

            /* Check if Zendesk CRM is enabled */
            if (zendeskCRMEnabled) {

              /* Retrieve Zendesk CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("zDRESTCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + agent.getCrmName() + " : integration is not enabled");
            }
          } else if (agent.getCrmName().contains(netsuiteCRMConst)) {

            /* Check if Netsuite CRM is enabled */
            if (netsuiteCRMEnabled) {

              /* Retrieve Netsuite CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("nSSOAPCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + agent.getCrmName() + " : integration is not enabled");
            }
          } else if (agent.getCrmName().contains(zohoCRMConst)) {

            /* Check if Zoho CRM is enabled */
            if (zohoCRMEnabled) {

              /* Retrieve ZOHO CRM implementation */
              cRMService = (CRMService) getApplicationContext().getBean("zHRESTCRMService");
            } else {

              /* Inform user about missing implementation */
              throw new CADException(CADResponseCodes._1003 + "Following CRM: " + agent.getCrmName() + " : integration is not enabled");
            }
          } else {

            /* Inform user about missing implementation */
            throw new CADException(CADResponseCodes._1003 + "Following CRM: " + agent.getCrmName() + ": integration is not enabled");
          }
        }

        /* Save this agent in cache for further usage at CRM service class */
        cRMSessionCache.admit(id, agent);
        break;
      default:
        /* Inform user about unauthorized agent */
        throw new CADException(CADResponseCodes._1003 + "Following CRM object is not supported");
    }
    return cRMService;
  }

  /**
   * This method will check the tenant/agent configuration and instantiate the relevant service
   * class
   * 
   * @return
   */
  public final synchronized String getCRMType(final String id) throws Exception {
    logger.debug("---Inside getCRMType");

    String crmType = null;

    /* Step 1: Check if user is agent or tenant */
    if (id.contains(agentIdSplitCharacter)) {

      logger.debug("---Inside getCRMType agent: " + id);

      /* Get agent information from LDAP(CRM info is stored at LDAP) */
      CADUser agent = agentInfoService.getAgentInformation(id);

      if (agent == null) {

        /* Inform user about unauthorized agent */
        throw new CADException(CADResponseCodes._1012 + "Agent");
      }

      /* Get CRM type */
      crmType = agent.getCrmName();

      /* Save this agent in cache for further usage at CRM service class */
      cRMSessionCache.admit(id, agent);
    } else {
      logger.debug("---Inside getCRMType tenant: " + id);

      /* Get tenant information from LDAP(CRM info is stored at DB) */
      final Tenant tenant = tenantInfoService.getTenantInformation(id);

      if (tenant == null) {

        /* Inform user about unauthorized agent */
        throw new CADException(CADResponseCodes._1012 + "Tenant");
      }

      /* Get CRM type */
      crmType = tenant.getCrmName();

      /* Save this tenant in cache for further usage at CRM service class */
      cRMSessionCache.admit(id, tenant);
    }

    /* Step 2: Check for specific CRM types */
    if (crmType != null && (crmType.contains(salesForceCRMConst) || crmType.equalsIgnoreCase(salesForceWCRMConst))) {

      return salesForceCRMConst;
    } else if (crmType != null && crmType.equalsIgnoreCase(microsoftCRMConst)) {

      return microsoftCRMConst;
    } else if (crmType != null && crmType.contains(customCRMConst)) {

      return customCRMConst;
    } else if (crmType != null && crmType.contains(contactualCRMConst)) {

      return contactualCRMConst;
    } else if (crmType != null && crmType.contains(eightByEightCRMConst)) {

      return contactualCRMConst;
    } else if (crmType != null && crmType.contains(zendeskCRMConst)) {

      return zendeskCRMConst;
    } else if (crmType != null && crmType.contains(netsuiteCRMConst)) {

      return netsuiteCRMConst;
    } else if (crmType != null && crmType.contains(zohoCRMConst)) {

      return zohoCRMConst;
    } else {

      /* Inform user about missing implementation */
      throw new CADException(CADResponseCodes._1003 + "Following CRM: '" + crmType + "' is not supported");
    }
  }

  public final String getAgentIdSplitCharacter() {
    return agentIdSplitCharacter;
  }

  public final void setAgentIdSplitCharacter(final String agentIdSplitCharacter) {
    this.agentIdSplitCharacter = agentIdSplitCharacter;
  }

  @Override
  public final void setApplicationContext(final ApplicationContext ApplicationContext) throws BeansException {
    this.applicationContext = ApplicationContext;
  }

  public final ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public final String getSalesForceCRMConst() {
    return salesForceCRMConst;
  }

  public final void setSalesForceCRMConst(final String salesForceCRMConst) {
    this.salesForceCRMConst = salesForceCRMConst;
  }

  public final String getSalesForceWCRMConst() {
    return salesForceWCRMConst;
  }

  public final void setSalesForceWCRMConst(final String salesForceWCRMConst) {
    this.salesForceWCRMConst = salesForceWCRMConst;
  }

  public final String getMashUpURLConst() {
    return mashUpURLConst;
  }

  public final void setMashUpURLConst(final String mashUpURLConst) {
    this.mashUpURLConst = mashUpURLConst;
  }

  public final String getCustomCRMConst() {
    return customCRMConst;
  }

  public final void setCustomCRMConst(final String customCRMConst) {
    this.customCRMConst = customCRMConst;
  }

  public final String getContactualCRMConst() {
    return contactualCRMConst;
  }

  public final void setContactualCRMConst(final String contactualCRMConst) {
    this.contactualCRMConst = contactualCRMConst;
  }

  public final String getMicrosoftCRMConst() {
    return microsoftCRMConst;
  }

  public final void setMicrosoftCRMConst(final String microsoftCRMConst) {
    this.microsoftCRMConst = microsoftCRMConst;
  }

  public final String getZendeskCRMConst() {
    return zendeskCRMConst;
  }

  public final void setZendeskCRMConst(final String zendeskCRMConst) {
    this.zendeskCRMConst = zendeskCRMConst;
  }

  public final String getNetsuiteCRMConst() {
    return netsuiteCRMConst;
  }

  public final void setNetsuiteCRMConst(final String netsuiteCRMConst) {
    this.netsuiteCRMConst = netsuiteCRMConst;
  }

  public final String getZohoCRMConst() {
    return zohoCRMConst;
  }

  public final void setZohoCRMConst(final String zohoCRMConst) {
    this.zohoCRMConst = zohoCRMConst;
  }

  public final boolean isSfdcCRMEnabled() {
    return sfdcCRMEnabled;
  }

  public final void setSfdcCRMEnabled(final boolean sfdcCRMEnabled) {
    this.sfdcCRMEnabled = sfdcCRMEnabled;
  }

  public final boolean isWapiCRMEnabled() {
    return wapiCRMEnabled;
  }

  public final void setWapiCRMEnabled(final boolean wapiCRMEnabled) {
    this.wapiCRMEnabled = wapiCRMEnabled;
  }

  public final boolean isContactualCRMEnabled() {
    return contactualCRMEnabled;
  }

  public final void setContactualCRMEnabled(final boolean contactualCRMEnabled) {
    this.contactualCRMEnabled = contactualCRMEnabled;
  }

  public final boolean isMicrosoftCRMEnabled() {
    return microsoftCRMEnabled;
  }

  public final void setMicrosoftCRMEnabled(final boolean microsoftCRMEnabled) {
    this.microsoftCRMEnabled = microsoftCRMEnabled;
  }

  public final boolean isZendeskCRMEnabled() {
    return zendeskCRMEnabled;
  }

  public final void setZendeskCRMEnabled(final boolean zendeskCRMEnabled) {
    this.zendeskCRMEnabled = zendeskCRMEnabled;
  }

  public final boolean isNetsuiteCRMEnabled() {
    return netsuiteCRMEnabled;
  }

  public final void setNetsuiteCRMEnabled(final boolean netsuiteCRMEnabled) {
    this.netsuiteCRMEnabled = netsuiteCRMEnabled;
  }

  public final boolean isZohoCRMEnabled() {
    return zohoCRMEnabled;
  }

  public final void setZohoCRMEnabled(final boolean zohoCRMEnabled) {
    this.zohoCRMEnabled = zohoCRMEnabled;
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

  /**
   * @return the eightByEightCRMConst
   */
  public final String getEightByEightCRMConst() {
    return this.eightByEightCRMConst;
  }

  /**
   * @param eightByEightCRMConst the eightByEightCRMConst to set
   */
  public final void setEightByEightCRMConst(final String eightByEightCRMConst) {
    this.eightByEightCRMConst = eightByEightCRMConst;
  }
}
