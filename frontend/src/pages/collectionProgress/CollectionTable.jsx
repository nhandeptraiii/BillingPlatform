import {
  useDispatch
} from "react-redux";

import styles from "./CollectionTable.module.scss";
import { markDebtRecord, printBillRecord } from "../../redux/slices/collectionProgressSlice";
import { toast } from "react-toastify";

const formatMoney = (value) => {

  return Number(
    value || 0
  ).toLocaleString("vi-VN");
};

const formatDate = (date) => {

  if (!date) return "-";

  return new Date(date)
    .toLocaleString("vi-VN");
};

const CollectionTable = ({
  records,
  loading,
  page,
  size,
  visibleColumns
}) => {
  const dispatch =
    useDispatch();

  const renderCollectionStatus =
    (status) => {

      switch (status) {

        case "DA_THANH_TOAN":
          return (
            <span
              className={
                styles.success
              }
            >
              Đã thanh toán
            </span>
          );

        default:
          return (
            <span
              className={
                styles.danger
              }
            >
              Chưa thu
            </span>
          );
      }
    };

  const renderDebtStatus =
    (status) => {

      switch (status) {

        case "DA_GACH_NO":
          return (
            <span
              className={
                styles.success
              }
            >
              Đã gạch nợ
            </span>
          );

        default:
          return (
            <span
              className={
                styles.warning
              }
            >
              Chưa gạch nợ
            </span>
          );
      }
    };

  if (loading) {

    return (
      <div
        className={
          styles.loading
        }
      >
        Đang tải dữ liệu...
      </div>
    );
  }

  return (

    <div
      className={
        styles.tableWrapper
      }
    >

      <table>

        <thead>

          <tr>
            {
              visibleColumns.stt && (
                <th className={styles.sttHeader}>
                  STT
                </th>
              )
            }
            {visibleColumns.customer && (
              <th className={styles.customerColumn}>
                Thông tin KH
              </th>
            )}
            {visibleColumns.amountDue && (
              <th>
                Cước
              </th>
            )}
            {visibleColumns.collectedAmount && (
              <th>
                Đã thu
              </th>
            )}
            {visibleColumns.consultant && (
              <th>
                Nhân viên
              </th>
            )}
            {visibleColumns.billingPeriod && (
              <th className={styles.periodColumn}>
                Kỳ cước
              </th>
            )}
            {visibleColumns.serviceType && (
              <th>
                Loại DV
              </th>
            )}
            {visibleColumns.ads && (
              <th>
                Nội dung Ads
              </th>
            )}
            {visibleColumns.collectedAt && (
              <th>
                Ngày thu
              </th>
            )}
            {visibleColumns.paymentStatus && (
              <th className={styles.statusColumn}>
                Trạng thái thu
              </th>
            )}
            {visibleColumns.debtStatus && (
              <th className={styles.statusColumn}>
                Gạch nợ
              </th>
            )}
          </tr>

        </thead>

        <tbody>

          {records.length === 0 && (

            <tr>

              <td
                colSpan={
                  Object.values(
                    visibleColumns
                  ).filter(Boolean).length
                }
                className={styles.empty}
              >
                Không có dữ liệu
              </td>

            </tr>
          )}

          {records.map(
            (item, index) => (

              <tr
                key={item.id}
              >
                {visibleColumns.stt && (
                  <td className={styles.stt}>
                    {page * size + index + 1}
                  </td>
                )}
                {visibleColumns.customer && (
                  <td>

                    <div
                      className={
                        styles.customerInfo
                      }
                    >

                      <div>
                        <strong>
                          {item.customerCode}
                        </strong>
                      </div>

                      <div>
                        {
                          item.customerName
                        }
                      </div>

                      <div>
                        {
                          item.subscriberNumber
                        }
                      </div>

                      <div>
                        {
                          item.phoneNumber
                        }
                      </div>

                      <div
                        className={
                          styles.address
                        }
                      >
                        {
                          item.fullAddress
                        }
                      </div>

                    </div>

                  </td>
                )}
                {visibleColumns.amountDue && (
                  <td>
                    {formatMoney(
                      item.amountDue
                    )}
                  </td>
                )}
                {visibleColumns.collectedAmount && (
                  <td>

                    <div>

                      {formatMoney(
                        item.collectedAmount
                      )}

                    </div>

                    {item.collectedByName && (

                      <div
                        className={
                          styles.smallText
                        }
                      >
                        {
                          item.collectedByName
                        }
                      </div>

                    )}

                  </td>
                )}
                {visibleColumns.consultant && (
                  <td>
                    {
                      item.assignedConsultantName
                    }
                  </td>
                )}
                {visibleColumns.billingPeriod && (
                  <td>
                    {
                      item.billingPeriodName
                    }
                  </td>
                )}
                {visibleColumns.serviceType && (
                  <td>
                    {
                      item.serviceType
                    }
                  </td>
                )}
                {visibleColumns.ads && (
                  <td>

                    <div
                      className={
                        styles.ads
                      }
                    >
                      {
                        item.adsContent
                      }
                    </div>

                  </td>
                )}
                {visibleColumns.collectedAt && (
                  <td>
                    {formatDate(
                      item.collectedAt
                    )}
                  </td>
                )}
                {visibleColumns.paymentStatus && (
                  <td>

                    <div
                      className={`${styles.switch} ${item.collectionStatus === "DA_THANH_TOAN"
                        ? styles.switchOn
                        : styles.switchOff
                        }`}
                      onClick={() =>
                        dispatch(
                          printBillRecord({
                            recordId: item.id,
                            collectedAmount:
                              item.amountDue
                          })
                        )
                          .unwrap()
                          .catch((error) => {

                            toast.error(error);

                          })
                      }
                    >
                      <span className={styles.knob}></span>
                    </div>

                  </td>
                )}
                {visibleColumns.debtStatus && (
                  <td>

                    <div
                      className={`${styles.switch} ${item.debtStatus === "DA_GACH_NO"
                        ? styles.debtOn
                        : styles.switchOff
                        }`}
                      onClick={() =>
                        dispatch(
                          markDebtRecord(item.id)
                        )
                          .unwrap()
                          .catch((error) => {

                            toast.error(error);

                          })
                      }
                    >
                      <span className={styles.knob}></span>
                    </div>

                  </td>
                )}
              </tr>

            )
          )}

        </tbody>

      </table>

    </div>
  );
};

export default CollectionTable;