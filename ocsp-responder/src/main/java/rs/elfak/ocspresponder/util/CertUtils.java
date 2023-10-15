package rs.elfak.ocspresponder.util;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CertUtils {

    private static final String BC_PROVIDER = "BC";

    static public KeyPair parseKeyPair(String s, String password) throws IOException, PKCSException {
        PEMParser pemParser = new PEMParser(new StringReader(s));
        PKCS8EncryptedPrivateKeyInfo object = (PKCS8EncryptedPrivateKeyInfo) pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BC_PROVIDER);
        InputDecryptorProvider decProvider = new JcePKCSPBEInputDecryptorProviderBuilder().setProvider(BC_PROVIDER).build(password.toCharArray());
        PrivateKeyInfo privateKeyInfo = object.decryptPrivateKeyInfo(decProvider);
        PrivateKey privateKey = converter.getPrivateKey(privateKeyInfo);
        RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
        RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            PublicKey myPublicKey = keyFactory.generatePublic(publicKeySpec);
            return new KeyPair(myPublicKey, privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    static public X509Certificate parseCertificate(InputStream pemCert) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(pemCert);
    }

}
