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

package com.inbravo.scribe.rest.service.crm.ms;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface MSCRMSchemaConstants {

  interface MSCRM_SAML_Constants {
    String _WSSSecurity = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    String _WSSSecurityUtility = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    String _ADDRESSING = "http://www.w3.org/2005/08/addressing";
  }

  interface MSCRM_2011_Schema_Constants {
    String _Retrieve = "http://schemas.microsoft.com/xrm/2011/Contracts/Services/IOrganizationService/Retrieve";
    String _CORETYPES = "http://schemas.microsoft.com/crm/2007/CoreTypes";
    String _IORGANIZATIONSERVICE = "http://schemas.microsoft.com/xrm/2011/Contracts/Services/IOrganizationService/";
  }

  interface MSCRM_2007_Schema_Constants {
    String _SOAPENV = "http://www.w3.org/2003/05/soap-envelope";
    String _WEBSERVICES = "http://schemas.microsoft.com/crm/2007/WebServices";
    String _CORETYPES = "http://schemas.microsoft.com/crm/2007/CoreTypes";
  }

  interface MSCRM_2006_Schema_Constants {
    String _QUERY = "http://schemas.microsoft.com/crm/2006/Query";
  }

  interface XML_Constants {
    String _XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
  }

  String WEBSERVICES = MSCRM_2007_Schema_Constants._WEBSERVICES;
  String CORETYPE = MSCRM_2007_Schema_Constants._CORETYPES;
  String QUERY = MSCRM_2006_Schema_Constants._QUERY;
  String XML_SCHEMA_INSTANCE = XML_Constants._XML_SCHEMA_INSTANCE;

  String CRM_AUTH_TOKEN_TAG = "CrmAuthenticationToken";
  String CRM_AUTH_TYPE_TAG = "AuthenticationType";
  String CRM_TICKET_TAG = "CrmTicket";
  String CRM_ORG_NAME_TAG = "OrganizationName";

  /* MS CRM security header template */
  String securityHeaderTemplate =
      "<EncryptedData "
          + "	xmlns=\"http://www.w3.org/2001/04/xmlenc#\""
          + " 	Id=\"Assertion0\" "
          + "	Type=\"http://www.w3.org/2001/04/xmlenc#Element\">"
          + "	<EncryptionMethod "
          + "		Algorithm=\"http://www.w3.org/2001/04/xmlenc#tripledes-cbc\"/>"
          + "	<ds:KeyInfo "
          + "		xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">"
          + "		<EncryptedKey>"
          + "			<EncryptionMethod "
          + "				Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\"/>"
          + "			<ds:KeyInfo Id=\"keyinfo\">"
          + "				<wsse:SecurityTokenReference "
          + "					xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"
          + "					<wsse:KeyIdentifier "
          + "						EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" "
          + "						ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509SubjectKeyIdentifier\">%s</wsse:KeyIdentifier>"
          + "				</wsse:SecurityTokenReference>" + "			</ds:KeyInfo>" + "			<CipherData>" + "				<CipherValue>%s</CipherValue>" + "			</CipherData>"
          + "		</EncryptedKey>" + "	</ds:KeyInfo>" + "	<CipherData>" + "		<CipherValue>%s</CipherValue>" + "	</CipherData>" + "</EncryptedData>";

  /* Office 365 authentication header */
  String CRM_AUTH_ENCRYPTED_DATA_TAG = "EncryptedData";

  interface XPATH_Constants {
    String XPATH_WSDL_IMPORT_IN_MS_RESPONSE = "//*[local-name()='import' and namespace-uri()='http://schemas.xmlsoap.org/wsdl/']/@location";
    String XPATH_URN_ADDRESS_IN_MS_RESPONSE =
        "//*[local-name()='AuthenticationPolicy' and namespace-uri()='http://schemas.microsoft.com/xrm/2011/Contracts/Services']/*[local-name()='SecureTokenService' and namespace-uri()='http://schemas.microsoft.com/xrm/2011/Contracts/Services']//*[local-name()='AppliesTo' and namespace-uri()='http://schemas.microsoft.com/xrm/2011/Contracts/Services']/text()";
    String XPATH_STE_ENDPOINT_IN_MS_RESPONSE =
        "//*[local-name()='Issuer' and namespace-uri()='http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702']/*[local-name()='Address' and namespace-uri()='http://www.w3.org/2005/08/addressing']/text()";
  }
}
