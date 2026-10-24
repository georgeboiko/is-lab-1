package itmo.is.lab1.service.error;

import lombok.Getter;

public class UniquenessViolationException extends IllegalStateException {

    @Getter
    private final String field;

    public UniquenessViolationException(String field, String message) {
        super(message);
        this.field = field;
    }
}
