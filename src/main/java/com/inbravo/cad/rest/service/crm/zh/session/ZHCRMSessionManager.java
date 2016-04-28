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

  @SuppressWarnings("unused")
  @Override
  public final BasicObject getSessionInfo(final String id) throws Exception {
    logger.debug("---Inside getSessionInfo, id: " + id);

    /* Check if session is already available at cache */
    final CADUser user = (BasicObject) cRMSessionCache.recover(id);

    /* Check if basic object is null or not */
    if (user == null) {


      logger.debug("---Inside getSessionInfo, agent: " + id);

      /* Validate crm service params */
      this.validateUser(user);

      /* Get session information from ZH */
      final String sessionId =
          zHAuthManager.getSessionId(agent.getCrmUserId(), agent.getCrmPassword(), agent.getCrmServiceURL(), agent.getCrmServiceProtocol());

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


  private final void validateUser(final CADUser user) {

    /* Service URL is a must for ZH */
    if (user.getCrmServiceURL() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service URL");
    }

    /* Service protocol is a must for ZH */
    if (user.getCrmServiceProtocol() == null) {
      throw new CADException(CADResponseCodes._1009 + "CRM integration information is missing: CRM service Protocol");
    }
  }
}
