/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.xml.sax.SAXException;

/**
 * This class is used to validate the XML representing an optimization problem
 * against the standard XSD. A problem can have either one or more objectives,
 * as well as an arbitrary number of constraints including none. And optionally,
 * the user may include gradients of each objective/constraint. A gradient is
 * basically the vector of partial derivatives of the objective/constraint with
 * respect to all variables. However, a user may supply only a subset of these
 * partial derivatives. If this is the case, it is the responsibility of the
 * higher level code (the code using this class) to calculate these partial
 * derivatives if they are required e.g. numerically. Check the XSD
 * documentation for more details at:
 * https://www.msu.edu/~seadahai/xml/problem.xsd
 *
 * @author Haitham Seada
 */
public class XMLValidator {

    /**
     * The XML Schema Definition used for validating the XML.
     */
    public static final String SCHEMA_URL
            = "https://www.msu.edu/~seadahai/xml/problem.xsd";

    /**
     * Validates the argument (XML file) against XSD which can be found at:
     * https://www.msu.edu/~seadahai/xml/problem.xsd
     *
     * @param xmlFile Input XML file
     * @throws SAXException If the XML file is not valid
     * @throws MalformedURLException If the URL of the XSD is not valid.
     * @throws IOException If the underlying XMLReader throws an exception.
     */
    public static void validate(File xmlFile) throws
            SAXException,
            MalformedURLException,
            IOException {
        //URL schemaFile = new URL(SCHEMA_URL);
        URL schemaFile = new File("E:\\KKTPM\\Java KKTPM\\Schema\\problem_01_01.xsd").toURI().toURL();
        Source xmlFileSource = new StreamSource(xmlFile);
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(xmlFileSource);
    }

    /**
     * Just for testing
     *
     * @throws MalformedURLException
     * @throws SAXException
     * @throws IOException
     */
    public static void main(String[] args) throws MalformedURLException, SAXException, IOException {
        String filePath = "E:\\KKTPM\\Java KKTPM\\XML\\wfg\\wfg1.xml";
        // XML input file
        File xmlFile = new File(filePath);
        try {
            // Validate
            XMLValidator.validate(xmlFile);
            // XML input file follows the schema
            System.out.println("\"" + xmlFile.getAbsoluteFile() + "\" is valid");
        } catch (SAXException ex) {
            // XML input file does not follow the schema
            System.out.println("\"" + xmlFile.getAbsoluteFile() + "\" is NOT valid");
            System.out.println("Reason: " + ex.getLocalizedMessage());
        }
    }
}
