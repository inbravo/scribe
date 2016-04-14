package com.inbravo.cad.rest.service.basic;

import org.apache.log4j.Logger;

import com.inbravo.cad.internal.service.factory.CADServiceFactory;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.service.crm.factory.CRMServiceFactory;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CADInternalService {

  private final Logger logger = Logger.getLogger(CADInternalService.class.getName());

  private CRMServiceFactory cRMServiceFactory;

  public final CADServiceFactory getServiceFactory(final CADCommandObject eDSACommandObject) throws Exception {

    logger.debug("---Inside getServiceFactory, Object type: " + eDSACommandObject.getObjectType() + ", providing CRM service");

    /* Return CRM service factory */
    return cRMServiceFactory;
  }

  public final CRMServiceFactory getcRMServiceFactory() {
    return cRMServiceFactory;
  }

  public final void setcRMServiceFactory(final CRMServiceFactory cRMServiceFactory) {
    this.cRMServiceFactory = cRMServiceFactory;
  }
}
