import styles from "./ColumnSelector.module.scss";

const columns = [

  {
    key: "stt",
    label: "STT"
  },

  {
    key: "customer",
    label: "Thông tin KH"
  },

  {
    key: "amountDue",
    label: "Cước"
  },

  {
    key: "collectedAmount",
    label: "Đã thu"
  },

  {
    key: "consultant",
    label: "Nhân viên"
  },

  {
    key: "billingPeriod",
    label: "Kỳ cước"
  },

  {
    key: "serviceType",
    label: "Loại DV"
  },

  {
    key: "ads",
    label: "Nội dung Ads"
  },

  {
    key: "collectedAt",
    label: "Ngày thu"
  },

  {
    key: "paymentStatus",
    label: "Trạng thái thu"
  },

  {
    key: "debtStatus",
    label: "Gạch nợ"
  }
];

const ColumnSelector = ({
  visibleColumns,
  setVisibleColumns
}) => {

  return (

    <div className={styles.popup}>

      {

        columns.map(
          (column) => (

<label
  key={column.key}
  className={styles.item}
>

              <input

                type="checkbox"

                checked={
                  visibleColumns[
                    column.key
                  ]
                }

                onChange={() => {

                  setVisibleColumns(
                    prev => ({

                      ...prev,

                      [column.key]:
                        !prev[
                          column.key
                        ]
                    })
                  );
                }}
              />

<span>
  {column.label}
</span>
            </label>

          )
        )

      }

    </div>
  );
};

export default ColumnSelector;