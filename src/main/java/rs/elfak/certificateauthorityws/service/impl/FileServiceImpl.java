package rs.elfak.certificateauthorityws.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import rs.elfak.certificateauthorityws.service.FileService;

import java.io.File;

@Service
public class FileServiceImpl implements FileService {

    private final String classPath = "classpath:";

    @Value("${certificate.base-path}")
    private String certificateBasePath;
    @Value("${certificate.extension}")
    private String certificateExtension;

    final
    ResourceLoader resourceLoader;

    public FileServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Resource getCertificate(String cert) {
        String pathToCert = classPath
                .concat(certificateBasePath)
                .concat(File.separator)
                .concat(cert)
                .concat(".")
                .concat(certificateExtension);
        return resourceLoader.getResource(pathToCert);
    }
}
