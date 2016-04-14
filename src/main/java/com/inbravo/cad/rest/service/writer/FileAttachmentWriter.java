package com.inbravo.cad.rest.service.writer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADExceptionMapper;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.constants.HTTPConstants;
import com.inbravo.cad.rest.service.msg.type.FileAttachment;

/**
 * 
 * @author amit.dixit
 * 
 */
@Provider
@Produces({HTTPConstants.mimeTypeOctetStream})
public final class FileAttachmentWriter implements MessageBodyWriter<FileAttachment> {

  private final Logger logger = Logger.getLogger(FileAttachmentWriter.class.getName());

  @Override
  public final long getSize(final FileAttachment fileAttachment, final Class<?> classObject, final Type type, final Annotation[] annotation,
      final MediaType mediaType) {
    return -1;
  }

  @Override
  public final boolean isWriteable(final Class<?> classObject, final Type type, final Annotation[] annotations, final MediaType mediaType) {

    return true;
  }

  @Override
  public final void writeTo(final FileAttachment fileAttachment, final Class<?> classObject, final Type type, final Annotation[] annotations,
      final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaderMap, final OutputStream outputStream) throws IOException {

    logger.debug("---Inside writeTo, object type: " + classObject.getName() + " & mediaType: " + mediaType.getType() + " & file : " + fileAttachment);

    final DataOutputStream binaryContentWriter = new DataOutputStream(outputStream);

    /* File input stream */
    MappedByteBuffer mbb = null;
    RandomAccessFile raf = null;
    File tempFile = null;
    try {

      /* Create header list */
      final List<Object> headerInfo = new ArrayList<Object>();

      /* If response object is not null */
      if (fileAttachment != null) {

        logger.debug("writeTo, reading temp local file : " + fileAttachment.getLocation());

        /* Add attachment info in header */
        headerInfo.add("attachment; filename=\"" + URLEncoder.encode(fileAttachment.getId(), "UTF-8") + "\"");

        /* Update header map for file name */
        httpHeaderMap.put("Content-Disposition", headerInfo);

        /* Get file from temp location */
        tempFile = new File(fileAttachment.getLocation());

        /* Create random access file on it */
        raf = new RandomAccessFile(tempFile, "rw");

        /* Map the file into memory for reading only */
        mbb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileAttachment.getSizeInBytes());

        /* Get byte from file */
        for (int i = 0; i < fileAttachment.getSizeInBytes(); i++) {

          /* Write the content on output stream */
          binaryContentWriter.write(mbb.get(i));
        }

        /* Flush/close the writer */
        if (binaryContentWriter != null) {

          binaryContentWriter.flush();
          binaryContentWriter.close();
        }
      } else {

        /* Write the content */
        binaryContentWriter.writeBytes(CADExceptionMapper.getErrorXML(CADResponseCodes._1004 + "No file found"));
      }

    } catch (final Exception e) {
      logger.error("---Inside writeTo, problem while writing file output: " + e, e);

      /* Delete all temp files */
      if (fileAttachment != null) {

        /* Delete all temp files */
        this.startDeleteTempFileThread(fileAttachment);
      }

      /* Write the content */
      binaryContentWriter.writeBytes(CADExceptionMapper.getErrorXML(CADResponseCodes._1001 + "Problem in generating attachment file"));

    } finally {

      /* Check if any exception at finally */
      try {

        /* Flush/close the writer */
        if (binaryContentWriter != null) {

          binaryContentWriter.flush();
          binaryContentWriter.close();
        }
        /* Clear the buffer */
        if (mbb != null) {
          mbb = null;
        }
        /* Close the file */
        if (raf != null) {
          raf.close();
          raf = null;
        }
        /* Close output stream */
        if (outputStream != null) {
          outputStream.flush();
          outputStream.close();
        }
        /* Delete all temp files */
        if (fileAttachment != null) {

          /* Delete all temp files */
          this.startDeleteTempFileThread(fileAttachment);
        }
      } catch (final Exception e) {

        /* Delete all temp files */
        if (fileAttachment != null) {

          /* Delete all temp files */
          this.startDeleteTempFileThread(fileAttachment);
        }
      }
    }
  }

  /**
   * 
   * @param fileAttachments
   */
  private final void startDeleteTempFileThread(final FileAttachment fileAttachment) {

    /* Spawn a thread to delete the files */
    new Thread() {

      public void run() {

        try {

          /* If attachment info is available */
          if (fileAttachment != null && fileAttachment.getLocation() != null) {

            final File fileToBeDeleted = new File(fileAttachment.getLocation());

            if (fileToBeDeleted != null && fileToBeDeleted.exists()) {

              logger.debug("---Inside deleteTempFileThread, deleting temp file : " + fileAttachment.getLocation() + ", status: "
                  + fileToBeDeleted.delete());
            }
          }

        } catch (final Exception e) {
          throw new CADException(CADResponseCodes._1000 + "Problem in deleting temporary files" + e, e);
        }
      }
    }.run();
  }
}
