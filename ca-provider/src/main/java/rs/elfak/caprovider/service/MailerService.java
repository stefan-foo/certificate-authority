package rs.elfak.caprovider.service;

import java.io.IOException;

public interface MailerService {

    void sendCertificateMail(String subject, String receiver, String password, String attachmentName, byte[] pkcs12) throws IOException;
}
