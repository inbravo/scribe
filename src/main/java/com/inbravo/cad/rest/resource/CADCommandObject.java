package com.inbravo.cad.rest.resource;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EDSA")
@XmlAccessorType(XmlAccessType.FIELD)
public final class CADCommandObject {

  @XmlElement(name = "EDSATransId")
  private String eDSATransId;

  @XmlElement(name = "ExtTransId")
  private String extTransId;

  @XmlElement(name = "Agent")
  private String agent;

  @XmlElement(name = "Tenant")
  private String tenant;

  private String ObjectType;

  @XmlElement(name = "CRMType")
  private String crmType;

  @XmlElement(name = "Batch")
  private String batch;

  @XmlElement(name = "Error")
  private String error;

  @XmlElement(name = "Object")
  private CADObject[] eDSAObject;

  @XmlElement(name = "ExternalServiceType")
  private String externalServiceType;

  @XmlElement(name = "ExternalUsername")
  private String externalUsername;

  @XmlElement(name = "ExternalPassword")
  private String externalPassword;

  @XmlElement(name = "timezone")
  private String timeZone;

  @XmlElement(name = "ExternalEndpointURL")
  private String externalEndpointURL;

  public final CADObject[] geteDSAObject() {
    return eDSAObject;
  }

  public final void seteDSAObject(final CADObject[] eDSAObject) {
    if (eDSAObject != null) {
      this.eDSAObject = eDSAObject.clone();
    }
  }

  public final String geteDSATransId() {
    return eDSATransId;
  }

  public final void seteDSATransId(final String eDSATransId) {
    this.eDSATransId = eDSATransId;
  }

  public final String getExtTransId() {
    return extTransId;
  }

  public final void setExtTransId(final String extTransId) {
    this.extTransId = extTransId;
  }

  public final String getAgent() {
    return agent;
  }

  public final void setAgent(final String agent) {
    this.agent = agent;
  }

  public final String getTenant() {
    return tenant;
  }

  public final void setTenant(final String tenant) {
    this.tenant = tenant;
  }

  public String getObjectType() {
    return ObjectType;
  }

  public void setObjectType(final String objectType) {
    ObjectType = objectType;
  }

  public final String getCrmType() {
    return crmType;
  }

  public final void setCrmType(final String crmType) {
    this.crmType = crmType;
  }

  public final void setExternalServiceType(final String externalServiceType) {
    this.externalServiceType = externalServiceType;
  }

  public final String getExternalServiceType() {
    return this.externalServiceType;
  }

  public final void setExternalUsername(final String externalUsername) {
    this.externalUsername = externalUsername;
  }

  public final String getExternalUsername() {
    return this.externalUsername;
  }

  public final void setExternalEndpointURL(final String externalEndpointURL) {
    this.externalEndpointURL = externalEndpointURL;
  }

  public final String getExternalEndpointURL() {
    return this.externalEndpointURL;
  }

  public final void setExternalPassword(final String externalPassword) {
    this.externalPassword = externalPassword;
  }

  public final String getExternalPassword() {
    return this.externalPassword;
  }

  public final String getBatch() {
    return batch;
  }

  public final void setBatch(final String batch) {
    this.batch = batch;
  }

  public final String getError() {
    return error;
  }

  public final void setError(final String error) {
    this.error = error;
  }

  /**
   * @return the timeZone
   */
  public final String getTimeZone() {
    return this.timeZone;
  }

  /**
   * @param timeZone the timeZone to set
   */
  public final void setTimeZone(final String timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  protected final void finalize() throws Throwable {

    agent = null;
    tenant = null;
    ObjectType = null;
    crmType = null;
    batch = null;
    eDSAObject = null;
    extTransId = null;
    eDSATransId = null;
    this.timeZone = null;
    super.finalize();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public final String toString() {
    return "EDSACommandObject [eDSATransId=" + this.eDSATransId + ", extTransId=" + this.extTransId + ", agent=" + this.agent + ", tenant="
        + this.tenant + ", ObjectType=" + this.ObjectType + ", crmType=" + this.crmType + ", batch=" + this.batch + ", error=" + this.error
        + ", eDSAObject=" + Arrays.toString(this.eDSAObject) + ", externalServiceType=" + this.externalServiceType + ", externalUsername="
        + this.externalUsername + ", externalPassword=" + this.externalPassword + ", timeZone=" + this.timeZone + "]";
  }
}
