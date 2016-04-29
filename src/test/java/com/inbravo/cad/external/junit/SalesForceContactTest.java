package com.inbravo.cad.external.junit;

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

import com.inbravo.cad.external.junit.core.CADClientMgmt;
import com.inbravo.cad.rest.resource.CADCommandObject;
import com.inbravo.cad.rest.resource.CADObject;

/**
 * 
 * @author Saurabh.Jain
 * 
 */
public class SalesForceContactTest {

  private String agentId = "crmtest~~ag2";
  private String cadURL = "http://localhost:8080/cad/";

  CADClientMgmt sFContactMgmt;

  /* Create JAXBContext for the object */
  JAXBContext context;

  /* Create marshaler */
  Unmarshaller unmarshaller;

  public SalesForceContactTest() throws Exception {
    sFContactMgmt = new CADClientMgmt(cadURL, "/object/contact");
    context = JAXBContext.newInstance(CADCommandObject.class);
    unmarshaller = context.createUnmarshaller();
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws ParserConfigurationException
   * @throws TransformerException
   */
  @org.junit.Test
  public void Create_Update_Delete_Contact_With_Agent() throws ParserConfigurationException, TransformerException {

    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Contact */

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode(""));

    /* Add Contact information */
    Element fname = doc.createElement("Firstname");
    fname.appendChild(doc.createTextNode("FnameContact" + value));

    /* Add Contact information */
    Element lname = doc.createElement("Lastname");
    lname.appendChild(doc.createTextNode("LnameContact" + value));

    /* Create EDSAObject */
    CADObject edsaObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();
    arr.add(fname);
    arr.add(lname);

    arr.add(id);

    /* Set element array at the EDSA object */
    edsaObject.setXmlContent(arr);

    /* Create Contact object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set EDSA object and agentID in EDSACommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 1: Call create contact */
    try {
      response = sFContactMgmt.createObject(cADCommandObject);

      /* Retrieve the object the object */
      cADCommandObject = (CADCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Contact creation is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Contact creation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call update contact */

    String contactId = "";
    List<Element> elementList = cADCommandObject.getcADObject()[0].getXmlContent();
    for (Element element : elementList) {
      if (element.getNodeName().equalsIgnoreCase("Id")) {
        contactId = element.getFirstChild().getNodeValue();
        break;
      }

    }
    /* update phone no in EDSA object */

    Element updatedFirstName = doc.createElement("Firstname");
    updatedFirstName.appendChild(doc.createTextNode("venky"));

    /* Add created Id field */
    Element createdId = doc.createElement("Id");
    createdId.appendChild(doc.createTextNode(contactId));

    arr.clear();
    arr.add(updatedFirstName);
    arr.add(lname);
    arr.add(createdId);

    /* Set element array at the EDSA object */
    edsaObject.setXmlContent(arr);

    /* Set EDSA object and agentID in EDSACommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});

    try {
      response = sFContactMgmt.updateObject(cADCommandObject);

      /* Retrieve the object */
      cADCommandObject = (CADCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Contact updation is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Contact updation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call delete contact */
    try {

      response = sFContactMgmt.deleteObject(contactId, "agent", agentId);

      assertEquals("Contact deletion is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Contact deletion is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
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
  public void Update_Non_Existing_Contact_With_Agent() throws ParserConfigurationException {
    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Contact */

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add Contact information */
    Element fname = doc.createElement("Firstname");
    fname.appendChild(doc.createTextNode("JUNIT_" + value + "F_NAME"));

    /* Add Contact information */
    Element lname = doc.createElement("Lastname");
    lname.appendChild(doc.createTextNode("JUNIT_" + value + "L_NAME"));

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode("invalidId"));

    /* Create EDSAObject */
    CADObject edsaObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();
    arr.add(fname);
    arr.add(lname);

    arr.add(id);

    /* Set element array at the EDSA object */
    edsaObject.setXmlContent(arr);

    /* Create Contact object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set EDSA object and agentID in EDSACommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 2: Call update contact */
    try {
      response = sFContactMgmt.updateObject(cADCommandObject);
      assertEquals("Contact doesnot exist", Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Contact doesnot exist", webApplicationException.getResponse().getStatus(), Status.SERVICE_UNAVAILABLE.getStatusCode());
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
  public void Create_Invalid_Contact_With_Agent() throws ParserConfigurationException {

    /* Create EDSAObject */
    CADObject edsaObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();

    /* Set element array at the EDSA object */
    edsaObject.setXmlContent(arr);

    /* Create Contact object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set EDSA object and agentID in EDSACommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});
    cADCommandObject.setCrmUserId(agentId);
    Response response = null;
    /* Step 2: Call update contact */
    try {
      response = sFContactMgmt.createObject(cADCommandObject);
      assertEquals("Contact could not be created.", Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Contact could not be created.", webApplicationException.getResponse().getStatus(), Status.BAD_REQUEST.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }
}
