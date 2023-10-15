package rs.elfak.caprovider.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

}
