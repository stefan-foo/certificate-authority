package rs.elfak.caprovider.service;

import rs.elfak.caprovider.db.tables.pojos.CertificateRequest;
import rs.elfak.caprovider.model.dto.CertificateRevocationDTO;
import rs.elfak.caprovider.model.dto.SMimeCertRequestDTO;

import java.util.List;

public interface CertsService {
    byte[] getCertificate(String cert);

    void requestCertificate(SMimeCertRequestDTO certificateRequest);

    List<CertificateRequest> getPendingIntermediateCARequests();

    void updateCertificateStatus(CertificateRevocationDTO revocation);
}
