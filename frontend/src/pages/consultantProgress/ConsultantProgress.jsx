import { useEffect, useState } from "react";
import dashboardService from "../../services/dashboardService";
import styles from "./ConsultantProgress.module.scss";

function ConsultantProgress() {
  const today = new Date();

  const [month, setMonth] = useState(today.getMonth() + 1);
  const [year, setYear] = useState(today.getFullYear());

  const [loading, setLoading] = useState(false);

  const [consultants, setConsultants] = useState([]);

  const currentYear = new Date().getFullYear();

const years = [
  currentYear - 2,
  currentYear - 1,
  currentYear,
  currentYear + 1
];

  const loadData = async () => {
    try {
      setLoading(true);

      const response =
        await dashboardService.getConsultants(
          month,
          year
        );

      setConsultants(response.data.data || []);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [month, year]);

  const handleExportExcel = async () => {
  try {
    const response =
      await dashboardService.exportConsultants(
        month,
        year
      );

    const blob = new Blob([response.data]);

    const url = window.URL.createObjectURL(blob);

    const link = document.createElement("a");

    link.href = url;

    link.download = `TienDoThuCuoc_${month}_${year}.xlsx`;

    document.body.appendChild(link);

    link.click();

    link.remove();

    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error(error);
  }
};

  const formatMoney = (value) => {
    return Number(value || 0).toLocaleString("vi-VN");
  };

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Tiến độ thu cước nhân viên</h1>

        <div className={styles.actions}>
          <select
            value={month}
            onChange={(e) =>
              setMonth(Number(e.target.value))
            }
          >
            {Array.from({ length: 12 }, (_, i) => (
              <option key={i + 1} value={i + 1}>
                Tháng {i + 1}
              </option>
            ))}
          </select>

<select
  value={year}
  onChange={(e) => setYear(Number(e.target.value))}
>
  {years.map((y) => (
    <option key={y} value={y}>
      {y}
    </option>
  ))}
</select>

          <button onClick={handleExportExcel}>
            Xuất Excel
          </button>
        </div>
      </div>

      <div className={styles.tableWrapper}>
        <table>
          <thead>
            <tr>
              <th>STT</th>
              <th>Nhân viên</th>
              <th>Chỉ tiêu KH</th>
              <th>Đã thu KH</th>
              <th>% KH</th>
              <th>Chỉ tiêu tiền</th>
              <th>Đã thanh toán</th>
              <th>% Tiền</th>
            </tr>
          </thead>

          <tbody>
            {loading ? (
              <tr>
                <td colSpan={8}>Đang tải dữ liệu...</td>
              </tr>
            ) : consultants.length === 0 ? (
              <tr>
                <td colSpan={8}>Không có dữ liệu</td>
              </tr>
            ) : (
              consultants.map((item, index) => {
                const recordPercent =
                  item.targetRecords > 0
                    ? (
                        (item.collectedRecords /
                          item.targetRecords) *
                        100
                      ).toFixed(2)
                    : 0;

                const amountPercent =
                  item.targetAmount > 0
                    ? (
                        (item.collectedAmount /
                          item.targetAmount) *
                        100
                      ).toFixed(2)
                    : 0;

                return (
                  <tr key={item.consultantId}>
                    <td>{index + 1}</td>

                    <td>{item.consultantName}</td>

                    <td>
                      {item.targetRecords.toLocaleString()}
                    </td>

                    <td>
                      {item.collectedRecords.toLocaleString()}
                    </td>

                    <td>{recordPercent}%</td>

                    <td>
                      {formatMoney(item.targetAmount)}
                    </td>

                    <td>
                      {formatMoney(item.collectedAmount)}
                    </td>

                    <td>{amountPercent}%</td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default ConsultantProgress;