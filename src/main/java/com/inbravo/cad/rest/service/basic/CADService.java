package com.inbravo.cad.rest.service.basic;

import com.inbravo.cad.rest.resource.CADCommandObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface CADService {

  public abstract CADCommandObject getObjectsCount(final CADCommandObject cADCommandObject) throws Exception;

  public abstract CADCommandObject getObjectsCount(final CADCommandObject cADCommandObject, final String query) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject cADCommandObject) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select, String order)
      throws Exception;

  public abstract CADCommandObject createObject(final CADCommandObject cADCommandObject) throws Exception;

  public abstract CADCommandObject updateObject(final CADCommandObject cADCommandObject) throws Exception;

  public abstract boolean deleteObject(final CADCommandObject cADCommandObject, final String idToBeDeleted) throws Exception;
}
