package com.inbravo.cad.internal.service.factory;

import com.inbravo.cad.rest.constants.CRMConstants.UserType;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.service.basic.CADService;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface CADServiceFactory {

  public CADService getService(final CADCommandObject eDSACommandObject, final UserType userType) throws Exception;
}
