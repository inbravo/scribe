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

package com.inbravo.scribe.rest.service.crm.session;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.constants.CRMConstants.MSCRMVersionType;
import com.inbravo.scribe.rest.service.crm.factory.CRMServiceFactory;
import com.inbravo.scribe.rest.service.crm.ms.session.MSCRMLiveIdSessionManager;
import com.inbravo.scribe.rest.service.crm.ms.session.MSCRMOffice365SessionManager;
import com.inbravo.scribe.rest.service.crm.ms.session.MSCRMSessionManagerFactory;

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

    if (crmType.equalsIgnoreCase(crmServiceFactory.getSalesForceCRMConst())) {

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
        throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM: " + crmType + " V " + vType + " : integration is not enabled");
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
      throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM: '" + crmType + "' integration is not enabled");
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
