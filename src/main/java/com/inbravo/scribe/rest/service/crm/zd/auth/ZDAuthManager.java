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

package com.inbravo.scribe.rest.service.crm.zd.auth;

import java.util.Map;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface ZDAuthManager {

  String getSessionId(final String userId, final String password, final String crmURL, final String crmProtocol, final String port) throws Exception;

  boolean login(final String userId, final String password, final String crmURL, final String crmProtocol, final String port) throws Exception;

  Map<String, String> getSessionInfoAfterValidLogin(final String userId, final String password, final String crmURL, final String crmProtocol,
      final String port) throws Exception;

  default int validateCrmPort(final String crmPort) {

    try {

      return Integer.parseInt(crmPort);
    } catch (final NumberFormatException e) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1003 + "CRM integration information is invalid: CRM Port");
    }
  }
}
