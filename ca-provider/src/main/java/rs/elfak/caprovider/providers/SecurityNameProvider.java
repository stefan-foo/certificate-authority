package rs.elfak.caprovider.providers;

import org.springframework.stereotype.Component;

@Component
public class SecurityNameProvider {

    private final String BC_PROVIDER = "BC";

    public String getName() {
        return BC_PROVIDER;
    }
}
