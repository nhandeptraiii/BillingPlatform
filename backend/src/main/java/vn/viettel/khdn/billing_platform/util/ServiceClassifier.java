package vn.viettel.khdn.billing_platform.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Nhận diện nhóm dịch vụ từ file đầu kỳ.
 * <p>
 * FTTH N1 = cột D (Số TB/Account) có chữ "ftth" VÀ cột K (Loại dịch vụ) có "N1"
 * là một từ độc lập (vd: "N1 TT", "N1 CH - GT"), không khớp "N10", "KN1"...
 * <p>
 * Quy tắc này PHẢI khớp với câu lệnh backfill trong
 * {@code FtthN1BackfillRunner} (REGEXP) để dữ liệu cũ và mới nhất quán.
 */
public final class ServiceClassifier {

    private static final Pattern N1_TOKEN = Pattern.compile("(?<![A-Z0-9])N1(?![A-Z0-9])");

    private ServiceClassifier() {
    }

    public static boolean isFtthN1(String subscriberNumber, String serviceType) {
        if (subscriberNumber == null || serviceType == null) {
            return false;
        }
        boolean hasFtth = subscriberNumber.toLowerCase(Locale.ROOT).contains("ftth");
        return hasFtth && N1_TOKEN.matcher(serviceType.toUpperCase(Locale.ROOT)).find();
    }
}
