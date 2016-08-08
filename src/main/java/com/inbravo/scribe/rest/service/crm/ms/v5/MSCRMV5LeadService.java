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

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.internal.service.dto.CADUser;
import com.inbravo.scribe.rest.resource.CADCommandObject;
import com.inbravo.scribe.rest.resource.CADObject;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMObjectService;
import com.inbravo.scribe.rest.service.crm.ms.session.MSCRMOffice365SessionManager;

/**
 * Service class for supporting Lead object in MS CRM.
 * 
 * @author vikas.verma
 * 
 */
public final class MSCRMV5LeadService extends MSCRMObjectService {

  private final Logger logger = Logger.getLogger(MSCRMV5LeadService.class.getName());

  /* MS Office 365 based login/session manager */
  private MSCRMOffice365SessionManager mSCRMSessionManager;

  /* MS CRM V5 based service manager */
  private MSCRMOffice365basedServiceManager mSCRMServiceManager;

  public MSCRMV5LeadService() {
    super();
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects");
    }

    final CADUser agent = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<CADObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
            agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), agent.getCrmSecurityToken(), null);

    /* Set the final object in command object */
    cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects query: " + query);
    }

    final CADUser agent = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<CADObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
            agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), agent.getCrmSecurityToken(), null, query);

    /* Set the final object in command object */
    cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects query: " + query + " & select: " + select);
    }

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

    final CADUser agent = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<CADObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
            agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), agent.getCrmSecurityToken(),
            crmFieldToBeSelectedList.toArray(new String[crmFieldToBeSelectedList.size()]), query);

    /* Set the final object in command object */
    cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select, final String order)
      throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects query: " + query + " & select: " + select + " & order: " + order);
    }

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

    final CADUser agent = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<CADObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
            agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), agent.getCrmSecurityToken(),
            crmFieldToBeSelectedList.toArray(new String[crmFieldToBeSelectedList.size()]), query, order);

    /* Set the final object in command object */
    cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final CADCommandObject getObjectsCount(final CADCommandObject cADCommandObject) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final CADCommandObject getObjectsCount(final CADCommandObject cADCommandObject, final String query) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final CADCommandObject createObject(final CADCommandObject cADCommandObject) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final boolean deleteObject(final CADCommandObject cADCommandObject, final String idToBeDeleted) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
  }

  @Override
  public CADCommandObject updateObject(final CADCommandObject cADCommandObject) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
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
