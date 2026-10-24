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

    @Getter
    private final Long operationId;

    private TicketImportException(Integer recordNumber, String field,
                                  String detail, boolean conflict) {
        this(recordNumber, field, detail, conflict, null);
    }

    private TicketImportException(Integer recordNumber, String field,
                                  String detail, boolean conflict, Long operationId) {
        super((recordNumber == null ? "" : "Record " + recordNumber + ", ")
                + field + ": " + detail);
        this.recordNumber = recordNumber;
        this.field = field;
        this.detail = detail;
        this.conflict = conflict;
        this.operationId = operationId;
    }

    public static TicketImportException invalid(String field, String message) {
        return new TicketImportException(null, field, message, false);
    }

    public static TicketImportException conflict(int recordNumber,
                                                String field, String message) {
        return new TicketImportException(recordNumber, field, message, true);
    }

    public TicketImportException atRecord(int number) {
        return new TicketImportException(number, field, detail, conflict, operationId);
    }

    public TicketImportException withOperationId(long id) {
        TicketImportException exception = new TicketImportException(
                recordNumber, field, detail, conflict, id);
        exception.initCause(this);
        return exception;
    }
}
