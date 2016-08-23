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

package com.inbravo.scribe.rest.service.crm.ms;

import java.util.List;

import com.inbravo.scribe.rest.resource.ScribeObject;

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
   * @param cADbject
   * @return
   * @throws Exception
   */
  ScribeObject createObject(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final ScribeObject cADbject) throws Exception;

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
  List<ScribeObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
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
  List<ScribeObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
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
  List<ScribeObject> getObjects(final String mSCRMObjectType, final String appProtocolType, final String crmHost, final String userId,
      final String password, final String orgName, final String[] crmSecurityToken, final String[] crmFieldsToSelect, final String query,
      final String order) throws Exception;
}
