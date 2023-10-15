package rs.elfak.caprovider.mapper;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rs.elfak.caprovider.db.enums.SubjectType;
import rs.elfak.caprovider.db.tables.pojos.CertificateRequest;
import rs.elfak.caprovider.db.tables.records.CertificateRecord;
import rs.elfak.caprovider.model.dto.SMimeCertRequestDTO;
import rs.elfak.caprovider.providers.SecurityNameProvider;
import rs.elfak.caprovider.util.CertUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;

@Component
public class CertificateMapper {

    final
    SecurityNameProvider securityNameProvider;

    public CertificateMapper(SecurityNameProvider securityNameProvider) {
        this.securityNameProvider = securityNameProvider;
    }

    public CertificateRecord fromSMIME(X509Certificate x509) throws CertificateEncodingException {
        CertificateRecord certificateRecord = new CertificateRecord();
        certificateRecord.setSubjectType(x509.getBasicConstraints() == -1 ? SubjectType.END_ENTITY : SubjectType.INTERMEDIATE_CA);
        certificateRecord.setValidFrom(x509.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        certificateRecord.setValidTo(x509.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        certificateRecord.setAlias(CertUtils.getSubjectName(x509).replace(" ", "").toLowerCase());
        certificateRecord.setSubjectName(x509.getSubjectX500Principal().getName());
        certificateRecord.setSubjectKeyId(CertUtils.getSubjectKeyIdentifier(x509));
        certificateRecord.setCertDer(x509.getEncoded());
        certificateRecord.setIssuingCertificate(null);
        certificateRecord.setSerialNumber(x509.getSerialNumber());
        return certificateRecord;
    }

    public CertificateRequest from(SMimeCertRequestDTO request) {
        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.setEmail(request.getEmail());
        certificateRequest.setRequestType(request.getRequestType());
        return certificateRequest;
    }
}
