import styles from "./ConsultantTable.module.scss";

const roleName = { MANAGER: "Quản lý khu vực", CONSULTANT: "Tư vấn viên", ADMIN: "Quản trị viên", NVKD: "Nhân viên kinh doanh" };

export default function ConsultantTable({ data = [], onEdit, onDelete, onResetPassword, page, pageSize, totalPages, onPageChange }) {
  return <div className={styles.tableWrapper}><table><thead><tr>
    <th>STT</th><th>Tài khoản</th><th>Họ tên</th><th>Số điện thoại</th><th>Vai trò</th><th>Khu vực</th><th>Người quản lý</th><th>Thao tác</th>
  </tr></thead><tbody>
    {data.map((item, index) => <tr key={item.id}>
      <td>{page * pageSize + index + 1}</td><td>{item.username}</td><td>{item.fullName}</td><td>{item.phone}</td><td>{roleName[item.role] || item.role}</td><td>{item.regionName || "—"}</td><td>{item.role === "CONSULTANT" ? (item.managerName || "—") : "—"}</td>
      <td><button className={styles.editBtn} onClick={() => onEdit(item)}>Sửa</button><button className={styles.resetBtn} onClick={() => onResetPassword(item)}>Đặt lại mật khẩu</button><button className={styles.deleteBtn} onClick={() => onDelete(item)}>Xóa</button></td>
    </tr>)}
  </tbody></table><div className={styles.pagination}>
    <button disabled={page === 0} onClick={() => onPageChange(0)}>⏮</button><button disabled={page === 0} onClick={() => onPageChange(page - 1)}>◀</button><span>Trang {page + 1}/{totalPages}</span><button disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>▶</button><button disabled={page >= totalPages - 1} onClick={() => onPageChange(totalPages - 1)}>⏭</button>
  </div></div>;
}
