package com.inbravo.cad.rest.service.msg.type;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class FileAttachment {

  private String id;

  private String location;

  private long sizeInBytes;

  public FileAttachment() {
    super();
  }

  public FileAttachment(final String id, final String tempLocation, final long sizeInBytes) {
    super();
    this.id = id;
    this.location = tempLocation;
    this.sizeInBytes = sizeInBytes;
  }

  /**
   * @return the id
   */
  public final String getId() {
    return this.id;
  }

  /**
   * @param id the id to set
   */
  public final void setId(final String id) {
    this.id = id;
  }

  /**
   * @return the location
   */
  public final String getLocation() {
    return this.location;
  }

  /**
   * @param location the location to set
   */
  public final void setLocation(final String location) {
    this.location = location;
  }

  /**
   * @return the sizeinBytes
   */
  public final long getSizeInBytes() {
    return this.sizeInBytes;
  }

  /**
   * @param sizeinBytes the sizeinBytes to set
   */
  public final void setSizeInBytes(final long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public final String toString() {
    return "FileAttachment [id=" + this.id + ", location=" + this.location + ", sizeInBytes=" + sizeInBytes + " ]";
  }

  public final void cleanup() throws Throwable {
    try {
      this.finalize();
    } catch (final Exception e) {

      /* Ignorable error */
      System.err.println("Ignorable error from com.inbravo.cad.rest.service.msg.type.cleanup()");
    }
  }

  public final String getFile() {
    return location + id;
  }

  @Override
  protected final void finalize() throws Throwable {
    id = null;
    location = null;
    super.finalize();
  }
}
