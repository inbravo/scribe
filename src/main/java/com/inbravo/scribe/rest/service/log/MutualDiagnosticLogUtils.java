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

package com.inbravo.scribe.rest.service.log;

import org.apache.log4j.MDC;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MutualDiagnosticLogUtils {

  private static final String cadTransIdConst = "CADTID";

  private static final String extTransIdConst = "EXTTID";

  private static final String userIdConst = "USERID";

  /**
   * This API is used for adding trasaction information in logs
   * 
   * @param cadTransId
   * @param extTransId
   * @param userId
   */
  public final void addTransactionInfo(final String cadTransId, final String extTransId, final String userId) {

    /* If CAD transaction id is not null */
    if (cadTransId != null) {

      /* Add CAD transaction id information in MDC */
      MDC.put(cadTransIdConst, cadTransId);
    }

    /* If external transaction id is not null */
    if (extTransId != null) {

      /* Add external transaction id information in MDC */
      MDC.put(extTransIdConst, extTransId);
    }

    /* If user id is not null */
    if (userId != null) {

      /* Add user id information in MDC */
      MDC.put(userIdConst, userId);
    }
  }

  /**
   * This API is used for removing trasaction information from logs
   */
  public final void discardTransactionInfo() {

    /* Remove CAD transaction id information in MDC */
    MDC.remove(cadTransIdConst);

    /* Remove external transaction id inform in MDC */
    MDC.remove(extTransIdConst);

    /* Remove user id inform in MDC */
    MDC.remove(userIdConst);
  }
}
