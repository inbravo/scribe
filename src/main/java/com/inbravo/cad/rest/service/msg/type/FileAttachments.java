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

package com.inbravo.cad.rest.service.msg.type;

import java.util.List;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class FileAttachments {

  private List<FileAttachment> fileAttachments;

  public FileAttachments(final List<FileAttachment> fileAttachments) {
    super();
    this.fileAttachments = fileAttachments;
  }

  /**
   * @return the fileAttachments
   */
  public final List<FileAttachment> getFileAttachments() {
    return this.fileAttachments;
  }

  /**
   * @param fileAttachments the fileAttachments to set
   */
  public final void setFileAttachments(List<FileAttachment> fileAttachments) {
    this.fileAttachments = fileAttachments;
  }

  /**
   * Tells about the size of attachment list
   * 
   * @return
   */
  public final int size() {

    /* Return length of list */
    if (fileAttachments != null) {
      return fileAttachments.size();
    } else {
      return 0;
    }
  }
}
