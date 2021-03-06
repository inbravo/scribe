package com.inbravo.scribe.external.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Element;

import com.inbravo.scribe.external.junit.core.ScribeClientMgmt;
import com.inbravo.scribe.rest.resource.ScribeCommandObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public class SalesForceObjectCountTest {

  private String agentId = "crmtest~~ag2";
  private String tenantId = "crmtest";
  private String cadURL = "http://localhost:8080/cad/";

  ScribeClientMgmt sFAccountMgmt;

  /* Create JAXBContext for the object */
  JAXBContext context;

  /* Create marshaler */
  Unmarshaller unmarshaller;

  public SalesForceObjectCountTest() throws Exception {
    context = JAXBContext.newInstance(ScribeCommandObject.class);
    unmarshaller = context.createUnmarshaller();
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Account_Object_Count_For_Agent() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/account/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForAgent(agentId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get Accounts counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get Accounts counts is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Lead_Object_Count_For_Agent() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/lead/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForAgent(agentId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get Leads counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get Leads counts is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Case_Object_Count_For_Agent() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/case/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForAgent(agentId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get case counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get case counts is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Contact_Object_Count_For_Agent() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/contact/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForAgent(agentId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get contacts counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get contacts counts is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Campaign_Object_Count_For_Agent() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/campaign/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForAgent(agentId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get campaigns counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get campaigns counts is failed", webApplicationException.getResponse().getStatus(),
            Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Account_Object_Count_For_Tenant() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/account/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForTenant(tenantId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get Accounts counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get Accounts counts is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Lead_Object_Count_For_Tenant() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/lead/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForTenant(tenantId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get Leads counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get Leads counts is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Case_Object_Count_For_Tenant() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/case/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForTenant(tenantId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get case counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get case counts is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Contact_Object_Count_For_Tenant() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/contact/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForTenant(tenantId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get contacts counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get contacts counts is failed", webApplicationException.getResponse().getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Campaign_Object_Count_For_Tenant() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/campaign/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForTenant(tenantId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get campaigns counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get campaigns counts is failed", webApplicationException.getResponse().getStatus(),
            Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Action_Object_Count_For_Tenant_With_Dynamic_Query() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/campaign/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForTenantWithDynamicQuery(tenantId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get campaigns counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get campaigns counts is failed", webApplicationException.getResponse().getStatus(),
            Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Action_Object_Count_For_Agent_With_Dynamic_Query() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/account/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForAgentWithDynamicQuery(agentId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertTrue("Get campaigns counts is failed", countStatus);
    } catch (Exception e) {
      if (e instanceof WebApplicationException) {
        WebApplicationException webApplicationException = (WebApplicationException) e;
        /* Check status code. It should be 404 */
        assertEquals("Get campaigns counts is failed", webApplicationException.getResponse().getStatus(),
            Status.INTERNAL_SERVER_ERROR.getStatusCode());
      } else {
        fail("Failed due to exception");
      }
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Invalid_Object_Count_For_Tenant() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/NARCOXX/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForTenant(tenantId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertFalse("Get campaigns counts is failed", countStatus);
    } catch (Exception e) {
      /* Test is passed if exceptions comes */
    }
  }

  /**
   * This test case will try to first create then update then delete an object
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void Test_Invalid_Object_Count_For_Agent() throws Exception {

    sFAccountMgmt = new ScribeClientMgmt(cadURL, "/object/NARCOXX/count");

    try {
      /* Get CAD command object from response */
      ScribeCommandObject cADCommandObject = sFAccountMgmt.getScribeCommandObjectForAgent(agentId);

      /* Check if count node exists in response */
      boolean countStatus = false;
      List<Element> elementList = cADCommandObject.getObject()[0].getXmlContent();
      for (Element element : elementList) {
        if (element.getNodeName().equalsIgnoreCase("Count")) {
          countStatus = true;
        }
      }
      assertFalse("Get invalid object counts is failed", countStatus);
    } catch (Exception e) {
      /* Test is passed if exceptions comes */
    }
  }
}
