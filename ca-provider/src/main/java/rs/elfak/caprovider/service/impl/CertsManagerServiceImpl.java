package rs.elfak.caprovider.service.impl;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.*;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;
import org.bouncycastle.pkcs.jcajce.JcePKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEOutputEncryptorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.elfak.caprovider.db.tables.pojos.CertificateRequest;
import rs.elfak.caprovider.model.CertificateSigningContext;
import rs.elfak.caprovider.providers.SecurityNameProvider;
import rs.elfak.caprovider.service.CertsManagerService;
import rs.elfak.caprovider.util.CertUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CertsManagerServiceImpl implements CertsManagerService {

    final SecurityNameProvider securityNameProvider;
    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    @Value("${certificate.key-size}")
    private Integer keySize;

    public CertsManagerServiceImpl(SecurityNameProvider securityNameProvider) {
        this.securityNameProvider = securityNameProvider;
    }

    @PostConstruct
    private void postConstruct() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public KeyPair getKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM, securityNameProvider.getName());
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }

    @Override
    public PKCS10CertificationRequest getCsr(CertificateRequest request, KeyPair keyPair) throws OperatorCreationException {
        X500Name x500Name = new X500Name(String.format("CN=%s", request.getEmail()));
        JcaPKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(x500Name, keyPair.getPublic());
        ContentSigner csrContentSigner = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .setProvider(securityNameProvider.getName())
                .build(keyPair.getPrivate());

        return p10Builder.build(csrContentSigner);
    }

    @Override
    public X509Certificate signCsr(
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
            OperatorCreationException {
        var caCert = context.getCaCertificate();
        var privateKey = context.getKeyPair().getPrivate();

        X500Name issuerName = new JcaX509CertificateHolder(caCert).getSubject();

        X509v3CertificateBuilder issuedCertBuilder = new X509v3CertificateBuilder(
                issuerName,
                new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16),
                notBefore,
                notAfter,
                csr.getSubject(),
                csr.getSubjectPublicKeyInfo());

        JcaX509ExtensionUtils issuedCertExtUtils = new JcaX509ExtensionUtils();

        KeyPurposeId[] keyPurposeIds = new KeyPurposeId[]{
                KeyPurposeId.id_kp_emailProtection, KeyPurposeId.id_kp_clientAuth
        };
        GeneralName caIssuersURI = new GeneralName(GeneralName.uniformResourceIdentifier, context.getAuthorityInfoAccessURI());
        GeneralName OCSPResponderURI = new GeneralName(GeneralName.uniformResourceIdentifier, context.getOcspResponderURI());
        AccessDescription[] accessDescriptions = new AccessDescription[] {
                new AccessDescription(AccessDescription.id_ad_caIssuers, caIssuersURI),
                new AccessDescription(AccessDescription.id_ad_ocsp, OCSPResponderURI)
        };

        issuedCertBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false))
                .addExtension(Extension.authorityKeyIdentifier, false, issuedCertExtUtils.createAuthorityKeyIdentifier(caCert))
                .addExtension(Extension.subjectKeyIdentifier, false, issuedCertExtUtils.createSubjectKeyIdentifier(csr.getSubjectPublicKeyInfo()))
                .addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyEncipherment | KeyUsage.digitalSignature))
                .addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(keyPurposeIds))
                .addExtension(Extension.authorityInfoAccess, false, new AuthorityInformationAccess(accessDescriptions))
                .addExtension(Extension.subjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.rfc822Name, requestDetails.getEmail())));

        JcaContentSignerBuilder csrBuilder = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(securityNameProvider.getName());
        ContentSigner caContentSigner = csrBuilder.build(privateKey);
        X509CertificateHolder issuedCertHolder = issuedCertBuilder.build(caContentSigner);
        X509Certificate issuedCert = new JcaX509CertificateConverter().setProvider(securityNameProvider.getName()).getCertificate(issuedCertHolder);

        issuedCert.verify(caCert.getPublicKey(), securityNameProvider.getName());

        return issuedCert;
    }

    @Override
    public PKCS12PfxPdu createPfx(X509Certificate certificate,
                                  PrivateKey certificateKey,
                                  String encryptionPassword,
                                  X509Certificate... chain
    ) throws IOException, PKCSException, NoSuchAlgorithmException, OperatorCreationException {
        char[] pwKey = encryptionPassword.toCharArray();

        List<PKCS12SafeBag> pkcs12ChainBags = new ArrayList<>();
        for (X509Certificate chainCert : chain) {
            PKCS12SafeBagBuilder pkcs12SafeBagBuilder = new JcaPKCS12SafeBagBuilder(chainCert)
                    .addBagAttribute(PKCS12SafeBag.friendlyNameAttribute, new DERBMPString(CertUtils.getSubjectName(chainCert)));
            pkcs12ChainBags.add(pkcs12SafeBagBuilder.build());
        }

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        SubjectKeyIdentifier pubKeyId = extUtils.createSubjectKeyIdentifier(certificate.getPublicKey());

        String subjectName = CertUtils.getSubjectName(certificate);
        PKCS12SafeBagBuilder certBagBuilder = new JcaPKCS12SafeBagBuilder(certificate)
                .addBagAttribute(PKCS12SafeBag.friendlyNameAttribute, new DERBMPString(subjectName))
                .addBagAttribute(PKCS12SafeBag.localKeyIdAttribute, pubKeyId);

        PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(certificateKey)
                .addBagAttribute(PKCS12SafeBag.localKeyIdAttribute, pubKeyId);

        pkcs12ChainBags.add(0, certBagBuilder.build());
        PKCS12PfxPduBuilder builder = new PKCS12PfxPduBuilder();

        builder.addEncryptedData(new JcePKCSPBEOutputEncryptorBuilder(new ASN1ObjectIdentifier("2.16.840.1.101.3.4.1.42"))
                        .setProvider(securityNameProvider.getName())
                        .build(pwKey),
                pkcs12ChainBags.toArray(new PKCS12SafeBag[]{}));
        builder.addData(keyBagBuilder.build());
//        builder.addData(pkcs12ChainBags.get(0));
//        builder.addData(pkcs12ChainBags.get(1));

        return builder.build(new JcePKCS12MacCalculatorBuilder(NISTObjectIdentifiers.id_sha256), pwKey);
    }

}
