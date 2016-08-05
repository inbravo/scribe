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

package com.inbravo.cad.rest.service.crm.ns.session;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.dto.BasicObject;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.cad.rest.service.crm.ns.NetSuiteSOAPClient;
import com.inbravo.cad.rest.service.crm.ns.v2k9.NSCRMV2k9ClientInfoProvidor;
import com.inbravo.cad.rest.service.crm.ns.v2k9.NSCRMV2k9ClientInfoProvidor.NSCRMV2k9ClientInfo;
import com.inbravo.cad.rest.service.crm.session.CRMSessionManager;
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
    final CADUser user = (CADUser) cRMSessionCache.recover(crmUserId);

    /* This code block will be usefull if cache size limit is reached */
    if (user == null) {

      logger.debug("---Inside getSoapBindingStub, user not found in cache hence going for fresh fetch. Seems like cache limit is reached");

    } else {
      logger.debug("---Inside getSoapBindingStub, user is found in cache");
    }

    NetSuiteBindingStub soapBindingStub = null;

    /* Get SOAP stub */
    if (user.getSoapStub() != null) {

      logger.debug("---Inside getSoapBindingStub, using stub from cache");

      /* Get stub from cache */
      soapBindingStub = (NetSuiteBindingStub) user.getSoapStub();
    } else {

      /* Get service URL information */
      final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(user.getCrmUserId(), user.getCrmPassword());

      logger.debug("---Inside getSoapBindingStub, creating fresh stub, client info: " + clientInfo);

      /* TODO : Login at NetSuite : pass role id as 3 for admin role */
      soapBindingStub =
          netSuiteSOAPClient.login(user.getCrmUserId(), user.getCrmPassword(), user.getCrmAccountId(), clientInfo.getWebservicesDomain());

      /* Set this stub in agent */
      user.setSoapStub(soapBindingStub);
    }

    /* Re-admit this agent with CRM session information */
    cRMSessionCache.admit(crmUserId, user);

    return soapBindingStub;
  }

  @Override
  public final BasicObject getSessionInfo(final String crmUserId) throws Exception {

    logger.debug("---Inside getSessionInfo id: " + crmUserId);

    /* Check if session is already available at cache */
    final BasicObject basicObject = (BasicObject) cRMSessionCache.recover(crmUserId);

    /* Check the type of user */
    if (basicObject instanceof CADUser) {

      logger.debug("---Inside getSessionInfo agent: " + crmUserId);

      /* Get agent information from cache */
      final CADUser agent = (CADUser) basicObject;

      /* Get service URL information */
      final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(agent.getCrmUserId(), agent.getCrmPassword());

      logger.debug("---Inside getSessionInfo, clientInfo: " + clientInfo);

      /* Login at NetSuite : pass role id as 3 for admin role */
      final NetSuiteBindingStub soapBindingStub =
          netSuiteSOAPClient.login(agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmAccountId(), clientInfo.getWebservicesDomain());

      /* Set this stub in agent */
      agent.setSoapStub(soapBindingStub);

      /* Set service URL */
      if (agent.getCrmServiceURL() == null) {

        agent.setCrmServiceURL(clientInfo.getSystemDomain());
        agent.setCrmServiceProtocol(null);
      }

      /* Save this freshly updated agent in session cache */
      cRMSessionCache.admit(crmUserId, agent);

      /* If everything is fine return true */
      return agent;
    } else {
      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1008 + " User information is not valid");
    }
  }


  @Override
  public final boolean login(final String crmUserId, final String crmPassword) throws Exception {

    logger.debug("---Inside login crmUserId: " + crmUserId);

    /* Check if session is already available at cache */
    final CADUser user = (CADUser) cRMSessionCache.recover(crmUserId);

    /* Get service URL information */
    final NSCRMV2k9ClientInfo clientInfo = clientInfoProvidor.getNSCRMV2k9ClientInfo(user.getCrmUserId(), user.getCrmPassword());

    logger.debug("---Inside login clientInfo: " + clientInfo);

    /* Login at NetSuite : pass role id as 3 for admin role */
    final NetSuiteBindingStub soapBindingStub =
        netSuiteSOAPClient.login(user.getCrmUserId(), user.getCrmPassword(), user.getCrmAccountId(), clientInfo.getWebservicesDomain());

    /* Set this stub in agent */
    user.setSoapStub(soapBindingStub);

    /* Set service URL */
    if (user.getCrmServiceURL() == null) {

      user.setCrmServiceURL(clientInfo.getSystemDomain());
      user.setCrmServiceProtocol(null);
    }

    /* Save this freshly updated agent in session cache */
    cRMSessionCache.admit(crmUserId, user);

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
