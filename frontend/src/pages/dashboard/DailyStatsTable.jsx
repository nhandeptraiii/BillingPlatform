import styles from "./DailyStatsTable.module.scss";

const DailyStatsTable = ({ data }) => {
  return (
    <div className={styles.wrapper}>
      <h3>Hoạt động hôm nay</h3>

      <table>
        <thead>
          <tr>
            <th>Nhân viên</th>
            <th>Bill đầu tiên</th>
            <th>Số bill thu</th>
          </tr>
        </thead>

        <tbody>
          {data.map((item) => (
            <tr key={item.consultantId}>
              <td>{item.consultantName}</td>

              <td>
                {new Date(
                  item.firstBillPrintedAt
                ).toLocaleTimeString()}
              </td>

              <td>{item.collectedCount}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default DailyStatsTable;