package com.inbravo.cad.rest.service.crm.ms.dto;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class MSCRMUserInformation {

  private String organizationName;

  private String crmTicket;

  private String passportTicket;

  private String crmServiceURL;

  private String[] office365SecurityToken;

  public final String getCrmServiceURL() {
    return crmServiceURL;
  }

  public final void setCrmServiceURL(final String crmServiceURL) {
    this.crmServiceURL = crmServiceURL;
  }

  public final String getOrganizationName() {
    return organizationName;
  }

  public final void setOrganizationName(final String organizationName) {
    this.organizationName = organizationName;
  }

  public final String getCrmTicket() {
    return crmTicket;
  }

  public final void setCrmTicket(final String crmTicket) {
    this.crmTicket = crmTicket;
  }

  public final String getPassportTicket() {
    return passportTicket;
  }

  public final void setPassportTicket(final String passportTicket) {
    this.passportTicket = passportTicket;
  }

  /**
   * @return the office365SecurityToken
   */
  public final String[] getOffice365SecurityToken() {
    return this.office365SecurityToken;
  }

  /**
   * @param office365SecurityToken the office365SecurityToken to set
   */
  public final void setOffice365SecurityToken(final String[] office365SecurityToken) {

    if (office365SecurityToken != null) {
      this.office365SecurityToken = office365SecurityToken.clone();
    }
  }
}
