package com.trifork.dgws.testclient;

import dk.sosi.seal.model.IDCard;
import dk.sosi.seal.model.Request;
import dk.sosi.seal.xml.XmlUtil;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.security.*;
import java.util.UUID;

/**
 *
 */
public class DGWSRequestHelper {

    private static final Logger logger = Logger.getLogger(DGWSRequestHelper.class);

    private String serviceEndpoint;

    private boolean whitelistingHeaderEnabled = false;

    // MessageID (both WSA and medcom header). Null means generate new.
    private String messageID = null;

    // SDSD System Header
    @Value("${sdsd.system.owner.name}")
    private String systemOwnerName;

    @Value("${sdsd.system.name}")
    private String systemName;

    @Value("${sdsd.system.version}")
    private String systemVersion;

    @Value("${sdsd.org.responsible.name}")
    private String orgResponsibleName;

    @Value("${sdsd.org.using.name}")
    private String orgUsingName;

    @Value("${sdsd.org.using.id.name.format}")
    private String orgUsingIdNameFormat;

    @Value("${sdsd.org.using.id.value}")
    private String orgUsingIdValue;

    @Value("${sdsd.requested.role}")
    private String requestedRole;

    @Value("${soapaction:}")
    private String soapAction;

    @Value("${sosi.certificate.type}")
    private String certificateType;

    private SOSI sosi;

    public void setWhitelistingHeaderEnabled(boolean enabled) {
        this.whitelistingHeaderEnabled = enabled;
    }

    @Required
    public void setSosi(SOSI sosi) {
        this.sosi = sosi;
    }

    @Required
    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public Document createRequest(Element body) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException, SignatureException, NoSuchProviderException, Helper.ServiceException, IOException {
        String flowId = UUID.randomUUID().toString().replaceAll("-", "");
        Request request = sosi.getFactory().createNewRequest(false, flowId);
        if (whitelistingHeaderEnabled) {
            request.addNonSOSIHeader(createWhitelistingHeader());
        }
        request.setIDCard(getIdCard());
        request.setBody(body);
        return request.serialize2DOMDocument();
    }

    private IDCard getIdCard() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
            Helper.ServiceException, InvalidKeyException, SignatureException, NoSuchProviderException, IOException {
        if (certificateType.equalsIgnoreCase("moces")) {
            Person person = new Person("Muhamad", "Danielsen", "muhamad@somedomain.dk", "2006271866");
            return sosi.getIDCard(person, true);
        } else {
            return sosi.getIDCard(null, false);
        }
    }

    private Element createWhitelistingHeader() {
        Document whiteListDoc = XmlUtil.createEmptyDocument();
        Element whitelistElem = whiteListDoc.createElementNS("http://www.sdsd.dk/dgws/2012/06", "WhiteListingHeader");
        whiteListDoc.appendChild(whitelistElem);
        if (systemOwnerName != null) {
            whitelistElem.appendChild(createElementAndAppend(whiteListDoc, "SystemOwnerName", systemOwnerName));
        }
        if (systemName != null) {
            whitelistElem.appendChild(createElementAndAppend(whiteListDoc, "SystemName", systemName));
        }
        if (systemVersion != null) {
            whitelistElem.appendChild(createElementAndAppend(whiteListDoc, "SystemVersion", systemVersion));
        }
        if (orgResponsibleName != null) {
            whitelistElem.appendChild(createElementAndAppend(whiteListDoc, "OrgResponsibleName", orgResponsibleName));
        }
        if (orgUsingIdValue != null) {
            whitelistElem.appendChild(createElementAndAppend(whiteListDoc, "OrgUsingID", orgUsingIdValue));
        }
        if (requestedRole != null) {
            whitelistElem.appendChild(createElementAndAppend(whiteListDoc, "RequestedRole", requestedRole));
        }
        return whitelistElem;
    }

    private Element createElementAndAppend(Document whiteListDoc, String name, String value) {
        Element element = whiteListDoc.createElementNS("http://www.sdsd.dk/dgws/2010/08", name);
        element.appendChild(whiteListDoc.createTextNode(value));
        return element;
    }

    private PostMethod createPostMethod(String endpoint) {
        PostMethod postMethod = new PostMethod(serviceEndpoint);
        postMethod.addRequestHeader("Content-Type", "text/xml");
        postMethod.addRequestHeader("Accept", "text/xml,application/xml;q=0.9");
        return postMethod;
    }

    private HttpClient createClient() {
        HttpClient client = new HttpClient();
        client.getParams().setParameter("http.useragent", "DGWS Test Client");
        client.getParams().setParameter("http.connection.timeout", new Integer(5000));
        return client;
    }

    public String performRequest(String request) throws IOException {
        HttpClient client = createClient();
        PostMethod postMethod = createPostMethod(serviceEndpoint);
        if (soapAction != null && soapAction.trim().length() > 0) {
            postMethod.addRequestHeader("SOAPAction", soapAction);
        }
        postMethod.setRequestEntity(new StringRequestEntity(request, "text/xml", "UTF-8"));
        StringBuilder headerLog = new StringBuilder("");
        headerLog.append("URI: ").append(postMethod.getURI()).append("\n");
        headerLog.append("Headers: \n");
        for (Header header : postMethod.getRequestHeaders()) {
            headerLog.append("\t").append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        logger.info(headerLog.toString());
        int status = client.executeMethod(postMethod);
        logger.info("Result: " + postMethod.getStatusLine().toString());
        String response = postMethod.getResponseBodyAsString();
        if (status != HttpStatus.SC_OK) {
            throw new RuntimeException("Soap request for URI '"+postMethod.getURI()+"' failed - status '"+status+": "+postMethod.getStatusText()+"': \n" + response);
        }
        return response;
    }

}
