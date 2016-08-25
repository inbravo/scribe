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
public final class MicroSoftIncidentTest {

  private String agentId = "crmtest~~ag2";
  private String tenantId = "crmtest";
  private String cadURL = "http://localhost:8080/cad/";

  public MicroSoftIncidentTest() throws Exception {}

  /**
   * Test Case to test create and search Incident.
   * 
   * @throws ParserConfigurationException
   * @throws HttpException
   * @throws IOException
   */
  @org.junit.Test
  public void Create_Search_Incident() throws ParserConfigurationException, HttpException, IOException {
    final Document doc = createDocumentobject();

    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    final Element root = doc.createElement("Scribe");
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

    final String titleOfIncident = "incident" + value;
    final Element title = doc.createElement("title");
    title.setTextContent(titleOfIncident);
    object.appendChild(title);

    final Element customerid = doc.createElement("customerid");
    customerid.setTextContent("{DBBAFC7F-FEE4-E011-9346-002655875BB9}");
    customerid.setAttribute("name", "NokiaChampion21");
    customerid.setAttribute("type", "account");
    object.appendChild(customerid);

    final String userXml = getStringFromDocument(doc);
    final HttpClient httpClient = new HttpClient();

    try {

      final int resultOfCreate = this.createIncident(httpClient, userXml);

      assertEquals("User updation is failed", HttpStatus.SC_OK, resultOfCreate);

      final String criterion = "title=" + titleOfIncident;

      int resultOfSearch = this.searchIncident(httpClient, criterion, queryString);

      assertEquals("User search is failed", HttpStatus.SC_OK, resultOfSearch);
    } catch (final Exception exception) {
      System.out.println(exception);
      fail("Found exception");
    }
  }

  /**
   * This test case will try to create an invalid incident
   * 
   * @throws ParserConfigurationException
   */
  @org.junit.Test
  public final void Create_Invalid_Incident() throws ParserConfigurationException {

    final Document doc = createDocumentobject();
    final Element root = doc.createElement("Scribe");
    doc.appendChild(root);

    final Element object = doc.createElement("Object");
    root.appendChild(object);

    final Element transactioncurrencyid = doc.createElement("transactioncurrencyid");
    transactioncurrencyid.setTextContent("C31633B3-FCE4-E011-865F-D8D3855B354E");
    object.appendChild(transactioncurrencyid);

    final Element customerid = doc.createElement("customerid");
    customerid.setTextContent("{DBBAFC7F-FEE4-E011-9346-002655}");
    customerid.setAttribute("name", "nkmk");
    customerid.setAttribute("type", "account");
    object.appendChild(customerid);

    final String incidentXml = this.getStringFromDocument(doc);
    final HttpClient httpClient = new HttpClient();

    /* Step 2: Call create incident */
    try {

      final int resultOfCreate = createIncident(httpClient, incidentXml);

      assertEquals("Incident could not be created.", Status.BAD_REQUEST.getStatusCode(), resultOfCreate);

    } catch (final Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;

        /* Check status code. It should be 404 */
        assertEquals("Incident could not be created.", webApplicationException.getResponse().getStatus(), Status.BAD_REQUEST.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

  }

  /**
   * creates an incident in MS. incident Xml is passed as request body.
   * 
   * @param httpClient
   * @param userXml
   * @return
   * @throws HttpException
   * @throws IOException
   */
  private final int createIncident(final HttpClient httpClient, final String userXml) throws Exception {

    final PostMethod post = new PostMethod(cadURL + "/object/incident?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    final RequestEntity entity = new StringRequestEntity(userXml, "application/xml", "ISO-8859-1");
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
   * Search an incident in microsoft on the basis of the criterion.
   * 
   * @param httpClient
   * @param criterion
   * @param queryString
   * @return
   * @throws URIException
   */
  private final int searchIncident(final HttpClient httpClient, final String criterion, final String queryString) throws Exception {
    final GetMethod get = new GetMethod(cadURL + "/object/incident/" + criterion);
    get.setQueryString(URIUtil.encodeQuery(queryString));
    get.addRequestHeader("Accept", "application/xml");
    int result = 0;
    try {
      /* Execute get method */
      result = httpClient.executeMethod(get);
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
