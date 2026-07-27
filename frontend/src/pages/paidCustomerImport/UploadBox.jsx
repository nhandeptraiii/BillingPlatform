import {
  useState, useEffect
} from "react";


import styles
  from "./UploadBox.module.scss";

import {
  useSelector
} from "react-redux";

import {
  toast
} from "react-toastify";
import paidCustomerImportService from "../../services/paidCustomerImportService";


const UploadBox = () => {

  const [selectedFile,
    setSelectedFile] =
    useState(null);

  const [loading,
    setLoading] =
    useState(false);

  const [importResult, setImportResult] = useState(null);

  const [dragging, setDragging] =
    useState(false);

  useEffect(() => {

    const preventDefault = (e) => {
      e.preventDefault();
      e.stopPropagation();
    };

    window.addEventListener(
      "dragover",
      preventDefault
    );

    window.addEventListener(
      "drop",
      preventDefault
    );

    return () => {

      window.removeEventListener(
        "dragover",
        preventDefault
      );

      window.removeEventListener(
        "drop",
        preventDefault
      );

    };

  }, []);

  const {

    selectedMonth,
    selectedYear

  }
    =
    useSelector(
      state =>
        state
          .paidCustomerImport
    );

  const handleFile =
    file => {

      if (!file) return;

      const extension =
        file.name
          .split(".")
          .pop()
          .toLowerCase();

      if (
        !["xlsx", "xls"]
          .includes(extension)
      ) {

        toast.error(
          "Chỉ hỗ trợ file Excel"
        );

        return;
      }

      setSelectedFile(file);

    };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDragEnter = (e) => {
    e.preventDefault();
    e.stopPropagation();

    setDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();

    setDragging(false);
  };

  const handleDrop = (e) => {

    e.preventDefault();
    e.stopPropagation();

    setDragging(false);

    const file =
      e.dataTransfer.files?.[0];

    if (!file) return;

    handleFile(file);

  };

  const handleImport =
    async () => {

      if (
        !selectedMonth ||
        !selectedYear
      ) {

        toast.warning(
          "Vui lòng chọn tháng năm"
        );

        return;

      }

      if (!selectedFile) {

        toast.warning(
          "Vui lòng chọn file"
        );

        return;
      }

      try {

        setLoading(true);

        const result =
          await paidCustomerImportService
            .importReconciliation(
              selectedMonth,
              selectedYear,
              selectedFile
            );

        const importData = result.data.data;

        setImportResult(importData);

       if (importData.failedCount === 0) {

  toast.success(
    `Import thành công ${importData.successCount}/${importData.totalRows} dòng.`
  );

} else {

  toast.warning(
    `Import hoàn tất: ${importData.successCount}/${importData.totalRows} dòng thành công, ${importData.failedCount} dòng lỗi. Xem chi tiết bên dưới.`,
    {
      autoClose: 6000
    }
  );

}

        setSelectedFile(null);

      } catch (error) {

        toast.error(
          error?.response?.data?.message ||
          "Import thất bại"
        );

      } finally {

        setLoading(false);

      }

    };

  const handleDownload =
    async () => {

      try {

        const response =
          await paidCustomerImportService
            .downloadTemplate();

        const url =
          window.URL.createObjectURL(
            new Blob([
              response.data
            ])
          );

        const link =
          document.createElement(
            "a"
          );

        link.href = url;

        link.download =
          "mau_import.xlsx";

        link.click();

      } catch {

        toast.error(
          "Không tải được file mẫu"
        );

      }

    };

  return (
    <>
      {loading && (
        <div className={styles.loadingOverlay}>
          <div className={styles.loadingContainer}>
            <div className={styles.spinnerWrapper}>
              <i className="fas fa-file-import"></i>
            </div>
            <h3>Đang import dữ liệu</h3>

            <p>
              Vui lòng không đóng trình duyệt hoặc
              chuyển trang trong quá trình import...
            </p>
          </div>
        </div>
      )}
      <div className={styles.card}>

        <div
          className={styles.download}
        >

          <button
            onClick={
              handleDownload
            }
          >
            Download File Mẫu
          </button>

        </div>

        <div
          className={`${styles.uploadBox}
  ${dragging ? styles.dragging : ""}`}

          onDragEnter={handleDragEnter}
          onDragLeave={handleDragLeave}
          onDragOver={handleDragOver}
          onDrop={handleDrop}
        >

          <div>
            📁
          </div>

          <h2>
            Kéo thả file Excel
            vào đây
          </h2>

          <p>
            Chỉ hỗ trợ
            .xlsx, .xls
          </p>

{!selectedFile ? (
  <label className={styles.chooseBtn}>
    Chọn File

    <input
      hidden
      type="file"
      accept=".xlsx,.xls"
      onChange={(e) => handleFile(e.target.files[0])}
    />
  </label>
) : (
  <button
    type="button"
    className={styles.chooseBtn}
    disabled={loading}
    onClick={handleImport}
  >
    {loading ? "Đang import..." : "Import dữ liệu"}
  </button>
)}

          {
            selectedFile && (

              <div
                className={
                  styles.fileInfo
                }
              >

                {selectedFile.name}

              </div>

            )
          }

        </div>


      </div>

      {
        importResult && (

          <div className={styles.resultCard}>

            <h3>Kết quả import</h3>

            <div className={styles.summary}>

              <div>
                Tổng dòng
                <strong>
                  {importResult.totalRows}
                </strong>
              </div>

              <div>
                Thành công
                <strong>
                  {importResult.successCount}
                </strong>
              </div>

              <div>
                Lỗi
                <strong>
                  {importResult.failedCount}
                </strong>
              </div>

              <div>
                Cảnh báo
                <strong>
                  {importResult.warningCount}
                </strong>
              </div>

            </div>

            {
              importResult.errors?.length > 0 && (

                <>

                  <h4>Chi tiết lỗi</h4>

                  <table className={styles.errorTable}>

                    <thead>

                      <tr>
                        <th>Dòng</th>
                        <th>Nội dung lỗi</th>
                      </tr>

                    </thead>

                    <tbody>

                      {
                        importResult.errors.map(
                          (item, index) => (

                            <tr key={index}>

                              <td>
                                {item.rowNumber}
                              </td>

                              <td>
                                {item.reason}
                              </td>

                            </tr>

                          )
                        )
                      }

                    </tbody>

                  </table>

                </>

              )
            }

          </div>

        )
      }
    </>

  );

};

export default UploadBox;