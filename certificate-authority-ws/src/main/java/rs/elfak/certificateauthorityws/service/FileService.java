package rs.elfak.certificateauthorityws.service;

import org.springframework.core.io.Resource;

public interface FileService {

    Resource getCertificate(String resName);
}
