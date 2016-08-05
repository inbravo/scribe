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

package com.inbravo.cad.rest.service.crm.zd.auth.basic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.inbravo.cad.exception.CADException;
import com.inbravo.cad.exception.CADResponseCodes;
import com.inbravo.cad.rest.service.crm.zd.auth.ZDAuthManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZDBasicAuthManager implements ZDAuthManager {

  private final Logger logger = Logger.getLogger(ZDBasicAuthManager.class.getName());

  private final String USER_TAG = "user";

  private final String GROUPS_TAG = "groups";

  @Override
  public final String getSessionId(final String userId, final String password, final String crmURL, final String crmProtocol, final int port)
      throws Exception {
    logger.debug("---Inside login userId: " + userId + " & password: " + password + " & crmURL: " + crmURL + " & crmProtocol: " + crmProtocol
        + " & crmPort: " + port);

    /* Validate protocol */
    if (crmProtocol == null || "".equalsIgnoreCase(crmProtocol)) {

      /* Send user error */
      throw new CADException(CADResponseCodes._1003 + "Invalid protocol to communicate with ZD: " + crmProtocol);
    }

    /* Trick: Check all live users on CRM currently */
    final GetMethod getMethod = new GetMethod(crmProtocol + "://" + crmURL + "/users/current.xml");

    getMethod.addRequestHeader("Content-Type", "application/xml");
    final HttpClient httpclient = new HttpClient();

    /* Set credentials */
    httpclient.getState().setCredentials(new AuthScope(crmURL, port), new UsernamePasswordCredentials(userId, password));

    try {
      int result = httpclient.executeMethod(getMethod);
      logger.debug("---Inside getSessionId response code: " + result + " & body: " + getMethod.getResponseBodyAsString());

      /* Check for authentication */
      if (result == HttpStatus.SC_UNAUTHORIZED) {
        logger.debug("---Inside getSessionId found unauthorized user");
        throw new CADException(CADResponseCodes._1012 + "Anauthorized by Zendesk CRM");
      } else {
        final String sessionId = getMethod.getResponseHeader("Set-Cookie").getValue();
        logger.debug("---Inside getSessionId sessionId: " + sessionId);
        return sessionId;
      }
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } finally {
      /* Release connection socket */
      if (getMethod != null) {
        getMethod.releaseConnection();
      }
    }
  }

  @Override
  public final boolean login(final String userId, final String password, final String crmURL, final String crmProtocol, final int port)
      throws Exception {
    logger.debug("---Inside login userId: " + userId + " & password: " + password + " & crmURL: " + crmURL + " & crmProtocol: " + crmProtocol);

    /* Validate protocol */
    if (crmProtocol == null || "".equalsIgnoreCase(crmProtocol)) {

      /* Send user error */
      throw new CADException(CADResponseCodes._1003 + "Invalid protocol to communicate with ZD: " + crmProtocol);
    }

    /* Trick: Check all live users on CRM currently */
    final GetMethod getMethod = new GetMethod(crmProtocol + "://" + crmURL + "/users/current.xml");

    getMethod.addRequestHeader("Content-Type", "application/xml");
    final HttpClient httpclient = new HttpClient();

    /* Set credentials */
    httpclient.getState().setCredentials(new AuthScope(crmURL, port), new UsernamePasswordCredentials(userId, password));

    try {
      int result = httpclient.executeMethod(getMethod);
      logger.debug("---Inside login response code: " + result + " & body: " + getMethod.getResponseBodyAsString());

      /* Check for authentication */
      if (result == HttpStatus.SC_UNAUTHORIZED) {
        return false;
      } else {
        return true;
      }
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } finally {
      /* Release connection socket */
      if (getMethod != null) {
        getMethod.releaseConnection();
      }
    }
  }

  @Override
  public final Map<String, String> getSessionInfoAfterValidLogin(final String userId, final String password, final String crmURL,
      final String crmProtocol, final int port) throws Exception {
    logger.debug("---Inside getSessionInfoAfterValidLogin userId: " + userId + " & password: " + password + " & crmURL: " + crmURL
        + " & crmProtocol: " + crmProtocol);

    /* Validate protocol */
    if (crmProtocol == null || "".equalsIgnoreCase(crmProtocol)) {

      /* Send user error */
      throw new CADException(CADResponseCodes._1003 + "Invalid protocol to communicate with ZD: " + crmProtocol);
    }

    /* Trick: Check all live users on CRM currently */
    final GetMethod getMethod = new GetMethod(crmProtocol + "://" + crmURL + "/users/current.xml");

    getMethod.addRequestHeader("Content-Type", "application/xml");
    final HttpClient httpclient = new HttpClient();

    /* Set credentials */
    httpclient.getState().setCredentials(new AuthScope(crmURL, port), new UsernamePasswordCredentials(userId, password));

    try {
      int result = httpclient.executeMethod(getMethod);
      logger.debug("---Inside getSessionInfoAfterValidLogin response code: " + result + " & body: " + getMethod.getResponseBodyAsString());

      /* Check for authentication */
      if (result == HttpStatus.SC_UNAUTHORIZED) {

        /* Return */
        throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials");
      } else {

        /* Create document factory */
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
          /* Create document builder */
          final DocumentBuilder builder = factory.newDocumentBuilder();
          final Document document = builder.parse(getMethod.getResponseBodyAsStream());

          /* Check if document is not null */
          if (document != null) {

            /* Get node list from document */
            final NodeList nodeList = document.getElementsByTagName(USER_TAG);

            if (nodeList != null && nodeList.getLength() != 0) {

              /* Create node map */
              final Map<String, String> nodeMap = new HashMap<String, String>();

              /* Populate node map */
              for (int i = 0; i < nodeList.item(0).getChildNodes().getLength(); i++) {

                /* This trick is to avoid empty nodes */
                if (!nodeList.item(0).getChildNodes().item(i).getNodeName().contains("#text")) {

                  /* Add value in map */
                  if (nodeList.item(0).getChildNodes().item(i).getNodeValue() != null) {

                    /* If node has a value */
                    nodeMap.put(nodeList.item(0).getChildNodes().item(i).getNodeName(), nodeList.item(0).getChildNodes().item(i).getNodeValue());
                  } else {

                    /* Check for group tag */
                    if (nodeList.item(0).getChildNodes().item(i).getNodeName().equalsIgnoreCase(GROUPS_TAG)) {

                      /* Get groups node list */
                      final NodeList groupsNodeList = nodeList.item(0).getChildNodes().item(i).getChildNodes();

                      if (groupsNodeList != null) {

                        /* Iterate over groups node list */
                        for (int j = 0; j < groupsNodeList.getLength(); j++) {

                          /* Get group node list */
                          final NodeList groupNodeList = groupsNodeList.item(j).getChildNodes();

                          if (groupNodeList != null) {

                            /* Get group id */
                            for (int k = 0; k < groupNodeList.getLength(); k++) {

                              if (groupNodeList.item(k).getNodeName().equalsIgnoreCase("ID")) {
                                nodeMap.put("group_id", groupNodeList.item(k).getTextContent());
                              }
                            }
                          }
                        }
                      }
                    } else {
                      nodeMap.put(nodeList.item(0).getChildNodes().item(i).getNodeName(), nodeList.item(0).getChildNodes().item(i).getTextContent());
                    }
                  }
                }
              }

              return nodeMap;
            } else {
              throw new CADException(CADResponseCodes._1020 + "User/Org information is not found in response");
            }
          } else {
            return null;
          }
        } catch (final ParserConfigurationException exception) {
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials", exception);
        } catch (final IllegalStateException exception) {
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials", exception);
        } catch (final SAXException exception) {
          throw new CADException(CADResponseCodes._1012 + "Login attempt at ZD is failed. Check credentials", exception);
        }
      }
    } catch (final IOException exception) {
      throw new CADException(CADResponseCodes._1020 + "Communication error while communicating with Zendesk CRM", exception);
    } finally {
      /* Release connection socket */
      if (getMethod != null) {
        getMethod.releaseConnection();
      }
    }
  }
}
