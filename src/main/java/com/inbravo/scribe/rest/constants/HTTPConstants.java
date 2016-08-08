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

package com.inbravo.scribe.rest.constants;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface HTTPConstants {

  String id = "id";

  String ObjectType = "objectType";

  String PahForObjectType = "/{objectType}";

  String PahForObjectTypesCount = "/{objectType}/count";

  String PahForObjectTypeById = "/{objectType}/{id}";

  String PahForObjectTypeByQuery = "/{objectType}/{query}";

  String PahForObjectCountByQuery = "/{objectType}/{query}/count";

  String PahForObjectTypeByQueryAndSelect = "/{objectType}/{query}/{select}";

  String PahForObjectTypeByQueryAndSelectAndOrder = "/{objectType}/{query}/{select}/{order}";

  String PathForProcessLogLevelChangeRequest = "/log";

  String resetById = "/reset/{id}";

  String object = "/object/";

  String session = "/session/";

  String loginById = "/login/{id}";

  String mimeTypePlainText = "text/plain";

  String mimeTypeOctetStream = "application/octet-stream";

  String mimeTypeXML = "application/xml";

  String mimeTypeJSON = "application/json";

  String query = "query";

  String fileName = "fileName";

  String select = "select";

  String order = "order";

  String andClause = "&";

  String orClause = "|";

  String crmUserId = "crmUserId";

  String logLevel = "logLevel";

  String noCRMFilter = "NONE";

  String allCRMObjectFields = "ALL";

  String anyObject = "ANY";

  String objectId = "objectId";
}
