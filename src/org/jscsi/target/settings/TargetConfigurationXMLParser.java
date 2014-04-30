package org.jscsi.target.settings;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TargetConfigurationXMLParser
{
    public static final String TARGET_LIST_ELEMENT_NAME = "TargetList"; // Name of node that contains list of targets
    
    public static final String TARGET_ELEMENT_NAME = "Target";  // Name for nodes that contain a target
    // Target configuration elements
    private static final String FILE_PATH_ELEMENT_NAME = "FilePath";
    public static final String STORAGE_FILE_ELEMENT_NAME = "StorageFile";
    
    public static final String GLOBAL_CONFIG_ELEMENT_NAME = "GlobalConfig"; // Name of node that contains global configuration
    
    // Global configuration elements
    public static final String ALLOW_SLOPPY_NEGOTIATION_ELEMENT_NAME = "AllowSloppyNegotiation";
    public static final String PORT_ELEMENT_NAME = "Port";




    /**
     * The file name of the configuration file.
     */
    public static final String CONFIGURATION_FILE_NAME = "jscsi-target.xml";

    /**
     * The name of the schema file describing the format of the configuration
     * file.
     */
    public static final String SCHEMA_FILE_NAME = "jscsi-target.xsd";

    /**
     * The primary folder containing all configuration files.
     */
    public static final File RELEASE_CONFIGURATION_DIRECTORY = new File(
            new StringBuilder(System.getProperty("user.dir")).append(
                    File.separator).toString());

    /**
     * The back-up folder which will be searched for the <code>xsd</code> and
     * <code>xml</code>files if they cannot be found in the
     * {@link #RELEASE_CONFIGURATION_DIRECTORY}.
     */
    public static final File DEVELOPMENT_CONFIGURATION_DIRECTORY = new File(
            new StringBuilder(System.getProperty("user.dir"))
                    .append(File.separator).append("src")
                    .append(File.separator).append("main")
                    .append(File.separator).append("resources")
                    .append(File.separator).toString());

    /**
     * Searches and tries to return a {@link File} with the specified
     * <i>fileName</i> from the given <i>mainDirectory</i>. If this {@link File}
     * does not exist, a {@link File} with the specified <i>fileName</i> from
     * the given <i>backUpDirectory</i> will be returned.
     * 
     * @param mainDirectory
     *            the first directory to search
     * @param backUpDirectory
     *            the folder to use if the first search fails
     * @param fileName
     *            the name of the file without path information
     * @return a {@link File} that may or may not exist
     */
    public static File getFile(final File mainDirectory,
            final File backUpDirectory, String fileName) {
        final File file = new File(mainDirectory, fileName);
        if (file.exists())
            return file;
        return new File(backUpDirectory, fileName);
    }

    public Document parse() throws SAXException, ParserConfigurationException, IOException
    {
    	URL schemaURL = null;
    	File overrideSchemaFile = getFile(RELEASE_CONFIGURATION_DIRECTORY, DEVELOPMENT_CONFIGURATION_DIRECTORY, SCHEMA_FILE_NAME);
    	if (overrideSchemaFile.exists())
    		schemaURL = new URL("file:"+overrideSchemaFile);
        return parse(schemaURL, getFile(
				        RELEASE_CONFIGURATION_DIRECTORY,
				        DEVELOPMENT_CONFIGURATION_DIRECTORY, CONFIGURATION_FILE_NAME));
    }
    
    /**
     * Reads the given configuration file in memory and creates a DOM
     * representation.
     * 
     * @throws SAXException
     *             If this operation is supported but failed for some reason.
     * @throws ParserConfigurationException
     *             If a {@link DocumentBuilder} cannot be created which
     *             satisfies the configuration requested.
     * @throws IOException
     *             If any IO errors occur.
     */
    public Document parse(final URL schemaLocation,
            final File configFile) throws SAXException,
            ParserConfigurationException, IOException {

        final SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = schemaFactory.newSchema(schemaLocation);

        // create a validator for the document
        final Validator validator = schema.newValidator();

        final DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
        domFactory.setNamespaceAware(true); // never forget this
        final DocumentBuilder builder = domFactory.newDocumentBuilder();
        final Document doc = builder.parse(configFile);

        final DOMSource source = new DOMSource(doc);
        final DOMResult result = new DOMResult();

        validator.validate(source, result);
        return (Document) result.getNode();
    }
    
    public TargetConfiguration parseSettings() throws SAXException, ParserConfigurationException, IOException
    {
        return parseSettings(parse().getDocumentElement());
    }
    /**
     * Parses all settings form the main configuration file.
     * 
     * @param root
     *            The root element of the configuration.
     */
    public TargetConfiguration parseSettings(final Element root) throws IOException {
        // TargetName
        TargetConfiguration returnConfiguration = new TargetConfiguration();
        Element targetListNode = (Element) root.getElementsByTagName(TARGET_LIST_ELEMENT_NAME).item(0);
        NodeList targetList = targetListNode.getElementsByTagName(TARGET_ELEMENT_NAME);
        for (int curTargetNum = 0; curTargetNum < targetList.getLength(); curTargetNum++)
        {
            TargetInfo curTargetInfo = parseTargetElement((Element) targetList.item(curTargetNum));
            returnConfiguration.addTargetInfo(curTargetInfo);
        }

        // else it is null

        // port
        if (root.getElementsByTagName(PORT_ELEMENT_NAME).getLength() > 0)
            returnConfiguration.setPort(Integer.parseInt(root.getElementsByTagName(PORT_ELEMENT_NAME).item(0)
                    .getTextContent()));
        else
            returnConfiguration.setPort(3260);


        // support sloppy text parameter negotiation (i.e. the jSCSI Initiator)?
        final Node allowSloppyNegotiationNode = root.getElementsByTagName(
                ALLOW_SLOPPY_NEGOTIATION_ELEMENT_NAME).item(0);
        if (allowSloppyNegotiationNode == null)
            returnConfiguration.setAllowSloppyNegotiation(false);
        else
            returnConfiguration.setAllowSloppyNegotiation(Boolean
                    .parseBoolean(allowSloppyNegotiationNode.getTextContent()));
        
        return returnConfiguration;
    }
        
    public TargetInfo parseTargetElement(Element targetElement)
    {
        String targetName = targetElement.getElementsByTagName(TextKeyword.TARGET_NAME).item(0)
                .getTextContent();
        // TargetAlias (optional)
        Node targetAliasNode = targetElement.getElementsByTagName(
                TextKeyword.TARGET_ALIAS).item(0);
        String targetAlias = null;
        if (targetAliasNode != null)
            targetAlias = targetAliasNode.getTextContent();
        NodeList fileProperties = targetElement
                .getElementsByTagName(STORAGE_FILE_ELEMENT_NAME).item(0).getChildNodes();
        String storageFilePath = null;
        for (int i = 0; i < fileProperties.getLength(); ++i) {
            if (FILE_PATH_ELEMENT_NAME.equals(fileProperties.item(i).getNodeName()))
                storageFilePath = fileProperties.item(i).getTextContent();
        }
        if (storageFilePath == null)
            storageFilePath = "storage.dat";
        
        StorageFileTargetInfo returnInfo = new StorageFileTargetInfo(targetName, targetAlias, storageFilePath);
        return returnInfo;
    }
}
