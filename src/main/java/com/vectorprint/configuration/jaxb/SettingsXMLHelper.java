/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vectorprint.configuration.jaxb;

/*
 * #%L
 * VectorPrintReport
 * %%
 * Copyright (C) 2012 - 2014 VectorPrint
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.vectorprint.IOHelper;
import com.vectorprint.configuration.generated.jaxb.Settingstype;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Threadsafe helper for translating xml to JAXB objects
 *
 * @author Eduard Drenth at VectorPrint.nl
 */
public class SettingsXMLHelper {

    private static Logger logger = Logger.getLogger(SettingsXMLHelper.class.getName());

    public static final String XSD = "/xsd/Settings.xsd";

    private static JAXBContext JAXBCONTEXT = null;
    private static Schema schema = null;

    static {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = sf.newSchema(new SAXSource(new InputSource(SettingsXMLHelper.class.getResourceAsStream(XSD))));
            JAXBCONTEXT = JAXBContext.newInstance("com.vectorprint.configuration.generated.jaxb");
        } catch (JAXBException ex) {
            logger.log(Level.SEVERE, "failed to load jaxb context", ex);
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, "failed to load schema", ex);
        }
    }

    public static Settingstype fromXML(Reader xml) throws JAXBException {
        Unmarshaller um = JAXBCONTEXT.createUnmarshaller();
        um.setSchema(schema);
        JAXBElement jb = (JAXBElement) um.unmarshal(xml);
        return (Settingstype) jb.getValue();
    }

    public static JAXBContext getJAXBCONTEXT() {
        return JAXBCONTEXT;
    }

    public static Schema getSchema() {
        return schema;
    }

    public static void validateXml(InputStream xml) throws SAXException, IOException {
        schema.newValidator().validate(new SAXSource(new InputSource(xml)));
    }

    public static void validateXml(Reader xml) throws SAXException, IOException {
        schema.newValidator().validate(new SAXSource(new InputSource(xml)));
    }

    public static void validateXml(URL xml) throws SAXException, IOException {
        try (InputStream in = xml.openStream()) {
            validateXml(in);
        }
    }

    public static void validateXml(String xml) throws SAXException, IOException {
        try (Reader in = new StringReader(xml)) {
            validateXml(in);
        }
    }

    /**
     * prints xsd, or validate xml (no exceptions => ok)
     *
     * @see #VALIDATE
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, SAXException {
        if (args != null && args.length > 0) {
            validateXml(new URL(args[0]));
        } else {
            System.out.println(getXsd());
        }
    }

    public static String getXsd() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        IOHelper.load(SettingsXMLHelper.class.getResourceAsStream(XSD), out);
        return out.toString();
    }

}
