package rs.elfak.ocspresponder.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bouncycastle.operator.ContentSigner;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateSigningContext {

    private String caAlias;
    private X509Certificate caCertificate;
    private KeyPair keyPair;
    private String authorityInfoAccessURI;
    private String ocspResponderURI;
    private ContentSigner contentSigner;

}
