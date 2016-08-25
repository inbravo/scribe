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

package com.inbravo.scribe.rest.resource;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * 
 * @author amit.dixit
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class ScribeMetaObject {

  @XmlElement(name = "CrmUserId")
  private String crmUserId;

  @XmlTransient
  private String crmPassword;

  @XmlElement(name = "CrmType")
  private String crmType;

  @XmlElement(name = "CrmURL")
  private String crmURL;

  @XmlElement(name = "CrmAccountId")
  private String crmAccountId;

  @XmlTransient
  private String crmSessionId;

  @XmlTransient
  private String crmOrgName;

  @XmlElement(name = "CrmServiceURL")
  private String crmServiceURL;

  @XmlElement(name = "CrmServiceProtocol")
  private String crmServiceProtocol;

  @XmlElement(name = "CrmPort")
  private String crmPort;

  @XmlTransient
  private String[] crmSecurityToken;

  public final String getCrmSessionId() {
    return crmSessionId;
  }

  public final void setCrmSessionId(final String crmSessionId) {
    this.crmSessionId = crmSessionId;
  }

  public final String getCrmOrgName() {
    return crmOrgName;
  }

  public final void setCrmOrgName(final String crmOrgName) {
    this.crmOrgName = crmOrgName;
  }

  public final String getCrmServiceURL() {
    return crmServiceURL;
  }

  public final void setCrmServiceURL(final String crmServiceURL) {
    this.crmServiceURL = crmServiceURL;
  }

  public final String getCrmServiceProtocol() {
    return crmServiceProtocol;
  }

  public final void setCrmServiceProtocol(final String crmServiceProtocol) {
    this.crmServiceProtocol = crmServiceProtocol;
  }

  public final String getCrmPort() {
    return crmPort;
  }

  public final void setCrmPort(final String crmPort) {
    this.crmPort = crmPort;
  }

  public final String getCrmUserId() {
    return crmUserId;
  }

  public final void setCrmUserId(final String crmUserId) {
    this.crmUserId = crmUserId;
  }

  public final String getCrmPassword() {
    return crmPassword;
  }

  public final void setCrmPassword(final String crmPassword) {
    this.crmPassword = crmPassword;
  }

  public final String getCrmType() {
    return crmType;
  }

  public final void setCrmType(final String crmType) {
    this.crmType = crmType;
  }

  public final String getCrmURL() {
    return crmURL;
  }

  public final void setCrmURL(final String crmURL) {
    this.crmURL = crmURL;
  }

  public final String getCrmAccountId() {
    return crmAccountId;
  }

  public final void setCrmAccountId(final String crmAccountId) {
    this.crmAccountId = crmAccountId;
  }

  public String[] getCrmSecurityToken() {
    return crmSecurityToken;
  }

  public void setCrmSecurityToken(final String[] crmSecurityToken) {
    this.crmSecurityToken = crmSecurityToken;
  }

  @Override
  public final String toString() {
    return "ScribeMetaObject [crmUserId=" + crmUserId + ", crmPassword=" + crmPassword + ", crmType=" + crmType + ", crmURL=" + crmURL
        + ", crmAccountId=" + crmAccountId + ", crmSessionId=" + crmSessionId + ", crmOrgName=" + crmOrgName + ", crmServiceURL=" + crmServiceURL
        + ", crmServiceProtocol=" + crmServiceProtocol + ", crmPort=" + crmPort + ", crmSecurityToken=" + Arrays.toString(crmSecurityToken) + "]";
  }
}
