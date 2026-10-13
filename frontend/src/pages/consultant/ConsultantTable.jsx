import styles from "./ConsultantTable.module.scss";

const roleName = { MANAGER: "Quản lý khu vực", CONSULTANT: "Tư vấn viên", ADMIN: "Quản trị viên", NVKD: "Nhân viên kinh doanh" };

export default function ConsultantTable({ data = [], onEdit, onDelete, onResetPassword, onClaim, currentUser, page, pageSize, totalPages, onPageChange }) {
  const currentRole = currentUser?.role || currentUser?.roleName || "";
  const currentUserId = currentUser?.id;

  return <div className={styles.tableWrapper}><table><thead><tr>
    <th>STT</th><th>Tài khoản</th><th>Họ tên</th><th>Số điện thoại</th><th>Vai trò</th><th>Khu vực</th><th>Người quản lý</th><th>Thao tác</th>
  </tr></thead><tbody>
    {data.map((item, index) => {
      const isConsultant = item.role === "CONSULTANT";
      const hasNoManager = !item.managerId;
      const isMyConsultant = String(item.managerId) === String(currentUserId);
      const isNvkd = currentRole === "NVKD";
      const canClaim = isNvkd && isConsultant && hasNoManager;
      const canManage = !isNvkd || isMyConsultant;

      return (
        <tr key={item.id}>
          <td>{page * pageSize + index + 1}</td>
          <td>{item.username}</td>
          <td>{item.fullName}</td>
          <td>{item.phone}</td>
          <td>{roleName[item.role] || item.role}</td>
          <td>{item.regionName || "—"}</td>
          <td>{isConsultant ? (item.managerName || (hasNoManager ? <span style={{ color: "#d97706", fontStyle: "italic", fontWeight: 500 }}>Chưa có NVKD</span> : "—")) : "—"}</td>
          <td>
            {canClaim && (
              <button className={styles.claimBtn} onClick={() => onClaim(item)} title="Tự nhận tư vấn viên này vào nhóm">
                Tự nhận
              </button>
            )}
            {canManage ? (
              <>
                <button className={styles.editBtn} onClick={() => onEdit(item)}>Sửa</button>
                <button className={styles.resetBtn} onClick={() => onResetPassword(item)}>Đặt lại mật khẩu</button>
                <button className={styles.deleteBtn} onClick={() => onDelete(item)}>Xóa</button>
              </>
            ) : (
              <span style={{ color: "#888", fontSize: "0.85rem", fontStyle: "italic" }}>Thuộc NVKD khác</span>
            )}
          </td>
        </tr>
      );
    })}
  </tbody></table><div className={styles.pagination}>
    <button disabled={page === 0} onClick={() => onPageChange(0)}>⏮</button><button disabled={page === 0} onClick={() => onPageChange(page - 1)}>◀</button><span>Trang {page + 1}/{totalPages}</span><button disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>▶</button><button disabled={page >= totalPages - 1} onClick={() => onPageChange(totalPages - 1)}>⏭</button>
  </div></div>;
}
