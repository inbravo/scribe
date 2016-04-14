package com.inbravo.cad.rest.service.crm.zh.auth;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface ZHAuthManager {

  String getSessionId(final String userId, final String password, final String crmURL, final String crmProtocol) throws Exception;

  boolean login(final String userId, final String password, final String crmURL, final String crmProtocol) throws Exception;
}
