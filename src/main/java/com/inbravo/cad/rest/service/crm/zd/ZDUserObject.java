package com.inbravo.cad.rest.service.crm.zd;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

/**
 * 
 * @author amit.dixit
 * 
 */
@XmlRootElement(name = "user")
public final class ZDUserObject extends ZDRequestObject {

  public ZDUserObject() {

  }

  public ZDUserObject(final List<Element> xmlContent) {
    super.setXmlContent(xmlContent);
  }
}
