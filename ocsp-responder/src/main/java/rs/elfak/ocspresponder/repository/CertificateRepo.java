package rs.elfak.ocspresponder.repository;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;
import rs.elfak.ocspresponder.db.tables.pojos.Certificate;
import rs.elfak.ocspresponder.db.tables.records.CertificateRecord;

import java.math.BigInteger;
import java.util.Optional;

import static rs.elfak.ocspresponder.db.Tables.CERTIFICATE;

@Repository
public class CertificateRepo {

    private final DSLContext dsl;

    public CertificateRepo(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Integer getCertIdForAlias(String alias) {
        Record1<Integer> certId = dsl.select(CERTIFICATE.ID).from(CERTIFICATE).where(CERTIFICATE.ALIAS.eq(alias)).fetchOne();
        return Optional.ofNullable(certId).map(c -> c.get(CERTIFICATE.ID)).orElse(null);
    }

    public Certificate getCertificate(Integer issuerId, BigInteger serialNumber) {
        return dsl.selectFrom(CERTIFICATE)
                .where(CERTIFICATE.SERIAL_NUMBER.eq(serialNumber).and(CERTIFICATE.ISSUING_CERTIFICATE.eq(issuerId)))
                .fetchOneInto(Certificate.class);
    }

}
