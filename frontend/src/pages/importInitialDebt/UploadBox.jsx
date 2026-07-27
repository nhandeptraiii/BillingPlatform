import styles from "./UploadBox.module.scss";

import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";

import {
  setPreviewData,
  setLoading
} from "../../redux/slices/importInitialDebtSlice";

import importInitialDebtService from "../../services/importInitialDebtService";

import { toast } from "react-toastify";

const UploadBox = () => {

  const [selectedFile, setSelectedFile] = useState(null);
  const [importResult, setImportResult] = useState(null);

  const dispatch = useDispatch();

  const { loading } = useSelector(
    state => state.importInitialDebt
  );

  const handleChangeFile = (file) => {

    if (!file) return;

    const extension = file.name
      .split(".")
      .pop()
      .toLowerCase();

    if (!["xlsx", "xls"].includes(extension)) {

      toast.error(
        "Chỉ cho phép upload file Excel (.xlsx, .xls)"
      );

      return;
    }

    setSelectedFile(file);

    setImportResult(null);
  };

  const handleInputChange = (e) => {
    handleChangeFile(e.target.files[0]);
  };

  const handleDrop = (e) => {

    e.preventDefault();

    const file = e.dataTransfer.files[0];

    handleChangeFile(file);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleImport = async () => {

    if (!selectedFile) {

      toast.warning("Vui lòng chọn file");

      return;
    }

    try {

      dispatch(setLoading(true));
      const response =
        await importInitialDebtService
          .importInitialDebt(
            selectedFile
          );

      const result =
        response.data;
      setImportResult(result);

      const {
        totalRows,
        successCount,
        failedCount,
        errors = []
      } = result;

      if (failedCount === 0) {

        toast.success(
          `Import thành công ${successCount}/${totalRows} bản ghi.`
        );

      } else if (successCount === 0) {

        toast.error(
          `Import thất bại (${failedCount}/${totalRows} bản ghi lỗi).\n\n${errors[0]?.reason || ""}`,
          {
            autoClose: 8000
          }
        );

      } else {

        toast.warning(
          `Import hoàn tất.\nThành công: ${successCount}\nLỗi: ${failedCount}\n\n${errors[0]?.reason || ""}`,
          {
            autoClose: 8000
          }
        );

      }

      setSelectedFile(null);

    } catch (error) {

      console.error(error);

      toast.error(
        error?.response?.data?.message ||
        "Import thất bại"
      );

    } finally {

      dispatch(setLoading(false));

    }
  };

  return (
    <div className={styles.card}>
      <div
        className={`${styles.uploadBox}
    ${loading ? styles.disabled : ""}`}
        onDrop={handleDrop}
        onDragOver={handleDragOver}
      >
        <div className={styles.icon}>
          📁
        </div>

        <h2>
          Kéo thả file Excel vào đây
        </h2>

        <p>
          Chỉ hỗ trợ .xlsx, .xls
        </p>

        {!selectedFile ? (
          <label className={styles.uploadBtn}>
            Chọn File

            <input
              type="file"
              hidden
              accept=".xlsx,.xls"
              onChange={handleInputChange}
            />
          </label>
        ) : (
          <>
            <button
              type="button"
              className={styles.uploadBtn}
              disabled={loading}
              onClick={handleImport}
            >
              {loading ? "Đang import..." : "Import dữ liệu"}
            </button>

            <div className={styles.fileInfo}>
              <span>{selectedFile.name}</span>

              <span>
                {(selectedFile.size / 1024).toFixed(2)} KB
              </span>
            </div>
          </>
        )}
      </div>

      {importResult && (

        <div className={styles.resultCard}>

          <h3>Kết quả import</h3>

          <div className={styles.summary}>

            <div>
              <span>Tổng dòng</span>
              <strong>{importResult.totalRows}</strong>
            </div>

            <div>
              <span>Thành công</span>
              <strong className={styles.success}>
                {importResult.successCount}
              </strong>
            </div>

            <div>
              <span>Lỗi</span>
              <strong className={styles.error}>
                {importResult.failedCount}
              </strong>
            </div>

            <div>
              <span>Cảnh báo</span>
              <strong className={styles.warning}>
                {importResult.warningCount}
              </strong>
            </div>

          </div>

          {importResult.errors?.length > 0 && (

            <>

              <h4>Chi tiết lỗi</h4>

              <div className={styles.errorWrapper}>

                <table className={styles.errorTable}>

                  <thead>

                    <tr>
                      <th>Dòng</th>
                      <th>Nội dung lỗi</th>
                    </tr>

                  </thead>

                  <tbody>

                    {importResult.errors
                      ?.slice(0, 300)
                      .map((item, index) => (
                        <tr key={index}>

                          <td>{item.rowNumber}</td>

                          <td>{item.reason}</td>

                        </tr>

                      ))}

                  </tbody>

                </table>

              </div>

            </>

          )}

        </div>

      )}
      {loading && (
        <div className={styles.loadingOverlay}>
          <div className={styles.loadingContent}>
            <div className={styles.spinner}></div>

            <h3>
              Đang import dữ liệu...
            </h3>

            <p className={styles.fileName}>
              {selectedFile?.name}
            </p>

            <p>
              Vui lòng không đóng trình duyệt
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default UploadBox;