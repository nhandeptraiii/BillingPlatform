import { toast } from "react-toastify";
import dashboardService from "../../services/dashboardService";
import styles from "./ConsultantProgressTable.module.scss";

const number = (value) => Number(value || 0).toLocaleString("vi-VN");
const percent = (part, total) => total > 0 ? (part / total * 100).toFixed(1) : "0.0";
const targetPercent = 97;
const remainingRecords = (records, collected) => Math.max(Math.ceil(Number(records || 0) * targetPercent / 100) - Number(collected || 0), 0);
const remainingAmount = (amount, collected) => Math.max(Number(amount || 0) * targetPercent / 100 - Number(collected || 0), 0);

export default function ConsultantProgressTable({ consultants = [], overview = {}, month, year }) {
  const exportExcel = async () => {
    try {
      const response = await dashboardService.exportConsultants(month, year);
      const url = URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.download = `TienDoThuCuoc_${month}_${year}.xlsx`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (error) { toast.error(error.response?.data?.message || "Không thể xuất báo cáo Excel"); }
  };
  const totals = consultants.reduce((sum, item) => ({
    records: sum.records + Number(item.targetRecords || 0), amounts: sum.amounts + Number(item.targetAmount || 0),
    collectedRecords: sum.collectedRecords + Number(item.collectedRecords || 0), collectedAmounts: sum.collectedAmounts + Number(item.collectedAmount || 0)
  }), { records: 0, amounts: 0, collectedRecords: 0, collectedAmounts: 0 });

  return <section className={styles.section}>
    <div className={styles.header}><div><h2>Kết quả thu cước theo nhân viên</h2><p>Số còn phải thu để đạt mức 97% của đầu kỳ.</p></div><button onClick={exportExcel}>Xuất Excel</button></div>
    <div className={styles.tableWrap}><table><thead>
      <tr><th rowSpan="2">STT</th><th rowSpan="2">Nhân viên</th><th colSpan="2">Đầu kỳ</th><th colSpan="4">Đã thu</th><th colSpan="2">Phải thu để đạt 97%</th></tr>
      <tr><th>Khách hàng</th><th>Số tiền</th><th>Khách hàng</th><th>% KH</th><th>Số tiền</th><th>% tiền</th><th>Khách hàng</th><th>Số tiền</th></tr>
    </thead><tbody>
      {!consultants.length ? <tr><td colSpan="10">Không có dữ liệu</td></tr> : consultants.map((item, index) => {
        const records = Number(item.targetRecords || 0); const amount = Number(item.targetAmount || 0);
        return <tr key={item.consultantId || index}><td>{index + 1}</td><td className={styles.name}>{item.consultantName}</td><td>{number(records)}</td><td>{number(amount)}</td><td>{number(item.collectedRecords)}</td><td>{percent(item.collectedRecords, records)}%</td><td>{number(item.collectedAmount)}</td><td>{percent(item.collectedAmount, amount)}%</td><td className={styles.required}>{number(remainingRecords(records, item.collectedRecords))}</td><td className={styles.required}>{number(remainingAmount(amount, item.collectedAmount))}</td></tr>;
      })}
    </tbody>{!!consultants.length && <tfoot><tr><th colSpan="2">TỔNG</th><th>{number(totals.records)}</th><th>{number(totals.amounts)}</th><th>{number(totals.collectedRecords)}</th><th>{percent(totals.collectedRecords, totals.records)}%</th><th>{number(totals.collectedAmounts)}</th><th>{percent(totals.collectedAmounts, totals.amounts)}%</th><th>{number(remainingRecords(overview.totalRecordsImported ?? totals.records, overview.totalMarkedDebtRecords ?? totals.collectedRecords))}</th><th>{number(remainingAmount(overview.totalExpectedAmount ?? totals.amounts, overview.totalCollectedAmount ?? totals.collectedAmounts))}</th></tr></tfoot>}</table></div>
  </section>;
}
