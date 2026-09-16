import { NavLink } from "react-router-dom";
import { useSelector } from "react-redux";
import styles from "./Sidebar.module.scss";
import logo from "../../assets/images/LOGOVIETTEL.png";
import { canImportEmployees, canManageTargets, canManageUsers } from "../../utils/role";

const MenuLink = ({ to, children, onClose }) => <NavLink to={to} onClick={onClose} className={({ isActive }) => isActive ? styles.active : ""}>{children}</NavLink>;

function Sidebar({ isOpen, isMobile, onClose }) {
  const user = useSelector(state => state.auth.user);
  return <>
    <div className={`${styles.overlay} ${isMobile && isOpen ? styles.show : ""}`} onClick={onClose} />
    <div className={`${styles.sidebar} ${isMobile ? (isOpen ? styles.open : styles.mobileHidden) : ""}`}>
      <div className={styles.logoContainer}><img src={logo} alt="logo" /></div>
      <nav className={styles.menu}>
        <MenuLink to="/" onClose={onClose}>Dashboard</MenuLink>
        {canManageUsers(user) && <MenuLink to="/consultants" onClose={onClose}>Quản lý người dùng</MenuLink>}
        {canImportEmployees(user) && <MenuLink to="/importInitialDebt" onClose={onClose}>Import danh sách đầu kỳ</MenuLink>}
        {canManageTargets(user) && <MenuLink to="/region-targets" onClose={onClose}>Giao chỉ tiêu</MenuLink>}
        <MenuLink to="/paid-customer-import" onClose={onClose}>Cập nhật KH đã thanh toán</MenuLink>
        <MenuLink to="/collectionProgress" onClose={onClose}>Danh sách tiến độ thu cước</MenuLink>
        <MenuLink to="/store-config" onClose={onClose}>Cài đặt tên shop</MenuLink>
      </nav>
    </div>
  </>;
}
export default Sidebar;
