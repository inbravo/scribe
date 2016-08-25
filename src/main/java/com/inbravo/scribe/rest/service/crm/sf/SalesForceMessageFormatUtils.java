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

package com.inbravo.scribe.rest.service.crm.sf;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.message.MessageElement;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.inbravo.scribe.rest.resource.ScribeObject;
import com.inbravo.scribe.rest.service.crm.CRMMessageFormatUtils;

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

  public final static MessageElement[] createMessageElementArray(final ScribeObject cADbject) throws Exception {

    logger.debug("---Inside createMessageElementArray size of input Object:" + cADbject.getXmlContent().size());
    MessageElement[] messageElementArray = null;

    /* Start the index from 0 */
    int index = 0;

    /* Create array with number of elements count from ScribeObject */
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
