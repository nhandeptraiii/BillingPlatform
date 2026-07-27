import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend,
} from "chart.js";

import { Bar } from "react-chartjs-2";

import styles from "./ConsultantChart.module.scss";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend
);

const ConsultantChart = ({ data = [] }) => {
  const chartData = {
    labels: data.map(
      (item) => item.consultantName
    ),

    datasets: [
      {
        label: "Đã in bill",

        data: data.map(
          (item) => item.collectedRecords
        ),

        backgroundColor: "#D70018",

        borderRadius: 6,
      },

      {
        label: "Chưa in bill",

        data: data.map(
          (item) =>
            Math.max(
              item.targetRecords -
              item.collectedRecords,
              0
            )
        ),

        backgroundColor: "#FFD6D9",

        borderRadius: 6,
      },
    ],
  };

  const options = {
    responsive: true,

    maintainAspectRatio: false,

    plugins: {
      legend: {
        display: true,

        position: "top",

        align: "end",

        labels: {
          usePointStyle: true,

          pointStyle: "rectRounded",

          padding: 20,

          font: {
            size: 13,
            weight: "500",
          },
        },
      },

      tooltip: {
        callbacks: {
          title: function (tooltipItems) {
            return data[
              tooltipItems[0].dataIndex
            ].consultantName;
          },

          label: () => null,

          afterBody: function (
            tooltipItems
          ) {
            const consultant =
              data[
              tooltipItems[0].dataIndex
              ];

            const remainingRecords =
              consultant.targetRecords -
              consultant.collectedRecords;

            const remainingAmount =
              consultant.targetAmount -
              consultant.collectedAmount;

            const percent =
              consultant.targetRecords > 0
                ? (
                  (consultant.collectedRecords /
                    consultant.targetRecords) *
                  100
                ).toFixed(1)
                : 0;

            return [
              `Đã thu hồ sơ: ${consultant.collectedRecords}`,
              `Chưa thu hồ sơ: ${remainingRecords}`,
              `Chỉ tiêu hồ sơ: ${consultant.targetRecords}`,

              "",

              `Đã thu tiền: ${new Intl.NumberFormat(
                "vi-VN"
              ).format(
                consultant.collectedAmount
              )} đ`,

              `Còn lại tiền: ${new Intl.NumberFormat(
                "vi-VN"
              ).format(
                remainingAmount
              )} đ`,

              `Chỉ tiêu tiền: ${new Intl.NumberFormat(
                "vi-VN"
              ).format(
                consultant.targetAmount
              )} đ`,

              "",

              `Hoàn thành: ${percent}%`,
            ];
          },
        },
      },
    },

    scales: {
      y: {
        beginAtZero: true,

        title: {
          display: true,
          text: "Số hồ sơ",
        },

        ticks: {
          precision: 0,
        },
      },
    },
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        Hiệu suất thu cước theo nhân viên
      </div>

      <div className={styles.chart}>
        <Bar
          data={chartData}
          options={options}
        />
      </div>
    </div>
  );
};

export default ConsultantChart;