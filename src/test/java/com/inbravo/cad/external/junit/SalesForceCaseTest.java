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
public class SalesForceCaseTest {

  private String agentId = "crmtest~~ag2";
  private String cadURL = "http://localhost:8080/cad/";

  CADClientMgmt sFCaseMgmt;

  /* Create JAXBContext for the object */
  JAXBContext context;

  /* Create marshaler */
  Unmarshaller unmarshaller;

  public SalesForceCaseTest() throws Exception {
    sFCaseMgmt = new CADClientMgmt(cadURL, "/object/case");
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
  public void Create_Update_Delete_Case_With_Agent() throws ParserConfigurationException, TransformerException {

    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Case */

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode(""));

    /* Add Case information */
    Element status = doc.createElement("Status");
    status.appendChild(doc.createTextNode("StatusCase" + value));

    /* Add Case information */
    Element reason = doc.createElement("Reason");
    reason.appendChild(doc.createTextNode("ReasonCase" + value));

    /* Create EDSAObject */
    CADObject edsaObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();
    arr.add(status);
    arr.add(reason);

    arr.add(id);

    /* Set element array at the EDSA object */
    edsaObject.setXmlContent(arr);

    /* Create Case object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set EDSA object and agentID in EDSACommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 1: Call create case */
    try {
      response = sFCaseMgmt.createObject(cADCommandObject);

      /* Retrieve the object the object */
      cADCommandObject = (CADCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Case creation is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Case creation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call update case */

    String caseId = "";
    List<Element> elementList = cADCommandObject.getcADObject()[0].getXmlContent();
    for (Element element : elementList) {
      if (element.getNodeName().equalsIgnoreCase("Id")) {
        caseId = element.getFirstChild().getNodeValue();
        break;
      }

    }
    /* update phone no in EDSA object */

    /* Add Case information */
    Element updateStatus = doc.createElement("Status");
    updateStatus.appendChild(doc.createTextNode("NewStatusCase" + value));

    /* Add Case information */
    Element updateReason = doc.createElement("Reason");
    updateReason.appendChild(doc.createTextNode("NewReasonCase" + value));

    /* Add created Id field */
    Element createdId = doc.createElement("Id");
    createdId.appendChild(doc.createTextNode(caseId));

    arr.clear();
    arr.add(updateStatus);
    arr.add(updateReason);
    arr.add(createdId);

    /* Set element array at the EDSA object */
    edsaObject.setXmlContent(arr);

    /* Set EDSA object and agentID in EDSACommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});

    try {
      response = sFCaseMgmt.updateObject(cADCommandObject);

      /* Retrieve the object */
      cADCommandObject = (CADCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Case is updated successfully.", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Case updation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call delete case */
    try {

      response = sFCaseMgmt.deleteObject(caseId, "agent", agentId);

      assertEquals("Case deletion is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Case deletion is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
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
  public void Update_Non_Existing_Case_With_Agent() throws ParserConfigurationException {
    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Case */

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add Case information */
    Element status = doc.createElement("Status");
    status.appendChild(doc.createTextNode("StatusCase" + value));

    /* Add Case information */
    Element reason = doc.createElement("Reason");
    reason.appendChild(doc.createTextNode("ReasonCase" + value));

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode("invalidId"));

    /* Create EDSAObject */
    CADObject edsaObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();
    arr.add(status);
    arr.add(reason);

    arr.add(id);

    /* Set element array at the EDSA object */
    edsaObject.setXmlContent(arr);

    /* Create Case object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set EDSA object and agentID in EDSACommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 2: Call update case */
    try {
      response = sFCaseMgmt.updateObject(cADCommandObject);
      assertEquals("Case doesnot exist", Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Case doesnot exist", webApplicationException.getResponse().getStatus(), Status.SERVICE_UNAVAILABLE.getStatusCode());
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
  public void Create_Invalid_Case_With_Agent() throws ParserConfigurationException {

    /* Create EDSAObject */
    CADObject edsaObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();

    /* Set element array at the EDSA object */
    edsaObject.setXmlContent(arr);

    /* Create Case object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set EDSA object and agentID in EDSACommandObject */
    cADCommandObject.setcADObject(new CADObject[] {edsaObject});
    cADCommandObject.setCrmUserId(agentId);
    Response response = null;
    /* Step 2: Call update case */
    try {
      response = sFCaseMgmt.createObject(cADCommandObject);
      assertEquals("Case could not be created.", Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Case could not be created.", webApplicationException.getResponse().getStatus(), Status.BAD_REQUEST.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }
}
