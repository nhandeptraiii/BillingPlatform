import { useEffect, useState } from "react";
import styles from "./ImportInitialDebt.module.scss";
import UploadBox from "./UploadBox";
import importInitialDebtService from "../../services/importInitialDebtService";
import billingPeriodService from "../../services/billingPeriodsService";
import { toast } from "react-toastify";

const ImportInitialDebt = () => {
  const [periods, setPeriods] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchPeriods = async () => {
    try {
      setLoading(true);
      const res = await billingPeriodService.getBillingPeriods({ page: 0, size: 20 });
      setPeriods(res.data?.content || res.data || []);
    } catch {
      setPeriods([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPeriods();
  }, []);

  const handleDownloadTemplate = async () => {
    try {
      const response = await importInitialDebtService.downloadTemplate();
      const blob = new Blob([response.data], {
        type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = "mau_import_dau_ky.xlsx";
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch {
      toast.error("Không thể tải file mẫu");
    }
  };

  const handleDeletePeriod = async (p) => {
    if (
      !window.confirm(
        `Bạn có chắc chắn muốn xóa dữ liệu của kỳ ${p.name}? Thao tác này sẽ xóa bản ghi đầu kỳ để bạn có thể import lại file mới.`
      )
    ) {
      return;
    }
    try {
      const res = await billingPeriodService.deleteBillingPeriod(p.id);
      toast.success(res.data?.message || `Đã xóa dữ liệu kỳ ${p.name}`);
      fetchPeriods();
    } catch (error) {
      toast.error(error.response?.data?.message || "Không thể xóa kỳ cước");
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <div>
          <h1>Import dữ liệu thu cước đầu kỳ</h1>
          <p>Quản lý import và xóa kỳ cước để import lại</p>
        </div>

        <button className={styles.downloadBtn} onClick={handleDownloadTemplate}>
          Download File Mẫu
        </button>
      </div>

      <UploadBox onUploadSuccess={fetchPeriods} />

      <div style={{ marginTop: 32, background: "white", padding: 24, borderRadius: 16, boxShadow: "0 2px 10px rgba(0,0,0,0.05)" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
          <h2 style={{ fontSize: 20, color: "#d70018", margin: 0 }}>Danh sách kỳ cước đã tạo</h2>
          <button
            onClick={fetchPeriods}
            style={{
              background: "none",
              border: "1px solid #ddd",
              padding: "6px 12px",
              borderRadius: 8,
              cursor: "pointer",
            }}
          >
            Làm mới
          </button>
        </div>

        {loading ? (
          <p style={{ color: "#888" }}>Đang tải danh sách kỳ cước...</p>
        ) : !periods.length ? (
          <p style={{ color: "#888" }}>Chưa có kỳ cước nào.</p>
        ) : (
          <div style={{ overflowX: "auto" }}>
            <table style={{ width: "100%", borderCollapse: "collapse", textAlign: "left" }}>
              <thead>
                <tr style={{ background: "#f8f9fa", borderBottom: "2px solid #dee2e6" }}>
                  <th style={{ padding: "10px 12px" }}>Tên kỳ</th>
                  <th style={{ padding: "10px 12px" }}>Tháng/Năm</th>
                  <th style={{ padding: "10px 12px" }}>Trạng thái</th>
                  <th style={{ padding: "10px 12px", textAlign: "center" }}>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {periods.map((p) => (
                  <tr key={p.id} style={{ borderBottom: "1px solid #eee" }}>
                    <td style={{ padding: "12px", fontWeight: 600 }}>{p.name}</td>
                    <td style={{ padding: "12px" }}>{p.month}/{p.year}</td>
                    <td style={{ padding: "12px" }}>
                      <span
                        style={{
                          display: "inline-block",
                          padding: "4px 8px",
                          borderRadius: 6,
                          fontSize: 12,
                          fontWeight: 600,
                          background: p.status === "CLOSED" ? "#e9ecef" : "#d1e7dd",
                          color: p.status === "CLOSED" ? "#6c757d" : "#0f5132",
                        }}
                      >
                        {p.status === "CLOSED" ? "Đã đóng" : "Đang mở"}
                      </span>
                    </td>
                    <td style={{ padding: "12px", textAlign: "center" }}>
                      {p.status !== "CLOSED" && (
                        <button
                          onClick={() => handleDeletePeriod(p)}
                          style={{
                            background: "#fff1f0",
                            color: "#d70018",
                            border: "1px solid #ffa39e",
                            padding: "6px 12px",
                            borderRadius: 6,
                            cursor: "pointer",
                            fontWeight: 600,
                          }}
                        >
                          Xóa kỳ này
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default ImportInitialDebt;