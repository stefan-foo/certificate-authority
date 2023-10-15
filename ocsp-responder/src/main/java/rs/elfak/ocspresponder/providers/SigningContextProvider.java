package rs.elfak.ocspresponder.providers;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCSException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import rs.elfak.ocspresponder.model.CertificateSigningContext;
import rs.elfak.ocspresponder.util.CertUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Service
public class SigningContextProvider {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String BC_PROVIDER = "BC";

    @Value("${certificate.ca.cert.path}")
    private String caPath;
    @Value("${certificate.ca.key.path}")
    private String caKeyPath;
    @Value("${certificate.ca.key.password}")
    private String caKeyPassword;
    @Value("${certificate.ca.cert.alias}")
    private String caAlias;
    @Value("${certificate.authority.info.uri}")
    private String authorityURI;
    @Value("http://localhost:8080/ocsp")
    private String ocspAuthorityURI;

    final ResourceLoader resourceLoader;

    private CertificateSigningContext context;

    public SigningContextProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void postConstruct() throws CertificateException, IOException, PKCSException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, OperatorCreationException {
        Security.addProvider(new BouncyCastleProvider());
        final String classPath = "classpath:";
        InputStream inputStream = resourceLoader.getResource(classPath.concat(caPath)).getInputStream();
        String caKey = resourceLoader.getResource(classPath.concat(caKeyPath)).getContentAsString(StandardCharsets.US_ASCII);
        KeyPair keyPair = CertUtils.parseKeyPair(caKey, caKeyPassword);

        ContentSigner csrContentSigner = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .setProvider(BC_PROVIDER)
                .build(keyPair.getPrivate());

        context = new CertificateSigningContext(
                caAlias,
                CertUtils.parseCertificate(inputStream),
                keyPair,
                authorityURI.concat("/").concat(caAlias),
                ocspAuthorityURI,
                csrContentSigner
        );
    }

    public CertificateSigningContext getContext() {
        return this.context;
    }

    public String getCaAlias() {
        return this.context.getCaAlias();
    }

    public X509Certificate getCaCertificate() {
        return this.context.getCaCertificate();
    }

}
