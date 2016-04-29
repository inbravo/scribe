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
public class SalesForceLeadTest {

  private String agentId = "crmtest~~ag2";
  private String cadURL = "http://localhost:8080/cad/";

  CADClientMgmt sFLeadMgmt;

  /* Create JAXBContext for the object */
  JAXBContext context;

  /* Create marshaler */
  Unmarshaller unmarshaller;

  public SalesForceLeadTest() throws Exception {
    sFLeadMgmt = new CADClientMgmt(cadURL, "/object/lead");
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
  public void Create_Update_Delete_Lead_With_Agent() throws ParserConfigurationException, TransformerException {

    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Lead */

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode(""));

    /* Add Lead information */
    Element fname = doc.createElement("Firstname");
    fname.appendChild(doc.createTextNode("FnameLead" + value));

    /* Add Lead information */
    Element lname = doc.createElement("Lastname");
    lname.appendChild(doc.createTextNode("LnameLead" + value));

    /* Add Lead information */
    Element comp = doc.createElement("Company");
    comp.appendChild(doc.createTextNode("CompanyLead" + value));

    /* Add Lead information */
    Element phone = doc.createElement("Phone");
    phone.appendChild(doc.createTextNode("777.444.3333"));

    /* Create CADObject */
    CADObject edsaObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();
    arr.add(fname);
    arr.add(lname);
    arr.add(comp);
    arr.add(phone);
    arr.add(id);

    /* Set element array at the CAD object */
    edsaObject.setXmlContent(arr);

    /* Create Lead object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 1: Call create lead */
    try {
      response = sFLeadMgmt.createObject(cADCommandObject);

      /* Retrieve the object the object */
      cADCommandObject = (CADCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Lead creation is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Lead creation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call update lead */

    String leadId = "";
    List<Element> elementList = cADCommandObject.getcADObject()[0].getXmlContent();
    for (Element element : elementList) {
      if (element.getNodeName().equalsIgnoreCase("Id")) {
        leadId = element.getFirstChild().getNodeValue();
        break;
      }

    }
    /* update phone no in CAD object */

    Element updatedPhone = doc.createElement("Phone");
    updatedPhone.appendChild(doc.createTextNode("333.444.7777"));

    /* Add created Id field */
    Element createdId = doc.createElement("Id");
    createdId.appendChild(doc.createTextNode(leadId));

    arr.clear();
    arr.add(fname);
    arr.add(lname);
    arr.add(comp);
    arr.add(updatedPhone);
    arr.add(createdId);

    /* Set element array at the CAD object */
    edsaObject.setXmlContent(arr);

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});

    try {
      response = sFLeadMgmt.updateObject(cADCommandObject);

      /* Retrieve the object */
      cADCommandObject = (CADCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Lead updation is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Lead updation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call delete lead */
    try {

      response = sFLeadMgmt.deleteObject(leadId, "agent", agentId);

      assertEquals("Lead is updated successfully.", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Lead updation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
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
  public void Update_Non_Existing_Lead_With_Agent() throws ParserConfigurationException {
    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Lead */

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add Lead information */
    Element fname = doc.createElement("Firstname");
    fname.appendChild(doc.createTextNode("JUNIT_" + value + "F_NAME"));

    /* Add Lead information */
    Element lname = doc.createElement("Lastname");
    lname.appendChild(doc.createTextNode("JUNIT_" + value + "L_NAME"));

    /* Add Lead information */
    Element comp = doc.createElement("Company");
    comp.appendChild(doc.createTextNode("JUNIT_" + value + "Comp_NAME"));

    /* Add Lead information */
    Element phone = doc.createElement("Phone");
    phone.appendChild(doc.createTextNode("723.444.3909"));

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode("invalidId"));

    /* Create CADObject */
    CADObject edsaObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();
    arr.add(fname);
    arr.add(lname);
    arr.add(comp);
    arr.add(phone);
    arr.add(id);

    /* Set element array at the CAD object */
    edsaObject.setXmlContent(arr);

    /* Create Lead object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 2: Call update lead */
    try {
      response = sFLeadMgmt.updateObject(cADCommandObject);
      assertEquals("Lead doesnot exist", Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Lead doesnot exist", webApplicationException.getResponse().getStatus(), Status.SERVICE_UNAVAILABLE.getStatusCode());
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
  public void Create_Invalid_Lead_With_Agent() throws ParserConfigurationException {

    /* Create CADObject */
    CADObject edsaObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();

    /* Set element array at the CAD object */
    edsaObject.setXmlContent(arr);

    /* Create Lead object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});
    cADCommandObject.setCrmUserId(agentId);
    Response response = null;
    /* Step 2: Call update lead */
    try {
      response = sFLeadMgmt.createObject(cADCommandObject);
      assertEquals("Lead could not be created.", Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Lead could not be created.", webApplicationException.getResponse().getStatus(), Status.BAD_REQUEST.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }
}
