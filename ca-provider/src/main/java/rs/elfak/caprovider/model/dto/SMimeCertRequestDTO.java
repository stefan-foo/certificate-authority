package rs.elfak.caprovider.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.elfak.caprovider.db.enums.RequestType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SMimeCertRequestDTO {
    RequestType requestType;
    String email;
}
