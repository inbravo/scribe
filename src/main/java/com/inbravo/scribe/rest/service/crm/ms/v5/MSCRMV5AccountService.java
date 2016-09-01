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

package com.inbravo.scribe.rest.service.crm.ms.v5;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.resource.ScribeCommandObject;
import com.inbravo.scribe.rest.resource.ScribeObject;
import com.inbravo.scribe.rest.service.crm.cache.ScribeCacheObject;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMObjectService;
import com.inbravo.scribe.rest.service.crm.ms.session.MSCRMOffice365SessionManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMV5AccountService extends MSCRMObjectService {

  private final Logger logger = Logger.getLogger(MSCRMV5AccountService.class.getName());

  /* MS Office 365 based login/session manager */
  private MSCRMOffice365SessionManager mSCRMSessionManager;

  /* MS CRM V5 based service manager */
  private MSCRMOffice365basedServiceManager mSCRMServiceManager;

  public MSCRMV5AccountService() {
    super();
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject cADCommandObject) throws Exception {
    logger.debug("----Inside getObjects");

    /* Get agent from session */
    final ScribeCacheObject cacheObject = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<ScribeObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), cacheObject.getScribeMetaObject().getCrmServiceProtocol(), cacheObject
            .getScribeMetaObject().getCrmServiceURL(), cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword(),
            cacheObject.getScribeMetaObject().getCrmOrgName(), cacheObject.getScribeMetaObject().getCrmSecurityToken(), null);

    /* Set the final object in command object */
    cADCommandObject.setObject(cADbjectList.toArray(new ScribeObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject cADCommandObject, final String query) throws Exception {

    logger.debug("----Inside getObjects query: " + query);

    /* Get agent from session */
    final ScribeCacheObject cacheObject = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<ScribeObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), cacheObject.getScribeMetaObject().getCrmServiceProtocol(), cacheObject
            .getScribeMetaObject().getCrmServiceURL(), cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword(),
            cacheObject.getScribeMetaObject().getCrmOrgName(), cacheObject.getScribeMetaObject().getCrmSecurityToken(), null, query);

    /* Set the final object in command object */
    cADCommandObject.setObject(cADbjectList.toArray(new ScribeObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject cADCommandObject, final String query, final String select) throws Exception {
    logger.debug("----Inside getObjects query: " + query + " & select: " + select);

    /* Create list to hold fields to be selected */
    final List<String> crmFieldToBeSelectedList = new ArrayList<String>();

    if (select != null && select.contains(",")) {

      /* Tokenize the select clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(select, ",");

      /* Iterate over elements */
      while (stringTokenizer.hasMoreElements()) {

        /* Add token in list */
        crmFieldToBeSelectedList.add(stringTokenizer.nextToken());
      }
    } else {
      crmFieldToBeSelectedList.add(select);
    }

    /* Get agent from session */
    final ScribeCacheObject cacheObject = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<ScribeObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), cacheObject.getScribeMetaObject().getCrmServiceProtocol(), cacheObject
            .getScribeMetaObject().getCrmServiceURL(), cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword(),
            cacheObject.getScribeMetaObject().getCrmOrgName(), cacheObject.getScribeMetaObject().getCrmSecurityToken(), crmFieldToBeSelectedList
                .toArray(new String[crmFieldToBeSelectedList.size()]), query);

    /* Set the final object in command object */
    cADCommandObject.setObject(cADbjectList.toArray(new ScribeObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject cADCommandObject, final String query, final String select, final String order)
      throws Exception {
    logger.debug("----Inside getObjects query: " + query + " & select: " + select + " & order: " + order);

    /* Create list to hold fields to be selected */
    final List<String> crmFieldToBeSelectedList = new ArrayList<String>();

    if (select != null && select.contains(",")) {

      /* Tokenize the select clause */
      final StringTokenizer stringTokenizer = new StringTokenizer(select, ",");

      /* Iterate over elements */
      while (stringTokenizer.hasMoreElements()) {

        /* Add token in list */
        crmFieldToBeSelectedList.add(stringTokenizer.nextToken());
      }
    } else {
      crmFieldToBeSelectedList.add(select);
    }

    /* Get agent from session */
    final ScribeCacheObject cacheObject = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<ScribeObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), cacheObject.getScribeMetaObject().getCrmServiceProtocol(), cacheObject
            .getScribeMetaObject().getCrmServiceURL(), cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword(),
            cacheObject.getScribeMetaObject().getCrmOrgName(), cacheObject.getScribeMetaObject().getCrmSecurityToken(), crmFieldToBeSelectedList
                .toArray(new String[crmFieldToBeSelectedList.size()]), query, order);

    /* Set the final object in command object */
    cADCommandObject.setObject(cADbjectList.toArray(new ScribeObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final ScribeCommandObject getObjectsCount(final ScribeCommandObject cADCommandObject) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final ScribeCommandObject getObjectsCount(final ScribeCommandObject cADCommandObject, final String query) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final ScribeCommandObject createObject(final ScribeCommandObject cADCommandObject) throws Exception {
    logger.debug("----Inside createObject");
    throw new ScribeException(ScribeResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final boolean deleteObject(final ScribeCommandObject cADCommandObject, final String idToBeDeleted) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + notSupportedError);
  }

  @Override
  public ScribeCommandObject updateObject(final ScribeCommandObject cADCommandObject) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + notSupportedError);
  }

  /**
   * @return the mSCRMSessionManager
   */
  public final MSCRMOffice365SessionManager getmSCRMSessionManager() {
    return this.mSCRMSessionManager;
  }

  /**
   * @param mSCRMSessionManager the mSCRMSessionManager to set
   */
  public final void setmSCRMSessionManager(final MSCRMOffice365SessionManager mSCRMSessionManager) {
    this.mSCRMSessionManager = mSCRMSessionManager;
  }

  /**
   * @return the mSCRMServiceManager
   */
  public final MSCRMOffice365basedServiceManager getmSCRMServiceManager() {
    return this.mSCRMServiceManager;
  }

  /**
   * @param mSCRMServiceManager the mSCRMServiceManager to set
   */
  public final void setmSCRMServiceManager(final MSCRMOffice365basedServiceManager mSCRMServiceManager) {
    this.mSCRMServiceManager = mSCRMServiceManager;
  }
}
