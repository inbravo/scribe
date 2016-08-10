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

package com.inbravo.scribe.exception;

/**
 * This class contains CAD response codes
 * 
 * @author amit.dixit
 * 
 */
public interface CADResponseCodes {

  /* Following code maps to HTTP error code: 500: internal error */
  String _1000 = "1000: internal issue : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1001 = "1001: SalesForce : Not able to create : ";

  /* Following code maps to HTTP error code: 500: internal error */
  String _1002 = "1002: internal property not found : ";

  /* Following code maps to HTTP error code: 415: unsupported media type */
  String _1003 = "1003: Not supported : ";

  /* Following code maps to HTTP error code: 404: not found */
  String _1004 = "1004: Not able to find : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1005 = "1005: SalesForce CRM : No records : ";

  /* Following code maps to HTTP error code: 503: service unavailable */
  String _1006 = "1006: SalesForce CRM : Login problem : ";

  /* Following code maps to HTTP error code: 400: bad request */
  String _1008 = "1008: Input value is not valid : ";

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
}
