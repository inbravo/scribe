package com.inbravo.cad.rest.service.crm.zd;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

/**
 * 
 * @author amit.dixit
 * 
 */
@XmlRootElement(name = "ticket")
public class ZDTicketObject extends ZDRequestObject {

  public ZDTicketObject() {

  }

  public ZDTicketObject(final List<Element> xmlContent) {
    super.setXmlContent(xmlContent);
  }
}
