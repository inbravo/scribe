package com.inbravo.scribe.external.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.inbravo.scribe.external.junit.core.CADClientMgmt;
import com.inbravo.scribe.rest.resource.ScribeCommandObject;
import com.inbravo.scribe.rest.resource.ScribeObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public class SalesForceAccountTest {

  private String agentId = "crmtest~~ag2";
  private String cadURL = "http://localhost:8080/cad/";

  CADClientMgmt sFAccountMgmt;

  /* Create JAXBContext for the object */
  JAXBContext context;

  /* Create marshaler */
  Unmarshaller unmarshaller;

  public SalesForceAccountTest() throws Exception {
    sFAccountMgmt = new CADClientMgmt(cadURL, "/object/account");
    context = JAXBContext.newInstance(ScribeCommandObject.class);
    unmarshaller = context.createUnmarshaller();
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws ParserConfigurationException
   * @throws TransformerException
   */
  @org.junit.Test
  public void Create_Update_Delete_Account_With_Agent() throws ParserConfigurationException, TransformerException {

    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Account */

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode(""));

    /* Add Account information */
    Element name = doc.createElement("Name");
    name.appendChild(doc.createTextNode("Test" + value));

    /* Add Account information */
    Element phone = doc.createElement("Phone");
    phone.appendChild(doc.createTextNode("703.444.3909"));

    /* Create CADObject */
    ScribeObject cadObject = new ScribeObject();
    List<Element> arr = new ArrayList<Element>();
    arr.add(name);
    arr.add(phone);
    arr.add(id);

    /* Set element array at the CAD object */
    cadObject.setXmlContent(arr);

    /* Create Account object */
    ScribeCommandObject cADCommandObject = new ScribeCommandObject();

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setObject(new ScribeObject[] {cadObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 1: Call create account */
    try {
      response = sFAccountMgmt.createObject(cADCommandObject);

      /* Retrieve the object the object */
      cADCommandObject = (ScribeCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Account creation is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Account creation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call update account */
    String accountId = "";
    List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
    for (Element element : elementList) {
      if (element.getNodeName().equalsIgnoreCase("Id")) {
        accountId = element.getFirstChild().getNodeValue();
        break;
      }

    }
    /* Update phone no in CAD object */
    Element updatedPhone = doc.createElement("Phone");
    updatedPhone.appendChild(doc.createTextNode("703.444.3111"));

    /* Add created Id field */
    Element createdId = doc.createElement("Id");
    createdId.appendChild(doc.createTextNode(accountId));

    arr.clear();
    arr.add(name);
    arr.add(updatedPhone);
    arr.add(createdId);

    /* Set element array at the CAD object */
    cadObject.setXmlContent(arr);

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setObject(new ScribeObject[] {cadObject});

    try {
      response = sFAccountMgmt.updateObject(cADCommandObject);

      /* Retrieve the object */
      cADCommandObject = (ScribeCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Account updation is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Account updation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call delete account */
    try {

      response = sFAccountMgmt.deleteObject(accountId, "agent", agentId);

      assertEquals("Account deletion is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Account deletion is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to update a non existing object
   * 
   * @throws ParserConfigurationException
   */
  @org.junit.Test
  public void Update_Non_Existing_Account_With_Agent() throws ParserConfigurationException {
    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Account */
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add Account information */
    Element name = doc.createElement("Name");
    name.appendChild(doc.createTextNode("JUNIT_" + value + "_NAME"));

    /* Add Account information */
    Element phone = doc.createElement("Phone");
    phone.appendChild(doc.createTextNode("723.444.3909"));

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode("invalidId"));

    /* Create CADObject */
    ScribeObject cadObject = new ScribeObject();
    List<Element> arr = new ArrayList<Element>();
    arr.add(name);
    arr.add(phone);
    arr.add(id);

    /* Set element array at the CAD object */
    cadObject.setXmlContent(arr);

    /* Create Account object */
    ScribeCommandObject cADCommandObject = new ScribeCommandObject();

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setObject(new ScribeObject[] {cadObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 2: Call update account */
    try {
      response = sFAccountMgmt.updateObject(cADCommandObject);
      assertEquals("Account doesnot exist", Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Account doesnot exist", webApplicationException.getResponse().getStatus(), Status.SERVICE_UNAVAILABLE.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

  }

  /**
   * This test case will try to create an invalid object
   * 
   * @throws ParserConfigurationException
   */
  @org.junit.Test
  public void Create_Invalid_Account_With_Agent() throws ParserConfigurationException {

    /* Create CADObject */
    ScribeObject cadObject = new ScribeObject();
    List<Element> arr = new ArrayList<Element>();

    /* Set element array at the CAD object */
    cadObject.setXmlContent(arr);

    /* Create Account object */
    ScribeCommandObject cADCommandObject = new ScribeCommandObject();

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setObject(new ScribeObject[] {cadObject});
    cADCommandObject.setCrmUserId(agentId);
    Response response = null;
    /* Step 2: Call update account */
    try {
      response = sFAccountMgmt.createObject(cADCommandObject);
      assertEquals("Account could not be created.", Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Account could not be created.", webApplicationException.getResponse().getStatus(), Status.BAD_REQUEST.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }
}
