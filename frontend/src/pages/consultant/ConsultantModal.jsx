import { useEffect, useState } from "react";
import { useSelector } from "react-redux";

import styles from "./ConsultantModal.module.scss";
import consultantService from "../../services/consultantService";
import { toast } from "react-toastify";
import regionService from "../../services/regionService";

const ConsultantModal = ({
  open,
  onClose,
  consultant,
  onSuccess
}) => {

  const initialForm = {
    username: "",
    fullName: "",
    phone: "",
    password: "",
    role: "CONSULTANT",
    regionId: ""
  };

  const [form, setForm] = useState(initialForm);
  const [regions, setRegions] = useState([]);

  const currentUser =
    useSelector(state => state.auth.user);

  const currentRole =
    currentUser?.role;

  useEffect(() => {

    if (!open) return;

    if (consultant) {

      setForm({
        username: consultant.username || "",
        fullName: consultant.fullName || "",
        phone: consultant.phone || "",
        password: "",
        role: consultant.role || "CONSULTANT",
        regionId: consultant.regionId || ""
      });

    } else {

      setForm(initialForm);

    }

  }, [consultant, open]);

  useEffect(() => {

    if (
      open &&
      currentRole === "ADMIN"
    ) {

      loadRegions();

    }

  }, [open, currentRole]);

  const loadRegions = async () => {

    try {

      const res =
        await regionService.getAll();

      setRegions(
        res.data.data || []
      );

    } catch (error) {

      console.log(error);

    }

  };

useEffect(() => {

  if (!open) return;

  if (
    currentRole === "MANAGER" &&
    !consultant
  ) {
    setForm(prev => ({
      ...prev,
      role: "CONSULTANT"
    }));
  }

}, [currentRole, consultant, open]);

if (!open) return null;


  const handleSubmit = async () => {
    try {
      if (
        currentRole === "ADMIN" &&
        !form.regionId
      ) {
        toast.error("Vui lòng chọn khu vực");
        return;
      }

      if (consultant) {

        const updatePayload = {
          fullName: form.fullName,
          phone: form.phone,
role:
    currentRole === "MANAGER"
      ? "CONSULTANT"
      : form.role        };

        if (currentRole === "ADMIN") {
          updatePayload.regionId = Number(form.regionId);
        }

        await consultantService.update(
          consultant.id,
          updatePayload
        );

        toast.success("Cập nhật người dùng thành công");

      } else {

        const createPayload = {
          username: form.username,
          fullName: form.fullName,
          phone: form.phone,
          password: form.password,
role:
    currentRole === "MANAGER"
      ? "CONSULTANT"
      : form.role        };

        if (currentRole === "ADMIN") {
          createPayload.regionId = Number(form.regionId);
        }

        await consultantService.create(createPayload);

        toast.success("Thêm người dùng thành công");
      }

      // refresh danh sách
      await onSuccess?.();

      setForm(initialForm);

      // đóng modal
      onClose();

    } catch (error) {

      const messages =
        error?.response?.data?.message;

      if (
        Array.isArray(messages) &&
        messages.length > 0
      ) {

        messages.forEach(item => {
          toast.error(item.message);
        });

      } else {

        toast.error(
          error?.response?.data?.message ||
          "Có lỗi xảy ra"
        );

      }

    }
  };

  return (
    <div className={styles.overlay}>

      <div className={styles.modal}>

        <h1>
          {consultant
            ? "Cập nhật thông tin người dùng"
            : "Thêm người dùng"}
        </h1>

        {!consultant && (
          <>
            <input
              placeholder="Tên đăng nhập"
              value={form.username}
              onChange={(e) =>
                setForm({
                  ...form,
                  username: e.target.value
                })
              }
            />

            <input
              type="password"
              placeholder="Mật khẩu"
              value={form.password}
              onChange={(e) =>
                setForm({
                  ...form,
                  password: e.target.value
                })
              }
            />
          </>
        )}

        <input
          placeholder="Họ tên"
          value={form.fullName}
          onChange={(e) =>
            setForm({
              ...form,
              fullName: e.target.value
            })
          }
        />

        <input
          placeholder="Số điện thoại"
          value={form.phone}
          onChange={(e) =>
            setForm({
              ...form,
              phone: e.target.value
            })
          }
        />

{
  currentRole === "ADMIN" && (

    <div className={styles.formGroup}>

      <label>Vai trò</label>

      <select
        value={form.role}
        onChange={(e) =>
          setForm({
            ...form,
            role: e.target.value
          })
        }
      >
        <option value="MANAGER">
          Quản lý khu vực
        </option>

        <option value="CONSULTANT">
          Tư vấn viên
        </option>

      </select>

    </div>

  )
}

        {
          currentRole === "ADMIN" && (

            <div className={styles.formGroup}>

              <label>Khu vực</label>

              <select
                value={form.regionId}
                onChange={(e) =>
                  setForm({
                    ...form,
                    regionId: e.target.value
                  })
                }
              >

                <option value="">
                  Chọn khu vực
                </option>

                {
                  regions.map(region => (
                    <option
                      key={region.id}
                      value={region.id}
                    >
                      {region.name}
                    </option>
                  ))
                }

              </select>

            </div>

          )
        }

        <div className={styles.actions}>
          <button
            onClick={() => {
              setForm(initialForm);
              onClose();
            }}
          >
            Hủy
          </button>

          <button
            className={styles.saveBtn}
            onClick={handleSubmit}
          >
            Lưu
          </button>
        </div>

      </div>

    </div>
  );
};

export default ConsultantModal;