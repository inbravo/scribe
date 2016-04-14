package com.inbravo.cad.rest.service.crm.zd.auth;

import java.util.Map;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface ZDAuthManager {

  String getSessionId(final String userId, final String password, final String crmURL, final String crmProtocol, final int port) throws Exception;

  boolean login(final String userId, final String password, final String crmURL, final String crmProtocol, final int port) throws Exception;

  Map<String, String> getSessionInfoAfterValidLogin(final String userId, final String password, final String crmURL, final String crmProtocol,
      final int port) throws Exception;
}
