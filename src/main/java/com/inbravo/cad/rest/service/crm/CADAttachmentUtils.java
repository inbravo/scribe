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

package com.inbravo.cad.rest.service.crm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.resource.CADObject;
import com.inbravo.cad.rest.service.msg.type.FileAttachment;
import com.inbravo.cad.rest.service.msg.type.FileAttachments;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class CADAttachmentUtils {

  private static final Logger logger = Logger.getLogger(CADAttachmentUtils.class.getName());

  /**
   * 
   * @return
   * @throws SQLException
   */
  public static final FileAttachments createFileAttachments(final CADCommandObject cADCommandObject) throws SQLException {

    /* Create attachment list */
    final List<FileAttachment> attachments = new ArrayList<FileAttachment>();

    /* If attachment request */
    if (cADCommandObject.getcADObject() != null && cADCommandObject.getcADObject().length != 0) {

      logger.debug("---Inside getObjects, no. of objects in response: " + cADCommandObject.getcADObject().length);

      /* Get object array */
      final CADObject[] cADbjectArr = cADCommandObject.getcADObject();

      /* Iterate over all objects */
      for (final CADObject cADbject : cADbjectArr) {

        /* Get object content */
        final List<Object> objectList = cADbject.getObjectContent();

        /* If valid list */
        if (objectList != null && objectList.size() != 0) {

          /* Iterate over all objects */
          for (final Object sbObject : objectList) {

            /* Get attachment object */
            if (sbObject instanceof FileAttachment) {

              /* Type case the attachment class */
              final FileAttachment attachment = (FileAttachment) sbObject;

              /* Add all attachments */
              attachments.add(attachment);
            }
          }
        }
      }
    } else {
      /* Inform user that object not found */
      throw new CADException(CADResponseCodes._1004 + cADCommandObject.getObjectType());
    }

    /* Check if attachments are found */
    if (attachments.size() >= 1) {

      /* Create new FileAttachments */
      return new FileAttachments(attachments);
    } else {
      /* Inform user that object not found */
      throw new CADException(CADResponseCodes._1004 + "Attachment file");
    }
  }
}
