package rs.elfak.caprovider.providers;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCSException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import rs.elfak.caprovider.model.CertificateSigningContext;
import rs.elfak.caprovider.util.CertUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Service
public class SigningContextProvider {

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
    @Value("${certificate.authority.info.ocsp}")
    private String ocspAuthorityURI;

    final ResourceLoader resourceLoader;

    private CertificateSigningContext context;

    public SigningContextProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void postConstruct() throws CertificateException, IOException, PKCSException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        Security.addProvider(new BouncyCastleProvider());
        final String classPath = "classpath:";
        InputStream inputStream = resourceLoader.getResource(classPath.concat(caPath)).getInputStream();
        String caKey = resourceLoader.getResource(classPath.concat(caKeyPath)).getContentAsString(StandardCharsets.US_ASCII);

        context = new CertificateSigningContext(
                caAlias,
                CertUtils.parseCertificate(inputStream),
                CertUtils.parseKeyPair(caKey, caKeyPassword),
                authorityURI.concat("/").concat(caAlias),
                ocspAuthorityURI
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
