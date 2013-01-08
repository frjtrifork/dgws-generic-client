package com.trifork.dgws.testclient;

import dk.sosi.seal.xml.XmlUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

/**
 *
 */
public class WSClient {
    private static final Logger logger = Logger.getLogger(WSClient.class);

    private static final String PROPERTY_XML_BODY_FILE = "xmlfile";

    public static void main(String[] args) throws Exception {

        String xmlFile = System.getProperty(PROPERTY_XML_BODY_FILE);

        if (args.length != 0) {
            String name = WSClient.class.getName();
            System.out.println("Usage: " + name + "-Dconfig=myconfig.properties -D" + PROPERTY_XML_BODY_FILE + "=<path_to_xml_file_to_use_as_body>\n");
            System.exit(0);
        }


        WSClient wsClient = new WSClient();
        File xmlFileForBody = null;
        if (xmlFile != null) {
            Resource res = new FileSystemResource(xmlFile);
            if (!res.exists()) {
                throw new RuntimeException("Input '" + xmlFile + "' not found");
            } else {
                xmlFileForBody = res.getFile();
            }
        }

        String response = wsClient.callWebService(xmlFileForBody);
        System.out.println(XmlUtil.getPrettyString(response));
    }

    private ClassPathXmlApplicationContext applicationContext;
    private final DocumentBuilder builder;

    public WSClient() throws ParserConfigurationException {
        applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        builder = docFactory.newDocumentBuilder();
    }

    private Node string2Node(String xmlstr) throws SAXException, IOException {
        InputSource inStream = new InputSource();
        inStream.setCharacterStream(new StringReader(xmlstr));

        Document docBody = builder.parse(inStream);
        return docBody.getFirstChild();
    }

    public String callWebService(File xmlBody) throws Exception {
        DGWSRequestHelper requestHelper = applicationContext.getBean(DGWSRequestHelper.class);
        String payload = getPayload(xmlBody);
        Document request = requestHelper.createRequest((Element) string2Node(payload));
        logger.info(XmlUtil.node2String(request.getDocumentElement(), true, true));

        return requestHelper.performRequest(XmlUtil.node2String(request.getDocumentElement()));
    }

    public String getPayload(File file) throws IOException {
        if (file == null) return "";
        return FileUtils.readFileToString(file, "UTF-8");

    }
}
