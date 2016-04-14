package com.inbravo.cad.rest.service.crm;

import com.inbravo.cad.rest.service.basic.CADService;

/**
 * 
 * @author amit.dixit
 * 
 */
public abstract class CRMService implements CADService {

  /**
   * Common validation logic for all the methods
   * 
   * @param agentId
   * @param tenant
   * @return
   * @throws CADException
   */
  public final String getTenantId(final String agentId, final String splitString) {
    final String[] agentIdArray = agentId.split(splitString);
    return agentIdArray[0];
  }
}
