import styles from "./ImportInitialDebt.module.scss";

import UploadBox from "./UploadBox";

import importInitialDebtService
from "../../services/importInitialDebtService";

import { toast } from "react-toastify";

const ImportInitialDebt = () => {

  const handleDownloadTemplate =
    async () => {

      try {

        const response =
          await importInitialDebtService
            .downloadTemplate();

        const blob = new Blob(
          [response.data],
          {
            type:
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          }
        );

        const url =
          window.URL.createObjectURL(blob);

        const link =
          document.createElement("a");

        link.href = url;

        link.download =
          "mau_import_dau_ky.xlsx";

        document.body.appendChild(link);

        link.click();

        link.remove();

      } catch (error) {

        console.error(error);

        toast.error(
          "Không thể tải file mẫu"
        );
      }
    };

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>

        <div>
          <h1>
            Import dữ liệu thu cước đầu kỳ
          </h1>

          <p>
            Quản lý import dữ liệu công nợ
            khách hàng
          </p>
        </div>

        <button
          className={styles.downloadBtn}
          onClick={handleDownloadTemplate}
        >
          Download File Mẫu
        </button>

      </div>

      <UploadBox />

    </div>
  );
};

export default ImportInitialDebt;