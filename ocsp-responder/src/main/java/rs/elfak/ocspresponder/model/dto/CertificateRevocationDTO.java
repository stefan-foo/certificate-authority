package rs.elfak.ocspresponder.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.elfak.ocspresponder.model.enums.RevocationReason;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRevocationDTO {

    private String email;
    private RevocationReason revocationReason;
    private String revocationMessage;
}
