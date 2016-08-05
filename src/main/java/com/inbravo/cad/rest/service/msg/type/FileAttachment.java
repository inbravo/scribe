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
