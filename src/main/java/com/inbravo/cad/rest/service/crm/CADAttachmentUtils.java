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
  public static final FileAttachments createFileAttachments(final CADCommandObject eDSACommandObject) throws SQLException {

    /* Create attachment list */
    final List<FileAttachment> attachments = new ArrayList<FileAttachment>();

    /* If attachment request */
    if (eDSACommandObject.geteDSAObject() != null && eDSACommandObject.geteDSAObject().length != 0) {

      logger.debug("---Inside getObjects, no. of objects in response: " + eDSACommandObject.geteDSAObject().length);

      /* Get object array */
      final CADObject[] eDSAObjectArr = eDSACommandObject.geteDSAObject();

      /* Iterate over all objects */
      for (final CADObject eDSAObject : eDSAObjectArr) {

        /* Get object content */
        final List<Object> objectList = eDSAObject.getObjectContent();

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
      throw new CADException(CADResponseCodes._1004 + eDSACommandObject.getObjectType());
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
