import styles from "./ConsultantTable.module.scss";

const ConsultantTable = ({
  data,
  onEdit,

  page,
  pageSize,
  totalPages,
  onPageChange
}) => {

  const getRoleName = (role) => {
    switch (role) {
      case "MANAGER":
        return "Quản lý";

      case "CONSULTANT":
        return "Tư vấn viên";
      
      case "ADMIN":
        return "Quản trị viên";

      default:
        return role;
    }
  };

  return (
    <div className={styles.tableWrapper}>
      <table>

        <thead>
          <tr>
            <th>STT</th>
            <th>Tài khoản</th>
            <th>Họ tên</th>
            <th>Số điện thoại</th>
            <th>Vai trò</th>
            <th>Khu vực</th>
            <th>Thao tác</th>
          </tr>
        </thead>

        <tbody>

          {data.map((item, index) => (
            <tr key={item.id}>
              <td>
                {page * pageSize + index + 1}
              </td>

              <td>{item.username}</td>

              <td>{item.fullName}</td>

              <td>{item.phone}</td>

              <td>{getRoleName(item.role)}</td>

              <td>{item.regionName}</td>
              <td>
                <button
                  className={styles.editBtn}
                  onClick={() => onEdit(item)}
                >
                  Sửa
                </button>
              </td>

            </tr>
          ))}

        </tbody>

      </table>
      <div className={styles.pagination}>

        <button
          disabled={page === 0}
          onClick={() => onPageChange(0)}
        >
          ⏮
        </button>

        <button
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
        >
          ◀
        </button>

        <span>
          Trang {page + 1}/{totalPages}
        </span>

        <button
          disabled={page === totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          ▶
        </button>

        <button
          disabled={page === totalPages - 1}
          onClick={() => onPageChange(totalPages - 1)}
        >
          ⏭
        </button>

      </div>
    </div>
  );
};

export default ConsultantTable;