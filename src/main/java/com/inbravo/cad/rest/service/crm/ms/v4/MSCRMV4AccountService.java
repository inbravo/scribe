package com.inbravo.cad.rest.service.crm.ms.v4;

import java.util.ArrayList;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.Tenant;
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
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject) throws Exception {
    logger.debug("---Inside getObjects");

    /* Get respective CRM session id */
    if (eDSACommandObject.getTenant() != null) {
      final Tenant tenant = mSCRMSessionManager.getTenantWithCRMSessionInformation(eDSACommandObject.getTenant());

      /* Pass crm fields list null to fetch all fields */
      final List<CADObject> eDSAObjectList =
          mSCRMServiceManager.getObjects(eDSACommandObject.getObjectType(), tenant.getCrmServiceProtocol(), tenant.getCrmServiceURL(),
              tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmOrgName(), new String[] {tenant.getCrmSessionId()}, null);

      /* Set the final object in command object */
      eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
    } else if (eDSACommandObject.getAgent() != null) {
      final CADUser agent = mSCRMSessionManager.getUserWithCRMSessionInformation(eDSACommandObject.getAgent());

      final List<CADObject> eDSAObjectList =
          mSCRMServiceManager.getObjects(eDSACommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
              agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), new String[] {agent.getCrmSessionId()}, null);

      /* Set the final object in command object */
      eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
    }
    return eDSACommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query) throws Exception {
    logger.debug("---Inside getObjects query: " + query);

    /* Get respective CRM session id */
    if (eDSACommandObject.getTenant() != null) {
      final Tenant tenant = mSCRMSessionManager.getTenantWithCRMSessionInformation(eDSACommandObject.getTenant());

      /* Pass crm fields list null to fetch all fields */
      final List<CADObject> eDSAObjectList =
          mSCRMServiceManager.getObjects(eDSACommandObject.getObjectType(), tenant.getCrmServiceProtocol(), tenant.getCrmServiceURL(),
              tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmOrgName(), new String[] {tenant.getCrmSessionId()}, null, query);

      /* Set the final object in command object */
      eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
    } else if (eDSACommandObject.getAgent() != null) {
      final CADUser agent = mSCRMSessionManager.getUserWithCRMSessionInformation(eDSACommandObject.getAgent());

      final List<CADObject> eDSAObjectList =
          mSCRMServiceManager.getObjects(eDSACommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
              agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), new String[] {agent.getCrmSessionId()}, null, query);

      /* Set the final object in command object */
      eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
    }
    return eDSACommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select) throws Exception {
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
    if (eDSACommandObject.getTenant() != null) {
      final Tenant tenant = mSCRMSessionManager.getTenantWithCRMSessionInformation(eDSACommandObject.getTenant());

      final List<CADObject> eDSAObjectList =
          mSCRMServiceManager.getObjects(eDSACommandObject.getObjectType(), tenant.getCrmServiceProtocol(), tenant.getCrmServiceURL(),
              tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmOrgName(), new String[] {tenant.getCrmSessionId()},
              crmFieldToBeSelectedList.toArray(new String[crmFieldToBeSelectedList.size()]), query);

      /* Set the final object in command object */
      eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
    } else if (eDSACommandObject.getAgent() != null) {
      final CADUser agent = mSCRMSessionManager.getUserWithCRMSessionInformation(eDSACommandObject.getAgent());

      final List<CADObject> eDSAObjectList =
          mSCRMServiceManager.getObjects(eDSACommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
              agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), new String[] {agent.getCrmSessionId()},
              crmFieldToBeSelectedList.toArray(new String[crmFieldToBeSelectedList.size()]), query);

      /* Set the final object in command object */
      eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
    }
    return eDSACommandObject;
  }

  @Override
  public final CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select, final String order)
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
    if (eDSACommandObject.getTenant() != null) {
      final Tenant tenant = mSCRMSessionManager.getTenantWithCRMSessionInformation(eDSACommandObject.getTenant());

      final List<CADObject> eDSAObjectList =
          mSCRMServiceManager.getObjects(eDSACommandObject.getObjectType(), tenant.getCrmServiceProtocol(), tenant.getCrmServiceURL(),
              tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmOrgName(), new String[] {tenant.getCrmSessionId()},
              crmFieldToBeSelectedList.toArray(new String[crmFieldToBeSelectedList.size()]), query, order);

      /* Set the final object in command object */
      eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
    } else if (eDSACommandObject.getAgent() != null) {
      final CADUser agent = mSCRMSessionManager.getUserWithCRMSessionInformation(eDSACommandObject.getAgent());

      final List<CADObject> eDSAObjectList =
          mSCRMServiceManager.getObjects(eDSACommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
              agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), new String[] {agent.getCrmSessionId()},
              crmFieldToBeSelectedList.toArray(new String[crmFieldToBeSelectedList.size()]), query, order);

      /* Set the final object in command object */
      eDSACommandObject.seteDSAObject(eDSAObjectList.toArray(new CADObject[eDSAObjectList.size()]));
    }
    return eDSACommandObject;
  }

  @Override
  public final CADCommandObject getObjectsCount(final CADCommandObject eDSACommandObject) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final CADCommandObject getObjectsCount(final CADCommandObject eDSACommandObject, final String query) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final CADCommandObject createObject(final CADCommandObject eDSACommandObject) throws Exception {
    logger.debug("---Inside createObject");

    /* Create new EDSAObject */
    CADObject eDSAObject = null;

    /* Get respective CRM session id */
    if (eDSACommandObject.getTenant() != null) {
      final Tenant tenant = mSCRMSessionManager.getTenantWithCRMSessionInformation(eDSACommandObject.getTenant());

      eDSAObject =
          mSCRMServiceManager.createObject(eDSACommandObject.getObjectType(), tenant.getCrmServiceProtocol(), tenant.getCrmServiceURL(),
              tenant.getCrmUserId(), tenant.getCrmPassword(), tenant.getCrmOrgName(), new String[] {tenant.getCrmSessionId()},
              eDSACommandObject.geteDSAObject()[0]);
    } else if (eDSACommandObject.getAgent() != null) {
      final CADUser agent = mSCRMSessionManager.getUserWithCRMSessionInformation(eDSACommandObject.getAgent());

      eDSAObject =
          mSCRMServiceManager.createObject(eDSACommandObject.getObjectType(), agent.getCrmServiceProtocol(), agent.getCrmServiceURL(),
              agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmOrgName(), new String[] {agent.getCrmSessionId()},
              eDSACommandObject.geteDSAObject()[0]);
    }

    /* Set the final object in command object */
    eDSACommandObject.seteDSAObject(new CADObject[] {eDSAObject});

    return eDSACommandObject;
  }

  @Override
  public final boolean deleteObject(final CADCommandObject eDSACommandObject, final String idToBeDeleted) throws Exception {
    throw new CADException(CADResponseCodes._1003 + notSupportedError);
  }

  @Override
  public final CADCommandObject updateObject(final CADCommandObject eDSACommandObject) throws Exception {
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
