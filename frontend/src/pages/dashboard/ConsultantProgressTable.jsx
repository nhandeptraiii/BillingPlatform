import { toast } from "react-toastify";
import dashboardService from "../../services/dashboardService";
import styles from "./ConsultantProgressTable.module.scss";

const number = (value) => Number(value || 0).toLocaleString("vi-VN");
const percent = (part, total) => total > 0 ? (part / total * 100).toFixed(1) : "0.0";

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
    } catch (error) {
      toast.error(error.response?.data?.message || "Không thể xuất báo cáo Excel");
    }
  };

  const totals = consultants.reduce((sum, item) => ({
    records: sum.records + Number(item.targetRecords || 0),
    amounts: sum.amounts + Number(item.targetAmount || 0),
    collectedRecords: sum.collectedRecords + Number(item.collectedRecords || 0),
    collectedAmounts: sum.collectedAmounts + Number(item.collectedAmount || 0),
    remainingRecords: item.remainingRecords != null ? sum.remainingRecords + Number(item.remainingRecords || 0) : sum.remainingRecords,
    remainingAmounts: item.remainingAmount != null ? sum.remainingAmounts + Number(item.remainingAmount || 0) : sum.remainingAmounts,
    hasTarget: sum.hasTarget || item.remainingRecords != null,
  }), {
    records: 0,
    amounts: 0,
    collectedRecords: 0,
    collectedAmounts: 0,
    remainingRecords: 0,
    remainingAmounts: 0,
    hasTarget: false
  });

  const subtitle = overview?.targetCustomerPercent != null || overview?.targetRevenuePercent != null
    ? `Chỉ tiêu kỳ: KH ${overview.targetCustomerPercent ?? "-"}% | Doanh thu ${overview.targetRevenuePercent ?? "-"}%`
    : "Kỳ này chưa thiết lập chỉ tiêu khu vực (vào 'Chỉ tiêu khu vực' để đặt)";

  return (
    <section className={styles.section}>
      <div className={styles.header}>
        <div>
          <h2>Kết quả thu cước theo nhân viên &amp; nhóm NVKD</h2>
          <p>{subtitle}</p>
        </div>
        <button onClick={exportExcel}>Xuất Excel</button>
      </div>

      <div className={styles.tableWrap}>
        <table>
          <thead>
            <tr>
              <th rowSpan="2">STT</th>
              <th rowSpan="2">Nhóm NVKD</th>
              <th rowSpan="2">Tư vấn viên</th>
              <th colSpan="2">Đầu kỳ</th>
              <th colSpan="4">Đã thu (đã gạch nợ)</th>
              <th colSpan="2">Phải thu để đạt chỉ tiêu</th>
            </tr>
            <tr>
              <th>Khách hàng</th>
              <th>Số tiền</th>
              <th>Khách hàng</th>
              <th>% KH</th>
              <th>Số tiền</th>
              <th>% tiền</th>
              <th>Khách hàng</th>
              <th>Số tiền</th>
            </tr>
          </thead>
          <tbody>
            {!consultants.length ? (
              <tr>
                <td colSpan="11">Không có dữ liệu</td>
              </tr>
            ) : (
              consultants.map((item, index) => {
                const records = Number(item.targetRecords || 0);
                const amount = Number(item.targetAmount || 0);
                const collectedRecs = Number(item.collectedRecords || 0);
                const collectedAmt = Number(item.collectedAmount || 0);

                return (
                  <tr key={item.consultantId || `unassigned-${index}`}>
                    <td>{index + 1}</td>
                    <td className={styles.name}>{item.managerName || (item.consultantId ? "Trực thuộc GĐKV" : "-")}</td>
                    <td className={styles.name}>{item.consultantName}</td>
                    <td>{number(records)}</td>
                    <td>{number(amount)}</td>
                    <td>{number(collectedRecs)}</td>
                    <td>{percent(collectedRecs, records)}%</td>
                    <td>{number(collectedAmt)}</td>
                    <td>{percent(collectedAmt, amount)}%</td>
                    <td className={styles.required}>
                      {item.remainingRecords != null ? number(item.remainingRecords) : <span style={{ color: "#888", fontWeight: 400 }}>Chưa đặt</span>}
                    </td>
                    <td className={styles.required}>
                      {item.remainingAmount != null ? number(item.remainingAmount) : <span style={{ color: "#888", fontWeight: 400 }}>Chưa đặt</span>}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
          {!!consultants.length && (
            <tfoot>
              <tr>
                <th colSpan="3">TỔNG</th>
                <th>{number(totals.records)}</th>
                <th>{number(totals.amounts)}</th>
                <th>{number(totals.collectedRecords)}</th>
                <th>{percent(totals.collectedRecords, totals.records)}%</th>
                <th>{number(totals.collectedAmounts)}</th>
                <th>{percent(totals.collectedAmounts, totals.amounts)}%</th>
                <th>{totals.hasTarget ? number(totals.remainingRecords) : "Chưa đặt"}</th>
                <th>{totals.hasTarget ? number(totals.remainingAmounts) : "Chưa đặt"}</th>
              </tr>
            </tfoot>
          )}
        </table>
      </div>
    </section>
  );
}
