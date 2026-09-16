import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { toast } from "react-toastify";
import consultantService from "../../services/consultantService";
import regionService from "../../services/regionService";
import { getRole } from "../../utils/role";
import styles from "./ConsultantModal.module.scss";

const initialForm = { username: "", fullName: "", phone: "", password: "", role: "CONSULTANT", regionId: "", managerId: "" };

export default function ConsultantModal({ open, onClose, consultant, onSuccess }) {
  const user = useSelector(state => state.auth.user);
  const currentRole = getRole(user);
  const [form, setForm] = useState(initialForm);
  const [regions, setRegions] = useState([]);
  const [managers, setManagers] = useState([]);

  useEffect(() => {
    if (!open) return;
    setForm(consultant ? {
      username: consultant.username || "", fullName: consultant.fullName || "", phone: consultant.phone || "", password: "",
      role: consultant.role || "CONSULTANT", regionId: consultant.regionId || "", managerId: consultant.managerId || ""
    } : initialForm);
  }, [open, consultant]);

  useEffect(() => {
    if (!open || !["ADMIN", "MANAGER"].includes(currentRole)) return;
    if (currentRole === "ADMIN") {
      regionService.getAll().then(res => setRegions(res.data.data || res.data || [])).catch(() => setRegions([]));
    }
    consultantService.getList({ page: 0, size: 100, role: "NVKD" })
      .then(res => setManagers((res.data.data || res.data).content || []))
      .catch(() => setManagers([]));
  }, [open, currentRole]);

  if (!open) return null;
  const set = (field, value) => setForm(previous => ({ ...previous, [field]: value }));
  const canChooseRole = currentRole === "ADMIN" || (currentRole === "MANAGER" && !consultant);
  const canAssignConsultant = ["ADMIN", "MANAGER"].includes(currentRole) && form.role === "CONSULTANT" && (currentRole === "ADMIN" || !consultant);

  const submit = async () => {
    if (currentRole === "ADMIN" && !form.regionId) return toast.error("Vui lòng chọn khu vực");
    const role = currentRole === "NVKD" ? "CONSULTANT" : form.role;
    try {
      if (consultant) {
        const payload = { username: currentRole === "ADMIN" ? form.username || undefined : undefined, fullName: form.fullName, phone: form.phone, role };
        if (currentRole === "ADMIN") payload.regionId = Number(form.regionId);
        await consultantService.update(consultant.id, payload);
        // PATCH /users/{id}/manager is ADMIN-only in the current backend.
        if (currentRole === "ADMIN" && role === "CONSULTANT" && String(form.managerId || "") !== String(consultant.managerId || "")) {
          await consultantService.assignManager(consultant.id, form.managerId ? Number(form.managerId) : null);
        }
        toast.success("Cập nhật người dùng thành công");
      } else {
        const payload = { username: form.username, fullName: form.fullName, phone: form.phone, password: form.password, role };
        if (currentRole === "ADMIN") payload.regionId = Number(form.regionId);
        // MANAGER can create a CONSULTANT and send managerId to assign that NVKD immediately.
        if (role === "CONSULTANT" && form.managerId) payload.managerId = Number(form.managerId);
        await consultantService.create(payload);
        toast.success("Thêm người dùng thành công");
      }
      await onSuccess?.(); onClose();
    } catch (error) { toast.error(error.response?.data?.message || "Có lỗi xảy ra"); }
  };

  return <div className={styles.overlay}><div className={styles.modal}>
    <h1>{consultant ? "Cập nhật thông tin người dùng" : "Thêm người dùng"}</h1>
    {!consultant || currentRole === "ADMIN" ? <input placeholder="Tên đăng nhập" value={form.username} onChange={e => set("username", e.target.value)} /> : null}
    {!consultant && <input type="password" placeholder="Mật khẩu" value={form.password} onChange={e => set("password", e.target.value)} />}
    <input placeholder="Họ tên" value={form.fullName} onChange={e => set("fullName", e.target.value)} />
    <input placeholder="Số điện thoại" value={form.phone} onChange={e => set("phone", e.target.value)} />
    {canChooseRole && <div className={styles.formGroup}><label>Vai trò</label><select value={form.role} onChange={e => setForm(previous => ({ ...previous, role: e.target.value, managerId: e.target.value === "CONSULTANT" ? previous.managerId : "" }))}>
      {currentRole === "ADMIN" && <option value="MANAGER">Quản lý khu vực</option>}<option value="NVKD">Nhân viên kinh doanh địa bàn</option><option value="CONSULTANT">Tư vấn viên</option>
    </select></div>}
    {currentRole === "ADMIN" && <div className={styles.formGroup}><label>Khu vực</label><select value={form.regionId} onChange={e => set("regionId", e.target.value)}><option value="">Chọn khu vực</option>{regions.map(region => <option key={region.id} value={region.id}>{region.name}</option>)}</select></div>}
    {canAssignConsultant && <div className={styles.formGroup}><label>Phân công cho NVKD (tùy chọn)</label><select value={form.managerId} onChange={e => set("managerId", e.target.value)}><option value="">Trực thuộc quản lý khu vực</option>{managers.filter(manager => currentRole === "MANAGER" || !form.regionId || String(manager.regionId) === String(form.regionId)).map(manager => <option key={manager.id} value={manager.id}>{manager.fullName}</option>)}</select></div>}
    <div className={styles.actions}><button onClick={onClose}>Hủy</button><button className={styles.saveBtn} onClick={submit}>Lưu</button></div>
  </div></div>;
}
