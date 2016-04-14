package com.inbravo.cad.rest.service.log;

import org.apache.log4j.MDC;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MutualDiagnosticLogUtils {

  private static final String edsaTransIdConst = "EDSATID";

  private static final String extTransIdConst = "EXTTID";

  private static final String userIdConst = "USERID";

  /**
   * This API is used for adding trasaction information in logs
   * 
   * @param edsaTransId
   * @param extTransId
   * @param userId
   */
  public final void addTransactionInfo(final String edsaTransId, final String extTransId, final String userId) {

    /* If EDSA transaction id is not null */
    if (edsaTransId != null) {

      /* Add EDSA transaction id information in MDC */
      MDC.put(edsaTransIdConst, edsaTransId);
    }

    /* If external transaction id is not null */
    if (extTransId != null) {

      /* Add external transaction id information in MDC */
      MDC.put(extTransIdConst, extTransId);
    }

    /* If user id is not null */
    if (userId != null) {

      /* Add user id information in MDC */
      MDC.put(userIdConst, userId);
    }
  }

  /**
   * This API is used for removing trasaction information from logs
   */
  public final void discardTransactionInfo() {

    /* Remove EDSA transaction id information in MDC */
    MDC.remove(edsaTransIdConst);

    /* Remove external transaction id inform in MDC */
    MDC.remove(extTransIdConst);

    /* Remove user id inform in MDC */
    MDC.remove(userIdConst);
  }
}
