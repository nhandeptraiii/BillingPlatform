
import styles  from "./PaidCustomerImport.module.scss";

import UploadBox from "./UploadBox";
import MonthYearSelector from "./MonthYearSelector";

const PaidCustomerImport =
  () => {

    return (

      <div className={styles.page}>

        <div className={styles.header}>

          <div>

            <h1>
              Cập nhật KH đã thanh toán
            </h1>

            <p>
              Import dữ liệu đối soát
              thanh toán khách hàng
            </p>

          </div>

        </div>

        <MonthYearSelector />

        <UploadBox />

      </div>

    );

  };

export default
  PaidCustomerImport;