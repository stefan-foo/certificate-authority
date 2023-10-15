package rs.elfak.caprovider.service.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rs.elfak.caprovider.db.enums.RequestType;
import rs.elfak.caprovider.db.tables.pojos.CertificateRequest;
import rs.elfak.caprovider.db.tables.records.CertificateRecord;
import rs.elfak.caprovider.exception.CertificateCreationException;
import rs.elfak.caprovider.exception.NotFoundException;
import rs.elfak.caprovider.mapper.CertificateMapper;
import rs.elfak.caprovider.model.dto.CertificateRevocationDTO;
import rs.elfak.caprovider.model.dto.SMimeCertRequestDTO;
import rs.elfak.caprovider.providers.SigningContextProvider;
import rs.elfak.caprovider.repository.CertificateRepo;
import rs.elfak.caprovider.repository.CertificateRequestRepo;
import rs.elfak.caprovider.service.CertsManagerService;
import rs.elfak.caprovider.service.CertsService;
import rs.elfak.caprovider.service.MailerService;
import rs.elfak.caprovider.util.CertUtils;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class CertsServiceImpl implements CertsService {

    private static final Logger LOGGER = LogManager.getLogger(CertsServiceImpl.class);
    private final String ATTACHMENT_PREFIX = "PKCS12_";
    final CertificateRequestRepo certificateRequestRepo;
    final CertsManagerService x509CertService;
    final CertificateRepo certificateRepo;
    final MailerService mailerService;
    final CertificateMapper mapper;
    final SigningContextProvider contextProvider;

    public CertsServiceImpl(
            SigningContextProvider contextProvider,
            CertificateRequestRepo certificateRequestRepo,
            CertsManagerService x509CertService,
            CertificateRepo certificateRepo,
            MailerService mailerService,
            CertificateMapper mapper) {
        this.contextProvider = contextProvider;
        this.certificateRequestRepo = certificateRequestRepo;
        this.certificateRepo = certificateRepo;
        this.x509CertService = x509CertService;
        this.mailerService = mailerService;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public byte[] getCertificate(String cert) {
        LOGGER.info("Certificate {} requested", cert);
        byte[] certificate = certificateRepo.getCertificateDerByAlias(cert);
        return Optional.ofNullable(certificate)
                .orElseThrow(() -> new NotFoundException("Certificate not found"));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void requestCertificate(SMimeCertRequestDTO req) {
        CertificateRequest certificateRequest = mapper.from(req);

        Integer requestId = certificateRequestRepo.saveCertificateRequest(certificateRequest);
        if (requestId <= 0) {
            throw new CertificateCreationException("Error occurred while requesting certificate");
        } else if (!certificateRequest.getRequestType().equals(RequestType.EMAIL_CERT)) {
            return;
        }
        LOGGER.info("Generating certificate for {}", certificateRequest.getEmail());
        try {
            KeyPair issuingKeyPair = x509CertService.getKeyPair();
            PKCS10CertificationRequest csr = x509CertService.getCsr(certificateRequest, issuingKeyPair);
            X509Certificate certificate = x509CertService.signCsr(contextProvider.getContext(), csr, certificateRequest, new Date(), new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000));

            Integer issuingCertId = certificateRepo.getCertIdForAlias(contextProvider.getCaAlias());
            CertificateRecord certificateRecord = mapper.fromSMIME(certificate);
            certificateRecord.setIssuingCertificate(issuingCertId);

            Integer createdCertId = certificateRepo.createCertificate(certificateRecord);
            certificateRequestRepo.approveRequest(requestId, createdCertId);

            String password = RandomStringUtils.randomAlphanumeric(10);
            String baseFileName = ATTACHMENT_PREFIX.concat(certificateRequest.getEmail());
            PKCS12PfxPdu pfx = x509CertService.createPfx(certificate, issuingKeyPair.getPrivate(), password, contextProvider.getCaCertificate());
            byte[] zipBytes = CertUtils.zipBytes(baseFileName.concat(".pfx"), pfx.getEncoded());
            mailerService.sendCertificateMail("Your Certificate", certificateRequest.getEmail(), password, baseFileName.concat(".zip"), zipBytes);
        } catch (Exception e) {
            LOGGER.error("Exception occurred during certificate creation {}", e.getMessage());
            throw new CertificateCreationException("Failed to create certificate", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<CertificateRequest> getPendingIntermediateCARequests() {
        return certificateRequestRepo.getPendingRequests(RequestType.INTERMEDIATE_CA);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateCertificateStatus(CertificateRevocationDTO revocation) {
        certificateRepo.updateCertificateStatus(revocation.getEmail(), revocation.getRevocationReason(), revocation.getRevocationMessage());
    }
}
