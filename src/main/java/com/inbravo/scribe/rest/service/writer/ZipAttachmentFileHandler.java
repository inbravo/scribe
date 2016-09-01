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

package com.inbravo.scribe.rest.service.writer;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.springframework.aop.target.CommonsPoolTargetSource;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.service.msg.type.FileAttachments;
import com.inbravo.scribe.rest.service.writer.utils.ZipWriter;

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

    logger.debug("----Inside handle, zipName: " + zipName);

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
        throw new ScribeException(ScribeResponseCodes._1000 + "Server file generation limit has reached");
      }

    } catch (final Exception e) {

      throw new ScribeException(ScribeResponseCodes._1000 + "Problem while file attachment service");
    } finally {

      try {
        /* Finally release the zip writer object */
        pool.releaseTarget(zWriter);
        zWriter = null;
      } catch (final Exception e) {
        throw new ScribeException(ScribeResponseCodes._1000 + "Problem while file attachment service");
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
