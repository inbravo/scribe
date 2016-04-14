package com.inbravo.cad.rest.service.crm.zh.session;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.internal.service.CADUserInfoService;
import com.inbravo.cad.internal.service.SuperUserInfoService;
import com.inbravo.cad.internal.service.dto.CADUser;
import com.inbravo.cad.internal.service.dto.BasicObject;
import com.inbravo.cad.internal.service.dto.Tenant;
import com.inbravo.cad.rest.service.crm.cache.CRMSessionCache;
import com.inbravo.cad.rest.service.crm.session.CRMSessionManager;
import com.inbravo.cad.rest.service.crm.zh.auth.ZHAuthManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZHCRMSessionManager implements CRMSessionManager {

  private final Logger logger = Logger.getLogger(ZHCRMSessionManager.class.getName());

  /* Authentication manager */
  private ZHAuthManager zHAuthManager;

  /* Agent id special character */
  private String agentIdSplitCharacter;

  /* CRM session cache */
  private CRMSessionCache cRMSessionCache;

  /* Agent information service */
  private CADUserInfoService agentInfoService;

  @SuppressWarnings("unused")
  @Override
  public final BasicObject getSessionInfo(final String id) throws Exception {
    logger.debug("---Inside getSessionInfo, id: " + id);

    /* Check if session is already available at cache */
    final BasicObject basicObject = (BasicObject) cRMSessionCache.recover(id);

    /* Check if basic object is null or not */
    if (basicObject == null) {


      logger.debug("---Inside getSessionInfo, agent: " + id);

      /* Get agent information from LDAP(CRM info is stored at LDAP) */
      CADUser agent = agentInfoService.getAgentInformation(id);

      /* Check if agent is not null */
      if (agent == null) {

        /* Inform user about unauthorized agent */
        throw new CADException(CADResponseCodes._1012 + "Agent information is not found");
      }

      /* Validate crm service params */
      this.validateUser(agent);

      /* Get session information from ZH */
      final String sessionId =
          zHAuthManager.getSessionId(agent.getCrmUserid(), agent.getCrmPassword(), agent.getCrmServiceURL(), agent.getCrmServiceProtocol());

      if (sessionId != null) {

        /* Set CRM API token information */
        agent.setCrmSessionId(sessionId);

        /* Save this agent in session cache */
        cRMSessionCache.admit(id, agent);

        /* If everything is fine return true */
        return agent;
      } else {
        /* Inform user about absent header value */
        throw new CADException(CADResponseCodes._1012 + "Login attempt at ZH is failed. Check credentials.");
      }

    } else {

      /* Check the type of user */
      if (basicObject instanceof CADUser) {

        logger.debug("---Inside getSessionInfo, agent from cache: " + id);

        /* Get agent information from cache */
        CADUser agent = (CADUser) basicObject;

        if (agent == null) {

          /* Get fresh agent from LDAP */
          agent = agentInfoService.getAgentInformation(id);

          /* If tenant is still not found; tenant is not valid */
          if (agent == null) {

            /* Inform user about unauthorized agent */
            throw new CADException(CADResponseCodes._1012 + "Agent information is not found");
          }
        }

        /* Validate crm service params */
        this.validateUser(agent);

        /* If not a batch request */
        if (agent.getCrmSessionId() == null) {

          /* Get session information from ZH */
          final String sessionId =
              zHAuthManager.getSessionId(agent.getCrmUserid(), agent.getCrmPassword(), agent.getCrmServiceURL(), agent.getCrmServiceProtocol());

          /* If session id is found */
          if (sessionId != null) {

            /* Set CRM API token information */
            agent.setCrmSessionId(sessionId);

            /* Save this agent in session cache */
            cRMSessionCache.admit(id, agent);

            /* If everything is fine return true */
            return agent;
          } else {
            /* Inform user about absent header value */
            throw new CADException(CADResponseCodes._1012 + "Login attempt at ZH is failed. Check credentials.");
          }
        } else {

          /* Return agent from cache */
          return agent;
        }

      } else {
        /* Inform user about absent header value */
        throw new CADException(CADResponseCodes._1008 + "Agent/Tenant information is not valid");
      }
    }
  }

  @Override
  public final boolean login(final String id) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the EDSA");
  }

  @Override
  public final boolean reset(final String id) throws Exception {
    throw new CADException(CADResponseCodes._1003 + "Following operation is not supported by the EDSA");
  }

  public final ZHAuthManager getzHAuthManager() {
    return zHAuthManager;
  }

  public final void setzHAuthManager(final ZHAuthManager zHAuthManager) {
    this.zHAuthManager = zHAuthManager;
  }

  public final String getAgentIdSplitCharacter() {
    return agentIdSplitCharacter;
  }

  public final void setAgentIdSplitCharacter(final String agentIdSplitCharacter) {
    this.agentIdSplitCharacter = agentIdSplitCharacter;
  }

  public final CRMSessionCache getcRMSessionCache() {
    return cRMSessionCache;
  }

  public final void setcRMSessionCache(final CRMSessionCache cRMSessionCache) {
    this.cRMSessionCache = cRMSessionCache;
  }

  public CADUserInfoService getAgentInfoService() {
    return agentInfoService;
  }

  public final void setAgentInfoService(final CADUserInfoService agentInfoService) {
    this.agentInfoService = agentInfoService;
  }


  private final void validateUser(final CADUser agent) {

    /* Service URL is a must for ZH */
    if (agent.getCrmServiceURL() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for ZH */
    if (agent.getCrmServiceProtocol() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service Protocol");
    }
  }
}
