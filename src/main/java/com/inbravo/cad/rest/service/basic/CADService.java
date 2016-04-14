package com.inbravo.cad.rest.service.basic;

import com.inbravo.cad.rest.resource.CADCommandObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface CADService {

  public abstract CADCommandObject getObjectsCount(final CADCommandObject eDSACommandObject) throws Exception;

  public abstract CADCommandObject getObjectsCount(final CADCommandObject eDSACommandObject, final String query) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject eDSACommandObject) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject eDSACommandObject, final String query, final String select, String order)
      throws Exception;

  public abstract CADCommandObject createObject(final CADCommandObject eDSACommandObject) throws Exception;

  public abstract CADCommandObject updateObject(final CADCommandObject eDSACommandObject) throws Exception;

  public abstract boolean deleteObject(final CADCommandObject eDSACommandObject, final String idToBeDeleted) throws Exception;
}
