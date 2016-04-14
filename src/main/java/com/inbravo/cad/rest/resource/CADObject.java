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

    String returnString = "EDSAObject [ObjectType=" + ObjectType;
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
