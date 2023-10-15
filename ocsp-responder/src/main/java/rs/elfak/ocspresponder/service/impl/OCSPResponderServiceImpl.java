package rs.elfak.ocspresponder.service.impl;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.ocsp.RevokedInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.*;
import org.springframework.stereotype.Service;
import rs.elfak.ocspresponder.db.tables.pojos.Certificate;
import rs.elfak.ocspresponder.model.CertificateSigningContext;
import rs.elfak.ocspresponder.providers.SigningContextProvider;
import rs.elfak.ocspresponder.repository.CertificateRepo;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class OCSPResponderServiceImpl {

    final
    CertificateRepo certificateRepo;
    final
    SigningContextProvider contextProvider;
    Integer responderCertId;

    public OCSPResponderServiceImpl(CertificateRepo certificateRepo, SigningContextProvider contextProvider) {
        this.certificateRepo = certificateRepo;
        this.contextProvider = contextProvider;
    }

    @PostConstruct
    private void postConstruct() {
        responderCertId = certificateRepo.getCertIdForAlias(contextProvider.getCaAlias());
    }

    public byte[] createOCSPResponse(byte[] ocspRequest) {
        try {
            OCSPReq ocspReq = new OCSPReq(ocspRequest);
            Req[] requestList = ocspReq.getRequestList();

            CertificateSigningContext context = contextProvider.getContext();
            X500Principal responderName = context.getCaCertificate().getSubjectX500Principal();
            BasicOCSPRespBuilder basicOCSPRespBuilder = new BasicOCSPRespBuilder(new RespID(new X500Name(responderName.getName())));

            for (Req req : requestList) {
                CertificateID certID = req.getCertID();
                Certificate certificate = certificateRepo.getCertificate(responderCertId, certID.getSerialNumber());
                if (certificate == null) {
                    continue;
                }
                basicOCSPRespBuilder.addResponse(certID, getOCSPStatus(certificate));
            }
            X509CertificateHolder issuerCertHolder = new X509CertificateHolder(context.getCaCertificate().getEncoded()) ;
            BasicOCSPResp build = basicOCSPRespBuilder.build(context.getContentSigner(), new X509CertificateHolder[] {issuerCertHolder}, new Date());
            OCSPResp response = new OCSPRespBuilder().build(OCSPRespBuilder.SUCCESSFUL, build);

            return response.getEncoded();
        } catch (IOException | OCSPException | CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Date convert(LocalDateTime dateToConvert) {
        if (dateToConvert == null) {
            return new Date();
        }
        return Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }
    private CertificateStatus getOCSPStatus(Certificate certificate) {
        if (certificate.getRevocationCode() == null) {
            return CertificateStatus.GOOD;
        } else {
            DERGeneralizedTime derGeneralizedTime = new DERGeneralizedTime(convert(certificate.getRevocationDate()));
            RevokedInfo info = new RevokedInfo(derGeneralizedTime, CRLReason.lookup(certificate.getRevocationCode()));
            return new RevokedStatus(info);
        }
    }
}
