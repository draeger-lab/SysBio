/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.kegg;

import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class with a mini example to test the writing and intending of the KGML
 * 
 * @author Finja B&uuml;chel
 * @version $Rev$
 */
public class KGMLWriterTest {

  public static void testIndentOfMiniXML() throws ParserConfigurationException,
      FileNotFoundException, XMLStreamException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

    // root element pathway
    Document doc = docBuilder.newDocument();

    Element rootElement = doc.createElement("pathway");
    rootElement.setAttribute("name", "superpw");
    doc.appendChild(rootElement);

    // staff elements
    Element staff = doc.createElement("Staff");
    rootElement.appendChild(staff);

    // set attribute to staff element
    Attr attr = doc.createAttribute("id");
    attr.setValue("1");
    staff.setAttributeNode(attr);

    // shorten way
    // staff.setAttribute("id", "1");

    // firstname elements
    Element firstname = doc.createElement("firstname");
    firstname.appendChild(doc.createTextNode("yong"));
    staff.appendChild(firstname);

    // lastname elements
    Element lastname = doc.createElement("lastname");
    lastname.appendChild(doc.createTextNode("mook kim"));
    staff.appendChild(lastname);

    // nickname elements
    Element nickname = doc.createElement("nickname");
    nickname.appendChild(doc.createTextNode("mkyong"));
    staff.appendChild(nickname);

    // salary elements
    Element salary = doc.createElement("salary");
    salary.appendChild(doc.createTextNode("100000"));
    staff.appendChild(salary);
    
    
    KGMLWriter.writeKGMLFileFromDoc(doc, "file.xml");
  }

  /**
   * @param args
   */

  public static void main(String[] args) throws Exception {
    testIndentOfMiniXML();
  }
}
