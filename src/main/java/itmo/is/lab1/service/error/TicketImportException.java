package itmo.is.lab1.service.error;

import lombok.Getter;

public class TicketImportException extends RuntimeException {

    @Getter
    private final Integer recordNumber;

    @Getter
    private final String field;

    private final String detail;

    @Getter
    private final boolean conflict;

    private TicketImportException(Integer recordNumber, String field,
                                  String detail, boolean conflict) {
        super((recordNumber == null ? "" : "Record " + recordNumber + ", ")
                + field + ": " + detail);
        this.recordNumber = recordNumber;
        this.field = field;
        this.detail = detail;
        this.conflict = conflict;
    }

    public static TicketImportException invalid(String field, String message) {
        return new TicketImportException(null, field, message, false);
    }

    public static TicketImportException conflict(int recordNumber,
                                                String field, String message) {
        return new TicketImportException(recordNumber, field, message, true);
    }

    public TicketImportException atRecord(int number) {
        return new TicketImportException(number, field, detail, conflict);
    }
}
