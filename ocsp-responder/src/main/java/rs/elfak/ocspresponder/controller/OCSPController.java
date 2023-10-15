package rs.elfak.ocspresponder.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.elfak.ocspresponder.service.impl.OCSPResponderServiceImpl;

import java.security.Security;
import java.util.Map;

@RestController()
@RequestMapping("/ocsp")
public class OCSPController {

    OCSPResponderServiceImpl ocspResponderService;

    public OCSPController(OCSPResponderServiceImpl ocspResponderService) {
        this.ocspResponderService = ocspResponderService;
    }

    @PostConstruct
    private void post() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @PostMapping(consumes = { "application/ocsp-request" })
    @Operation(summary = "Responds to OCSP requests")
    public ResponseEntity<byte[]> getResponse(@RequestHeader Map<String,String> headers, @RequestBody byte[] req) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/ocsp-response")
                .body(ocspResponderService.createOCSPResponse(req));
    }

}
