import { useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { toast } from "react-toastify";
import billingPeriodService from "../../services/billingPeriodsService";
import dashboardService from "../../services/dashboardService";
import regionService from "../../services/regionService";
import regionTargetService from "../../services/regionTargetService";
import { getRole } from "../../utils/role";
import numeral from "numeral";
import styles from "./RegionTargets.module.scss";

const value = (number) => numeral(number || 0).format("0,0");

export default function RegionTargets() {
  const user = useSelector(state => state.auth.user);
  const role = getRole(user);
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());
  const [periods, setPeriods] = useState([]);
  const [regions, setRegions] = useState([]);
  const [regionId, setRegionId] = useState(role === "ADMIN" ? "" : String(user?.regionId || ""));
  const [form, setForm] = useState({ targetCustomerPercent: "", targetRevenuePercent: "", targetFtthN1Percent: "" });
  const [overview, setOverview] = useState({});
  const [saving, setSaving] = useState(false);

  useEffect(() => { (async () => {
    try {
      const [periodResponse, regionResponse] = await Promise.all([
        billingPeriodService.getBillingPeriods({ page: 0, size: 50 }),
        role === "ADMIN" ? regionService.getAll() : Promise.resolve({ data: [] })
      ]);
      setPeriods(periodResponse.data.content || periodResponse.data.data?.content || []);
      setRegions(regionResponse.data.data || regionResponse.data || []);
    } catch (error) { toast.error(error.response?.data?.message || "Không thể tải dữ liệu giao chỉ tiêu"); }
  })(); }, [role]);

  useEffect(() => { (async () => {
    try {
      const [targetResponse, overviewResponse] = await Promise.all([
        regionTargetService.getAll({ month, year, ...(role === "ADMIN" && regionId ? { regionId } : {}) }),
        dashboardService.getOverview(month, year)
      ]);
      const targets = targetResponse.data.data || targetResponse.data || [];
      const target = targets.find(item => !regionId || String(item.regionId) === String(regionId)) || targets[0];
      setForm({
        targetCustomerPercent: target?.targetCustomerPercent ?? "",
        targetRevenuePercent: target?.targetRevenuePercent ?? "",
        targetFtthN1Percent: target?.targetFtthN1Percent ?? ""
      });
      setOverview(overviewResponse.data.data || overviewResponse.data || {});
    } catch (error) { toast.error(error.response?.data?.message || "Không thể tải chỉ tiêu"); }
  })(); }, [month, year, regionId, role]);

  const periodId = useMemo(() => periods.find(p => Number(p.month) === month && Number(p.year) === year)?.id, [periods, month, year]);
  const calculate = (base, percent) => Math.round((Number(base) || 0) * (Number(percent) || 0) / 100);
  const calculateMoney = (base, percent) => (Number(base) || 0) * (Number(percent) || 0) / 100;

  const save = async (event) => {
    event.preventDefault();
    if (!periodId) return toast.warning("Chưa có dữ liệu đầu kỳ cho tháng/năm đã chọn");
    if (role === "ADMIN" && !regionId) return toast.warning("Vui lòng chọn khu vực");
    setSaving(true);
    try {
      await regionTargetService.save({
        billingPeriodId: Number(periodId),
        ...(role === "ADMIN" ? { regionId: Number(regionId) } : {}),
        targetCustomerPercent: Number(form.targetCustomerPercent),
        targetRevenuePercent: Number(form.targetRevenuePercent),
        targetFtthN1Percent: form.targetFtthN1Percent === "" ? null : Number(form.targetFtthN1Percent)
      });
      toast.success("Đã lưu chỉ tiêu");
    } catch (error) { toast.error(error.response?.data?.message || "Không thể lưu chỉ tiêu"); }
    finally { setSaving(false); }
  };

  return <div className={styles.page}>
    <h1>Giao chỉ tiêu khu vực</h1><p>Chọn tháng, năm để thiết lập chỉ tiêu. Hệ thống tự dùng dữ liệu đầu kỳ tương ứng.</p>
    <form className={styles.card} onSubmit={save}>
      <label>Tháng<select value={month} onChange={e => setMonth(Number(e.target.value))}>{Array.from({ length: 12 }, (_, i) => <option key={i + 1} value={i + 1}>Tháng {i + 1}</option>)}</select></label>
      <label>Năm<select value={year} onChange={e => setYear(Number(e.target.value))}>{[2024, 2025, 2026, 2027, 2028].map(item => <option key={item} value={item}>{item}</option>)}</select></label>
      {role === "ADMIN" && <label>Khu vực<select value={regionId} onChange={e => setRegionId(e.target.value)} required><option value="">Chọn khu vực</option>{regions.map(region => <option key={region.id} value={region.id}>{region.name}</option>)}</select></label>}
      <label>Chỉ tiêu toàn bộ – % khách hàng<input type="number" min="0" max="100" step="0.1" value={form.targetCustomerPercent} onChange={e => setForm({ ...form, targetCustomerPercent: e.target.value })} required /></label>
      <label>Chỉ tiêu toàn bộ – % doanh thu<input type="number" min="0" max="100" step="0.1" value={form.targetRevenuePercent} onChange={e => setForm({ ...form, targetRevenuePercent: e.target.value })} required /></label>
      <label>Chỉ tiêu FTTH N1 – % hoàn thành<input type="number" min="0" max="100" step="0.1" value={form.targetFtthN1Percent} onChange={e => setForm({ ...form, targetFtthN1Percent: e.target.value })} placeholder="Không bắt buộc" /></label>
      <button disabled={saving}>{saving ? "Đang lưu..." : "Lưu chỉ tiêu"}</button>
    </form>
    <section className={styles.preview}><h2>Quy đổi chỉ tiêu từ dữ liệu đầu kỳ</h2>
      <div className={styles.previewGrid}>
        <div><h3>Toàn bộ kỳ cước</h3><p>Khách hàng mục tiêu: <strong>{value(calculate(overview.totalRecordsImported, form.targetCustomerPercent))}</strong> / {value(overview.totalRecordsImported)}</p><p>Doanh thu mục tiêu: <strong>{value(calculateMoney(overview.totalExpectedAmount, form.targetRevenuePercent))} đ</strong> / {value(overview.totalExpectedAmount)} đ</p></div>
        <div><h3>FTTH N1</h3><p>Khách hàng mục tiêu: <strong>{value(calculate(overview.ftthN1TotalRecords, form.targetFtthN1Percent))}</strong> / {value(overview.ftthN1TotalRecords)}</p><p>Doanh thu mục tiêu: <strong>{value(calculateMoney(overview.ftthN1ExpectedAmount, form.targetFtthN1Percent))} đ</strong> / {value(overview.ftthN1ExpectedAmount)} đ</p></div>
      </div>
    </section>
  </div>;
}
