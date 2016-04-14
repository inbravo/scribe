package com.inbravo.cad.rest.service.crm.zd;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

/**
 * 
 * @author amit.dixit
 * 
 */
@XmlRootElement(name = "forum")
public final class ZDForumObject extends ZDRequestObject {

  public ZDForumObject() {

  }

  public ZDForumObject(final List<Element> xmlContent) {
    super.setXmlContent(xmlContent);
  }
}
