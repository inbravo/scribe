/*
 * MIT License
 * 
 * Copyright (c) 2016 Amit Dixit (github.com/inbravo)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.inbravo.scribe.rest.constants;

/**
 * 
 * @author amit.dixit
 * 
 */
public interface CRMConstants {

  public static final String salesForceCRM = "SF";

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
