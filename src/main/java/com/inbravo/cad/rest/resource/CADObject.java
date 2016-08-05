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

package com.inbravo.cad.rest.resource;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;

import org.w3c.dom.Element;

/**
 * 
 * @author amit.dixit
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class CADObject {

  @XmlAttribute
  private String ObjectType;

  @XmlAnyElement
  private List<Element> xmlContent;

  private List<Object> objectContent;

  public final String getObjectType() {
    return ObjectType;
  }

  public final void setObjectType(final String objectType) {
    ObjectType = objectType;
  }

  public final void setXmlContent(final List<Element> xmlContent) {
    this.xmlContent = xmlContent;
  }

  public final List<Element> getXmlContent() {
    return xmlContent;
  }

  public final List<Object> getObjectContent() {
    return this.objectContent;
  }

  public final void setObjectContent(final List<Object> objectContent) {
    this.objectContent = objectContent;
  }

  @Override
  protected final void finalize() throws Throwable {
    super.finalize();
  }

  @Override
  public final String toString() {

    String returnString = "CADObject [ObjectType=" + ObjectType;
    returnString = returnString + ", xmlContent=";

    if (xmlContent != null) {

      /* Iterate over elements */
      for (final Element element : xmlContent) {

        /* Create element value string */
        returnString = returnString + " { name: " + element.getNodeName();

        /* Get node value */
        if (element.getNodeValue() != null) {
          returnString = returnString + " & value: " + element.getNodeValue() + " }";
        } else {
          returnString = returnString + " & value: " + element.getTextContent() + " }";
        }
      }
    }
    returnString = returnString + " ]";
    return returnString;
  }
}
