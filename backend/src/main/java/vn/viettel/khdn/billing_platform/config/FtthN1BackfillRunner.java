package vn.viettel.khdn.billing_platform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Backfill cờ is_ftth_n1 cho các bản ghi cũ (cột mới thêm nên đang NULL).
 * Chạy idempotent: chỉ cập nhật dòng còn NULL. Quy tắc khớp với
 * {@code ServiceClassifier.isFtthN1}.
 */
@Component
public class FtthN1BackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FtthN1BackfillRunner.class);

    private static final String SQL = """
            UPDATE customer_billing_records
               SET is_ftth_n1 = (LOWER(subscriber_number) LIKE '%ftth%'
                   AND UPPER(service_type) REGEXP '(^|[^A-Z0-9])N1([^A-Z0-9]|$)')
             WHERE is_ftth_n1 IS NULL
            """;

    private final JdbcTemplate jdbcTemplate;

    public FtthN1BackfillRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int updated = jdbcTemplate.update(SQL);
            if (updated > 0) {
                log.info("[FtthN1Backfill] Đã cập nhật is_ftth_n1 cho {} bản ghi", updated);
            }
            // Dọn dẹp manager_id cho các tài khoản không còn là CONSULTANT (ví dụ đã đổi sang NVKD)
            jdbcTemplate.update("UPDATE users SET manager_id = NULL WHERE role <> 'CONSULTANT' AND manager_id IS NOT NULL");
            // Đồng bộ region_id cho các tư vấn viên chưa có region_id nhưng đã được giao hồ sơ trong khu vực
            jdbcTemplate.update("""
                    UPDATE users u
                    JOIN (
                        SELECT assigned_consultant_id, MAX(region_id) AS region_id
                        FROM customer_billing_records
                        WHERE assigned_consultant_id IS NOT NULL AND region_id IS NOT NULL
                        GROUP BY assigned_consultant_id
                    ) r ON u.id = r.assigned_consultant_id
                    SET u.region_id = r.region_id
                    WHERE u.region_id IS NULL
                    """);
        } catch (Exception e) {
            log.warn("[FtthN1Backfill] Bỏ qua backfill: {}", e.getMessage());
        }
    }
}
