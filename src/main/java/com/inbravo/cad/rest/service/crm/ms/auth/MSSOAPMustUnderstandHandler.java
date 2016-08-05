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

package com.inbravo.cad.rest.service.crm.ms.auth;

import java.util.Iterator;

import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.log4j.Logger;

/**
 * This is a mandatory class for applying security header checks in response from MS CRm
 * 
 * @author amit.dixit
 * 
 */
public final class MSSOAPMustUnderstandHandler extends AbstractHandler {

  private final Logger logger = Logger.getLogger(MSSOAPMustUnderstandHandler.class.getName());

  public MSSOAPMustUnderstandHandler() {}

  /**
   * 
   * Process the security block from the message context header.
   */
  public final InvocationResponse invoke(final MessageContext msgContext) throws AxisFault {

    /* Get header */
    final SOAPHeader header = msgContext.getEnvelope().getHeader();

    if (header != null) {
      final Iterator<?> blocks = header.examineAllHeaderBlocks();

      while (blocks.hasNext()) {

        /* Get header block */
        final SOAPHeaderBlock block = (SOAPHeaderBlock) blocks.next();

        if (block != null) {

          /* Check for security header block */
          if (block.getLocalName().equalsIgnoreCase("Security") || block.getLocalName().equalsIgnoreCase("Action")
              || block.getLocalName().equalsIgnoreCase("To")) {

            logger.debug("---Inside invoke; found '" + block.getLocalName() + "' header. Marking it processed");

            /* Mark it processed to avoid exception at client side */
            block.setProcessed();
          }
        }
      }
    }

    return InvocationResponse.CONTINUE;
  }
}
