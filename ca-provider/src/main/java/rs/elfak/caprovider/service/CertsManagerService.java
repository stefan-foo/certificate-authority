package rs.elfak.caprovider.service;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.bouncycastle.pkcs.PKCSException;
import rs.elfak.caprovider.db.tables.pojos.CertificateRequest;
import rs.elfak.caprovider.model.CertificateSigningContext;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public interface CertsManagerService {
    KeyPair getKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException;

    PKCS10CertificationRequest getCsr(CertificateRequest request, KeyPair keyPair) throws OperatorCreationException;

    X509Certificate signCsr(
            CertificateSigningContext context,
            PKCS10CertificationRequest csr,
            CertificateRequest requestDetails,
            Date notBefore,
            Date notAfter
    ) throws IOException,
            NoSuchAlgorithmException,
            CertificateException,
            SignatureException,
            InvalidKeyException,
            NoSuchProviderException,
            OperatorCreationException;

    PKCS12PfxPdu createPfx(X509Certificate certificate,
                           PrivateKey certificateKey,
                           String encryptionPassword,
                           X509Certificate... chain
    ) throws IOException, PKCSException, NoSuchAlgorithmException, OperatorCreationException, CertificateEncodingException;
}
