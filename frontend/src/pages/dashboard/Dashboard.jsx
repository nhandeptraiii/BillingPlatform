import { useEffect } from "react";

import { useDispatch, useSelector } from "react-redux";
import { useState } from "react";

import {
  fetchDashboard,
} from "../../redux/slices/dashboardSlice";

import KPISection from "./KPISection";

import ConsultantChart from "./ConsultantChart";

import DailyStatsTable from "./DailyStatsTable";

import WarningTable from "./WarningTable";

import styles from "./Dashboard.module.scss";
import ConsultantRanking from "./ConsultantRanking";
import ProgressSection from "./ProgressSection";

const Dashboard = () => {

  const dispatch = useDispatch();
  const now = new Date();

  const {
    overview,
    consultants,
    dailyStats,
    warnings,
  } = useSelector(
    (state) => state.dashboard
  );

  useEffect(() => {

    const now = new Date();

    dispatch(
      fetchDashboard({
        month: now.getMonth() + 1,
        year: now.getFullYear(),
        date: now.toISOString().split("T")[0],
      })
    );

  }, []);

  const [month, setMonth] = useState(
    now.getMonth() + 1
  );

  const [year, setYear] = useState(
    now.getFullYear()
  );

  const loadDashboard = () => {
    dispatch(
      fetchDashboard({
        month,
        year,
        date: new Date()
          .toISOString()
          .split("T")[0],
      })
    );
  };
  useEffect(() => {
    loadDashboard();
  }, [month, year]);

  return (
    <div className={styles.container}>

      <div className={styles.header}>
        <div>
          <h1>Dashboard Quản Lý Thu Cước</h1>
        </div>

        <div className={styles.filters}>
          <select
            value={month}
            onChange={(e) =>
              setMonth(Number(e.target.value))
            }
          >
            {Array.from({ length: 12 }, (_, i) => (
              <option
                key={i + 1}
                value={i + 1}
              >
                Tháng {i + 1}
              </option>
            ))}
          </select>

          <select
            value={year}
            onChange={(e) =>
              setYear(Number(e.target.value))
            }
          >
            {[2024, 2025, 2026, 2027].map(
              (year) => (
                <option
                  key={year}
                  value={year}
                >
                  {year}
                </option>
              )
            )}
          </select>
        </div>
      </div>

      <KPISection overview={overview} />

      <ConsultantChart
        data={consultants}
      />

      <div className={styles.progressSection}>
        <ProgressSection
          overview={overview}
        />
      </div>

      <div className={styles.bottomSection}>
        <ConsultantRanking
          consultants={consultants}
        />

        <DailyStatsTable
          data={dailyStats}
        />
      </div>

      <WarningTable
        warnings={warnings}
      />
    </div>
  );
};

export default Dashboard;