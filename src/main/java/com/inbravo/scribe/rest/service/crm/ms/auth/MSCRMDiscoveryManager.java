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

package com.inbravo.scribe.rest.service.crm.ms.auth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.service.crm.cache.ScribeCacheObject;
import com.inbravo.scribe.rest.service.crm.ms.dto.MSCRMUserInformation;
import com.microsoft.schemas.crm._2007.CrmDiscoveryService.CrmDiscoveryServiceLocator;
import com.microsoft.schemas.crm._2007.CrmDiscoveryService.CrmDiscoveryServiceSoap;
import com.microsoft.schemas.crm._2007.CrmDiscoveryService.OrganizationDetail;
import com.microsoft.schemas.crm._2007.CrmDiscoveryService.RetrieveCrmTicketRequest;
import com.microsoft.schemas.crm._2007.CrmDiscoveryService.RetrieveCrmTicketResponse;
import com.microsoft.schemas.crm._2007.CrmDiscoveryService.RetrieveOrganizationsRequest;
import com.microsoft.schemas.crm._2007.CrmDiscoveryService.RetrieveOrganizationsResponse;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMDiscoveryManager {

  private final Logger logger = Logger.getLogger(MSCRMDiscoveryManager.class.getName());

  private MSAuthManager mSAuthManager;

  private String crmDiscoveryServiceEndpoint = SOAPExecutor.CRM_DISCOVERY_SERVICE_ENDPOINT;

  /**
   * This API will be used for finding the organization information of an agent/tenant
   * 
   * @param userId
   * @param password
   * @param passportTicket
   * @return
   * @throws ServiceException
   * @throws JaxenException
   * @throws IOException
   * @throws SOAPException
   */
  private final OrganizationDetail retrieveOrganizationDetail(final String userId, final String password, final String passportTicket,
      final CrmDiscoveryServiceSoap crmDiscoveryService) throws IOException {

    final RetrieveOrganizationsRequest request = new RetrieveOrganizationsRequest();
    request.setPassportTicket(passportTicket);
    request.setUserId(userId);
    request.setPassword(password);

    /* Get organization information from discovery service */
    final RetrieveOrganizationsResponse response = (RetrieveOrganizationsResponse) crmDiscoveryService.execute(request);
    final OrganizationDetail[] orgDetails = response.getOrganizationDetails();

    /* Return orgnization information */
    if (orgDetails != null && orgDetails.length != 0) {
      return orgDetails[0];
    } else {
      return null;
    }
  }

  /**
   * This API will be used for generating a CRM ticket for communicating with Microsoft CRM
   * 
   * @param userId
   * @param password
   * @return
   * @throws ServiceException
   * @throws JaxenException
   * @throws IOException
   * @throws SOAPException
   */
  public final MSCRMUserInformation getMSCRMUserInformation(final ScribeCacheObject user) throws Exception {

    logger.debug("---Inside getMSCRMUserInformation");
    try {

      String userName = null;
      String password = null;
      String crmServiceURL = null;
      String crmServiceProtocal = null;

      logger.debug("---Inside getMSCRMUserInformation for agent: " + user.getScribeMetaObject().getCrmUserId());

      /* get CRM credentials */
      userName = user.getScribeMetaObject().getCrmUserId();
      password = user.getScribeMetaObject().getCrmPassword();
      crmServiceURL = user.getScribeMetaObject().getCrmServiceURL();
      crmServiceProtocal = user.getScribeMetaObject().getCrmServiceProtocol();

      /* First get Microsoft passport ticket */
      final String[] passportTicket = mSAuthManager.getCRMAuthToken(user);

      /* Create discovery service locator */
      final CrmDiscoveryServiceLocator locator = new CrmDiscoveryServiceLocator();

      /* Create discovery service */
      final CrmDiscoveryServiceSoap crmDiscoveryService =
          locator.getCrmDiscoveryServiceSoap12(new URL(crmServiceProtocal + "://" + crmServiceURL + crmDiscoveryServiceEndpoint));

      /* First get organization details */
      final OrganizationDetail org = this.retrieveOrganizationDetail(userName, password, passportTicket[0], crmDiscoveryService);

      /* Create new local DTO */
      MSCRMUserInformation mSCRMUserInformation = new MSCRMUserInformation();

      /* Set organization information */
      if (org != null && org.getOrganizationName() != null) {

        /* Create request object to get CRM ticket */
        final RetrieveCrmTicketRequest request = new RetrieveCrmTicketRequest();
        request.setPassportTicket(passportTicket[0]);
        request.setOrganizationName(org.getOrganizationName());

        /* Get CRM ticket from Discovery service */
        final RetrieveCrmTicketResponse response = (RetrieveCrmTicketResponse) crmDiscoveryService.execute(request);

        /* Add the necessary information in local DTO */
        mSCRMUserInformation.setCrmTicket(response.getCrmTicket());
        mSCRMUserInformation.setOrganizationName(org.getOrganizationName());
        mSCRMUserInformation.setPassportTicket(passportTicket[0]);
        mSCRMUserInformation.setCrmServiceURL(org.getCrmServiceUrl());
      } else {

        /* This check is to disallocate the object from memory */
        mSCRMUserInformation = null;
      }
      return mSCRMUserInformation;
    } catch (final MalformedURLException e) {
      throw new ScribeException(ScribeResponseCodes._1008 + "Protocol type is not found in CRM configuration", e);
    } catch (final AxisFault e) {
      throw new ScribeException(ScribeResponseCodes._1013 + "Recieved a web service error", e);
    } catch (final RemoteException e) {
      throw new ScribeException(ScribeResponseCodes._1013 + "Communication error", e);
    }
  }

  public final String getCrmDiscoveryServiceEndpoint() {
    return crmDiscoveryServiceEndpoint;
  }

  public final void setCrmDiscoveryServiceEndpoint(final String crmDiscoveryServiceEndpoint) {
    this.crmDiscoveryServiceEndpoint = crmDiscoveryServiceEndpoint;
  }

  /**
   * @return the mSAuthManager
   */
  public final MSAuthManager getmSAuthManager() {
    return this.mSAuthManager;
  }

  /**
   * @param mSAuthManager the mSAuthManager to set
   */
  public final void setmSAuthManager(final MSAuthManager mSAuthManager) {
    this.mSAuthManager = mSAuthManager;
  }
}
