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

package com.inbravo.scribe.rest.service.crm.ms.auth;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.internal.service.dto.CADUser;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMMessageFormatUtils;
import com.inbravo.scribe.rest.service.crm.ms.MSCRMSchemaConstants;

/**
 * 
 * @author amit.dixit
 * 
 */
public abstract class MSAuthManager implements ApplicationContextAware {

  private final Logger logger = Logger.getLogger(MSAuthManager.class.getName());

  private ApplicationContext applicationContext;

  protected abstract String[] getCRMAuthToken(final CADUser cadUser) throws Exception;

  /**
   * This API is required for distinguishing login type e.g. Office 365 or Live Id
   * 
   * @param cADCommandObject
   * @return
   */
  public final synchronized Map<String, String> getMSCRMOrganizationInfo(final String serviceURL, final String serviceProtocol) throws Exception {

    logger.debug("---Inside getMSCRMOrganizationInfo, serviceURL: " + serviceURL + " & serviceProtocol : " + serviceProtocol);
    try {
      /* Get SOAP executer */
      final SOAPExecutor executor = (SOAPExecutor) getApplicationContext().getBean("sOAPExecutor");

      /* Validate CRM URL */
      if (serviceURL == null && "".equals(serviceURL)) {

        /* Send user error */
        throw new CADException(CADResponseCodes._1008 + "CRM integration information is missing: MS CRM service URL");
      }

      logger.debug("---Inside getMSCRMOrganizationInfo, calling WSDL serviceURL: " + serviceURL);

      /* Call CRM service URL to check desired authentication type */
      final String WSDL = executor.callHttpURL(serviceProtocol.trim() + "://" + serviceURL.trim() + SOAPExecutor.CRM_ENDPOINT_V5 + "?wsdl");

      /* Read the response and get all relevant data */
      final String WSDLImportURL =
          MSCRMMessageFormatUtils.getValueFromXML(WSDL, MSCRMSchemaConstants.XPATH_Constants.XPATH_WSDL_IMPORT_IN_MS_RESPONSE);

      logger.debug("---Inside getMSCRMOrganizationInfo, calling WSDL import serviceURL: " + WSDLImportURL);

      /* Call HTTP URL */
      final String wsdlImport = executor.callHttpURL(WSDLImportURL);

      /* Parse the reponse */
      final String urnAddress =
          MSCRMMessageFormatUtils.getValueFromXML(wsdlImport, MSCRMSchemaConstants.XPATH_Constants.XPATH_URN_ADDRESS_IN_MS_RESPONSE);

      /* STS endpoint */
      final String stsEnpoint =
          MSCRMMessageFormatUtils.getValueFromXML(wsdlImport, MSCRMSchemaConstants.XPATH_Constants.XPATH_STE_ENDPOINT_IN_MS_RESPONSE);

      logger.debug("---Inside getmSCRMObjectService, WSDL: " + WSDL + " wsdlImport: " + wsdlImport + ", & urnAddress: " + urnAddress
          + ", & stsEnpoint: " + stsEnpoint);

      /* Create additonal information map for next level usage */
      final Map<String, String> nodeMap = new HashMap<String, String>();

      /* Update map */
      nodeMap.put("URNAddress", urnAddress);
      nodeMap.put("STSEnpoint", stsEnpoint);

      return nodeMap;

    } catch (final Exception e) {

      /* Inform user about absent header value */
      throw new CADException(CADResponseCodes._1012 + "MS CRM organization URL is not working: " + serviceProtocol.trim() + "://" + serviceURL.trim()
          + SOAPExecutor.CRM_ENDPOINT_V5 + "?wsdl", e);
    }
  }

  @Override
  public final void setApplicationContext(final ApplicationContext ApplicationContext) throws BeansException {
    this.applicationContext = ApplicationContext;
  }

  public final ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
