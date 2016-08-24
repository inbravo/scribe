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

package com.inbravo.scribe.rest.service.crm.ns.session;

import org.apache.log4j.Logger;

import com.inbravo.scribe.rest.service.crm.cache.BasicObject;
import com.inbravo.scribe.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.scribe.rest.service.crm.cache.ScribeCacheObject;
import com.inbravo.scribe.rest.service.crm.ns.NetSuiteSOAPClient;
import com.inbravo.scribe.rest.service.crm.ns.v2k9.NSCRMV2k9ClientInfoProvidor;
import com.inbravo.scribe.rest.service.crm.ns.v2k9.NSCRMV2k9ClientInfoProvidor.NSCRMV2k9ClientInfo;
import com.inbravo.scribe.rest.service.crm.session.CRMSessionManager;
import com.netsuite.webservices.platform.NetSuiteBindingStub;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class NetSuiteCRMSessionManager implements CRMSessionManager {

  private final Logger logger = Logger.getLogger(NetSuiteCRMSessionManager.class.getName());

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* NetSuite SOAP client */
  private NetSuiteSOAPClient netSuiteSOAPClient;

  /* Agent/tenant id seperator */
  private String agentIdSplitCharacter;

  /* REST based web service URL info provider */
  private NSCRMV2k9ClientInfoProvidor clientInfoProvidor;

  /**
   * API to get web service stub for Agent
   * 
   * @param agentId
   * @return
   * @throws Exception
   */
  public final synchronized NetSuiteBindingStub getSoapBindingStub(final String crmUserId, final String crmPassword) throws Exception {

    logger.debug("---Inside getSoapBindingStub: " + crmUserId);

    /* Recover user from cache */
    final ScribeCacheObject cacheObject = (ScribeCacheObject) cRMSessionCache.recover(crmUserId);

    /* This code block will be usefull if cache size limit is reached */
    if (cacheObject == null) {

      logger.debug("---Inside getSoapBindingStub, user not found in cache hence going for fresh fetch. Seems like cache limit is reached");

    } else {
      logger.debug("---Inside getSoapBindingStub, user is found in cache");
    }

    NetSuiteBindingStub soapBindingStub = null;

    /* Get SOAP stub */
    if (cacheObject.getSoapStub() != null) {

      logger.debug("---Inside getSoapBindingStub, using stub from cache");

      /* Get stub from cache */
      soapBindingStub = (NetSuiteBindingStub) cacheObject.getSoapStub();
    } else {

      /* Get service URL information */
      final NSCRMV2k9ClientInfo clientInfo =
          clientInfoProvidor.getNSCRMV2k9ClientInfo(cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword());

      logger.debug("---Inside getSoapBindingStub, creating fresh stub, client info: " + clientInfo);

      /* TODO : Login at NetSuite : pass role id as 3 for admin role */
      soapBindingStub =
          netSuiteSOAPClient.login(cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword(), cacheObject
              .getScribeMetaObject().getCrmAccountId(), clientInfo.getWebservicesDomain());

      /* Set this stub in agent */
      cacheObject.setSoapStub(soapBindingStub);
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(crmUserId, cacheObject);

    return soapBindingStub;
  }

  @Override
  public final BasicObject getSessionInfo(final String crmUserId) throws Exception {

    logger.debug("---Inside getSessionInfo id: " + crmUserId);

    /* Check if session is already available at cache */
    final ScribeCacheObject cacheObject = (ScribeCacheObject) cRMSessionCache.recover(crmUserId);

    /* Get service URL information */
    final NSCRMV2k9ClientInfo clientInfo =
        clientInfoProvidor.getNSCRMV2k9ClientInfo(cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword());

    logger.debug("---Inside getSessionInfo, clientInfo: " + clientInfo);

    /* Login at NetSuite : pass role id as 3 for admin role */
    final NetSuiteBindingStub soapBindingStub =
        netSuiteSOAPClient.login(cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword(), cacheObject
            .getScribeMetaObject().getCrmAccountId(), clientInfo.getWebservicesDomain());

    /* Set this stub in agent */
    cacheObject.setSoapStub(soapBindingStub);

    /* Set service URL */
    if (cacheObject.getScribeMetaObject().getCrmServiceURL() == null) {

      cacheObject.getScribeMetaObject().setCrmServiceURL(clientInfo.getSystemDomain());
      cacheObject.getScribeMetaObject().setCrmServiceProtocol(null);
    }

    /* Save this freshly updated agent in session cache */
    cRMSessionCache.admit(crmUserId, cacheObject);

    /* If everything is fine return true */
    return cacheObject;
  }


  @Override
  public final boolean login(final String crmUserId, final String crmPassword) throws Exception {

    logger.debug("---Inside login crmUserId: " + crmUserId);

    /* Check if session is already available at cache */
    final ScribeCacheObject cacheObject = (ScribeCacheObject) cRMSessionCache.recover(crmUserId);

    /* Get service URL information */
    final NSCRMV2k9ClientInfo clientInfo =
        clientInfoProvidor.getNSCRMV2k9ClientInfo(cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword());

    logger.debug("---Inside login clientInfo: " + clientInfo);

    /* Login at NetSuite : pass role id as 3 for admin role */
    final NetSuiteBindingStub soapBindingStub =
        netSuiteSOAPClient.login(cacheObject.getScribeMetaObject().getCrmUserId(), cacheObject.getScribeMetaObject().getCrmPassword(), cacheObject
            .getScribeMetaObject().getCrmAccountId(), clientInfo.getWebservicesDomain());

    /* Set this stub in agent */
    cacheObject.setSoapStub(soapBindingStub);

    /* Set service URL */
    if (cacheObject.getScribeMetaObject().getCrmServiceURL() == null) {

      cacheObject.getScribeMetaObject().setCrmServiceURL(clientInfo.getSystemDomain());
      cacheObject.getScribeMetaObject().setCrmServiceProtocol(null);
    }

    /* Save this freshly updated agent in session cache */
    cRMSessionCache.admit(crmUserId, cacheObject);

    /* If everything is fine return true */
    return true;
  }

  @Override
  public final boolean reset(final String crmUserId, final String crmPassword) throws Exception {

    logger.debug("---Inside reset crmUserId: " + crmUserId);

    /* Check if session is already available at cache */
    return this.login(crmUserId, crmPassword);
  }

  public final void setNetSuiteSOAPClient(final NetSuiteSOAPClient netSuiteSOAPClient) {
    this.netSuiteSOAPClient = netSuiteSOAPClient;
  }

  public final CRMSessionCache getcRMSessionCache() {
    return cRMSessionCache;
  }

  public final void setcRMSessionCache(final CRMSessionCache cRMSessionCache) {
    this.cRMSessionCache = cRMSessionCache;
  }

  public final String getCrmUserIdIdSplitCharacter() {
    return agentIdSplitCharacter;
  }

  public final void setCrmUserIdIdSplitCharacter(final String agentIdSplitCharacter) {
    this.agentIdSplitCharacter = agentIdSplitCharacter;
  }

  /**
   * @return the clientInfoProvidor
   */
  public final NSCRMV2k9ClientInfoProvidor getClientInfoProvidor() {
    return this.clientInfoProvidor;
  }

  /**
   * @param clientInfoProvidor the clientInfoProvidor to set
   */
  public final void setClientInfoProvidor(final NSCRMV2k9ClientInfoProvidor clientInfoProvidor) {
    this.clientInfoProvidor = clientInfoProvidor;
  }
}
