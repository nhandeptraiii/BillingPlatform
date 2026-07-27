import ProgressChart from "./ProgressChart";
import styles from "./ProgressSection.module.scss";

const ProgressSection = ({ overview }) => {

  const recordPercent =
    overview.totalRecordsImported > 0
      ? (
          overview.totalMarkedDebtRecords /
          overview.totalRecordsImported
        ) * 100
      : 0;

  return (
    <div className={styles.container}>
      <ProgressChart
        title="Tỷ lệ hồ sơ đã thanh toán"
        percent={recordPercent}
      />

      <ProgressChart
        title="Tỷ lệ tiền cước đã thanh toán"
        percent={
          overview.amountProgressPercentage || 0
        }
      />
    </div>
  );
};

export default ProgressSection;