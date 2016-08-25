package com.inbravo.scribe.external.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Random;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.util.URIUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MicroSoftAccountTest {

  private String agentId = "crmtest~~ag2";
  private String tenantId = "crmtest";
  private String cadURL = "http://localhost:8080/cad/";

  public MicroSoftAccountTest() throws Exception {}

  /**
   * Test Case to test create and search account.
   * 
   * @throws ParserConfigurationException
   * @throws HttpException
   * @throws IOException
   */
  @org.junit.Test
  public final void Create_Search_Account() throws ParserConfigurationException, HttpException, IOException {
    Document doc = createDocumentobject();

    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    Element root = doc.createElement("Scribe");
    doc.appendChild(root);

    String queryString = "";
    if (agentId != null && !agentId.trim().equalsIgnoreCase("")) {
      Element agent = doc.createElement("Agent");
      agent.setTextContent(agentId);
      root.appendChild(agent);

      queryString = "?Agent=" + agentId;
    } else if (tenantId != null && !tenantId.trim().equalsIgnoreCase("")) {
      Element tenant = doc.createElement("Tenant");
      tenant.setTextContent(tenantId);
      root.appendChild(tenant);

      queryString = "?Tenant=" + tenantId;
    } else {
      System.out.println("TenantId or AgentId not found.");
      return;
    }
    final Element object = doc.createElement("Object");
    root.appendChild(object);

    final Element telephone1 = doc.createElement("telephone1");
    telephone1.setTextContent("123" + value);
    object.appendChild(telephone1);

    final String name = "name" + value;
    final Element eName = doc.createElement("name");
    eName.setTextContent(name);
    object.appendChild(eName);

    final Element emailaddress1 = doc.createElement("emailaddress1");
    emailaddress1.setTextContent(value + "@CLT20.com");
    object.appendChild(emailaddress1);

    final String accountXml = getStringFromDocument(doc);

    final HttpClient httpClient = new HttpClient();

    try {

      final int resultOfCreate = createAccount(httpClient, accountXml);

      assertEquals("User updation is failed", HttpStatus.SC_OK, resultOfCreate);

      final String criterion = "name=" + name;

      final int resultOfSearch = searchAccount(httpClient, criterion, queryString);

      assertEquals("User search is failed", HttpStatus.SC_OK, resultOfSearch);
    } catch (final Exception exception) {
      System.out.println(exception);
      fail("Found exception");
    }
  }

  /**
   * This test case will try to create an invalid account
   * 
   * @throws ParserConfigurationException
   */
  @org.junit.Test
  public final void Create_Invalid_Account() throws ParserConfigurationException {

    final Document doc = createDocumentobject();

    final Element root = doc.createElement("Scribe");
    doc.appendChild(root);

    final Element object = doc.createElement("Object");

    root.appendChild(object);

    final String accountXml = getStringFromDocument(doc);

    final HttpClient httpClient = new HttpClient();

    /* Step 2: Call create account */
    try {
      final int resultOfCreate = createAccount(httpClient, accountXml);

      assertEquals("Account could not be created.", Status.BAD_REQUEST.getStatusCode(), resultOfCreate);

    } catch (final Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;

        /* Check status code. It should be 404 */
        assertEquals("Account could not be created.", webApplicationException.getResponse().getStatus(), Status.BAD_REQUEST.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

  }

  /**
   * creates a account in MS. account Xml is passed as request body.
   * 
   * @param httpClient
   * @param accountXml
   * @return
   * @throws HttpException
   * @throws URIException
   */

  private final int createAccount(final HttpClient httpClient, final String accountXml) throws Exception {

    final PostMethod post = new PostMethod(cadURL + "/object/account?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    final RequestEntity entity = new StringRequestEntity(accountXml, "application/xml", "ISO-8859-1");
    post.setRequestEntity(entity);
    int result = 0;
    try {
      result = httpClient.executeMethod(post);
    } catch (final Exception e) {
      throw e;
    } finally {
      if (post != null) {
        post.releaseConnection();
      }
    }
    return result;
  }

  /**
   * Search an account in MS CRM on the basis of the criterion.
   * 
   * @param httpClient
   * @param criterion
   * @param queryString
   * @return
   * @throws Exception
   */
  private final int searchAccount(final HttpClient httpClient, final String criterion, final String queryString) throws Exception {
    final GetMethod get = new GetMethod(cadURL + "/object/account/" + criterion);
    get.setQueryString(URIUtil.encodeQuery(queryString));
    get.addRequestHeader("Accept", "application/xml");
    int result = 0;
    try {
      /* Execute get method */
      result = httpClient.executeMethod(get);
    } catch (final Exception e) {
      throw e;
    } finally {
      if (get != null) {
        get.releaseConnection();
      }
    }
    return result;
  }

  /**
   * Creates a new Document object
   * 
   * @return
   * @throws ParserConfigurationException
   */
  private final Document createDocumentobject() throws ParserConfigurationException {
    final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    final Document doc = docBuilder.newDocument();
    return doc;
  }

  /**
   * Converts Document object to String.
   * 
   * @param doc
   * @return
   */
  private final String getStringFromDocument(final Document doc) {
    try {
      final DOMSource domSource = new DOMSource(doc);
      final StringWriter writer = new StringWriter();
      final StreamResult result = new StreamResult(writer);
      final TransformerFactory tf = TransformerFactory.newInstance();
      final Transformer transformer = (Transformer) tf.newTransformer();
      transformer.transform(domSource, result);
      return writer.toString();
    } catch (final TransformerException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
