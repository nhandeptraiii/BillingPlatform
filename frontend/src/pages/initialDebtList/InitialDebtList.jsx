import styles from "./InitialDebtList.module.scss";

import FilterPanel from "./FilterPanel";
import ActionToolbar from "./ActionToolbar";
import DebtTable from "./DebtTable";

const InitialDebtList = () => {
  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Danh sách công nợ đầu kỳ</h1>
        <p>
          Quản lý dữ liệu công nợ khách hàng
        </p>
      </div>

      <FilterPanel />

      <ActionToolbar />

      <DebtTable />
    </div>
  );
};

export default InitialDebtList;