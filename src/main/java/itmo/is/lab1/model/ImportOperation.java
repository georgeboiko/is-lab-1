package itmo.is.lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "import_operations")
@Getter
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ImportStatus status;

    @Column(name = "added_count")
    private Integer addedCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "error_message")
    private String errorMessage;

    protected ImportOperation() {}

    public ImportOperation(UserAccount user) {
        this.user = user;
        this.status = ImportStatus.RUNNING;
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void complete(int addedCount) {
        requireRunning();
        if (addedCount < 0) {
            throw new IllegalArgumentException("Added count must not be negative");
        }
        this.status = ImportStatus.SUCCESS;
        this.addedCount = addedCount;
        this.finishedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void fail(String message) {
        requireRunning();
        this.status = ImportStatus.FAILED;
        this.addedCount = null;
        this.finishedAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.errorMessage = message;
    }

    private void requireRunning() {
        if (status != ImportStatus.RUNNING) {
            throw new IllegalStateException("Import operation has already finished");
        }
    }
}
