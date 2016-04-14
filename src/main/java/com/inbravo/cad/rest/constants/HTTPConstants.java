package com.inbravo.cad.rest.constants;

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

  String agent = "agent";

  String tenant = "tenant";

  String logLevel = "logLevel";

  String noCRMFilter = "NONE";

  String allCRMObjectFields = "ALL";

  String anyObject = "ANY";

  String objectId = "objectId";
}
