package com.inbravo.cad.rest.service.crm.ms.v4;

import java.util.ArrayList;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.resource.CADObject;
import com.inbravo.cad.rest.service.crm.ms.MSCRMObjectService;
import com.inbravo.cad.rest.service.crm.ms.session.MSCRMLiveIdSessionManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMV4AccountService extends MSCRMObjectService {

  private final Logger logger = Logger.getLogger(MSCRMV4AccountService.class.getName());

  private MSCRMLiveIdSessionManager mSCRMSessionManager;

  private MSCRMLiveIdBasedServiceManager mSCRMServiceManager;

  public MSCRMV4AccountService() {
    super();
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject) throws Exception {

    logger.debug("---Inside getObjects");

    /* Get respective CRM session id */
    final CADUser user = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<CADObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), user.getCrmServiceProtocol(), user.getCrmServiceURL(), user.getCrmUserId(),
            user.getCrmPassword(), user.getCrmOrgName(), new String[] {user.getCrmSessionId()}, null);

    /* Set the final object in command object */
    cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query) throws Exception {

    logger.debug("---Inside getObjects query: " + query);

    /* Get respective CRM session id */
    final CADUser user = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<CADObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), user.getCrmServiceProtocol(), user.getCrmServiceURL(), user.getCrmUserId(),
            user.getCrmPassword(), user.getCrmOrgName(), new String[] {user.getCrmSessionId()}, null, query);

    /* Set the final object in command object */
    cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select) throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select);

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

    /* Get respective CRM session id */
    final CADUser user = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<CADObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), user.getCrmServiceProtocol(), user.getCrmServiceURL(), user.getCrmUserId(),
            user.getCrmPassword(), user.getCrmOrgName(), new String[] {user.getCrmSessionId()},
            crmFieldToBeSelectedList.toArray(new String[crmFieldToBeSelectedList.size()]), query);

    /* Set the final object in command object */
    cADCommandObject.setcADObject(cADbjectList.toArray(new CADObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select, final String order)
      throws Exception {
    logger.debug("---Inside getObjects query: " + query + " & select: " + select + " & order: " + order);

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

    /* Get respective CRM session id */
    final CADUser user = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    final List<CADObject> cADbjectList =
        mSCRMServiceManager.getObjects(cADCommandObject.getObjectType(), user.getCrmServiceProtocol(), user.getCrmServiceURL(), user.getCrmUserId(),
            user.getCrmPassword(), user.getCrmOrgName(), new String[] {user.getCrmSessionId()},
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
    logger.debug("---Inside createObject");

    /* Get user information */
    final CADUser agent = mSCRMSessionManager.getCrmUserInfoWithCRMSessionInformation(cADCommandObject.getCrmUserId());

    /* Create CAD object */
    final CADObject cADbject =
        mSCRMServiceManager.createObject(cADCommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
            agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), new String[] {agent.getCrmSessionId()},
            cADCommandObject.getcADObject()[0]);

    /* Set the final object in command object */
    cADCommandObject.setcADObject(new CADObject[] {cADbject});

    return cADCommandObject;
  }

  @Override
  public final boolean deleteObject(final CADCommandObject cADCommandObject, final String idToBeDeleted) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final CADCommandObject updateObject(final CADCommandObject cADCommandObject) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
  }

  /**
   * @return the mSCRMSessionManager
   */
  public final MSCRMLiveIdSessionManager getmSCRMSessionManager() {
    return this.mSCRMSessionManager;
  }

  /**
   * @param mSCRMSessionManager the mSCRMSessionManager to set
   */
  public final void setmSCRMSessionManager(final MSCRMLiveIdSessionManager mSCRMSessionManager) {
    this.mSCRMSessionManager = mSCRMSessionManager;
  }

  /**
   * @return the mSCRMServiceManager
   */
  public final MSCRMLiveIdBasedServiceManager getmSCRMServiceManager() {
    return this.mSCRMServiceManager;
  }

  /**
   * @param mSCRMServiceManager the mSCRMServiceManager to set
   */
  public final void setmSCRMServiceManager(final MSCRMLiveIdBasedServiceManager mSCRMServiceManager) {
    this.mSCRMServiceManager = mSCRMServiceManager;
  }
}
