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

package com.inbravo.scribe.rest.service.crm.cache;

import java.util.Map;

import org.apache.axis.client.Stub;

import com.inbravo.scribe.rest.resource.ScribeCommandObject;
import com.inbravo.scribe.rest.resource.ScribeMetaObject;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ScribeCacheObject implements BasicObject {

  private static final long serialVersionUID = 1L;

  /* For SFDC/NS CRM */
  private Stub soapStub;

  private ScribeMetaObject scribeMetaObject;

  /* For MS/NS CRM */
  private Map<String, String> additionalInfo;

  public ScribeCacheObject(final ScribeMetaObject scribeMetaObject) {
    this.scribeMetaObject = scribeMetaObject;
  }

  public Stub getSoapStub() {
    return soapStub;
  }

  public final void setSoapStub(final Stub soapStub) {
    this.soapStub = soapStub;
  }

  public final Map<String, String> getAdditionalInfo() {
    return additionalInfo;
  }

  public final void setAdditionalInfo(final Map<String, String> additionalInfo) {
    this.additionalInfo = additionalInfo;
  }


  public ScribeMetaObject getScribeMetaObject() {
    return scribeMetaObject;
  }

  public void setScribeMetaObject(final ScribeMetaObject scribeMetaObject) {
    this.scribeMetaObject = scribeMetaObject;
  }

  /**
   * 
   * @param cADCommandObject
   * @return
   */
  public static final ScribeCacheObject build(final ScribeCommandObject cADCommandObject) {

    /* Create new meta object */
    final ScribeMetaObject metaObject = new ScribeMetaObject();
    metaObject.setCrmUserId(cADCommandObject.getCrmUserId());
    metaObject.setCrmPassword(cADCommandObject.getCrmPassword());
    metaObject.setCrmType(cADCommandObject.getCrmType());

    /* Set meta info in command object */
    cADCommandObject.setMetaObject(metaObject);

    return new ScribeCacheObject(cADCommandObject.getMetaObject());
  }

  /**
   * 
   * @param cADMetaObject
   * @return
   */
  public static final ScribeCacheObject build(final ScribeMetaObject cADMetaObject) {

    return new ScribeCacheObject(cADMetaObject);
  }
}
