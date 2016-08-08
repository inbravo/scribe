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
import com.inbravo.scribe.rest.resource.CADCommandObject;
import com.inbravo.scribe.rest.resource.CADObject;

/**
 * 
 * @author Saurabh.Jain
 * 
 */
public class SalesForceCampaignTest {

  private String agentId = "crmtest~~ag2";
  private String cadURL = "http://localhost:8080/cad/";

  CADClientMgmt sFCampaignMgmt;

  /* Create JAXBContext for the object */
  JAXBContext context;

  /* Create marshaler */
  Unmarshaller unmarshaller;

  public SalesForceCampaignTest() throws Exception {
    sFCampaignMgmt = new CADClientMgmt(cadURL, "/object/campaign");
    context = JAXBContext.newInstance(CADCommandObject.class);
    unmarshaller = context.createUnmarshaller();
  }

  /**
   * This test campaign will try to first create then update then delete an object
   * 
   * @throws ParserConfigurationException
   * @throws TransformerException
   */
  @org.junit.Test
  public void Create_Update_Delete_Campaign_With_Agent() throws ParserConfigurationException, TransformerException {

    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Campaign */

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode(""));

    /* Add Campaign information */
    Element name = doc.createElement("Name");
    name.appendChild(doc.createTextNode("NameCampaign" + value));

    /* Add Campaign information */
    Element startdate = doc.createElement("StartDate");
    startdate.appendChild(doc.createTextNode("2010-12-12"));

    /* Create CADObject */
    CADObject cadObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();

    arr.add(startdate);
    arr.add(name);
    arr.add(id);

    /* Set element array at the CAD object */
    cadObject.setXmlContent(arr);

    /* Create Campaign object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setcADObject(new CADObject[] {cadObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 1: Call create campaign */
    try {
      response = sFCampaignMgmt.createObject(cADCommandObject);

      /* Retrieve the object the object */
      cADCommandObject = (CADCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Campaign is created successfully.", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Campaign creation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call update campaign */
    String campaignId = "";
    List<Element> elementList = cADCommandObject.getcADObject()[0].getXmlContent();
    for (Element element : elementList) {
      if (element.getNodeName().equalsIgnoreCase("Id")) {
        campaignId = element.getFirstChild().getNodeValue();
        break;
      }

    }
    /* update phone no in CAD object */

    /* Add Campaign information */
    Element updateStatus = doc.createElement("Status");
    updateStatus.appendChild(doc.createTextNode("NewStatusCampaign" + value));

    /* Add Campaign information */
    Element updateStartdate = doc.createElement("StartDate");
    updateStartdate.appendChild(doc.createTextNode("2010-02-10"));

    /* Add created Id field */
    Element createdId = doc.createElement("Id");
    createdId.appendChild(doc.createTextNode(campaignId));

    arr.clear();

    arr.add(updateStartdate);
    arr.add(createdId);

    /* Set element array at the CAD object */
    cadObject.setXmlContent(arr);

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setcADObject(new CADObject[] {cadObject});

    try {
      response = sFCampaignMgmt.updateObject(cADCommandObject);

      /* Retrieve the object */
      cADCommandObject = (CADCommandObject) unmarshaller.unmarshal((InputStream) response.getEntity());

      assertEquals("Campaign is updated successfully.", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Campaign updation is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

    /* Step 2: Call delete campaign */
    try {
      response = sFCampaignMgmt.deleteObject(campaignId, "agent", agentId);
      assertEquals("Campaign deletion is failed", Status.OK.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Campaign deletion is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

  }

  /**
   * This test campaign will try to update a non existing object
   * 
   * @throws ParserConfigurationException
   */
  @org.junit.Test
  public void Update_Non_Existing_Campaign_With_Agent() throws ParserConfigurationException {
    /* Create a random number */
    Random rand = new Random();
    int value = rand.nextInt(1000);

    /* Create an Campaign */

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Use the factory to create a builder
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();

    /* Add Campaign information */
    Element status = doc.createElement("Status");
    status.appendChild(doc.createTextNode("StatusCampaign" + value));

    /* Add Campaign information */
    Element startdate = doc.createElement("StartDate");
    startdate.appendChild(doc.createTextNode("2010-02-02"));

    /* Add Campaign information */
    Element name = doc.createElement("Name");
    name.appendChild(doc.createTextNode("NameCampaign" + value));

    /* Add blank Id field */
    Element id = doc.createElement("Id");
    id.appendChild(doc.createTextNode("invalidId"));

    /* Create CADObject */
    CADObject cadObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();
    arr.add(status);
    arr.add(startdate);
    arr.add(name);
    arr.add(id);

    /* Set element array at the CAD object */
    cadObject.setXmlContent(arr);

    /* Create Campaign object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setcADObject(new CADObject[] {cadObject});
    cADCommandObject.setCrmUserId(agentId);

    Response response = null;
    /* Step 2: Call update campaign */
    try {
      response = sFCampaignMgmt.updateObject(cADCommandObject);
      assertEquals("Campaign doesnot exist", Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Campaign does not exist", webApplicationException.getResponse().getStatus(), Status.SERVICE_UNAVAILABLE.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }

  }

  /**
   * This test campaign will try to create an invalid object
   * 
   * @throws ParserConfigurationException
   */
  @org.junit.Test
  public void Create_Invalid_Campaign_With_Agent() throws ParserConfigurationException {

    /* Create CADObject */
    CADObject cadObject = new CADObject();
    List<Element> arr = new ArrayList<Element>();

    /* Set element array at the CAD object */
    cadObject.setXmlContent(arr);

    /* Create Campaign object */
    CADCommandObject cADCommandObject = new CADCommandObject();

    /* Set CAD object and agentID in CADCommandObject */
    cADCommandObject.setcADObject(new CADObject[] {cadObject});
    cADCommandObject.setCrmUserId(agentId);
    Response response = null;
    /* Step 2: Call update campaign */
    try {
      response = sFCampaignMgmt.createObject(cADCommandObject);
      assertEquals("Campaign could not be created.", Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Campaign could not be created.", webApplicationException.getResponse().getStatus(), Status.BAD_REQUEST.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }
}
