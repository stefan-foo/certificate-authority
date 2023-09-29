package rs.elfak.certificateauthorityws.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.elfak.certificateauthorityws.service.FileService;

@RestController()
@RequestMapping("/certs")
class CertsController {

    final
    FileService fileService;

    public CertsController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("{cert}")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable("cert") String certName) {
        Resource certificate = fileService.getCertificate(certName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/pkix-cert"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(certificate.getFilename()).build().toString())
                .body(certificate);
    }
}