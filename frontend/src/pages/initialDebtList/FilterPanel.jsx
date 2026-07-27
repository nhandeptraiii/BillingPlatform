import styles from "./FilterPanel.module.scss";

const FilterPanel = () => {
  return (
    <div className={styles.wrapper}>
      <input placeholder="Tìm kiếm KH..." />

      <select>
        <option>Kỳ thanh toán</option>
      </select>

      <select>
        <option>Trạng thái thu</option>
      </select>

      <button>Tìm kiếm</button>

      <button className={styles.reset}>
        Làm mới
      </button>
    </div>
  );
};

export default FilterPanel;