import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";

import styles from "./ConsultantManagement.module.scss";

import { fetchConsultants } from "../../redux/slices/consultantSlice";

import ConsultantTable from "./ConsultantTable";
import ConsultantModal from "./ConsultantModal";
import consultantService from "../../services/consultantService";
import { toast } from "react-toastify";
import { canImportEmployees } from "../../utils/role";

const ConsultantManagement = () => {
  const dispatch = useDispatch();

  const consultants = useSelector(
    state => state.consultant.list
  );

  const loading = useSelector(
    state => state.consultant.loading
  );

  const [openModal, setOpenModal] = useState(false);
  const [selectedRow, setSelectedRow] = useState(null);
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [role, setRole] = useState("");

  const [searchText, setSearchText] =
  useState("");
  const currentUser = useSelector(state => state.auth.user);

  useEffect(() => {

  const timer =
    setTimeout(() => {

      setKeyword(
        searchText
      );

    }, 500);

  return () =>
    clearTimeout(timer);

}, [searchText]);


const handleReset = () => {

  setSearchText("");

  setKeyword("");

  setRole("");

  setPage(0);
};

  const {
    totalPages
  } = useSelector(
    state => state.consultant
  );

  const handleCreate = () => {
    setSelectedRow(null);
    setOpenModal(true);
  };

  const handleEdit = (row) => {
    setSelectedRow(row);
    setOpenModal(true);
  };

  const refresh = () => dispatch(fetchConsultants({ page, size: 5, keyword, role }));
  const handleDelete = async (row) => {
    if (!window.confirm(`Xóa tài khoản ${row.fullName}?`)) return;
    try {
      await consultantService.delete(row.id);
      toast.success("Đã xóa tài khoản thành công");
      refresh();
    } catch (error) {
      if (error.response?.status === 409 || error.response?.data?.message?.includes("bàn giao")) {
        try {
          const res = await consultantService.getList({ page: 0, size: 100, role: "CONSULTANT" });
          const list = (res.data?.data?.content || res.data?.content || []).filter(u => u.id !== row.id && u.status !== "INACTIVE");
          if (list.length === 0) {
            toast.error("Không còn tư vấn viên nào khác trong khu vực để bàn giao hồ sơ.");
            return;
          }
          const promptMsg = `${error.response?.data?.message || "Cần bàn giao hồ sơ cước trước khi xóa."}\n\nDanh sách tư vấn viên tiếp nhận:\n` +
            list.map((u, i) => `${i + 1}. ${u.fullName} (@${u.username})`).join("\n") +
            `\n\nNhập số thứ tự tư vấn viên nhận bàn giao (1 - ${list.length}):`;
          const input = window.prompt(promptMsg);
          if (!input) return;
          const index = parseInt(input.trim(), 10) - 1;
          const selected = list[index];
          if (!selected) {
            toast.error("Lựa chọn không hợp lệ");
            return;
          }
          await consultantService.delete(row.id, selected.id);
          toast.success(`Đã bàn giao hồ sơ sang "${selected.fullName}" và xử lý tài khoản thành công`);
          refresh();
        } catch (innerError) {
          toast.error(innerError.response?.data?.message || "Lỗi khi bàn giao hồ sơ");
        }
      } else {
        toast.error(error.response?.data?.message || "Không thể xóa tài khoản");
      }
    }
  };

  const handleClaim = async (row) => {
    if (!window.confirm(`Bạn muốn tự nhận tư vấn viên "${row.fullName}" vào nhóm quản lý của mình?`)) return;
    try {
      await consultantService.assignManager(row.id, currentUser.id);
      toast.success(`Đã nhận tư vấn viên ${row.fullName} vào nhóm quản lý thành công!`);
      refresh();
    } catch (error) {
      toast.error(error.response?.data?.message || "Không thể nhận tư vấn viên");
    }
  };
  const handleResetPassword = async (row) => {
    const newPassword = window.prompt(`Nhập mật khẩu mới cho ${row.fullName}:`);
    if (!newPassword) return;
    try { await consultantService.resetPassword(row.id, { newPassword }); toast.success("Đã đặt lại mật khẩu"); }
    catch (error) { toast.error(error.response?.data?.message || "Không thể đặt lại mật khẩu"); }
  };
  const handleImport = async (event) => {
    const file = event.target.files?.[0]; if (!file) return;
    if (!/\.xlsx?$/i.test(file.name)) { toast.warning("Chỉ hỗ trợ file Excel (.xlsx, .xls)"); return; }
    try { const { data } = await consultantService.importConsultants(file); toast.success(data.message || "Import nhân viên thành công"); refresh(); }
    catch (error) { toast.error(error.response?.data?.message || "Import nhân viên thất bại"); }
    finally { event.target.value = ""; }
  };

  useEffect(() => {

  dispatch(
    fetchConsultants({
      page,
      size: 5,
      keyword,
      role
    })
  );

}, [
  dispatch,
  page,
  keyword,
  role
]);

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <h1>Quản lý người dùng</h1>
          <p className={styles.quanly}>
            Quản lý danh sách tài khoản người dùng
          </p>
        </div>

        <div className={styles.headerActions}>
        {canImportEmployees(currentUser) && <label className={styles.importBtn}>Import nhân viên<input type="file" accept=".xlsx,.xls" hidden onChange={handleImport} /></label>}
        <button
          className={styles.addBtn}
          onClick={handleCreate}
        >
          + Thêm mới
        </button>
        </div>
      </div>

      <div className={styles.searchSection}>

<input
  type="text"
  placeholder="Tìm theo tên, username hoặc số điện thoại..."
  value={searchText}
  onChange={(e) => {

    setPage(0);

    setSearchText(
      e.target.value
    );

  }}
/>

<select
  value={role}
  onChange={(e) => {

    setPage(0);

    setRole(
      e.target.value
    );

  }}
>
          <option value="">
            Tất cả vai trò
          </option>

          <option value="MANAGER">
            Người quản lý
          </option>

          <option value="CONSULTANT">
            Tư vấn viên
          </option>

          <option value="NVKD">
            Nhân viên kinh doanh
          </option>
        </select>

        <button
          className={styles.resetBtn}
          onClick={handleReset}
        >
          Làm mới
        </button>

      </div>

      <ConsultantTable
        data={consultants}
        loading={loading}
        onEdit={handleEdit}
        onDelete={handleDelete}
        onResetPassword={handleResetPassword}
        onClaim={handleClaim}
        currentUser={currentUser}
        page={page}
        pageSize={5}
        totalPages={totalPages}
        onPageChange={setPage}
      />

      <ConsultantModal
        open={openModal}
        onClose={() => setOpenModal(false)}
        consultant={selectedRow}
        onSuccess={refresh}
      />
    </div>
  );
};

export default ConsultantManagement;
