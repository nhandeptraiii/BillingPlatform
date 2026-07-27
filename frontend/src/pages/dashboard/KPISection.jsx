import styles from "./KPISection.module.scss";
import numeral from "numeral";

const KPISection = ({ overview }) => {
  return (
    <div className={styles.grid}>

  <div className={styles.card}>
    <h4>Tổng hồ sơ import</h4>
    <span>
      {overview.totalRecordsImported || 0}
    </span>
  </div>

  <div className={styles.card}>
    <h4>Đã in bill</h4>
    <span>
      {overview.totalCollectedRecords || 0}
    </span>
  </div>

  <div className={styles.cardDebt}>
    <h4>Đã gạch nợ</h4>
    <span>
      {overview.totalMarkedDebtRecords || 0}
    </span>
  </div>

  <div className={styles.card}>
    <h4>Đã thanh toán</h4>
    <span>
      {numeral(
        overview.totalCollectedAmount || 0
      ).format("0,0")}
      đ
    </span>
  </div>

  <div className={styles.card}>
    <h4>Phải thu</h4>
    <span>
      {numeral(
        overview.totalExpectedAmount || 0
      ).format("0,0")}
      đ
    </span>
  </div>

</div>
  );
};

export default KPISection;