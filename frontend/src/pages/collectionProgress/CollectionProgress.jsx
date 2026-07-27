import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";

import styles from "./CollectionProgress.module.scss";

import CollectionFilter from "./CollectionFilter";

// phần 3 sẽ làm
import CollectionTable from "./CollectionTable";
import CollectionPagination from "./CollectionPagination";

import {
  bulkMarkDebtByFilter,
  exportExcel,
  fetchCollectionProgress,
  fetchConsultants
} from "../../redux/slices/collectionProgressSlice";

import {
  selectRecords,
  selectLoading,
  selectPagination
} from "../../redux/selector/collectionProgressSelector";
import ColumnSelector from "./ColumnSelector";

const CollectionProgress = () => {

  const dispatch = useDispatch();

  const [visibleColumns, setVisibleColumns] =
    useState({

      stt: true,

      customer: true,

      amountDue: true,

      collectedAmount: true,

      consultant: true,

      billingPeriod: true,

      serviceType: true,

      ads: true,

      collectedAt: true,

      paymentStatus: true,

      debtStatus: true
    });

  const [showColumns, setShowColumns] =
    useState(false);

  const pagination = useSelector(selectPagination);


  const records =
    useSelector(selectRecords);

  const loading =
    useSelector(selectLoading);

  useEffect(() => {

    dispatch(fetchConsultants());

  }, [dispatch]);

  useEffect(() => {

    dispatch(
      fetchCollectionProgress()
    );

  }, [dispatch]);

  return (

    <div className={styles.wrapper}>

      <div className={styles.header}>

        <div>

          <h1>
            Tiến độ thu cước
          </h1>

          <p>
            Quản lý hồ sơ khách hàng và thu cước
          </p>

        </div>

      </div>


      <CollectionFilter />

      <div className={styles.tableSection}>

<div className={styles.toolbar}>

  <div className={styles.leftActions}>

    <button
      className={styles.exportBtn}
      onClick={() =>
        dispatch(exportExcel())
      }
    >
      Xuất Excel
    </button>

    <button
      className={styles.bulkDebtBtn}
      onClick={() => {

        if (
          window.confirm(
            "Gạch nợ tất cả dữ liệu đang lọc?"
          )
        ) {

          dispatch(
            bulkMarkDebtByFilter()
          );
        }
      }}
    >
      ✓ Gạch nợ tất cả
    </button>

  </div>

  <div className={styles.tableToolbar}>

    <button
      className={styles.columnBtn}
      onClick={() =>
        setShowColumns(
          !showColumns
        )
      }
    >
      ⚙️ Tùy chỉnh cột
    </button>

    {
      showColumns && (

        <ColumnSelector
          visibleColumns={
            visibleColumns
          }
          setVisibleColumns={
            setVisibleColumns
          }
        />

      )
    }

  </div>

</div>

        <CollectionTable
          records={records}
          loading={loading}
          page={pagination.page}
          size={pagination.size}
          visibleColumns={visibleColumns}
        />

        <CollectionPagination />

      </div>

    </div>
  );
};

export default CollectionProgress;