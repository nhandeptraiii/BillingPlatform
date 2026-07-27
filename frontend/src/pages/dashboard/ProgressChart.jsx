import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
} from "chart.js";

import { Doughnut } from "react-chartjs-2";

import styles from "./ProgressChart.module.scss";

ChartJS.register(
  ArcElement,
  Tooltip,
  Legend
);

const ProgressChart = ({
  title,
  percent = 0,
}) => {

  const displayPercent =
    Number(percent).toFixed(2);

  const chartPercent =
    Math.min(Number(percent), 100);

  const data = {
    datasets: [
      {
        data: [
          chartPercent,
          100 - chartPercent,
        ],

        backgroundColor: [
          "#D70018",
          "#EAEAEA",
        ],

        borderWidth: 0,
      },
    ],
  };

  const options = {
    responsive: true,

    maintainAspectRatio: false,

    cutout: "75%",

    plugins: {
      legend: {
        display: false,
      },
    },
  };

  return (
    <div className={styles.card}>
      <h3>{title}</h3>

      <div className={styles.chartWrapper}>
        <Doughnut
          data={data}
          options={options}
        />

        <div className={styles.center}>
          {displayPercent}%
        </div>
      </div>
    </div>
  );
};

export default ProgressChart;