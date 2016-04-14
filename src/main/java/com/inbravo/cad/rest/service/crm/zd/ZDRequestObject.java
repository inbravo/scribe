package com.inbravo.cad.rest.service.crm.zd;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;

import org.w3c.dom.Element;

/**
 * 
 * @author amit.dixit
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ZDRequestObject {

  @XmlAnyElement
  private List<Element> xmlContent;

  public ZDRequestObject() {

  }

  public ZDRequestObject(final List<Element> xmlContent) {
    this.xmlContent = xmlContent;
  }

  public final void setXmlContent(final List<Element> xmlContent) {
    this.xmlContent = xmlContent;
  }

  public final List<Element> getXmlContent() {
    return xmlContent;
  }
}
