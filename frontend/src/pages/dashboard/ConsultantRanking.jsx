import styles from "./ConsultantRanking.module.scss";

const ConsultantRanking = ({
  consultants = [],
}) => {

  const ranking = [...consultants]
    .map((item) => ({
      ...item,

      progress:
        item.targetRecords > 0
          ? (
              item.collectedRecords /
              item.targetRecords
            ) *
            100
          : 0,
    }))
    .sort(
      (a, b) =>
        b.progress - a.progress
    );

  return (
    <div className={styles.card}>
      <h3>
        Xếp hạng nhân viên thu cước
      </h3>

      {ranking.map((item, index) => (
        <div
          key={item.consultantId}
          className={styles.row}
        >
          <div>
            #{index + 1} {" "}
            {item.consultantName}
          </div>

          <div className={styles.progress}>
            <div
              className={styles.fill}
              style={{
                width:
                  item.progress + "%",
              }}
            />
          </div>

          <span>
            {item.progress.toFixed(1)}%
          </span>
        </div>
      ))}
    </div>
  );
};

export default ConsultantRanking;