package rs.elfak.caprovider.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.elfak.caprovider.service.MailerService;

import java.io.IOException;

@Service
public class MailerServiceImpl implements MailerService {

    @Value("${spring.sendgrid.api-key}")
    String sendGridApiKey;
    private final Email sender = new Email("cert.auth.demo@outlook.com");

    @Override
    public void sendCertificateMail(String subject, String receiver, String password, String attachmentName, byte[] pkcs12) throws IOException {
        Email to = new Email(receiver);
        Content content = new Content("text/plain", String.format("Your password is: %s", password));
        Mail mail = new Mail(sender, subject, to, content);

        mail.addAttachments(createCertificateAttachment(attachmentName, pkcs12));
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            throw ex;
        }
    }

    private Attachments createCertificateAttachment(String certName, byte[] cert) {
        Attachments attachments = new Attachments();
        attachments.setContent(new String(Base64.encodeBase64(cert)));
        attachments.setType("application/zip");
        attachments.setFilename(certName);
        return attachments;
    }
}
