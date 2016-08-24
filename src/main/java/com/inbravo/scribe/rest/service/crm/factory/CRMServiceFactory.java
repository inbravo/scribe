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

package com.inbravo.scribe.rest.service.crm.factory;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.internal.service.factory.ScribeServiceFactory;
import com.inbravo.scribe.rest.constants.CRMConstants;
import com.inbravo.scribe.rest.resource.ScribeCommandObject;
import com.inbravo.scribe.rest.service.crm.CRMService;
import com.inbravo.scribe.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.scribe.rest.service.crm.cache.ScribeCacheObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CRMServiceFactory implements ApplicationContextAware, ScribeServiceFactory {

  private final Logger logger = Logger.getLogger(CRMServiceFactory.class.getName());

  /* Boolean value to enable Zoho CRM */
  private boolean zohoCRMEnabled;

  /* Boolean value to enable SFDC CRM */
  private boolean sfdcCRMEnabled;

  /* Boolean value to enable Zendesk CRM */
  private boolean zendeskCRMEnabled;

  /* Boolean value to enable NetSuite CRM */
  private boolean netsuiteCRMEnabled;

  /* Boolean value to enable MS CRM */
  private boolean microsoftCRMEnabled;

  /* SCRM session cache for holding cacheObject/agent */
  private CRMSessionCache cRMSessionCache;

  private ApplicationContext applicationContext;

  /* Constant for holding SFDC CRM name */
  private String salesForceCRMConst = CRMConstants.salesForceCRM;

  /* Constant for holding MS CRM name */
  private String microsoftCRMConst = CRMConstants.microsoftCRM;

  /* Constant for holding Zendesk CRM name */
  private String zendeskCRMConst = CRMConstants.zendeskCRM;

  /* Constant for holding Netsuite CRM name */
  private String netsuiteCRMConst = CRMConstants.netsuiteCRM;

  /* Constant for holding Zoho CRM name */
  private String zohoCRMConst = CRMConstants.zohoCRM;

  /**
   * This method will check the cacheObject/agent configuration and instantiate the relevant service
   * class
   * 
   * @return
   */
  public final synchronized CRMService getService(final ScribeCommandObject cADCommandObject) throws Exception {

    logger.debug("---Inside getService");
    CRMService cRMService = null;

    /* Create an empty cacheObject object */
    ScribeCacheObject cacheObject = null;

    /* Pick information from cache if batch request */
    if (cADCommandObject.getBatch() != null) {

      logger.debug("---Inside getService: found a batch request. Going to fetch cached cacheObject information");

      /* Check if cacheObject in cache */
      cacheObject = (ScribeCacheObject) cRMSessionCache.recover(cADCommandObject.getCrmUserId());
    } else {

      if (cADCommandObject.getMetaObject() != null) {

        cacheObject = ScribeCacheObject.build(cADCommandObject.getMetaObject());
      } else {

        cacheObject = ScribeCacheObject.build(cADCommandObject);
      }
    }

    /* Validate cacheObject CRM information */
    if (cacheObject.getScribeMetaObject().getCrmType() == null) {

      /* Inform cacheObject about missing property */
      throw new ScribeException(ScribeResponseCodes._1012 + "CRM integration information is missing");
    }

    /* Check target CRM service type */
    if (cacheObject.getScribeMetaObject().getCrmType().equalsIgnoreCase(salesForceCRMConst)) {

      /* Check if SFDC CRM is enabled */
      if (sfdcCRMEnabled) {

        /* Retrieve Sales Force CRM implementation */
        cRMService = (CRMService) getApplicationContext().getBean("sFSOAPCRMService");
      } else {
        /* Inform cacheObject about missing implementation */
        throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM: " + cacheObject.getScribeMetaObject().getCrmType()
            + " : integration is not enabled");
      }

    } else if (cacheObject.getScribeMetaObject().getCrmType().equalsIgnoreCase(microsoftCRMConst)) {

      /* Check if Microsoft CRM is enabled */
      if (microsoftCRMEnabled) {

        /* Retrieve Microsoft CRM implementation */
        cRMService = (CRMService) getApplicationContext().getBean("mSSOAPCRMService");
      } else {

        /* Inform cacheObject about missing implementation */
        throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM: " + cacheObject.getScribeMetaObject().getCrmType()
            + " : integration is not enabled");
      }
    } else if (cacheObject.getScribeMetaObject().getCrmType().contains(zendeskCRMConst)) {

      /* Check if Zendesk CRM is enabled */
      if (zendeskCRMEnabled) {

        /* Retrieve Zendesk CRM implementation */
        cRMService = (CRMService) getApplicationContext().getBean("zDRESTCRMService");
      } else {

        /* Inform cacheObject about missing implementation */
        throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM: " + cacheObject.getScribeMetaObject().getCrmType()
            + " : integration is not enabled");
      }
    } else if (cacheObject.getScribeMetaObject().getCrmType().equalsIgnoreCase(netsuiteCRMConst)) {

      /* Check if Netsuite CRM is enabled */
      if (netsuiteCRMEnabled) {

        /* Retrieve Netsuite CRM implementation */
        cRMService = (CRMService) getApplicationContext().getBean("nSSOAPCRMService");
      } else {

        /* Inform cacheObject about missing implementation */
        throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM: " + cacheObject.getScribeMetaObject().getCrmType()
            + " : integration is not enabled");
      }
    } else if (cacheObject.getScribeMetaObject().getCrmType().equalsIgnoreCase(zohoCRMConst)) {

      /* Check if Zoho CRM is enabled */
      if (zohoCRMEnabled) {

        /* Retrieve ZOHO CRM implementation */
        cRMService = (CRMService) getApplicationContext().getBean("zHRESTCRMService");
      } else {

        /* Inform cacheObject about missing implementation */
        throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM: " + cacheObject.getScribeMetaObject().getCrmType()
            + " : integration is not enabled");
      }
    } else {

      /* Inform cacheObject about missing implementation */
      throw new ScribeException(ScribeResponseCodes._1003 + "Following CRM: " + cacheObject.getScribeMetaObject().getCrmType()
          + " : integration is not enabled");
    }


    /* Save this cacheObject in cache */
    cRMSessionCache.admit(cADCommandObject.getCrmUserId(), cacheObject);

    return cRMService;
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
}
