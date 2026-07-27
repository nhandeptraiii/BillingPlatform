import { NavLink } from "react-router-dom";
//import { useSelector } from "react-redux";
import styles from "./Sidebar.module.scss";
import logo from "../../assets/images/LOGOVIETTEL.png";


function Sidebar({ isOpen, isMobile, onClose }) {
  
  return (
    <>
      {/* overlay mobile */}
      <div
        className={`${styles.overlay} ${
          isMobile && isOpen ? styles.show : ""
        }`}
        onClick={onClose}
      />

      <div
        className={`${styles.sidebar} ${
          isMobile
            ? isOpen
              ? styles.open
              : styles.mobileHidden
            : ""
        }`}
      >
        <div className={styles.logoContainer}>
          <img src={logo} alt="logo" />
        </div>

        <nav className={styles.menu}>
          <NavLink to="/" onClick={onClose} className={({ isActive }) => isActive ? styles.active : ""}>
            Dashboard
          </NavLink>

          <NavLink to="/consultants" onClick={onClose} className={({ isActive }) => isActive ? styles.active : ""}>
            Quản lý người dùng
          </NavLink>

          <NavLink to="/importInitialDebt" onClick={onClose} className={({ isActive }) => isActive ? styles.active : ""}>
            Import danh sách đầu kì
          </NavLink>

          {/* <NavLink to="/initialDebt" onClick={onClose} className={({ isActive }) => isActive ? styles.active : ""}>
            Danh sách import đầu kì
          </NavLink> */}

          <NavLink to="/paid-customer-import" onClick={onClose} className={({ isActive }) => isActive ? styles.active : ""}>
            Cập nhật KH đã thanh toán
          </NavLink>

          <NavLink to="/collectionProgress" onClick={onClose} className={({ isActive }) => isActive ? styles.active : ""}>
            Danh sách tiến độ thu cước
          </NavLink>

          <NavLink to="/consultant-progress" onClick={onClose} className={({ isActive }) => isActive ? styles.active : ""}>
            Báo cáo tiến độ thu cước
          </NavLink>

          <NavLink to="/store-config" onClick={onClose} className={({ isActive }) => isActive ? styles.active : ""}>
            Cài đặt tên shop
          </NavLink>

        </nav>
      </div>
    </>
  );
}

export default Sidebar;