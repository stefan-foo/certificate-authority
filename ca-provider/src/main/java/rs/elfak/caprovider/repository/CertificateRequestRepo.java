package rs.elfak.caprovider.repository;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import rs.elfak.caprovider.db.enums.RequestStatus;
import rs.elfak.caprovider.db.enums.RequestType;
import rs.elfak.caprovider.db.tables.pojos.CertificateRequest;
import rs.elfak.caprovider.db.tables.records.CertificateRecord;
import rs.elfak.caprovider.db.tables.records.CertificateRequestRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static rs.elfak.caprovider.db.Tables.CERTIFICATE;
import static rs.elfak.caprovider.db.tables.CertificateRequest.CERTIFICATE_REQUEST;

@Repository
public class CertificateRequestRepo {

    private final DSLContext dsl;

    public CertificateRequestRepo(DSLContext dsl) {
        this.dsl = dsl;
    }
    public Integer saveCertificateRequest(CertificateRequest certificateRequest) {
        return dsl.insertInto(CERTIFICATE_REQUEST, CERTIFICATE_REQUEST.STATUS, CERTIFICATE_REQUEST.EMAIL, CERTIFICATE_REQUEST.REQUEST_TYPE)
                .values(RequestStatus.PENDING, certificateRequest.getEmail(), certificateRequest.getRequestType())
                .returningResult(CERTIFICATE_REQUEST.ID)
                .fetchOne(CERTIFICATE_REQUEST.ID, Integer.class);
    }

    public boolean updateCertificateRequestStatus(Integer id, RequestStatus status) {
        int execute = dsl.update(CERTIFICATE_REQUEST)
                .set(CERTIFICATE_REQUEST.STATUS, RequestStatus.APPROVED)
                .where(CERTIFICATE_REQUEST.ID.eq(id))
                .execute();
        return execute > 0;
    }

    public boolean approveRequest(Integer requestId, Integer certificateId) {
        int execute = dsl.update(CERTIFICATE_REQUEST)
                .set(CERTIFICATE_REQUEST.STATUS, RequestStatus.APPROVED)
                .set(CERTIFICATE_REQUEST.CERTIFICATE, certificateId)
                .where(CERTIFICATE_REQUEST.ID.eq(requestId))
                .execute();
        return execute > 0;
    }

    public List<CertificateRequest> getPendingRequests(RequestType requestType) {
        return dsl.selectFrom(CERTIFICATE_REQUEST)
                .where(CERTIFICATE_REQUEST.REQUEST_TYPE.eq(requestType))
                .fetchInto(CertificateRequest.class);
    }
}
