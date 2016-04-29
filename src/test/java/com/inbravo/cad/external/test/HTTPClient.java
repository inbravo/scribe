package com.inbravo.cad.external.test;

import java.io.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is a class to test the CAD Change edsaURL and test files path testFolder
 * 
 * @author amit.dixit
 * 
 */
public class HTTPClient {

  private static final String cadURL = "http://localhost:8080/cad";

  private static String testFolder = "D:/personal/mygithub/cad/docs/test-files/";

  public static void main(final String[] args) throws Exception {

    showMenu();

    String choice = getUserInput();
    switch (new Integer(choice).intValue()) {
      case 1:
        createAccount();
        break;
      case 2:
        updateAccount();
        break;
      case 3:
        deleteAccount();
        break;
      case 4:
        createCustomer();
        break;
      case 5:
        updateCustomer();
        break;
      case 6:
        createLead();
        break;
      case 7:
        createContact();
        break;
      case 8:
        createFAQCatagory();
        break;
      case 9:
        readCustomers();
        break;
      case 10:
        login();
        break;
      case 11:
        createUser();
        break;
      case 12:
        updateUser();
        break;
      case 13:
        deleteUser();
        break;
      case 14:
        createTask();
        break;
      case 15:
        createOpportunity();
        break;
      case 16:
        createIncident();
        break;
      case 17:
        createSupportCase();
        break;
      case 18:
        createPhoneCall();
        break;
      case 19:
        createCase();
        break;
      case 20:
        createFollowup();
        break;
      case 21:
        updateContact();
        break;
      case 22:
        updateCase();
        break;
      case 23:
        updateFollowup();
        break;
    }
  }

  private static void showMenu() {

    System.out.println(" 1. Create Account");
    System.out.println(" 2. Update Account");
    System.out.println(" 3. Delete Account");
    System.out.println(" 4. Create Customer");
    System.out.println(" 5. Update Customer");
    System.out.println(" 6. Create Lead");
    System.out.println(" 7. Create Contact");
    System.out.println(" 8. Create FAQCatagory");
    System.out.println(" 9. Read Customers");
    System.out.println(" 10. Tenant login");
    System.out.println(" 11. Create user");
    System.out.println(" 12. Update user");
    System.out.println(" 13. Delete user");
    System.out.println(" 14. Create task");
    System.out.println(" 15. Create opportunity");
    System.out.println(" 16. Create incident");
    System.out.println(" 17. Create SupportCase");
    System.out.println(" 18. Create PhoneCall");
    System.out.println(" 19. Create Case");
    System.out.println(" 20. Create Followup");
    System.out.println(" 21. Update Contact");
    System.out.println(" 22. Update Case");
    System.out.println(" 23. Update Followup");
    System.out.println("Enter the number of the operation to run (e.g. Enter 1 for Create Customer): ");
  }

  private static String getUserInput() {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
      return br.readLine();
    } catch (IOException ex) {
      return null;
    }
  }

  private static void createAccount() throws HttpException, IOException {

    File input = new File(testFolder + "Account.xml");
    PostMethod post =
        new PostMethod(cadURL + "/object/User?_type=xml&externalServiceType=FBMC&externalUsername=8x8webservice&externalPassword=E4qtjWZLYKre");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void updateAccount() throws HttpException, IOException {
    System.out.println("Sent HTTP PUT request to update Account");
    File input = new File(testFolder + "Account.xml");
    PutMethod put =
        new PutMethod(cadURL + "/object/User?_type=xml&externalServiceType=FBMC&externalUsername=8x8webservice&externalPassword=E4qtjWZLYKre");
    put.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    put.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      int result = httpclient.executeMethod(put);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + put.getResponseBodyAsString());
    } finally {
      put.releaseConnection();
    }
  }

  private static void deleteAccount() throws HttpException, IOException, Exception {
    System.out.println("Sent HTTP DELETE request to delete Account");
    String accountId = null;
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(new File(testFolder + "Account.xml"));
    NodeList nodeList = doc.getElementsByTagName("Id");
    if (nodeList != null) {
      for (int i = 0; i <= nodeList.getLength(); i++) {
        Node accountIdNode = nodeList.item(i);
        if (accountIdNode != null) {
          System.out.println("Account Id " + accountIdNode.getTextContent());
          accountId = accountIdNode.getTextContent();
        }
      }
    }

    URL url = null;
    try {
      url = new URL(cadURL + "/object/account/" + accountId + "?_type=xml&agent=sfdcten~~ag1");
    } catch (MalformedURLException mue) {
      System.err.println(mue);
    }

    DeleteMethod delete = new DeleteMethod(url.toString());
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + delete.getURI());
      int result = httpclient.executeMethod(delete);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + delete.getResponseBodyAsString());
    } finally {
      delete.releaseConnection();
    }
  }

  private static void createCustomer() throws HttpException, IOException {
    System.out.println("Sent HTTP POST request to add Customer");
    File input = new File(testFolder + "Customer.xml");
    PostMethod post = new PostMethod(cadURL + "/object/customer?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void updateCustomer() throws HttpException, IOException {
    System.out.println("Sent HTTP PUT request to update Customer");
    File input = new File(testFolder + "Customer.xml");
    PutMethod put = new PutMethod(cadURL + "/object/customer?_type=xml");
    put.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    put.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + put.getURI());
      int result = httpclient.executeMethod(put);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + put.getResponseBodyAsString());
    } finally {
      put.releaseConnection();
    }
  }

  private static void createLead() throws HttpException, IOException {
    System.out.println("Sent HTTP POST request to create Lead");
    File input = new File(testFolder + "Lead.xml");
    PostMethod post = new PostMethod(cadURL + "/object/lead?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void createContact() throws HttpException, IOException {
    System.out.println("Sent HTTP POST request to createContact");
    File input = new File(testFolder + "Contact.xml");
    PostMethod post = new PostMethod(cadURL + "/object/oltp?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void updateContact() throws HttpException, IOException {
    System.out.println("Sent HTTP POST request to createContact");
    File input = new File(testFolder + "Contact.xml");
    PutMethod put = new PutMethod(cadURL + "/object/oltp?_type=xml");
    put.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    put.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + put.getURI());
      int result = httpclient.executeMethod(put);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + put.getResponseBodyAsString());
    } finally {
      put.releaseConnection();
    }
  }

  private static void createFAQCatagory() throws HttpException, IOException {
    System.out.println("Sent HTTP POST request to createFAQCatagory");
    File input = new File(testFolder + "FAQCatagory.xml");
    PostMethod post = new PostMethod(cadURL + "/object/faqcategory?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void readCustomers() throws HttpException, IOException {

    for (int i = 0; i < 1000; i++) {

      new Thread() {
        public void run() {
          GetMethod post = new GetMethod(cadURL + "/object/customer?_type=xml&tenant=dev1ten2");
          post.addRequestHeader("Content-Type", "application/xml");
          HttpClient httpclient = new HttpClient();
          try {

            try {
              System.out.println("URI: " + post.getURI());
              int result = httpclient.executeMethod(post);
              System.out.println("Response status code: " + result);
              System.out.println("Response body: " + post.getResponseBodyAsString());
            } catch (Exception exception) {
              System.err.println(exception);
            }
          } finally {
            post.releaseConnection();
          }
        }
      };
    }
  }

  private static void login() throws HttpException, IOException {
    System.out.println("Sent HTTP POST request to createContact");
  }

  private static void createUser() throws HttpException, IOException {

    File input = new File(testFolder + "User.xml");
    PostMethod post = new PostMethod(cadURL + "/object/user?_type=xml&batch=dummy");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("hii");
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void updateUser() throws HttpException, IOException {

    File input = new File(testFolder + "User.xml");
    PutMethod put = new PutMethod(cadURL + "/object/user?_type=xml");
    put.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    put.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + put.getURI());
      int result = httpclient.executeMethod(put);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + put.getResponseBodyAsString());
    } finally {
      put.releaseConnection();
    }
  }

  private static void deleteUser() throws HttpException, IOException, Exception {
    System.out.println("Sent HTTP DELETE request to delete Account");
  }

  private static void createTask() throws HttpException, IOException {

    File input = new File(testFolder + "Task.xml");
    PostMethod post = new PostMethod(cadURL + "/object/task?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void createSupportCase() throws HttpException, IOException {

    File input = new File(testFolder + "SupportCase.xml");
    PostMethod post = new PostMethod(cadURL + "/object/supportcase?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void createPhoneCall() throws HttpException, IOException {

    File input = new File(testFolder + "PhoneCall.xml");
    PostMethod post = new PostMethod(cadURL + "/object/phonecall?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void createOpportunity() throws HttpException, IOException {

    File input = new File(testFolder + "Opportunity.xml");
    PostMethod post = new PostMethod(cadURL + "/object/opportunity?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void createIncident() throws HttpException, IOException {

    File input = new File(testFolder + "Incident.xml");
    PostMethod post = new PostMethod(cadURL + "/object/incident?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void createCase() throws HttpException, IOException {

    File input = new File(testFolder + "Case.xml");
    PostMethod post = new PostMethod(cadURL + "/object/oltp?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void updateCase() throws HttpException, IOException {

    File input = new File(testFolder + "Case.xml");
    PutMethod post = new PutMethod(cadURL + "/object/oltp?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void createFollowup() throws HttpException, IOException {

    File input = new File(testFolder + "Followup.xml");
    PostMethod post = new PostMethod(cadURL + "/object/oltp?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }

  private static void updateFollowup() throws HttpException, IOException {

    File input = new File(testFolder + "Followup.xml");
    PutMethod post = new PutMethod(cadURL + "/object/oltp?_type=xml");
    post.addRequestHeader("Content-Type", "application/xml");
    RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
    post.setRequestEntity(entity);
    HttpClient httpclient = new HttpClient();
    try {
      System.out.println("URI: " + post.getURI());
      int result = httpclient.executeMethod(post);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + post.getResponseBodyAsString());
    } finally {
      post.releaseConnection();
    }
  }
}
