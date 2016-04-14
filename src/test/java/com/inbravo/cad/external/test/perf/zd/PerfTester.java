package com.inbravo.cad.external.test.perf.zd;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Calculates the time in creating large no of users.
 * 
 * @author vikas.verma
 * 
 */
public final class PerfTester {

  public static Long passedCases = 0L;

  public static Long failedCases = 0L;

  public static float globalTime = 0L;

  /**
   * @param args
   * @throws Exception
   */
  @SuppressWarnings("unused")
  public static final void main(final String[] args) throws Exception {

    final String edsaUrl = "http://vm05dev185:8080/edsa";
    final String type = "agent";
    final String Id = "dev6ctl~~amit";
    final String queries = "1";
    final String emailInput = null;
    System.out.println("---Inside main EDSA URL: " + edsaUrl + " & user type: " + type + " & user id " + Id + " & number of querie(s): " + queries);

    if (edsaUrl == null || type == null || Id == null || queries == null) {
      System.err.println("Please provide EDSA URL/User type/Id/No of querie(s) as input");

      /* Return */
      System.exit(0);
    }

    final int rand = new Random().nextInt(1000);
    final String name = "Load User " + rand;
    String email = null;
    String queryString = "";

    /* Check if email is given as input */
    if (emailInput == null) {

      System.out.println("---Inside main user has not provided the email. Going for creating user, first");
      email = "LoadEmail" + rand + "@test.com";

      /* Check for type of users and create a user */
      if (type.equalsIgnoreCase("agent")) {
        new CreateUser(name, email, edsaUrl, Id, null).createUser();
        queryString = "agent=" + Id;
      } else if (type.equalsIgnoreCase("tenant")) {
        new CreateUser(name, email, edsaUrl, null, Id).createUser();
        queryString = "tenant=" + Id;
      } else {
        System.err.println("Please provide either agent or tenant type of user");

        /* Return */
        System.exit(0);
      }

      /* Minutes * seconds * milliseconds */
      final long sleepTime = 3 * 60 * 1000;

      System.out.println("---Inside main current thread is going to sleep for " + sleepTime
          + " millisecond(s). Zen-Desk CRM does not gives, newly created objects, immediately.");
      Thread.sleep(sleepTime);
      System.out.println("---Inside main current thread is live now; going to send " + queries + " EDSA read request(s)");
    } else {

      System.out.println("---Inside main user has provided the email. Going for direct search");
      email = emailInput;

      /* Check for type of users and create a user */
      if (type.equalsIgnoreCase("agent")) {
        queryString = "agent=" + Id;
      } else if (type.equalsIgnoreCase("tenant")) {
        queryString = "tenant=" + Id;
      } else {
        System.err.println("Please provide either agent or tenant type of user");

        /* Return */
        System.exit(0);
      }
    }

    /* Create thread pool */
    final ExecutorService pool = Executors.newFixedThreadPool(100);

    /* future objects to store time taken */
    final Set<Future<Float>> set = new HashSet<Future<Float>>();

    /* Add dummy batch id in query */
    queryString = queryString + "&batch=dummy";

    /* search records */
    for (int i = 1; i <= Integer.parseInt(queries); i++) {

      /* Check for type of users */
      final Callable<Float> callable = new SearchUser("email=" + email, queryString, edsaUrl);

      final Future<Float> future = pool.submit(callable);

      /* Add future in set */
      set.add(future);
    }

    float totalTime = 0L;

    /* Get future object */
    for (final Future<Float> future : set) {

      /* Get search time */
      final float searchTime = future.get();

      /* Add all times */
      totalTime += searchTime;
    }

    /* Convert nano seconds to milliseconds */
    totalTime = totalTime / 1000000F;

    System.out.println("---Inside main total time taken to search " + queries + " record(s) in duration : " + totalTime + " millisecond(s) or "
        + (totalTime / 1000F) + " second(s) or " + (totalTime / (60000F)) + " minute(s)" + " & global time: " + (PerfTester.globalTime / 1000000F)
        + " & number of cases passed: " + PerfTester.passedCases + " & number of cases failed: " + PerfTester.failedCases);

    /* Exit smooth */
    System.exit(0);
  }

  /**
   * Creates a new Document object
   * 
   * @return
   * @throws ParserConfigurationException
   */
  public final static Document createDocumentobject() throws ParserConfigurationException {
    final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    return docBuilder.newDocument();
  }

  /**
   * Converts Document object to String.
   * 
   * @param doc
   * @return
   */
  public static String getStringFromDocument(final Document doc) throws Exception {
    try {
      final DOMSource domSource = new DOMSource(doc);
      final StringWriter writer = new StringWriter();
      final StreamResult result = new StreamResult(writer);
      final TransformerFactory tf = TransformerFactory.newInstance();
      final Transformer transformer = tf.newTransformer();
      transformer.transform(domSource, result);
      return writer.toString();
    } catch (final TransformerException ex) {
      throw ex;
    }
  }
}


/**
 * Class to create user in Zen desk
 * 
 * @author vikas.verma
 * 
 */
final class CreateUser implements Callable<Long> {
  private String name;
  private String edsaUrl;
  private String agentId;
  private String tenantId;
  private String email;

  public CreateUser(String name, String email, String edsaUrl, String agentId, String tenantId) {
    super();
    this.name = name;
    this.email = email;
    this.edsaUrl = edsaUrl;
    this.agentId = agentId;
    this.tenantId = tenantId;

  }

  @Override
  public final Long call() throws Exception {
    return createUser();
  }

  public final Long createUser() throws Exception {
    final Document doc = PerfTester.createDocumentobject();

    final Element root = doc.createElement("EDSA");
    doc.appendChild(root);
    if (agentId != null && !agentId.trim().equalsIgnoreCase("")) {
      final Element agent = doc.createElement("Agent");
      agent.setTextContent(agentId);
      root.appendChild(agent);

    } else if (tenantId != null && !tenantId.trim().equalsIgnoreCase("")) {
      final Element tenant = doc.createElement("Tenant");
      tenant.setTextContent(tenantId);
      root.appendChild(tenant);
    } else {
      System.out.println("----TenantId or AgentId not found.");
      return 0L;
    }
    final Element object = doc.createElement("Object");
    root.appendChild(object);

    object.setAttribute("objectType", "user");

    final Element emailElement = doc.createElement("email");

    emailElement.setTextContent(email);
    object.appendChild(emailElement);

    final Element nameElement = doc.createElement("name");
    nameElement.setTextContent(name);
    object.appendChild(nameElement);

    final Element roles = doc.createElement("roles");
    roles.setTextContent("4");
    object.appendChild(roles);

    final Element restrictionId = doc.createElement("restriction-id");
    restrictionId.setTextContent("1");
    object.appendChild(restrictionId);

    final Element currentTags = doc.createElement("current-tags");
    currentTags.setTextContent("Impetus");
    object.appendChild(currentTags);

    final String userXml = PerfTester.getStringFromDocument(doc);

    final Long startTime = System.currentTimeMillis();

    try {
      /* Create user */
      this.createUserAtEDSA(userXml);
    } catch (final Exception e) {
      System.err.println("***Error recieved in creating user: " + e);
    }
    final Long endTime = System.currentTimeMillis();

    /* Return time difference */
    return (endTime - startTime);
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
  private final int createUserAtEDSA(final String userXml) throws Exception {

    /* Create new Http client */
    final HttpClient httpClient = new HttpClient();

    /* Create new Post method */
    final PostMethod post = new PostMethod(this.edsaUrl + "/object/user?_type=xml");

    /* Add request header */
    post.addRequestHeader("Content-Type", "application/xml");

    /* Create new request entity */
    final RequestEntity entity = new StringRequestEntity(userXml, "application/xml", "ISO-8859-1");
    post.setRequestEntity(entity);

    int result = 0;
    try {

      /* Execute the post method */
      result = httpClient.executeMethod(post);
      System.out.println("---Inside createUserAtEDSA URL : " + post.getURI() + " & response code: " + result + " & body: "
          + post.getResponseBodyAsString());
    } catch (final Exception e) {
      throw e;
    } finally {
      post.releaseConnection();
    }
    return result;
  }
}


/**
 * Class to search user in Zen desk
 * 
 * @author vikas.verma
 * 
 */
final class SearchUser implements Callable<Float> {

  private String criterion;
  private String queryString;
  private String edsaUrl;

  public SearchUser(final String criterion, final String queryString, final String edsaUrl) {
    super();
    this.criterion = criterion;
    this.queryString = queryString;
    this.edsaUrl = edsaUrl;
  }

  @Override
  public final Float call() throws Exception {
    final float startTime = System.nanoTime();
    try {

      /* Search CRM object at EDSA */
      final int resultCode = this.searchObjectAtEDSA(criterion, queryString);

      if (resultCode == HttpStatus.SC_OK) {
        PerfTester.passedCases++;
      } else {
        PerfTester.failedCases++;
      }

      /* Count query time */
      final float queryTime = System.nanoTime() - startTime;

      /* Increment the global time */
      PerfTester.globalTime = PerfTester.globalTime + queryTime;

      System.out.println("---Inside call, time taken for " + (PerfTester.passedCases + PerfTester.failedCases) + " number(s) of EDSA queries: "
          + PerfTester.globalTime + " nanosecond(s)");

      /* Return time difference */
      return queryTime;
    } catch (final Exception e) {
      throw e;
    }
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
  private final int searchObjectAtEDSA(final String criterion, final String queryString) throws Exception {
    final HttpClient httpClient = new HttpClient();
    final GetMethod get = new GetMethod(this.edsaUrl + "/object/ANY/" + criterion);
    get.setQueryString(URIUtil.encodeQuery(queryString));
    get.addRequestHeader("Accept", "application/xml");
    int result = 0;
    try {
      /* Execute get method */
      result = httpClient.executeMethod(get);
      System.out.println("---Inside searchObjectAtEDSA URL : " + get.getURI() + " & response code: " + result + " & body: "
          + get.getResponseBodyAsString());
    } catch (final Exception e) {
      throw e;
    } finally {
      get.releaseConnection();
    }
    return result;
  }
}
