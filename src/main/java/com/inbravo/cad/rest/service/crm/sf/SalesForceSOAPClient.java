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

package com.inbravo.cad.rest.service.crm.sf;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.sforce.soap.partner.CallOptions;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.QueryOptions;
import com.sforce.soap.partner.SessionHeader;
import com.sforce.soap.partner.SforceServiceLocator;
import com.sforce.soap.partner.SoapBindingStub;
import com.sforce.soap.partner.fault.ExceptionCode;
import com.sforce.soap.partner.fault.InvalidIdFault;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class SalesForceSOAPClient {

  private final Logger logger = Logger.getLogger(SalesForceSOAPClient.class.getName());

  /* Sales Force web service client id */
  private String clientId;

  /* Sales Force web service client timeout */
  private String timeout;

  /* Sales Force query batch size */
  private String queryBatchSize;

  private final synchronized SoapBindingStub createSoapBinding() throws Exception {
    logger.debug("---Inside createSoapBinding");
    SoapBindingStub soapBindingStub = null;
    try {
      /* Going to create new stub */
      soapBindingStub = (SoapBindingStub) new SforceServiceLocator().getSoap();
    } catch (ServiceException serviceException) {
      /* log this error */
      logger.error("=*=Inside createSoapBinding", serviceException);
      /* Inform user about service exception */
      throw new CADException(CADResponseCodes._1000 + "Service exception while creating Sales Force stub", serviceException);
    }
    return soapBindingStub;
  }

  /**
   * 
   * @param externalUsername
   * @param externalPassword
   * @return
   */
  public final synchronized SoapBindingStub login(final String externalUsername, final String externalPassword) throws Exception {
    logger.debug("---Inside login CRM user name: " + externalUsername + " & CRM Password:" + externalPassword);

    if (externalUsername == null & externalUsername == null) {
      throw new CADException(CADResponseCodes._1008 + "Sales Force CRM credentials not found");
    }

    /* Call options */
    final CallOptions callOptions = new CallOptions();
    callOptions.setClient(clientId);

    /* Create new Stub */
    final SoapBindingStub soapBindingStub = this.createSoapBinding();

    /* Set header */
    soapBindingStub.setHeader("SforceService", "CallOptions", callOptions);

    try {
      /* Set timeout */
      soapBindingStub.setTimeout(Integer.parseInt(getTimeout()));
    } catch (final NumberFormatException e) {
      throw new CADException(CADResponseCodes._1002 + "'Timeout'");
    }
    LoginResult loginResult = null;
    try {
      /* Trim the parameters. Do not trim password */
      loginResult = soapBindingStub.login(externalUsername.trim(), externalPassword);
      logger.debug("---Inside login session timeout from Sales Force : " + getTimeout() + " msec(s)");
      soapBindingStub.getTimeout();
    } catch (final InvalidIdFault invalidIdFault) {

      /* Inform user about invalid user id */
      throw new CADException(CADResponseCodes._1006 + "Invalid Userid", invalidIdFault);
    } catch (final UnexpectedErrorFault unexpectedErrorFault) {

      /* Inform user about unexpected error */
      throw new CADException(CADResponseCodes._1006 + "Unexpected Error", unexpectedErrorFault);
    } catch (final LoginFault loginFault) {

      /* Inform user about login error */
      final ExceptionCode exCode = loginFault.getExceptionCode();
      final String exMessage = loginFault.getExceptionMessage();
      if (exCode == ExceptionCode.FUNCTIONALITY_NOT_ENABLED || exCode == ExceptionCode.INVALID_CLIENT || exCode == ExceptionCode.INVALID_LOGIN
          || exCode == ExceptionCode.LOGIN_DURING_RESTRICTED_DOMAIN || exCode == ExceptionCode.LOGIN_DURING_RESTRICTED_TIME
          || exCode == ExceptionCode.ORG_LOCKED || exCode == ExceptionCode.PASSWORD_LOCKOUT || exCode == ExceptionCode.SERVER_UNAVAILABLE
          || exCode == ExceptionCode.TRIAL_EXPIRED || exCode == ExceptionCode.UNSUPPORTED_CLIENT) {
        logger.debug("---Inside login problem at SalesForce login: " + CADResponseCodes._1006 + "Exception code: " + exCode.getValue()
            + " : Exception message: " + exMessage);
        throw new CADException(CADResponseCodes._1006 + "Exception code: " + exCode.getValue() + " : Exception message: " + exMessage, loginFault);
      }
    } catch (final RemoteException remoteException) {
      logger.debug("---Inside login problem at SalesForce login: " + CADResponseCodes._1006 + "Remote Error");
      /* Inform user about remote error */
      throw new CADException(CADResponseCodes._1006 + "Remote Error", remoteException);
    }

    /* Check if password is expired */
    if (loginResult.isPasswordExpired()) {
      logger.debug("---Inside login: " + CADResponseCodes._1006 + "Password expired");
      throw new CADException(CADResponseCodes._1006 + "Password expired");
    }

    /* Set end point address */
    soapBindingStub._setProperty(SoapBindingStub.ENDPOINT_ADDRESS_PROPERTY, loginResult.getServerUrl());
    logger.debug("---Inside login SalesForce URL: " + loginResult.getServerUrl());

    /* Maintain the session */
    soapBindingStub.setMaintainSession(true);

    /* Set query option to fetch the records in bunch */
    final QueryOptions qo = new QueryOptions();
    try {
      qo.setBatchSize(Integer.parseInt(queryBatchSize));
    } catch (final NumberFormatException numberFormatException) {
      throw new CADException(CADResponseCodes._1002 + "'BatchSize'", numberFormatException);
    }

    soapBindingStub.setHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "QueryOptions", qo);

    /* Set session id as last element in header list */
    final SessionHeader sh = new SessionHeader();
    sh.setSessionId(loginResult.getSessionId());
    soapBindingStub.setHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader", sh);

    return soapBindingStub;
  }

  public final String getClientId() {
    return clientId;
  }

  public final void setClientId(final String clientId) {
    this.clientId = clientId;
  }

  public final void setTimeout(final String timeout) {
    this.timeout = timeout;
  }

  public final String getTimeout() {
    return timeout;
  }

  public final String getQueryBatchSize() {
    return queryBatchSize;
  }

  public final void setQueryBatchSize(final String queryBatchSize) {
    this.queryBatchSize = queryBatchSize;
  }
}
