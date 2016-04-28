package com.inbravo.cad.rest.resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EDSA")
@XmlAccessorType(XmlAccessType.FIELD)
public final class CADCommandObject {

  @XmlElement(name = "IntTansId")
  private String intTansId;

  @XmlElement(name = "ExtTransId")
  private String extTransId;

  @XmlElement(name = "CrmUserId")
  private String crmUserId;

  @XmlElement(name = "CrmPassword")
  private String crmPassword;

  private String ObjectType;

  @XmlElement(name = "CRMType")
  private String crmType;

  @XmlElement(name = "Batch")
  private String batch;

  @XmlElement(name = "Error")
  private String error;

  @XmlElement(name = "Object")
  private CADObject[] cADObject;

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


  public final String getIntTansId() {
    return intTansId;
  }

  public final void setIntTansId(final String intTansId) {
    this.intTansId = intTansId;
  }

  public final String getExtTransId() {
    return extTransId;
  }

  public final void setExtTransId(final String extTransId) {
    this.extTransId = extTransId;
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

  public final CADObject[] getcADObject() {
    return cADObject;
  }

  public final void setcADObject(CADObject[] cADObject) {
    this.cADObject = cADObject;
  }

  public final String getTimeZone() {
    return this.timeZone;
  }

  public final void setTimeZone(final String timeZone) {
    this.timeZone = timeZone;
  }
}
