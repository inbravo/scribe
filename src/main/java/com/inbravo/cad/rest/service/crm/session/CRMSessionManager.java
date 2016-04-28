package com.inbravo.cad.rest.service.crm.session;

import com.inbravo.cad.internal.service.dto.BasicObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface CRMSessionManager {

  boolean reset(final String crmUserId, final String crmPassword) throws Exception;

  boolean login(final String crmUserId, final String crmPassword) throws Exception;

  BasicObject getSessionInfo(final String id) throws Exception;
}
