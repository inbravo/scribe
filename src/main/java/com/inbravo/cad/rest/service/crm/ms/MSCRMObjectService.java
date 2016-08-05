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

package com.inbravo.cad.rest.service.crm.ms;

import com.inbravo.cad.rest.resource.CADCommandObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public abstract class MSCRMObjectService {

  public static final String notSupportedError = "Following operation is not supported by the CAD";

  public abstract CADCommandObject createObject(final CADCommandObject cADCommandObject) throws Exception;

  public abstract boolean deleteObject(final CADCommandObject cADCommandObject, final String idToBeDeleted) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject cADCommandObject) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select) throws Exception;

  public abstract CADCommandObject getObjects(final CADCommandObject cADCommandObject, final String query, final String select, final String order)
      throws Exception;

  public abstract CADCommandObject getObjectsCount(CADCommandObject cADCommandObject) throws Exception;

  public abstract CADCommandObject getObjectsCount(final CADCommandObject cADCommandObject, final String query) throws Exception;

  public abstract CADCommandObject updateObject(final CADCommandObject cADCommandObject) throws Exception;
}
