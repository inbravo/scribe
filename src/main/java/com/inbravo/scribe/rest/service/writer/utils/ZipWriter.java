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

package com.inbravo.scribe.rest.service.writer.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import com.inbravo.scribe.exception.CADException;
import com.inbravo.scribe.exception.CADResponseCodes;
import com.inbravo.scribe.rest.service.msg.type.FileAttachment;
import com.inbravo.scribe.rest.service.msg.type.FileAttachments;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZipWriter {

  private final Logger logger = Logger.getLogger(ZipWriter.class.getName());

  /* Defaultly only 6 files per zip is allowed */
  private int maxNoOfFileAllowedInZip = 6;

  /**
   * 
   * @param httpHeaderMap
   * @param outputStream
   * @param fileAttachments
   * @param zipName
   * @param tempFolderLocation
   * @param fileBuffer
   */
  public void write(final OutputStream outputStream, final FileAttachments fileAttachments, final String zipName) throws Exception {

    logger.debug("---Inside write, zip file : " + zipName);

    ZipOutputStream zos = null;
    MappedByteBuffer mbb = null;
    RandomAccessFile raf = null;
    FileChannel fc = null;
    File tempFile = null;
    try {

      /* Create new Zip output stream */
      zos = new ZipOutputStream(outputStream);
      ZipEntry zipEntry = null;

      /* Number of files in attachment */
      int fileCount = 0;

      /* Run over all attachments */
      for (final FileAttachment fa : fileAttachments.getFileAttachments()) {

        logger.debug("---Inside write, adding file: " + fa.getLocation() + " in zip: " + zipName);

        /* Check if max file count limit has not reached */
        if (fileCount <= maxNoOfFileAllowedInZip) {

          /* Increment the file count */
          fileCount++;

          /* Create an entry to be added in zip */
          zipEntry = new ZipEntry(fa.getId());

          try {
            /* Add file in zip */
            zos.putNextEntry(zipEntry);

            /* Get file from temp location */
            tempFile = new File(fa.getLocation());

            /* Create random access file on it */
            raf = new RandomAccessFile(tempFile, "rw");

            /* create a new file channel */
            fc = raf.getChannel();

            /* Map the file into memory for reading only */
            mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fa.getSizeInBytes());

            /* Get byte from file */
            for (int i = 0; i < fa.getSizeInBytes(); i++) {

              /* Write the content on output stream */
              zos.write(mbb.get(i));
            }

            /* Get byte from file */
            for (int i = 0; i < fa.getSizeInBytes(); i++) {

              /* Write the content on zip output stream */
              zos.write(mbb.get(i));
            }
            /* Clear the buffer */
            mbb.clear();

            /* Close thie entry */
            zos.closeEntry();

            /* Close the channel */
            fc.close();

            /* Close the file */
            raf.close();
          } catch (final java.util.zip.ZipException ze) {

            /* Check if duplicate entry */
            if (ze.getMessage().contains("duplicate entry")) {
              logger.debug("---Inside write, ignoring duplicate file in zip: " + fa.getLocation());
            } else {
              throw ze;
            }
          }
        } else {
          logger.info("---Inside write, not able to add file: " + fa.getLocation() + " in zip: " + zipName
              + ". max number of files allowed in zip is: " + maxNoOfFileAllowedInZip);
        }
      }

      /* Flush and finish the stream */
      zos.flush();
      zos.finish();

    } catch (final IOException e) {

      /* If any i/o exception delete the genrated temp file */
      if (tempFile != null) {
        tempFile.delete();
      }

      /* Cant generate zip */
      throw new CADException(CADResponseCodes._1000 + "Problem while writing zip" + e);
    } finally {

      try {

        /* Close the zip stream */
        if (zos != null) {
          zos.flush();
          zos.close();
        }
        /* Clear the buffer */
        if (mbb != null) {
          mbb = null;
        }
        /* Close the channel */
        if (fc != null) {
          fc.close();
          fc = null;
        }
        /* Close the file */
        if (raf != null) {
          raf.close();
          raf = null;
        }

      } catch (final IOException e) {

        /* Cant generate zip */
        throw new CADException(CADResponseCodes._1000 + "Problem while closing zip read/write streams");
      }
    }
  }

  /**
   * @return the maxNoOfFileAllowedInZip
   */
  public final int getMaxNoOfFileAllowedInZip() {
    return this.maxNoOfFileAllowedInZip;
  }

  /**
   * @param maxNoOfFileAllowedInZip the maxNoOfFileAllowedInZip to set
   */
  public final void setMaxNoOfFileAllowedInZip(final int maxNoOfFileAllowedInZip) {
    this.maxNoOfFileAllowedInZip = maxNoOfFileAllowedInZip;
  }
}
