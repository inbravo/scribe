package com.inbravo.cad.rest.service.crm.ms;

import java.util.List;

import com.inbravo.cad.rest.resource.CADObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface MSCRMServiceManager {

  /**
   * 
   * @param mSCRMObjectType
   * @param appProtocolType
   * @param crmHost
   * @param userId
   * @param password
   * @param orgName
   * @param crmSecurityToken
   * @param eDSAObject
   * @return
   * @throws Exception
   */
  CADObject createObject(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final CADObject eDSAObject) throws Exception;

  /**
   * 
   * @param mSCRMObjectType
   * @param appProtocolType
   * @param crmHost
   * @param userId
   * @param password
   * @param orgName
   * @param crmSecurityToken
   * @param crmFields
   * @return
   * @throws Exception
   */
  List<CADObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFields) throws Exception;

  /**
   * 
   * @param mSCRMObjectType
   * @param appProtocolType
   * @param crmHost
   * @param userId
   * @param password
   * @param orgName
   * @param crmSecurityToken
   * @param crmFieldsToSelect
   * @param query
   * @return
   * @throws Exception
   */
  List<CADObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFieldsToSelect, final String query)
      throws Exception;

  /**
   * 
   * @param mSCRMObjectType
   * @param appProtocolType
   * @param crmHost
   * @param userId
   * @param password
   * @param orgName
   * @param crmSecurityToken
   * @param crmFieldsToSelect
   * @param query
   * @param order
   * @return
   * @throws Exception
   */
  List<CADObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFieldsToSelect, final String query,
      final String order) throws Exception;
}
