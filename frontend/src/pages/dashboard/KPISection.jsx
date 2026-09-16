import styles from "./KPISection.module.scss";
import numeral from "numeral";

const KPISection = ({ overview = {} }) => {
  const ftthTargetRecords = Math.round((overview.ftthN1TotalRecords || 0) * (overview.targetFtthN1Percent || 0) / 100);
  const ftthTargetAmount = (overview.ftthN1ExpectedAmount || 0) * (overview.targetFtthN1Percent || 0) / 100;
  return <div className={styles.grid}>
    <div className={styles.card}><h4>Tổng số KH đầu kỳ</h4><span>{numeral(overview.totalRecordsImported || 0).format("0,0")}</span></div>
    <div className={styles.cardDebt}><h4>Tổng số KH đã thu</h4><span>{numeral(overview.totalMarkedDebtRecords || 0).format("0,0")}</span></div>
    <div className={styles.card}><h4>Tổng số tiền đã thu</h4><span>{numeral(overview.totalCollectedAmount || 0).format("0,0")} đ</span></div>
    <div className={styles.card}><h4>Tổng số tiền đầu kỳ</h4><span>{numeral(overview.totalExpectedAmount || 0).format("0,0")} đ</span></div>
    {overview.targetFtthN1Percent != null && <div className={styles.cardFtth}><h4>FTTH N1 – KH mục tiêu ({overview.targetFtthN1Percent}%)</h4><span>{numeral(ftthTargetRecords).format("0,0")}</span></div>}
    {overview.targetFtthN1Percent != null && <div className={styles.cardFtth}><h4>FTTH N1 – doanh thu mục tiêu</h4><span>{numeral(ftthTargetAmount).format("0,0")} đ</span></div>}
  </div>;
};
export default KPISection;
