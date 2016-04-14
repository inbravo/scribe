package com.inbravo.cad.rest.service.writer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.constants.HTTPConstants;
import com.inbravo.cad.rest.service.msg.type.FileAttachment;
import com.inbravo.cad.rest.service.msg.type.FileAttachments;

/**
 * 
 * @author amit.dixit
 * 
 */
@Provider
@Produces({HTTPConstants.mimeTypeOctetStream})
public final class FileAttachmentsWriter implements MessageBodyWriter<FileAttachments> {

  private final Logger logger = Logger.getLogger(FileAttachmentsWriter.class.getName());

  private String zipFileName = "attachments.zip";

  private ZipAttachmentFileHandler handler;

  @Override
  public final long getSize(final FileAttachments fileAttachments, final Class<?> classObject, final Type type, final Annotation[] annotation,
      final MediaType mediaType) {

    return -1;
  }

  @Override
  public final boolean isWriteable(final Class<?> classObject, final Type type, final Annotation[] annotations, final MediaType mediaType) {

    return true;
  }

  @Override
  public final void writeTo(final FileAttachments fileAttachments, final Class<?> classObject, final Type type, final Annotation[] annotations,
      final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaderMap, final OutputStream outputStream) throws IOException {

    logger.debug("---Inside writeTo, object type: " + classObject.getName() + " & mediaType: " + mediaType.getType());

    /* Create output stream writer */
    final OutputStreamWriter binaryContentWriter = new OutputStreamWriter(outputStream);
    try {

      /* Create header list */
      final List<Object> objectList = new ArrayList<Object>();

      /* If file attachments is not null */
      if (fileAttachments != null && fileAttachments.getFileAttachments() != null) {

        /* Add attachment info in header */
        objectList.add("attachment;filename=" + zipFileName);

        /* Update header map */
        httpHeaderMap.put("Content-Disposition", objectList);

        /* Create the file using handler */
        handler.handle(outputStream, fileAttachments, this.zipFileName);
      } else {

        /* Inform user about the error */
        binaryContentWriter.write(CADResponseCodes._1004 + "No attachment records found");
      }

    } catch (final Exception e) {
      logger.error("---Inside writeTo, problem while writing zip output: " + e, e);

      /* Delete all temp files */
      if (fileAttachments != null) {
        this.startDeleteTempFileThread(fileAttachments);
      }

      /* Write the content */
      binaryContentWriter.write(CADResponseCodes._1001 + "Problem in writing zip attachment" + e);

    } finally {
      try {
        /* Flush/close the writer */
        if (binaryContentWriter != null) {
          binaryContentWriter.flush();
          binaryContentWriter.close();
        }
        /* Close output stream */
        if (outputStream != null) {
          outputStream.flush();
          outputStream.close();
        }
        /* Delete all temp files */
        if (fileAttachments != null) {
          this.startDeleteTempFileThread(fileAttachments);
        }
      } catch (final Exception e) {
        logger.error("---Inside writeTo, problem while writing zip output: " + e);

        /* Delete all temp files */
        if (fileAttachments != null) {
          this.startDeleteTempFileThread(fileAttachments);
        }
      }
    }
  }

  /**
   * @return the handler
   */
  public final ZipAttachmentFileHandler getHandler() {
    return this.handler;
  }

  /**
   * @param handler the handler to set
   */
  public final void setHandler(final ZipAttachmentFileHandler handler) {
    this.handler = handler;
  }

  /**
   * @return the zipFileName
   */
  public final String getZipFileName() {
    return this.zipFileName;
  }

  /**
   * @param zipFileName the zipFileName to set
   */
  public final void setZipFileName(final String zipFileName) {
    this.zipFileName = zipFileName;
  }

  /**
   * 
   * @param fileAttachments
   */
  private final void startDeleteTempFileThread(final FileAttachments fileAttachments) {

    /* Spawn a thread to delete the files */
    new Thread() {
      public void run() {

        try {

          /* Run over all attachments */
          for (final FileAttachment fa : fileAttachments.getFileAttachments()) {

            final File fileToBeDeleted = new File(fa.getLocation());

            if (fileToBeDeleted != null && fileToBeDeleted.exists()) {

              logger.debug("---Inside deleteTempFileThread, deleting temp file : " + fa.getLocation() + ", status: " + fileToBeDeleted.delete());
            }
          }
        } catch (final Exception e) {
          throw new CADException(CADResponseCodes._1000 + "Problem in deleting temporary files", e);
        }
      }
    }.run();
  }

  @Override
  protected final void finalize() throws Throwable {
    handler = null;
    zipFileName = null;
    super.finalize();
  }
}
