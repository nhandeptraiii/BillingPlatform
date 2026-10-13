package vn.viettel.khdn.billing_platform.util.error;

/**
 * Ném khi xóa/vô hiệu hóa một người dùng còn hồ sơ thu cước đang giao cho họ
 * mà chưa chỉ định người nhận. Frontend dùng mã lỗi "TRANSFER_REQUIRED" để mở hộp chọn người nhận.
 */
public class TransferRequiredException extends RuntimeException {

    private final long recordCount;

    public TransferRequiredException(String message, long recordCount) {
        super(message);
        this.recordCount = recordCount;
    }

    public long getRecordCount() {
        return recordCount;
    }
}
