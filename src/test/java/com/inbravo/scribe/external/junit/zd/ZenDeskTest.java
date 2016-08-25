package com.inbravo.scribe.external.junit.zd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Random;

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
 * @author vikas.verma
 * 
 */
public class ZenDeskTest {

  private String agentId = System.getProperty("AGENT_ID");
  private String tenantId = System.getProperty("TENANT_ID");
  private String cadURL = System.getProperty("SCRIBE_URL");

  public ZenDeskTest() throws Exception {}

  /**
   * Test Case to test create and search user.
   * 
   * @throws ParserConfigurationException
   * @throws HttpException
   * @throws IOException
   */
  @org.junit.Test
  public void Create_Search_User() throws ParserConfigurationException, HttpException, IOException {
    Document doc = createDocumentobject();

    Element root = doc.createElement("CAD");
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
    Element object = doc.createElement("Object");
    root.appendChild(object);

    int rand = new Random().nextInt(1000);
    Element email = doc.createElement("email");

    final String emailText = "testUser" + rand + "@impetus.co.in";
    email.setTextContent(emailText);
    object.appendChild(email);

    Element name = doc.createElement("name");
    final String nameText = "Test User" + rand + "@impetus.co.in";
    name.setTextContent(nameText);
    object.appendChild(name);

    Element roles = doc.createElement("roles");
    roles.setTextContent("4");
    object.appendChild(roles);

    Element restrictionId = doc.createElement("restriction-id");
    restrictionId.setTextContent("1");
    object.appendChild(restrictionId);

    Element currentTags = doc.createElement("current-tags");
    currentTags.setTextContent("Impetus");
    object.appendChild(currentTags);

    String userXml = getStringFromDocument(doc);
    System.out.println("User XML: " + userXml);

    HttpClient httpClient = new HttpClient();

    try {
      int resultOfCreate = createUser(httpClient, userXml);
      System.out.println("resultOfCreate: " + resultOfCreate);

      assertEquals("User creation is failed", HttpStatus.SC_OK, resultOfCreate);

      String criterion = "email=" + emailText;

      int resultOfSearch = searchUser(httpClient, criterion, queryString);
      System.out.println("resultOfSearch: " + resultOfSearch);

      assertEquals("User search is failed", HttpStatus.SC_OK, resultOfSearch);
    } catch (final Exception exception) {
      System.out.println(exception);
      fail("Found exception");
    }
  }

  /**
   * creates a user in Zendesk. UserXml is passed as request body.
   * 
   * @param httpClient
   * @param userXml
   * @return
   * @throws HttpException
   * @throws IOException
   */
  private int createUser(HttpClient httpClient, String userXml) throws Exception {

    PostMethod post = new PostMethod(cadURL + "/object/user?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new StringRequestEntity(userXml, "application/xml", "ISO-8859-1");
    post.setRequestEntity(entity);
    int result = 0;
    try {
      System.out.println("URI: " + post.getURI());
      result = httpClient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());

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
   * Search a user in Zendesk on the basis of the criterion.
   * 
   * @param httpClient
   * @param criterion
   * @param queryString
   * @return
   * @throws URIException
   */
  private int searchUser(HttpClient httpClient, String criterion, String queryString) throws Exception {
    final GetMethod get = new GetMethod(cadURL + "/object/ANY/" + criterion);
    get.setQueryString(URIUtil.encodeQuery(queryString));
    get.addRequestHeader("Accept", "application/xml");
    int result = 0;
    try {
      /* Execute get method */
      result = httpClient.executeMethod(get);
      System.out.println("Response body: " + get.getResponseBodyAsString());
    } catch (Exception e) {
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
  private Document createDocumentobject() throws ParserConfigurationException {
    DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    Document doc = docBuilder.newDocument();

    return doc;
  }

  /**
   * Converts Document object to String.
   * 
   * @param doc
   * @return
   */
  private String getStringFromDocument(Document doc) {
    try {
      DOMSource domSource = new DOMSource(doc);
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.transform(domSource, result);
      return writer.toString();
    } catch (TransformerException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
