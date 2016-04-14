package com.inbravo.cad.exception;

/**
 * This class contains CAD response codes
 * 
 * @author amit.dixit
 * 
 */
public interface CADResponseCodes {

  /* Following code maps to HTTP error code: 500: internal error */
  String _1000 = "1000: CAD internal issue : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1001 = "1001: Sales Force : Not able to create : ";

  /* Following code maps to HTTP error code: 500: internal error */
  String _1002 = "1002: CAD internal property not found : ";

  /* Following code maps to HTTP error code: 415: unsupported media type */
  String _1003 = "1003: Not supported : ";

  /* Following code maps to HTTP error code: 404: not found */
  String _1004 = "1004: Not able to find : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1005 = "1005: Sales Force CRM : No records : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1006 = "1006: Sales Force CRM : Login problem : ";

  /* Following code maps to HTTP error code: 500: internal error */
  String _1007 = "1007: LDAP connectivity issue : ";

  /* Following code maps to HTTP error code: 400: bad request */
  String _1008 = "1008: Input value is not valid : ";

  /* Following code maps to HTTP error code: 401 Unauthorized */
  String _1009 = "1009: LDAP/DB configuration for Tenant/Agent is not found : ";

  /* Following code maps to HTTP error code: 500: internal error */
  String _1010 = "1010: DB issue : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1011 = "1011: WAPI issue : ";

  /* Following code maps to HTTP error code: 401 Unauthorized */
  String _1012 = "1012: Not authorized : ";

  /* Following code maps to HTTP error code: 400: bad request */
  String _1013 = "1013: Microsoft CRM : Service request error : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1015 = "1015: Microsoft CRM : Passport service error : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1020 = "1020: Zendesk CRM : Service error : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1021 = "1021: NetSuite CRM : Service error : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1022 = "1022: Zoho CRM : Service error : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1023 = "1023: OLTP BLOB service : Server is busy. Please try later : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1024 = "1024: FBMC service : Service error : ";
}
