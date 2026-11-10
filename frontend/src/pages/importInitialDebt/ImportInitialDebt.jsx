import { useCallback, useEffect, useState } from "react";
import styles from "./ImportInitialDebt.module.scss";
import UploadBox from "./UploadBox";
import importInitialDebtService from "../../services/importInitialDebtService";
import billingPeriodService from "../../services/billingPeriodsService";
import { toast } from "react-toastify";

const ImportInitialDebt = () => {
  const [periods, setPeriods] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [busyId, setBusyId] = useState(null);

  const fetchPeriods = useCallback(async () => {
    try {
      setLoading(true);
      const res = await billingPeriodService.getBillingPeriods({ page, size: 20 });
      const data = res.data.data || res.data;
      if (page > 0 && page >= data.totalPages) {
        setPage(Math.max(0, data.totalPages - 1));
        return;
      }
      setPeriods(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      toast.error(error.response?.data?.message || "Không thể tải danh sách kỳ cước");
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchPeriods();
  }, [fetchPeriods]);

  const handleClosePeriod = async (p) => {
    if (!window.confirm(`Đóng kỳ ${p.name}? Kỳ này dùng chung cho các khu vực. Sau khi đóng sẽ không thể xóa kỳ.`)) return;
    setBusyId(p.id);
    try {
      await billingPeriodService.closeBillingPeriod(p.id);
      toast.success(`Đã đóng kỳ ${p.name}`);
      await fetchPeriods();
    } catch (error) {
      toast.error(error.response?.data?.message || "Không thể đóng kỳ cước");
    } finally {
      setBusyId(null);
    }
  };

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
    setBusyId(p.id);
    try {
      const res = await billingPeriodService.deleteBillingPeriod(p.id);
      toast.success((res.data.data || res.data)?.message || `Đã xóa dữ liệu kỳ ${p.name}`);
      await fetchPeriods();
    } catch (error) {
      toast.error(error.response?.data?.message || "Không thể xóa kỳ cước");
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <div>
          <h1>Quản lý kỳ cước</h1>
          <p>Import dữ liệu đầu kỳ, xem danh sách, đóng và xóa kỳ cước</p>
        </div>

        <button className={styles.downloadBtn} onClick={handleDownloadTemplate}>
          Download File Mẫu
        </button>
      </div>

      <UploadBox onUploadSuccess={() => page === 0 ? fetchPeriods() : setPage(0)} />

      <div style={{ marginTop: 32, background: "white", padding: 24, borderRadius: 16, boxShadow: "0 2px 10px rgba(0,0,0,0.05)" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
          <h2 style={{ fontSize: 20, color: "#d70018", margin: 0 }}>Danh sách kỳ cước đã tạo</h2>
          <button
            onClick={fetchPeriods}
            disabled={loading || busyId !== null}
            style={{
              background: "none",
              color: "#374151",
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
                        <>
                        <button
                          disabled={busyId !== null}
                          onClick={() => handleClosePeriod(p)}
                          style={{ background: "#eef2ff", color: "#3730a3", border: "1px solid #a5b4fc", padding: "6px 12px", borderRadius: 6, marginRight: 8, cursor: "pointer", fontWeight: 600 }}
                        >Đóng kỳ cước</button>
                        <button
                          disabled={busyId !== null}
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
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      {totalPages > 1 && <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 16, marginTop: 16 }}>
        <button className={styles.downloadBtn} disabled={page === 0 || loading || busyId !== null} onClick={() => setPage(page - 1)}>Trang trước</button>
        <span>Trang {page + 1}/{totalPages}</span>
        <button className={styles.downloadBtn} disabled={page >= totalPages - 1 || loading || busyId !== null} onClick={() => setPage(page + 1)}>Trang sau</button>
      </div>}
    </div>
  );
};

export default ImportInitialDebt;
