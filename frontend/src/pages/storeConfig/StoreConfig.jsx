import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";

import styles from "./StoreConfig.module.scss";

import {
  setConfig,
  setLoading
} from "../../redux/slices/storeConfigSlice";

import storeConfigService from "../../services/storeConfigService";

import { toast } from "react-toastify";

const StoreConfig = () => {

  const dispatch = useDispatch();

  const { config, loading } = useSelector(
    state => state.storeConfig
  );

  const [form, setForm] = useState({
    storeName: "",
    address: "",
    hotline: "",
    adsText: ""
  });

  const loadData = async () => {
    try {

      dispatch(setLoading(true));

      const response = await storeConfigService.getConfig();

      const data = response.data || {};

      dispatch(setConfig(data));

      setForm({
        storeName: data.storeName || "",
        address: data.address || "",
        hotline: data.hotline || "",
        adsText: data.adsText || ""
      });

    } catch (error) {

      toast.error("Không tải được cấu hình cửa hàng");

    } finally {

      dispatch(setLoading(false));

    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleChange = (e) => {

    const { name, value } = e.target;

    setForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSave = async () => {

    try {

      dispatch(setLoading(true));

      await storeConfigService.updateConfig(form);

      dispatch(setConfig(form));

      toast.success("Cập nhật thành công");

    } catch (error) {

      toast.error("Cập nhật thất bại");

    } finally {

      dispatch(setLoading(false));

    }
  };

  return (
    <div className={styles.page}>

      <div className={styles.card}>

        <div className={styles.header}>
          <h1>Cấu hình in hóa đơn</h1>
          <p>
            Thiết lập thông tin hiển thị trên bill bán hàng
          </p>
        </div>

        <div className={styles.form}>

          <div className={styles.formGroup}>
            <label>Tên cửa hàng</label>

            <input
            className={styles.input_custom}
              type="text"
              name="storeName"
              value={form.storeName}
              onChange={handleChange}
              placeholder="Nhập tên cửa hàng"
            />
          </div>

          <div className={styles.formGroup}>
            <label>Địa chỉ</label>

            <input
            className={styles.input_custom}
              type="text"
              name="address"
              value={form.address}
              onChange={handleChange}
              placeholder="Nhập địa chỉ"
            />
          </div>

          <div className={styles.formGroup}>
            <label>Hotline</label>

            <input
            className={styles.input_custom}
              type="text"
              name="hotline"
              value={form.hotline}
              onChange={handleChange}
              placeholder="Nhập hotline"
            />
          </div>

          <div className={styles.formGroup}>
            <label>Nội dung quảng cáo cuối bill</label>

            <textarea
              rows="4"
              name="adsText"
              value={form.adsText}
              onChange={handleChange}
              placeholder="Ví dụ: Cảm ơn quý khách đã sử dụng dịch vụ"
            />
          </div>

          <div className={styles.actions}>
            <button
              onClick={handleSave}
              disabled={loading}
            >
              {loading
                ? "Đang lưu..."
                : "Lưu cấu hình"}
            </button>
          </div>

        </div>

      </div>

    </div>
  );
};

export default StoreConfig;