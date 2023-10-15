package rs.elfak.caprovider.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.elfak.caprovider.db.enums.RevocationStatusCode;
import rs.elfak.caprovider.model.enums.RevocationReason;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRevocationDTO {

    private String email;
    private RevocationReason revocationReason;
    private String revocationMessage;
}
