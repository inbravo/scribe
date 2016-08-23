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
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Scribe")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ScribeCommandObject {

  @XmlElement(name = "IntTansId")
  private String intTansId;

  @XmlElement(name = "ExtTransId")
  private String extTransId;

  private String ObjectType;

  @XmlElement(name = "Batch")
  private String batch;

  @XmlElement(name = "Error")
  private String error;

  @XmlElement(name = "timezone")
  private String timeZone;

  @XmlElement(name = "MetaObject")
  private ScribeMetaObject metaObject;

  @XmlElement(name = "Object")
  private ScribeObject[] object;

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
    return this.metaObject.getCrmUserId();
  }

  public final void setCrmUserId(final String crmUserId) {
    this.metaObject.setCrmUserId(crmUserId);
  }

  public final String getCrmPassword() {
    return this.metaObject.getCrmPassword();
  }

  public final void setCrmPassword(final String crmPassword) {
    this.metaObject.setCrmPassword(crmPassword);
  }

  public String getObjectType() {
    return ObjectType;
  }

  public void setObjectType(final String objectType) {
    ObjectType = objectType;
  }

  public final String getCrmType() {
    return this.metaObject.getCrmType();
  }

  public final void setCrmType(final String crmType) {
    this.metaObject.setCrmType(crmType);
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

  public ScribeMetaObject getMetaObject() {
    return metaObject;
  }

  public void setMetaObject(final ScribeMetaObject metaObject) {
    this.metaObject = metaObject;
  }

  public final ScribeObject[] getObject() {
    return object;
  }

  public final void setObject(final ScribeObject[] object) {
    this.object = object;
  }

  public final String getTimeZone() {
    return this.timeZone;
  }

  public final void setTimeZone(final String timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  public String toString() {
    return "ScribeCommandObject [intTansId=" + intTansId + ", extTransId=" + extTransId + ", ObjectType=" + ObjectType + ", batch=" + batch
        + ", error=" + error + ", timeZone=" + timeZone + ", metaObject=" + metaObject + ", object=" + Arrays.toString(object) + "]";
  }
}
