package com.trifork.dgws.testclient;

import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Properties;

import dk.sosi.seal.pki.Federation;
import dk.sosi.seal.pki.SOSIFederation;
import dk.sosi.seal.pki.SOSITestFederation;
import dk.sosi.seal.vault.CredentialPair;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import dk.sosi.seal.SOSIFactory;
import dk.sosi.seal.model.AuthenticationLevel;
import dk.sosi.seal.model.CareProvider;
import dk.sosi.seal.model.IDCard;
import dk.sosi.seal.model.SecurityTokenRequest;
import dk.sosi.seal.model.SignatureUtil;
import dk.sosi.seal.model.UserInfo;
import dk.sosi.seal.model.constants.SubjectIdentifierTypeValues;
import dk.sosi.seal.vault.CredentialVault;
import dk.sosi.seal.vault.GenericCredentialVault;
import dk.sosi.seal.xml.XmlUtil;
import com.trifork.dgws.testclient.Helper.ServiceException;
import org.w3c.dom.Document;

public class SOSI {

    private Resource keystore;
    private String keystorePassword;
    private String keystoreAlias;

    @Value("${sosi.sts.url}")
    private String stsUrl;

    @Value("${sosi.careprovider.name}")
    private String careproviderName;

    @Value("${sosi.careprovider.cvr}")
    private String careproviderCvr;

    @Value("${sosi.test.federation}")
    private Boolean useTestFederation;

    @Value("${sosi.system.name}")
    private String sosiSystemName;

    private IDCard idCard;
    private GenericCredentialVault vault;
    private CareProvider careProvider;
    private SOSIFactory factory;
    private Federation federation;
    private Properties props;
    private PrivateKey privateKey;
    private X509Certificate certificate;

    public void init() throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        props = SignatureUtil.setupCryptoProviderForJVM();
        if (!keystore.exists()) {
            throw  new RuntimeException("Keystore '"+keystore.getURI() +"' could not be found");
        }
        vault = new InputStreamCredentialVault(props, keystore.getInputStream(), keystorePassword);
        privateKey = (PrivateKey) vault.getKeyStore().getKey(keystoreAlias, keystorePassword.toCharArray());
        certificate = (X509Certificate) vault.getKeyStore().getCertificate(keystoreAlias);

        if (useTestFederation) {
            federation = new SOSITestFederation(props);
        } else {
            federation = new SOSIFederation(props);
        }
        factory = new SOSIFactory(federation, vault, props);
        careProvider = new CareProvider(SubjectIdentifierTypeValues.CVR_NUMBER, careproviderCvr, careproviderName);
    }

    @Required
    public void setKeystore(Resource keystore) {
        this.keystore = keystore;
    }

    @Required
    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    @Required
    public void setKeystoreAlias(String keystoreAlias) {
        this.keystoreAlias = keystoreAlias;
    }

    public SOSIFactory getFactory() {
        return factory;
    }

    public IDCard getIDCard(Person person, boolean moces)
            throws ServiceException, NoSuchAlgorithmException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException {
        if (idCard != null) {
            return idCard;
        }
        if (moces) {
            // Medarbejder certifikat
            UserInfo userInfo = new UserInfo(person.getCpr(), person.getFirstName(), person.getLastName(),
                    person.getEmail(), "test user", "Doctor", "000");
            idCard = factory.createNewUserIDCard(sosiSystemName, userInfo, careProvider,
                    AuthenticationLevel.MOCES_TRUSTED_USER, null, null, certificate, null);
        } else {
            // Virksomheds certifikat
            idCard = factory.createNewSystemIDCard(sosiSystemName, careProvider,
                    AuthenticationLevel.VOCES_TRUSTED_SYSTEM, null, null, certificate, null);

        }
        idCard = signIdCard(factory, idCard, moces);
        return idCard;
    }

    private IDCard signIdCard(SOSIFactory factory, IDCard card, boolean moces)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, ServiceException {

        SecurityTokenRequest req = factory.createNewSecurityTokenRequest();
        req.setIDCard(card);
        if (moces) {
            byte[] bytesForSigning = req.getIDCard().getBytesForSigning(req.serialize2DOMDocument());

            Signature jceSign = Signature.getInstance("SHA1withRSA", SignatureUtil.getCryptoProvider(
                    SignatureUtil.setupCryptoProviderForJVM(), SOSIFactory.PROPERTYNAME_SOSI_CRYPTOPROVIDER_SHA1WITHRSA));
            jceSign.initSign(privateKey);
            jceSign.update(bytesForSigning);
            String signature = XmlUtil.toBase64(jceSign.sign());

            req.getIDCard().injectSignature(signature, certificate);
        } else {
            vault.setSystemCredentialPair(new CredentialPair(certificate, privateKey));
        }

        String xml = XmlUtil.node2String(req.serialize2DOMDocument(), false, true);
        String res = null;
        try {
            res = Helper.sendRequest(stsUrl, "", xml, true);
        } catch (ServiceException e) {
            // Hack to support SOAP Faults
            testForSoapFaultAndThrowSoapFaultException(e.getMessage(), xml);
            throw e;
        }
        // Hack to support SOAP Faults
        testForSoapFaultAndThrowSoapFaultException(res, xml);

        SecurityTokenRequest stRes = factory.deserializeSecurityTokenRequest(res);
        card = stRes.getIDCard();
        return card;
    }

    private void testForSoapFaultAndThrowSoapFaultException(String res, String requestXml) {
        int faultIndex = res.indexOf("<faultstring>");
        if (faultIndex >= 0) {
            System.err.println("Failed request:\n\n" + requestXml + "\n\n");
            int faultEndIndex = res.indexOf("</faultstring>");
            String msg = res.substring(faultIndex + 13, faultEndIndex);
            throw new RuntimeException(msg);
        }
    }
}
