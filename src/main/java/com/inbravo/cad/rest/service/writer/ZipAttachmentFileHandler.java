package com.inbravo.cad.rest.service.writer;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.springframework.aop.target.CommonsPoolTargetSource;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.service.msg.type.FileAttachments;
import com.inbravo.cad.rest.service.writer.utils.ZipWriter;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZipAttachmentFileHandler {

  private final Logger logger = Logger.getLogger(ZipAttachmentFileHandler.class.getName());

  /* Object pool */
  private CommonsPoolTargetSource pool;

  public final void handle(final OutputStream outputStream, final FileAttachments fileAttachments, final String zipName) throws Exception {

    logger.debug("---Inside handle, zipName: " + zipName);

    /* Get writer bean from pool */
    ZipWriter zWriter = null;
    try {

      /* Get zip writer bean from pool */
      zWriter = (ZipWriter) pool.getTarget();

      if (zWriter != null) {

        /* Create zip file */
        zWriter.write(outputStream, fileAttachments, zipName);
      } else {
        /* Inform user about error */
        throw new CADException(CADResponseCodes._1023 + "Server file generation limit has reached");
      }

    } catch (final Exception e) {

      if (e.getMessage().contains(CADResponseCodes._1023)) {
        throw new CADException(CADResponseCodes._1023 + "Server file generation limit has reached");
      } else {
        throw new CADException(CADResponseCodes._1000 + "Problem while file attachment service");
      }
    } finally {

      try {
        /* Finally release the zip writer object */
        pool.releaseTarget(zWriter);
        zWriter = null;
      } catch (final Exception e) {
        throw new CADException(CADResponseCodes._1000 + "Problem while file attachment service");
      }
    }
  }

  /**
   * @return the pool
   */
  public final CommonsPoolTargetSource getPool() {
    return this.pool;
  }

  /**
   * @param pool the pool to set
   */
  public final void setPool(final CommonsPoolTargetSource pool) {
    this.pool = pool;
  }
}
