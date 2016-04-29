package com.inbravo.cad.rest.constants;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface CRMConstants {

  public static final String salesForceCRM = "sfdc";

  public static final String coCRM = "co";

  public static final String microsoftCRM = "MS";

  public static final String zendeskCRM = "ZD";

  public static final String netsuiteCRM = "NS";

  public static final String zohoCRM = "ZH";

  public enum CRMTypes {
    SalesForce, Microsoft, Zendesk, NetSuite
  };

  public enum UserType {
    Tenant, Agent
  };

  public enum MSCRMObjectType {
    Contact, Account, Task, Lead, Opportunity, Incident, Case
  };

  public enum MSCRMSchemaType {
    Account_Tasks, Contact_Tasks, Lead_Tasks, Opportunity_Tasks, Incident_Tasks,
  };

  public enum MSCRMVersionType {
    V4, V5
  };

  public enum ZDCRMObjectType {
    User, Ticket
  };

  public enum ZDCRMFieldType {
    Email, Description
  };

  public enum NSCRMObjectType {
    Customer, Contact, Employee, Task, SupportCase, PhoneCall, Opportunity
  };
}
