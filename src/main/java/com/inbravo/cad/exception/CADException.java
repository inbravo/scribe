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

package com.inbravo.cad.exception;

/**
 * Custom exception class for reporting more detailed error to user
 * 
 * @author amit.dixit
 * 
 */
public final class CADException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private int cADTransId;

  public CADException(final Throwable throwable) {
    super(throwable);
  }

  public CADException(final String errorMessage) {
    super(errorMessage);
  }

  public CADException(final String errorCode, final String errorMessage) {
    super(errorMessage);
  }

  public CADException(final String errorMessage, final Exception cause) {
    super(errorMessage, cause);
  }

  public CADException(final String errorMessage, final Exception cause, final int cADTransId) {
    super(errorMessage, cause);
    this.cADTransId = cADTransId;
  }

  public final int getCADTransId() {
    return this.cADTransId;
  }

  public final void setCADTransId(final int cADTransId) {
    this.cADTransId = cADTransId;
  }
}