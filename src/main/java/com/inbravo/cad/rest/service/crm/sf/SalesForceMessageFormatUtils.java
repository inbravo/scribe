package com.inbravo.cad.rest.service.crm.sf;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.message.MessageElement;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.inbravo.cad.rest.resource.CADObject;
import com.inbravo.cad.rest.service.crm.CRMMessageFormatUtils;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class SalesForceMessageFormatUtils extends CRMMessageFormatUtils {

  private static final Logger logger = Logger.getLogger(SalesForceMessageFormatUtils.class.getName());

  private SalesForceMessageFormatUtils() {
    super();
  }

  public final static MessageElement[] createMessageElementArray(final CADObject cADbject) throws Exception {

    logger.debug("---Inside createMessageElementArray size of input Object:" + cADbject.getXmlContent().size());
    MessageElement[] messageElementArray = null;

    /* Start the index from 0 */
    int index = 0;

    /* Create array with number of elements count from CADObject */
    messageElementArray = new MessageElement[cADbject.getXmlContent().size()];

    /* Iterate on the Element list and create SOAP object */
    for (final Element element : cADbject.getXmlContent()) {
      messageElementArray[index] = createMessageElement(element.getNodeName(), element.getTextContent());
      index++;
    }
    logger.debug("---Inside createMessageElementArray size of Sales Force Object:" + messageElementArray.length);
    return messageElementArray;
  }

  public final static Element[] createElementArray(final MessageElement[] messageElementArray) throws Exception {
    logger.debug("---Inside createElementArray");
    final Element[] elementArray = new Element[messageElementArray.length];
    final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    docFactory.setNamespaceAware(true);
    docFactory.setValidating(false);
    final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

    /* Create new document */
    final Document document = docBuilder.newDocument();

    /* Start the index from 0 */
    int index = 0;

    /* Iterate over messageElementArray and create Element array */
    for (final MessageElement messsageElement : messageElementArray) {
      final Element randomElement = document.createElement(messsageElement.getNodeName());
      randomElement.setNodeValue(messsageElement.getNodeValue());
      elementArray[index] = randomElement;
      index++;
    }
    return elementArray;
  }
}
