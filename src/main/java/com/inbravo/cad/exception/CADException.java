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
