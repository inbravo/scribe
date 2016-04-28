package com.inbravo.cad.rest.service.crm.session;

import org.apache.log4j.Logger;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.constants.CRMConstants.MSCRMVersionType;
import com.inbravo.cad.rest.service.crm.factory.CRMServiceFactory;
import com.inbravo.cad.rest.service.crm.ms.session.MSCRMLiveIdSessionManager;
import com.inbravo.cad.rest.service.crm.ms.session.MSCRMOffice365SessionManager;
import com.inbravo.cad.rest.service.crm.ms.session.MSCRMSessionManagerFactory;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CRMSessionFactory implements ApplicationContextAware {

  private final Logger logger = Logger.getLogger(CRMSessionFactory.class.getName());

  /* Spring specific context */
  private ApplicationContext applicationContext;

  /* CRM service factory */
  private CRMServiceFactory crmServiceFactory;

  /**
   * 
   * @param crmType
   * @return
   * @throws Exception
   */
  public final CRMSessionManager getCRMSessionManager(final String crmType) throws Exception {

    logger.debug("---Inside getCRMSessionManager crmType: " + crmType);

    logger.debug("---Inside getCRMSessionManager crmType: " + crmType);

    if (crmType.equalsIgnoreCase(crmServiceFactory.getSalesForceCRMConst()) || crmType.equalsIgnoreCase(crmServiceFactory.getSalesForceWCRMConst())) {

      /* Retrieve Sales Force CRM session manager */
      return (CRMSessionManager) getApplicationContext().getBean("salesForceCRMSessionManager");

    } else if (crmType.equalsIgnoreCase(crmServiceFactory.getMicrosoftCRMConst())) {

      /* Differentiate between V4 and V5 */
      final MSCRMSessionManagerFactory factory = (MSCRMSessionManagerFactory) getApplicationContext().getBean("mSCRMSessionManagerFactory");

      /* Get version type */
      final MSCRMVersionType vType = factory.checkMSCRMVersion(crmType);

      /* If V5 */
      if (vType.equals(MSCRMVersionType.V5)) {

        /* Get Office 365 based V5 service */
        return (MSCRMOffice365SessionManager) getApplicationContext().getBean("mSCRMOffice365SessionManager");

      } else /* If V4 */
      if (vType.equals(MSCRMVersionType.V4)) {

        /* Get Live id based V4 service */
        return (MSCRMLiveIdSessionManager) getApplicationContext().getBean("mSCRMLiveIdSessionManager");

      } else {
        /* Inform user about no implementation */
        throw new CADException(CADResponseCodes._1003 + "Following CRM: " + crmType + " V " + vType + " : integration is not enabled");
      }
    } else if (crmType.equalsIgnoreCase(crmServiceFactory.getZendeskCRMConst())) {

      /* Retrieve Zen desk CRM session manager */
      return (CRMSessionManager) getApplicationContext().getBean("zDCRMSessionManager");

    } else if (crmType.equalsIgnoreCase(crmServiceFactory.getNetsuiteCRMConst())) {

      /* Retrieve NetSuite CRM session manager */
      return (CRMSessionManager) getApplicationContext().getBean("netSuiteCRMSessionManager");

    } else if (crmType.equalsIgnoreCase(crmServiceFactory.getZohoCRMConst())) {

      /* Retrieve Zoho CRM session manager */
      return (CRMSessionManager) getApplicationContext().getBean("zHCRMSessionManager");
    } else {

      /* Inform user about no implementation */
      throw new CADException(CADResponseCodes._1003 + "Following CRM: '" + crmType + "' integration is not enabled");
    }
  }

  @Override
  public final void setApplicationContext(final ApplicationContext ApplicationContext) throws BeansException {
    this.applicationContext = ApplicationContext;
  }

  public final ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public final CRMServiceFactory getCrmServiceFactory() {
    return crmServiceFactory;
  }

  public final void setCrmServiceFactory(final CRMServiceFactory crmServiceFactory) {
    this.crmServiceFactory = crmServiceFactory;
  }
}
