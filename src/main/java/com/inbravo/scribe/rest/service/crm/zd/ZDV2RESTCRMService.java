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

package com.inbravo.scribe.rest.service.crm.zd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.inbravo.scribe.exception.ScribeException;
import com.inbravo.scribe.exception.ScribeResponseCodes;
import com.inbravo.scribe.rest.constants.HTTPConstants;
import com.inbravo.scribe.rest.resource.ScribeCommandObject;
import com.inbravo.scribe.rest.resource.ScribeObject;
import com.inbravo.scribe.rest.service.crm.CRMService;
import com.inbravo.scribe.rest.service.crm.cache.ScribeCacheObject;
import com.inbravo.scribe.rest.service.crm.zd.session.ZDCRMSessionManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZDV2RESTCRMService extends CRMService {

  private final Logger logger = Logger.getLogger(ZDV2RESTCRMService.class.getName());

  private String crmFieldsSeparator;

  private String orderFieldsSeparator;

  private String zdAPISubPath = "/api/v2/";

  private ZDCRMSessionManager zDCRMSessionManager;

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject cADCommandObject) throws Exception {
    logger.debug("---Inside getObjects");

    /* Check if all record types are to be searched */
    if (cADCommandObject.getObjectType().trim().equalsIgnoreCase(HTTPConstants.anyObject)) {

      return this.searchAllTypeOfObjects(cADCommandObject, null, null, null);
    } else {
      GetMethod getMethod = null;
      try {

        String serviceURL = null;
        String serviceProtocol = null;
        String userId = null;
        String password = null;
        String crmPort = "80";

        /* Get agent from session manager */
        final ScribeCacheObject cacheObject = zDCRMSessionManager.getCrmUserIdWithCRMSessionInformation(cADCommandObject.getCrmUserId());

        /* Get CRM information from agent */
        serviceURL = cacheObject.getScribeMetaObject().getCrmServiceURL();
        serviceProtocol = cacheObject.getScribeMetaObject().getCrmServiceProtocol();
        userId = cacheObject.getScribeMetaObject().getCrmUserId();
        password = cacheObject.getScribeMetaObject().getCrmPassword();
        crmPort = cacheObject.getScribeMetaObject().getCrmPort();

        /* Create Zen desk URL */
        final String zenDeskURL = serviceProtocol + "://" + serviceURL + zdAPISubPath + cADCommandObject.getObjectType().toLowerCase() + "s.json";

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside getObjects zenDeskURL: " + zenDeskURL);
        }

        /* Instantiate get method */
        getMethod = new GetMethod(zenDeskURL);

        /* Set request content type */
        getMethod.addRequestHeader("Content-Type", "application/json");
        getMethod.addRequestHeader("accept", "application/json");

        final HttpClient httpclient = new HttpClient();

        /* Set credentials */
        httpclient.getState().setCredentials(new AuthScope(serviceURL, this.validateCrmPort(crmPort)),
            new UsernamePasswordCredentials(userId, password));

        /* Execute method */
        int result = httpclient.executeMethod(getMethod);

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside getObjects response code: " + result + " & body: " + getMethod.getResponseBodyAsString());
        }

        if (result == HttpStatus.SC_OK) {

          /* Create Scribe object from JSON response */
          return this.createSearchResponse(getMethod.getResponseBodyAsString(), cADCommandObject.getObjectType().toLowerCase());

        } else if (result == HttpStatus.SC_FORBIDDEN) {
          throw new ScribeException(ScribeResponseCodes._1020 + "Query is forbidden by Zendesk CRM");
        } else if (result == HttpStatus.SC_BAD_REQUEST) {
          throw new ScribeException(ScribeResponseCodes._1003 + "Invalid request content");
        } else if (result == HttpStatus.SC_UNAUTHORIZED) {
          throw new ScribeException(ScribeResponseCodes._1012 + "Anauthorized by Zendesk CRM");
        } else if (result == HttpStatus.SC_NOT_FOUND) {
          throw new ScribeException(ScribeResponseCodes._1004 + "Requested record not found at Zendesk CRM");
        }
      } catch (final ScribeException exception) {
        throw exception;
      } catch (final JSONException e) {

        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", e);
      } catch (final ParserConfigurationException exception) {

        throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", exception);
      } catch (final SAXException exception) {

        throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", exception);
      } catch (final IOException exception) {

        throw new ScribeException(ScribeResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
      } finally {
        /* Release connection socket */
        if (getMethod != null) {
          getMethod.releaseConnection();
        }
      }
      return cADCommandObject;
    }
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject cADCommandObject, final String query) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects query: " + query);
    }

    /* Check if all record types are to be searched */
    if (cADCommandObject.getObjectType().trim().equalsIgnoreCase(HTTPConstants.anyObject)) {

      return this.searchAllTypeOfObjects(cADCommandObject, query, null, null);
    } else {
      throw new ScribeException(ScribeResponseCodes._1003
          + "Following operation is not supported by the CRM; Please search 'ANY' object type for searcing all record types");
    }
  }

  @Override
  public final ScribeCommandObject getObjects(final ScribeCommandObject cADCommandObject, final String query, final String select) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects query: " + query + " & select: " + select);
    }

    /* Check if all record types are to be searched */
    if (cADCommandObject.getObjectType().trim().equalsIgnoreCase(HTTPConstants.anyObject)) {
      return this.searchAllTypeOfObjects(cADCommandObject, query, select, null);
    } else {
      throw new ScribeException(ScribeResponseCodes._1003
          + "Following operation is not supported by the CRM; Please search 'ANY' object type for searcing all record types");
    }
  }

  @Override
  public ScribeCommandObject getObjects(final ScribeCommandObject cADCommandObject, final String query, final String select, final String order)
      throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside getObjects query: " + query + " & select: " + select + " & order: " + order);
    }

    /* Check if all record types are to be searched */
    if (cADCommandObject.getObjectType().trim().equalsIgnoreCase(HTTPConstants.anyObject)) {

      if (order != null) {

        /* Throw user error that ordering is not supported */
        throw new ScribeException(ScribeResponseCodes._1003 + "Ordering is not supported at ZD integration");
      } else {
        return this.searchAllTypeOfObjects(cADCommandObject, query, select, null);
      }
    } else {
      throw new ScribeException(ScribeResponseCodes._1003
          + "Following operation is not supported by the CRM; Please search 'ANY' object type for searcing all record types");
    }
  }

  @Override
  public final ScribeCommandObject getObjectsCount(final ScribeCommandObject cADCommandObject) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + "Following operation is not supported by the Scribe");
  }

  @Override
  public final ScribeCommandObject getObjectsCount(final ScribeCommandObject cADCommandObject, final String query) throws Exception {
    throw new ScribeException(ScribeResponseCodes._1003 + " Following operation is not supported by the Scribe");
  }

  @Override
  public final ScribeCommandObject updateObject(final ScribeCommandObject cADCommandObject) throws Exception {

    logger.debug("---Inside updateObject");
    PutMethod putMethod = null;
    try {
      String serviceURL = null;
      String serviceProtocol = null;
      String userId = null;
      String password = null;
      String sessionId = null;
      String crmPort = "80";

      /* Get agent from session manager */
      final ScribeCacheObject cacheObject = zDCRMSessionManager.getCrmUserIdWithCRMSessionInformation(cADCommandObject.getCrmUserId());

      /* Get CRM information from agent */
      serviceURL = cacheObject.getScribeMetaObject().getCrmServiceURL();
      serviceProtocol = cacheObject.getScribeMetaObject().getCrmServiceProtocol();
      userId = cacheObject.getScribeMetaObject().getCrmUserId();
      password = cacheObject.getScribeMetaObject().getCrmPassword();
      crmPort = cacheObject.getScribeMetaObject().getCrmPort();

      String crmObjectId = null;

      /* Check if response content in request, is not null */
      if (cADCommandObject.getObject() != null && cADCommandObject.getObject().length == 1) {

        /* Get Id of CRM object */
        crmObjectId = ZDCRMMessageFormatUtils.getNodeValue("ID", cADCommandObject.getObject()[0]);

        if (crmObjectId == null) {

          /* Inform user about invalid request */
          throw new ScribeException(ScribeResponseCodes._1008 + "CRM object id is not present in request");
        }
      } else {
        /* Inform user about invalid request */
        throw new ScribeException(ScribeResponseCodes._1008 + "CRM object information is not present in request");
      }

      /* Create Zen desk URL */
      final String zenDeskURL =
          serviceProtocol + "://" + serviceURL + zdAPISubPath + cADCommandObject.getObjectType().toLowerCase() + "s/" + crmObjectId + ".json";

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside updateObject zenDeskURL: " + zenDeskURL);
      }

      /* Instantiate put method */
      putMethod = new PutMethod(zenDeskURL);

      /* Set request content type */
      putMethod.addRequestHeader("Content-Type", "application/json");
      putMethod.addRequestHeader("accept", "application/json");

      /* Cookie is required to be set for session management */
      putMethod.addRequestHeader("Cookie", sessionId);

      /* Add request entity */
      final RequestEntity entity = new StringRequestEntity(ZDCRMMessageFormatUtils.getCreateRequestJSON(cADCommandObject), null, null);
      putMethod.setRequestEntity(entity);

      final HttpClient httpclient = new HttpClient();

      /* Set credentials */
      httpclient.getState().setCredentials(new AuthScope(serviceURL, this.validateCrmPort(crmPort)),
          new UsernamePasswordCredentials(userId, password));

      /* Execute method */
      int result = httpclient.executeMethod(putMethod);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside updateObject response code: " + result + " & body: " + putMethod.getResponseBodyAsString());
      }

      /* Check if object is updated */
      if (result == HttpStatus.SC_OK || result == HttpStatus.SC_CREATED) {

        /* Return the response object */
        return this.createCreateResponse(putMethod.getResponseBodyAsString(), cADCommandObject.getObjectType().toLowerCase());

      } else if (result == HttpStatus.SC_BAD_REQUEST || result == HttpStatus.SC_METHOD_NOT_ALLOWED || result == HttpStatus.SC_NOT_ACCEPTABLE
          || result == HttpStatus.SC_UNPROCESSABLE_ENTITY) {

        /* Throw user error with valid reasons for failure */
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid request : "
            + ZDCRMMessageFormatUtils.getErrorFromResponse(putMethod.getResponseBodyAsStream()));
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {

        /* Throw user error with valid reasons for failure */
        throw new ScribeException(ScribeResponseCodes._1012 + "Anauthorized by Zendesk CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {

        /* Throw user error with valid reasons for failure */
        throw new ScribeException(ScribeResponseCodes._1004 + "Requested data not found at Zendesk CRM");
      } else if (result == HttpStatus.SC_MOVED_TEMPORARILY) {

        /* Throw user error with valid reasons for failure */
        throw new ScribeException(ScribeResponseCodes._1004
            + "Requested data not found at Zendesk CRM : Seems like Zendesk Service URL/Protocol is not correct");
      }
    } catch (final ScribeException exception) {
      throw exception;
    } catch (final JSONException e) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", e);
    } catch (final ParserConfigurationException exception) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", exception);
    } catch (final SAXException exception) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", exception);
    } catch (final IOException exception) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } finally {
      /* Release connection socket */
      if (putMethod != null) {
        putMethod.releaseConnection();
      }
    }
    return cADCommandObject;
  }

  @Override
  public final ScribeCommandObject createObject(final ScribeCommandObject cADCommandObject) throws Exception {
    logger.debug("---Inside createObject");
    PostMethod postMethod = null;
    try {

      String serviceURL = null;
      String serviceProtocol = null;
      String userId = null;
      String password = null;
      String sessionId = null;
      String crmPort = "80";

      /* Get agent from session manager */
      final ScribeCacheObject cacheObject = zDCRMSessionManager.getCrmUserIdWithCRMSessionInformation(cADCommandObject.getCrmUserId());

      /* Get CRM information from agent */
      serviceURL = cacheObject.getScribeMetaObject().getCrmServiceURL();
      serviceProtocol = cacheObject.getScribeMetaObject().getCrmServiceProtocol();
      userId = cacheObject.getScribeMetaObject().getCrmUserId();
      password = cacheObject.getScribeMetaObject().getCrmPassword();
      crmPort = cacheObject.getScribeMetaObject().getCrmPort();

      /* Create Zen desk URL */
      final String zenDeskURL = serviceProtocol + "://" + serviceURL + zdAPISubPath + cADCommandObject.getObjectType().toLowerCase() + "s.json";

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside createObject zenDeskURL: " + zenDeskURL);
      }

      /* Instantiate get method */
      postMethod = new PostMethod(zenDeskURL);

      /* Set request content type */
      postMethod.addRequestHeader("Content-Type", "application/json");
      postMethod.addRequestHeader("accept", "application/json");

      /* Cookie is required to be set for session management */
      postMethod.addRequestHeader("Cookie", sessionId);

      /* Add request entity */
      final RequestEntity entity = new StringRequestEntity(ZDCRMMessageFormatUtils.getCreateRequestJSON(cADCommandObject), null, null);
      postMethod.setRequestEntity(entity);

      final HttpClient httpclient = new HttpClient();

      /* Set credentials */
      httpclient.getState().setCredentials(new AuthScope(serviceURL, this.validateCrmPort(crmPort)),
          new UsernamePasswordCredentials(userId, password));

      /* Execute method */
      int result = httpclient.executeMethod(postMethod);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside createObject response code: " + result + " & body: " + postMethod.getResponseBodyAsString());
      }

      /* Check if object is created */
      if (result == HttpStatus.SC_OK || result == HttpStatus.SC_CREATED) {

        /* Create Scribe object from JSON response */
        return this.createCreateResponse(postMethod.getResponseBodyAsString(), cADCommandObject.getObjectType().toLowerCase());

      } else if (result == HttpStatus.SC_BAD_REQUEST || result == HttpStatus.SC_METHOD_NOT_ALLOWED || result == HttpStatus.SC_NOT_ACCEPTABLE
          || result == HttpStatus.SC_UNPROCESSABLE_ENTITY) {

        /* Throw user error with valid reasons */
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid request : "
            + ZDCRMMessageFormatUtils.getErrorFromResponse(postMethod.getResponseBodyAsString()));
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {

        /* Throw user error with valid reasons */
        throw new ScribeException(ScribeResponseCodes._1012 + "Anauthorized by Zendesk CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {

        /* Throw user error with valid reasons */
        throw new ScribeException(ScribeResponseCodes._1004 + "Requested data not found at Zendesk CRM");
      } else if (result == HttpStatus.SC_MOVED_TEMPORARILY) {

        /* Throw user error with valid reasons */
        throw new ScribeException(ScribeResponseCodes._1004
            + "Requested data not found at Zendesk CRM : Seems like Zendesk Service URL/Protocol is not correct");
      }
    } catch (final ScribeException exception) {
      throw exception;
    } catch (final JSONException e) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", e);
    } catch (final ParserConfigurationException exception) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", exception);
    } catch (final SAXException exception) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", exception);
    } catch (final IOException exception) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } finally {
      /* Release connection socket */
      if (postMethod != null) {
        postMethod.releaseConnection();
      }
    }
    return cADCommandObject;
  }

  @Override
  public final boolean deleteObject(final ScribeCommandObject cADCommandObject, final String idToBeDeleted) throws Exception {

    logger.debug("---Inside deleteObject");
    throw new ScribeException(ScribeResponseCodes._1003 + " Following operation is not supported by the Scribe");
  }

  /**
   * 
   * @param cADCommandObject
   * @param query
   * @param select
   * @param order
   * @return
   * @throws Exception
   */
  private final ScribeCommandObject searchAllTypeOfObjects(final ScribeCommandObject cADCommandObject, final String query, final String select,
      final String order) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("---Inside searchAllTypeOfObjects query: " + query + " & select: " + select + " & order: " + order);
    }
    GetMethod getMethod = null;
    try {

      String serviceURL = null;
      String serviceProtocol = null;
      String userId = null;
      String password = null;
      String sessionId = null;
      String crmPort = "80";

      /* Get agent from session manager */
      final ScribeCacheObject cacheObject = zDCRMSessionManager.getCrmUserIdWithCRMSessionInformation(cADCommandObject.getCrmUserId());

      /* Get CRM information from agent */
      serviceURL = cacheObject.getScribeMetaObject().getCrmServiceURL();
      serviceProtocol = cacheObject.getScribeMetaObject().getCrmServiceProtocol();
      userId = cacheObject.getScribeMetaObject().getCrmUserId();
      password = cacheObject.getScribeMetaObject().getCrmPassword();
      crmPort = cacheObject.getScribeMetaObject().getCrmPort();

      /* Create Zen desk URL */
      final String zenDeskURL = serviceProtocol + "://" + serviceURL + zdAPISubPath + "search.json";

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside searchAllTypeOfObjects zenDeskURL: " + zenDeskURL + " & sessionId: " + sessionId);
      }

      /* Instantiate get method */
      getMethod = new GetMethod(zenDeskURL);

      /* Set request content type */
      getMethod.addRequestHeader("Content-Type", "application/json");

      /* Cookie is required to be set in case of search */
      getMethod.addRequestHeader("Cookie", sessionId);

      final HttpClient httpclient = new HttpClient();

      /* Set credentials */
      httpclient.getState().setCredentials(new AuthScope(serviceURL, this.validateCrmPort(crmPort)),
          new UsernamePasswordCredentials(userId, password));

      /* Check if user has not provided a valid query */
      if (ZDCRMMessageFormatUtils.validateQuery(query)) {
        getMethod.setQueryString(new NameValuePair[] {new NameValuePair("query", ZDCRMMessageFormatUtils.createZDQuery(query))});
      }

      /* Execute method */
      int result = httpclient.executeMethod(getMethod);

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside searchAllTypeOfObjects response code: " + result + " & body: " + getMethod.getResponseBodyAsString());
      }

      if (result == HttpStatus.SC_OK) {

        /* Create JSON object from response */
        final JSONObject jObj = new JSONObject(getMethod.getResponseBodyAsString());

        /* Create new Scribe object list */
        final List<ScribeObject> cADbjectList = new ArrayList<ScribeObject>();

        /* Get select field list */
        List<String> selectFieldList = null;

        /* Check if user is asking for all fields */
        if (select != null && !select.equalsIgnoreCase(HTTPConstants.allCRMObjectFields)) {

          /* Create select field list */
          selectFieldList = ZDCRMMessageFormatUtils.createSelectFieldList(select);
        }

        /* Get all keys */
        @SuppressWarnings("rawtypes")
        final Iterator itr = jObj.keys();

        /* Iterate over user json object list */
        while (itr.hasNext()) {

          /* Get key name */
          final String key = (String) itr.next();

          /* Get array value */
          if (jObj.get(key).getClass().isAssignableFrom(JSONArray.class)) {

            /* Get array object */
            final JSONArray arrObj = (JSONArray) jObj.get(key);

            /* Run over all JSON objects */
            for (int i = 0; i < arrObj.length(); i++) {

              /* Get individual object */
              final JSONObject subObj = (JSONObject) arrObj.get(i);

              /* Create list of elements */
              final List<Element> elementList = new ArrayList<Element>();

              /* Create new Scribe object */
              final ScribeObject cADbject = new ScribeObject();

              /* Get all keys */
              @SuppressWarnings("rawtypes")
              final Iterator subItr = subObj.keys();

              /* Loop over each user in list */
              while (subItr.hasNext()) {

                /* Get key name */
                final String subKey = (String) subItr.next();

                /* Get value */
                final Object value = subObj.get(subKey);

                if (selectFieldList != null) {

                  /* Add only user requested fields */
                  if (selectFieldList.contains(subKey.trim().toUpperCase())) {

                    /* Add element in list */
                    elementList.add(ZDCRMMessageFormatUtils.createMessageElement(subKey, value));
                  }
                } else {

                  /* Add element in list */
                  elementList.add(ZDCRMMessageFormatUtils.createMessageElement(subKey, value));
                }

                /* Set Scribe object type */
                if (subKey.equalsIgnoreCase("result_type")) {

                  cADbject.setObjectType("" + value);
                }

                if (logger.isDebugEnabled()) {
                  logger.debug("---Inside searchAllTypeOfObjects key : " + subKey + " & value: " + value);
                }
              }

              /* Add all CRM fields */
              cADbject.setXmlContent(elementList);

              /* Add Scribe object in list */
              cADbjectList.add(cADbject);
            }
          } else {
            if (logger.isDebugEnabled()) {
              logger.debug("---Inside searchAllTypeOfObjects unexpected JSON object type in response : " + jObj);
            }
          }
        }

        /* Check if no record found */
        if (cADbjectList.size() == 0) {

          /* Throw user error */
          throw new ScribeException(ScribeResponseCodes._1004 + "No records found at Zendesk");
        }

        /* Set the final object in command object */
        cADCommandObject.setObject(cADbjectList.toArray(new ScribeObject[cADbjectList.size()]));

      } else if (result == HttpStatus.SC_BAD_REQUEST || result == HttpStatus.SC_METHOD_NOT_ALLOWED || result == HttpStatus.SC_NOT_ACCEPTABLE
          || result == HttpStatus.SC_UNPROCESSABLE_ENTITY) {

        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1003 + "Invalid request : "
            + ZDCRMMessageFormatUtils.getErrorFromResponse(getMethod.getResponseBodyAsString()));
      } else if (result == HttpStatus.SC_UNAUTHORIZED) {

        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1012 + "Anauthorized by Zendesk CRM");
      } else if (result == HttpStatus.SC_NOT_FOUND) {

        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1004 + "Requested record not found at Zendesk CRM");
      } else if (result == HttpStatus.SC_MOVED_TEMPORARILY) {

        /* Throw user error */
        throw new ScribeException(ScribeResponseCodes._1004
            + "Requested data not found at Zendesk CRM : Seems like Zendesk Service URL/Protocol is not correct");
      }
    } catch (final ScribeException exception) {
      throw exception;
    } catch (final JSONException e) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", e);
    } catch (final ParserConfigurationException exception) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", exception);
    } catch (final SAXException exception) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Recieved an invalid response from Zendesk CRM", exception);
    } catch (final IOException exception) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } catch (final Exception e) {
      throw new ScribeException(ScribeResponseCodes._1000 + "Problem while communicating with Zendesk CRM", e);
    } finally {

      /* Release connection socket */
      if (getMethod != null) {
        getMethod.releaseConnection();
      }
    }
    return cADCommandObject;
  }

  /**
   * 
   * @param responseStr
   * @return
   * @throws Exception
   */
  private final ScribeCommandObject createSearchResponse(final String responseStr, final String objectType) throws Exception {

    /* Create new Scribe object */
    final ScribeCommandObject cADCommandObject = new ScribeCommandObject();

    /* Create JSON object from response */
    final JSONObject jObj = new JSONObject(responseStr);

    /* Create new Scribe object list */
    final List<ScribeObject> cADbjectList = new ArrayList<ScribeObject>();

    /* Get all keys */
    @SuppressWarnings("rawtypes")
    final Iterator itr = jObj.keys();

    /* Iterate over user json object list */
    while (itr.hasNext()) {

      /* Get key name */
      final String key = (String) itr.next();

      /* Get array value */
      if (jObj.get(key).getClass().isAssignableFrom(JSONArray.class)) {

        /* Get array object */
        final JSONArray arrObj = (JSONArray) jObj.get(key);

        /* Run over all JSON objects */
        for (int i = 0; i < arrObj.length(); i++) {

          /* Get individual object */
          final JSONObject subObj = (JSONObject) arrObj.get(i);

          /* Create list of elements */
          final List<Element> elementList = new ArrayList<Element>();

          /* Create new Scribe object */
          final ScribeObject cADbject = new ScribeObject();

          /* Get all keys */
          @SuppressWarnings("rawtypes")
          final Iterator subItr = subObj.keys();

          /* Loop over each user in list */
          while (subItr.hasNext()) {

            /* Get key name */
            final String subKey = (String) subItr.next();

            /* Get value */
            final Object value = subObj.get(subKey);

            /* Add element in list */
            elementList.add(ZDCRMMessageFormatUtils.createMessageElement(subKey, value));

            if (logger.isDebugEnabled()) {
              logger.debug("---Inside createSearchResponse key : " + subKey + " & value: " + value);

            }

            /* Set Scribe object type */
            cADbject.setObjectType(objectType);
          }

          /* Add all CRM fields */
          cADbject.setXmlContent(elementList);

          /* Add Scribe object in list */
          cADbjectList.add(cADbject);
        }
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("---Inside createSearchResponse unexpected JSON object type in response : " + jObj.get(key));
        }
      }
    }

    /* Check if no record found */
    if (cADbjectList.size() == 0) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1004 + "No records found at Zendesk");
    }

    /* Set the final object in command object */
    cADCommandObject.setObject(cADbjectList.toArray(new ScribeObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  /**
   * 
   * @param responseStr
   * @return
   * @throws Exception
   */
  private final ScribeCommandObject createCreateResponse(final String responseStr, final String objectType) throws Exception {

    /* Create new Scribe object */
    final ScribeCommandObject cADCommandObject = new ScribeCommandObject();

    /* Create JSON object on complete response */
    final JSONObject jObj = new JSONObject(responseStr);

    /* Create new Scribe object list */
    final List<ScribeObject> cADbjectList = new ArrayList<ScribeObject>();

    /* Get all keys */
    @SuppressWarnings("rawtypes")
    final Iterator itr = jObj.keys();

    /* Create list of elements */
    final List<Element> elementList = new ArrayList<Element>();

    /* Create new Scribe object */
    final ScribeObject cADbject = new ScribeObject();

    /* Iterate over user json object list */
    while (itr.hasNext()) {

      /* Get key name */
      final String key = (String) itr.next();

      if (logger.isDebugEnabled()) {
        logger.debug("---Inside createCreateResponse key : " + key + " & value: " + jObj.get(key));
      }

      /* If single JSON object */
      if (jObj.get(key).getClass().isAssignableFrom(JSONObject.class)) {

        /* Get first JSON object */
        final JSONObject firstJsonObj = (JSONObject) jObj.get(key);

        if (logger.isDebugEnabled()) {
          logger.debug("---Inside createCreateResponse firstJsonObj : " + firstJsonObj);
        }

        /* Get all keys */
        @SuppressWarnings("rawtypes")
        final Iterator subItr = firstJsonObj.keys();

        /* Iterate over user json object list */
        while (subItr.hasNext()) {

          /* Get key name */
          final String subKey = (String) subItr.next();

          /* Get value */
          final Object value = firstJsonObj.get(subKey);

          /* Add element in list */
          elementList.add(ZDCRMMessageFormatUtils.createMessageElement(subKey, value));

          if (logger.isDebugEnabled()) {
            logger.debug("---Inside createCreateResponse key : " + subKey + " & value: " + value);
          }
        }

      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("---Inside createCreateResponse unexpected JSON object type in response : " + jObj.get(key));
        }
      }
    }

    /* Set Scribe object type */
    cADbject.setObjectType(objectType);

    /* Add all CRM fields */
    cADbject.setXmlContent(elementList);

    /* Add Scribe object in list */
    cADbjectList.add(cADbject);

    /* Check if no record found */
    if (cADbjectList.size() == 0) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1004 + "No records found at Zendesk");
    }

    /* Set the final object in command object */
    cADCommandObject.setObject(cADbjectList.toArray(new ScribeObject[cADbjectList.size()]));

    return cADCommandObject;
  }

  public final int validateCrmPort(final String crmPort) {

    try {

      return Integer.parseInt(crmPort);
    } catch (final NumberFormatException e) {

      /* Throw user error */
      throw new ScribeException(ScribeResponseCodes._1003 + "CRM integration information is invalid: CRM Port");
    }
  }

  /**
   * @return the zdAPISubPath
   */
  public final String getZdAPISubPath() {
    return this.zdAPISubPath;
  }

  /**
   * @param zdAPISubPath the zdAPISubPath to set
   */
  public final void setZdAPISubPath(final String zdAPISubPath) {
    this.zdAPISubPath = zdAPISubPath;
  }

  public final String getCrmFieldsSeparator() {
    return crmFieldsSeparator;
  }

  public final void setCrmFieldsSeparator(final String crmFieldsSeparator) {
    this.crmFieldsSeparator = crmFieldsSeparator;
  }

  public final String getOrderFieldsSeparator() {
    return orderFieldsSeparator;
  }

  public final void setOrderFieldsSeparator(final String orderFieldsSeparator) {
    this.orderFieldsSeparator = orderFieldsSeparator;
  }

  public final ZDCRMSessionManager getzDCRMSessionManager() {
    return zDCRMSessionManager;
  }

  public final void setzDCRMSessionManager(final ZDCRMSessionManager zDCRMSessionManager) {
    this.zDCRMSessionManager = zDCRMSessionManager;
  }
}
