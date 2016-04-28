package com.inbravo.cad.rest.constants;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface CRMConstants {

  public static final String salesForceCRM = "sfdc";

  public static final String salesForceWCRM = "sfdcw";

  public static final String coCRM = "co";

  public static final String customCRM = "custom";

  public static final String microsoftCRM = "MS";

  public static final String zendeskCRM = "ZD";

  public static final String netsuiteCRM = "NS";

  public static final String zohoCRM = "ZH";


  public enum CRMTypes {
    SalesForce, Contactual, Microsoft, Zendesk, NetSuite
  };

  public enum UserType {
    Tenant, Agent
  };

  public enum CTLCRMObjectType {
    Contact, FollowUp, Case, Customer, OLTP, OLAP, OLTPRCDG, CtlAttachments, Task
  };

  public enum CTLCRMCustomFieldType {
    LST, TXT, DAT, NUM, URL, TXU, NUU
  };

  public enum CTLCRMPickListStatus {
    ACT, DEF
  }

  public enum CTLCRMPickListUsability {
    NUL, UNA, OPN, CLS, INT, CHT
  }

  public enum CTLLDAPObjectType {
    AgentSkills, QueueSkills, Schedules
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

  public enum CtlAttachmentCRMObjectType {
    FAQ, Case, Followup, Draft
  };

  public enum FBMCObjectType {
    ValidateUser, ChangePin
  };
}
