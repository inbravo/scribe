/*
 * MIT License
 * 
 * Copyright (c) 2016 Amit Dixit (github.com/inbravo)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

  public final CADServiceFactory getServiceFactory(final CADCommandObject cADCommandObject) throws Exception {

    logger.debug("---Inside getServiceFactory, Object type: " + cADCommandObject.getObjectType() + ", providing CRM service");

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
