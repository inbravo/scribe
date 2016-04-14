package com.inbravo.cad.rest.service.crm.ms;

import com.inbravo.cad.rest.resource.CADCommandObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public abstract class MSCRMObjectService {

  public static final String notSupportedError = "Following operation is not supported by the EDSA";

  public abstract CADCommandObject createObject(final CADCommandObject eDSACommandObject) throws Exception;

  public abstract boolean deleteObject(final CADCommandObject eDSACommandObject, final String idToBeDeleted) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject eDSACommandObject) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select, final String order)
      throws Exception;

  public abstract CADCommandObject getObjectsCount(CADCommandObject eDSACommandObject) throws Exception;

  public abstract CADCommandObject getObjectsCount(final CADCommandObject eDSACommandObject, final String query) throws Exception;

  public abstract CADCommandObject updateObject(final CADCommandObject eDSACommandObject) throws Exception;
}
