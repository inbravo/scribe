package com.inbravo.cad.rest.service.crm.session;

import com.inbravo.cad.internal.service.dto.BasicObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface CRMSessionManager {

  boolean reset(final String id) throws Exception;

  boolean login(final String id) throws Exception;

  BasicObject getSessionInfo(final String id) throws Exception;
}
