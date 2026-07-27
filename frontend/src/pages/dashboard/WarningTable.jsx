import styles from "./WarningTable.module.scss";

const formatMoney = (value) =>
  new Intl.NumberFormat("vi-VN").format(
    value
  );

const formatDate = (value) => {
  if (!value) return "Chưa phát sinh";

  return new Date(value).toLocaleString(
    "vi-VN"
  );
};

const WarningTable = ({ warnings }) => {

  const items =
    warnings?.content || [];

  return (
    <div className={styles.wrapper}>

      <div className={styles.header}>
        <h3>
          Cảnh báo cần xử lý
        </h3>

        <span>
          {items.length} cảnh báo
        </span>
      </div>

      {items.length === 0 ? (
        <div className={styles.empty}>
          Không có cảnh báo
        </div>
      ) : (
        items.map((item) => {

          let message =
            "Cần kiểm tra";

          if (
            item.collectionStatus ===
              "DA_THANH_TOAN" &&
            item.debtStatus ===
              "CHUA_GACH_NO"
          ) {
            message =
              "Đã thu tiền nhưng chưa gạch nợ Viettel";
          }

          return (
            <div
              key={item.id}
              className={styles.alertCard}
            >
              <div className={styles.icon}>
                ⚠️
              </div>

              <div className={styles.content}>
                <div
                  className={
                    styles.customer
                  }
                >
                  {item.customerName}
                </div>

                <div
                  className={
                    styles.amount
                  }
                >
                  Đã thu{" "}
                  {formatMoney(
                    item.collectedAmount
                  )}
                  đ
                </div>

                <div
                  className={
                    styles.warning
                  }
                >
                  {message}
                </div>

                <div
                  className={
                    styles.meta
                  }
                >
                  Thu bởi{" "}
                  {item.collectedByName}
                </div>

                <div
                  className={
                    styles.meta
                  }
                >
                  {formatDate(
                    item.collectedAt
                  )}
                </div>
              </div>
            </div>
          );
        })
      )}
    </div>
  );
};

export default WarningTable;