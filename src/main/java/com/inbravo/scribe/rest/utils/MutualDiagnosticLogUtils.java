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

package com.inbravo.scribe.rest.utils;

import org.apache.log4j.MDC;

import com.inbravo.scribe.rest.resource.ScribeCommandObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MutualDiagnosticLogUtils {

  private static final String intTransIdConst = "INT";

  private static final String extTransIdConst = "EXT";

  private boolean enabled = true;


  /**
   * This API is used for adding trasaction information in logs
   * 
   * @param cadTransId
   * @param extTransId
   * @param userId
   */
  public final void addTransactionInfo(final ScribeCommandObject cADCommandObject) {

    if (enabled) {

      /* If Scribe transaction id is not null */
      if (cADCommandObject.getIntTansId() != null) {

        /* Add internal transaction id information in MDC */
        MDC.put(intTransIdConst, cADCommandObject.getIntTansId());
      }

      /* If external transaction id is not null */
      if (cADCommandObject.getExtTransId() != null) {

        /* Add external transaction id information in MDC */
        MDC.put(extTransIdConst, cADCommandObject.getExtTransId());
      }
    }
  }

  /**
   * This API is used for removing trasaction information from logs
   */
  public final void discardTransactionInfo() {

    if (enabled) {

      /* Remove internal transaction id information in MDC */
      MDC.remove(intTransIdConst);

      /* Remove external transaction id inform in MDC */
      MDC.remove(extTransIdConst);
    }
  }

  public final boolean isEnabled() {
    return enabled;
  }

  public final void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }
}
