package rs.elfak.caprovider.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import rs.elfak.caprovider.db.tables.pojos.CertificateRequest;
import rs.elfak.caprovider.model.dto.CertificateRevocationDTO;
import rs.elfak.caprovider.model.dto.SMimeCertRequestDTO;
import rs.elfak.caprovider.service.CertsService;

import java.util.List;

@RestController()
@RequestMapping("/certs")
class CertsController {
    final CertsService certsService;

    public CertsController(CertsService fileService) {
        this.certsService = fileService;
    }

    @GetMapping("/{cert}")
    @Operation(summary = "Returns DER encoded certificate for specified cert alias")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable("cert") String cert) {
        byte[] certificate = certsService.getCertificate(cert);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/pkix-cert"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(cert.concat(".der")).build().toString())
                .body(certificate);
    }

    @PostMapping("/request")
    @Operation(summary = "Creates certificate request for specified email",
    description = "Requests of type EMAIL_CERT will be processed immediately")
    public ResponseEntity<Void> createCertRequest(@RequestBody SMimeCertRequestDTO certificateRequest) {
        certsService.requestCertificate(certificateRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/revoke")
    @Operation(summary = "Revokes certificate")
    public ResponseEntity<Void> revokeCertificate(@RequestBody CertificateRevocationDTO request) {
        certsService.updateCertificateStatus(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/ica/pending")
    @Operation(summary = "Fetch list of pending certificate requests")
    public ResponseEntity<List<CertificateRequest>> getPendingIntermediateCARequests() {
        return ResponseEntity.ok(certsService.getPendingIntermediateCARequests());
    }

}