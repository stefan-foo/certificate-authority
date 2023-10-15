package rs.elfak.caprovider.repository;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;
import rs.elfak.caprovider.db.tables.records.CertificateRecord;
import rs.elfak.caprovider.model.enums.RevocationReason;

import java.time.LocalDateTime;
import java.util.Optional;

import static rs.elfak.caprovider.db.Tables.CERTIFICATE;

@Repository
public class CertificateRepo {

    private final DSLContext dsl;

    public CertificateRepo(DSLContext dsl) {
        this.dsl = dsl;
    }

    public byte[] getCertificateDerByAlias(String alias) {
        Record1<byte[]> content = dsl.select(CERTIFICATE.CERT_DER)
                .from(CERTIFICATE)
                .where(CERTIFICATE.ALIAS.eq(alias))
                .fetchOne();
        return Optional.ofNullable(content)
                .map((c) -> c.get(CERTIFICATE.CERT_DER))
                .orElse(null);
    }

    public Integer createCertificate(CertificateRecord certificate) {
        CertificateRecord certificateRecord = dsl.newRecord(CERTIFICATE, certificate);

        return dsl.insertInto(CERTIFICATE,
                        CERTIFICATE.ALIAS,
                        CERTIFICATE.SUBJECT_NAME,
                        CERTIFICATE.SUBJECT_KEY_ID,
                        CERTIFICATE.SUBJECT_TYPE,
                        CERTIFICATE.CERT_DER,
                        CERTIFICATE.VALID_FROM,
                        CERTIFICATE.VALID_TO,
                        CERTIFICATE.ISSUING_CERTIFICATE,
                        CERTIFICATE.SERIAL_NUMBER)
                .values(
                        certificateRecord.getAlias(),
                        certificateRecord.getSubjectName(),
                        certificateRecord.getSubjectKeyId(),
                        certificateRecord.getSubjectType(),
                        certificateRecord.getCertDer(),
                        certificateRecord.getValidFrom(),
                        certificateRecord.getValidTo(),
                        certificateRecord.getIssuingCertificate(),
                        certificateRecord.getSerialNumber()
                ).returningResult(CERTIFICATE.ID)
                .fetchOne(CERTIFICATE.ID, Integer.class);
    }

    public Integer getCertIdForAlias(String alias) {
        Record1<Integer> certId = dsl.select(CERTIFICATE.ID).from(CERTIFICATE).where(CERTIFICATE.ALIAS.eq(alias)).fetchOne();
        return Optional.ofNullable(certId).map(c -> c.get(CERTIFICATE.ID)).orElse(null);
    }

    public void updateCertificateStatus(String email, RevocationReason reason, String revocationMessage) {
        dsl.update(CERTIFICATE)
                .set(CERTIFICATE.REVOCATION_CODE, reason.getCode())
                .set(CERTIFICATE.REVOCATION_MESSAGE, revocationMessage)
                .set(CERTIFICATE.REVOCATION_DATE, LocalDateTime.now())
                .where(CERTIFICATE.ALIAS.eq(email))
                .execute();
    }
}
