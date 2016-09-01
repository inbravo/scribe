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

package com.inbravo.scribe.rest.service.crm.ns;

import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.netsuite.webservices.platform.NetSuiteBindingStub;
import com.netsuite.webservices.platform.NetSuiteServiceLocator;
import com.netsuite.webservices.platform.core.Passport;
import com.netsuite.webservices.platform.core.Status;
import com.netsuite.webservices.platform.faults.ExceededRecordCountFault;
import com.netsuite.webservices.platform.faults.ExceededRequestLimitFault;
import com.netsuite.webservices.platform.faults.ExceededUsageLimitFault;
import com.netsuite.webservices.platform.faults.InvalidCredentialsFault;
import com.netsuite.webservices.platform.faults.InvalidSessionFault;
import com.netsuite.webservices.platform.messages.SearchPreferences;
import com.netsuite.webservices.platform.messages.SessionResponse;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class NetSuiteSOAPClient {

  private final Logger logger = Logger.getLogger(NetSuiteSOAPClient.class.getName());

  /* NetSuite web service client timeout */
  private String timeout;

  /* Max records required in search */
  private int maxRecordInSearch = 20;

  /**
   * 
   * @param userEmail
   * @param password
   * @return
   * @throws Exception
   */
  private final synchronized NetSuiteBindingStub createSoapBinding(final String userEmail, final String password, final String webserviceDomain)
      throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("----Inside createSoapBinding: userEmail : " + userEmail + " & password: " + password + " & webserviceDomain: " + webserviceDomain);
    }

    NetSuiteBindingStub soapBindingStub = null;
    try {
      /* Going to create new stub */
      final NetSuiteServiceLocator service = new NetSuiteServiceLocator();

      /* Enable NetSuite to maintain session */
      service.setMaintainSession(true);

      URL nsServiceURL = null;

      try {

        /* Create web service url */
        nsServiceURL = new URL(webserviceDomain + "/services/NetSuitePort_2009_1");

        if (logger.isDebugEnabled()) {
          logger.debug("----Inside createSoapBinding: nsServiceURL : " + nsServiceURL);
        }
      } catch (final ScribeException e) {

        /* log this error */
        logger.debug("----Inside createSoapBinding error while getting web service url, going for default web service url of NS v2009.1");
        nsServiceURL = null;
      }

      if (nsServiceURL != null) {

        /* Create port using web service URL */
        soapBindingStub = (NetSuiteBindingStub) service.getNetSuitePort(nsServiceURL);
      } else {

        /* Create port using no web service URL */
        soapBindingStub = (NetSuiteBindingStub) service.getNetSuitePort();
      }

      try {
        /* Set timeout */
        soapBindingStub.setTimeout(Integer.parseInt(getTimeout()));
      } catch (final NumberFormatException e) {

        /* Inform the user about system error */
        throw new ScribeException(ScribeResponseCodes._1002 + "'NS_Session_Timeout' in scribe.properties");
      }
    } catch (final ServiceException serviceException) {

      /* log this error */
      logger.error("=*=Inside createSoapBinding", serviceException);

      /* Inform user about service exception */
      throw new ScribeException(ScribeResponseCodes._1000 + "Service exception while creating NetSuite stub", serviceException);
    }
    return soapBindingStub;
  }

  /**
   * 
   * @param externalUsername
   * @param externalPassword
   * @return
   */
  public final synchronized NetSuiteBindingStub login(final String externalUsername, final String externalPassword, final String accountId,
      final String webserviceDomain) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("----Inside login CRM user name: " + externalUsername + " & CRM Password:" + externalPassword + " & accountId: " + accountId
          + " & webserviceDomain: " + webserviceDomain);
    }

    if (externalUsername == null & externalUsername == null & accountId == null) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1008 + "NetSuite CRM credentials not found");
    }

    /* Create new Stub */
    final NetSuiteBindingStub soapBindingStub = this.createSoapBinding(externalUsername, externalPassword, webserviceDomain);

    /* Clear all headers */
    soapBindingStub.clearHeaders();

    /* Create new header element */
    final SOAPHeaderElement passportHeader = new SOAPHeaderElement("urn:messages_2009_1.platform.webservices.netsuite.com", "passport");

    SessionResponse sessionResponse = null;
    try {

      /* Create passport for login */
      final Passport passport = new Passport();

      /* Set CRM user credentials */
      passport.setAccount(accountId);
      passport.setEmail(externalUsername);
      passport.setPassword(externalPassword);

      /* Set passport in passport header */
      passportHeader.setObjectValue(passport);

      /* New header element for search preference and BodyFieldsOnly */
      final SOAPHeaderElement searchPrefHeader = new SOAPHeaderElement("urn:messages.platform.webservices.netsuite.com", "searchPreferences");

      /* Create new search preference */
      final SearchPreferences searchPrefs = new SearchPreferences();

      /* Set default page size */
      searchPrefs.setPageSize(maxRecordInSearch);

      /* Get body fields only */
      searchPrefs.setBodyFieldsOnly(new Boolean(true));

      /* Set value in header */
      searchPrefHeader.setObjectValue(searchPrefs);

      /* Set header in stub */
      soapBindingStub.setHeader(passportHeader);
      soapBindingStub.setHeader(searchPrefHeader);

      /* Trim the parameters. Do not trim password */
      sessionResponse = soapBindingStub.login(passport);

      /* Get status from response */
      final Status status = sessionResponse.getStatus();

      if (status != null) {
        if (logger.isDebugEnabled()) {
          logger.debug("----Inside login, NetSuite login attempt is successfull? : " + status.isIsSuccess());
        }
      } else {
        throw new ScribeException(ScribeResponseCodes._1021 + "NetSuite webservice error while login : Login status is not available");
      }

    } catch (final ExceededRecordCountFault e) {

      /* Inform user about unexpected error */
      throw new ScribeException(ScribeResponseCodes._1021 + "NetSuite webservice error : Response records count exceeded", e);
    } catch (final ExceededRequestLimitFault e) {

      /* Inform user about unexpected error */
      throw new ScribeException(ScribeResponseCodes._1021 + "NetSuite webservice error : Request limit is exceeded", e);
    } catch (final ExceededUsageLimitFault e) {

      /* Inform user about unexpected error */
      throw new ScribeException(ScribeResponseCodes._1021 + "NetSuite webservice error : Usage limit is exceeded", e);
    } catch (final InvalidSessionFault e) {

      /* Inform user about unexpected error */
      throw new ScribeException(ScribeResponseCodes._1021 + "NetSuite webservice error : Invalid session fault", e);
    } catch (final InvalidCredentialsFault e) {

      /* Inform user about unexpected error */
      throw new ScribeException(ScribeResponseCodes._1012 + "NetSuite webservice error : Credentials are not valid", e);
    } catch (final AxisFault e) {

      logger.debug("----Inside login : AxisFault : " + e.dumpToString());

      /* Inform user about unexpected error */
      throw new ScribeException(ScribeResponseCodes._1021 + "NetSuite webservice error while login : reason : " + e.getFaultString());
    } catch (final RemoteException e) {

      /* Inform user about remote error */
      logger.debug("----Inside login problem at NetSuite login: " + ScribeResponseCodes._1021 + " : Remote Error", e);

      /* Inform user about remote error */
      throw new ScribeException(ScribeResponseCodes._1021 + " : Remote Error : " + e);

    } catch (final Exception e) {

      /* Inform user about remote error */
      logger.error("----Inside login problem at NetSuite login: " + ScribeResponseCodes._1021 + " : Login Error", e);

      /* Inform user about remote error */
      throw new ScribeException(ScribeResponseCodes._1021 + " : Login Error : " + e.getMessage());
    }

    return soapBindingStub;
  }

  public final String getTimeout() {
    return timeout;
  }

  public final void setTimeout(final String timeout) {
    this.timeout = timeout;
  }

  /**
   * @return the maxRecordInSearch
   */
  public final int getMaxRecordInSearch() {
    return this.maxRecordInSearch;
  }

  /**
   * @param maxRecordInSearch the maxRecordInSearch to set
   */
  public final void setMaxRecordInSearch(final int maxRecordInSearch) {
    this.maxRecordInSearch = maxRecordInSearch;
  }
}
