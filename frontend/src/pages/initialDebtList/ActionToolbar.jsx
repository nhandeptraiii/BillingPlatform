import styles from "./ActionToolbar.module.scss";

const ActionToolbar = () => {
  return (
    <div className={styles.toolbar}>
      <button>Export Excel</button>
      <button>In Bill</button>
      <button>Gạch Nợ</button>
    </div>
  );
};

export default ActionToolbar;