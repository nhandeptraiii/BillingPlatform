import styles from "./DebtTable.module.scss";
import { useSelector } from "react-redux";

const DebtTable = () => {
const stateData = useSelector(state => state);

console.log("Redux:", stateData);

const debts = stateData.importInitialDebt?.debts || [];

  return (
    <div className={styles.tableWrapper}>
      <table>
        <thead>
          <tr>
            <th></th>
            <th>Khách hàng</th>
            <th>Cước</th>
            <th>Kỳ TT</th>
            <th>Nhân viên</th>
            <th>Đã thu</th>
            <th>In bill</th>
            <th>Gạch nợ</th>
          </tr>
        </thead>

        <tbody>
          {debts.map(item => (
            <tr key={item.id}>
              <td>
                <input type="checkbox" />
              </td>

              <td>
                <div>
                  <b>{item.customerName}</b>
                  <p>{item.customerCode}</p>
                  <p>{item.phone}</p>
                </div>
              </td>

              <td>
                {item.amount.toLocaleString()}
              </td>

              <td>{item.period}</td>

              <td>{item.employee}</td>

              <td>
                <span
                  className={
                    item.collected
                      ? styles.success
                      : styles.danger
                  }
                >
                  {item.collected
                    ? "Đã thu"
                    : "Chưa thu"}
                </span>
              </td>

              <td>
                {item.printed ? "Đã in" : "Chưa in"}
              </td>

              <td>
                {item.cleared
                  ? "Đã gạch"
                  : "Chưa gạch"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default DebtTable;