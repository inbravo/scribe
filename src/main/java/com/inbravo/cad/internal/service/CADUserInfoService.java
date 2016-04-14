package com.inbravo.cad.internal.service;

import com.inbravo.cad.internal.service.dto.CADUser;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface CADUserInfoService {

  CADUser getInfo(String id) throws Exception;
}
