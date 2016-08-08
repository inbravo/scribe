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

package com.inbravo.scribe.rest.service.crm.ms.dto;

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
