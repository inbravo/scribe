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
