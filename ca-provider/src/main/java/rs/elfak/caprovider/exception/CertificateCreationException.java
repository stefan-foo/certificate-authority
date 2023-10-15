package rs.elfak.caprovider.exception;

public class CertificateCreationException extends RuntimeException {
    public CertificateCreationException(String message) {
        super(message);
    }
    public CertificateCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
