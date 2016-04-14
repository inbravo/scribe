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
