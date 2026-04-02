package com.prj.learnvocabularybe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migration thủ công chạy sau khi Spring Boot khởi động xong.
 *
 * Lý do cần class này:
 * Supabase dùng PgBouncer (port 6543) — không tương thích với server-side prepared statements
 * của PostgreSQL JDBC. Hibernate ddl-auto=update có thể thất bại im lặng,
 * dẫn tới cột / bảng mới không được tạo → HTTP 500 khi gọi API.
 *
 * Tất cả SQL ở đây đều idempotent (IF NOT EXISTS / IF EXISTS) — an toàn khi chạy nhiều lần.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void runMigrations() {
        log.info("=== DatabaseInitializer: bắt đầu kiểm tra schema ===");

        // Bảng users — cột thông báo (legacy, giữ để tương thích)
        addColumnIfMissing("users", "notification_enabled", "BOOLEAN NOT NULL DEFAULT FALSE");
        addColumnIfMissing("users", "notification_time",    "VARCHAR(5) DEFAULT '08:00'");

        // Bảng study_reminders — lưu nhiều lịch nhắc học mỗi user
        createStudyRemindersTable();

        log.info("=== DatabaseInitializer: hoàn tất ===");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Thêm cột vào bảng nếu chưa tồn tại (idempotent) */
    private void addColumnIfMissing(String table, String column, String definition) {
        String sql = String.format(
                "ALTER TABLE %s ADD COLUMN IF NOT EXISTS %s %s",
                table, column, definition
        );
        try {
            jdbcTemplate.execute(sql);
            log.info("  ✓ Cột [{}.{}] đã sẵn sàng", table, column);
        } catch (Exception e) {
            log.error("  ✗ Không thể thêm cột [{}.{}]: {}", table, column, e.getMessage());
        }
    }

    /**
     * Tạo bảng study_reminders nếu chưa tồn tại.
     *
     * Cấu trúc:
     *   id         — primary key tự tăng
     *   user_id    — FK → users.id, xóa cascade khi user bị xóa
     *   time       — giờ nhắc dạng "HH:mm"
     *   enabled    — bật/tắt từng lịch
     *   created_at — thời điểm tạo
     *   UNIQUE(user_id, time) — không cho trùng giờ trong cùng user
     */
    private void createStudyRemindersTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS study_reminders (
                    id         BIGSERIAL    PRIMARY KEY,
                    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    time       VARCHAR(5)   NOT NULL,
                    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
                    CONSTRAINT uq_user_time UNIQUE (user_id, time)
                )
                """;
        try {
            jdbcTemplate.execute(sql);
            log.info("  ✓ Bảng [study_reminders] đã sẵn sàng");
        } catch (Exception e) {
            log.error("  ✗ Không thể tạo bảng [study_reminders]: {}", e.getMessage());
        }
    }
}
