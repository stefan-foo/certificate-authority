package rs.elfak.caprovider.service.impl;

import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.asn1.ocsp.ResponseBytes;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.springframework.stereotype.Service;

@Service
public class OCSPService {

    public void getOCSPResponse(OCSPReq ocspReq) {
        X509CertificateHolder[] certs = ocspReq.getCerts();
//        ResponseBytes responseBytes = new ResponseBytes();
//        OCSPResponse response = new OCSPResponse(OCSPResponseStatus.SUCCESSFUL, new byte[10]);
//        return new OCSPResp();
    }
}
