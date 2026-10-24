package itmo.is.lab1.dto.response;

import itmo.is.lab1.model.ImportOperation;
import itmo.is.lab1.model.ImportStatus;
import itmo.is.lab1.model.UserAccount;

import java.time.OffsetDateTime;

public record ImportOperationResponse(
        Long id,
        ImportStatus status,
        UserResponse user,
        Integer addedCount,
        OffsetDateTime createdAt,
        OffsetDateTime finishedAt,
        String errorMessage
) {
    public static ImportOperationResponse from(ImportOperation operation) {
        UserAccount user = operation.getUser();
        return new ImportOperationResponse(
                operation.getId(), operation.getStatus(),
                new UserResponse(user.getId(), user.getUsername(), user.getRole()),
                operation.getStatus() == ImportStatus.SUCCESS ? operation.getAddedCount() : null,
                operation.getCreatedAt(), operation.getFinishedAt(), operation.getErrorMessage());
    }
}
