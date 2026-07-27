import {
  useEffect,
  useState
} from "react";

import {
  useDispatch,
  useSelector
} from "react-redux";

import styles from "./CollectionFilter.module.scss";


import { fetchCollectionProgress, resetFilter, setPage, updateFilter } from "../../redux/slices/collectionProgressSlice";
import { selectConsultants, selectFilters } from "../../redux/selector/collectionProgressSelector";

const CollectionFilter = () => {

  const dispatch =
    useDispatch();

  const consultants =
    useSelector(
      selectConsultants
    );

  const filters =
    useSelector(
      selectFilters
    );

  const [searchText, setSearchText] =
    useState(filters.search);

  const handleSearch = () => {

    dispatch(setPage(0));

    dispatch(
      fetchCollectionProgress()
    );
  };

  const currentYear =
    new Date().getFullYear();

const handleReset = () => {

  setSearchText("");

  dispatch(
    resetFilter()
  );
};

useEffect(() => {

  const timer =
    setTimeout(() => {

      dispatch(
        updateFilter({
          search: searchText
        })
      );

    }, 500);

  return () =>
    clearTimeout(timer);

}, [searchText, dispatch]);

useEffect(() => {

  dispatch(setPage(0));

  dispatch(
    fetchCollectionProgress()
  );

}, [

  filters.month,

  filters.year,

  filters.assignedUserId,

  filters.collectionStatus,

  filters.debtStatus,

  filters.search,

  dispatch

]);

  return (

    <div className={styles.container}>

      {/* Hàng bộ lọc */}
      <div className={styles.filterRow}>

        <div className={styles.periodGroup}>

          {/* Tháng */}

          <select
            value={filters.month}
            className={styles.monthSelect}
            onChange={(e) =>
              dispatch(
                updateFilter({
                  month: Number(e.target.value)
                })
              )
            }
          >
            {
              Array.from(
                { length: 12 },
                (_, i) => (
                  <option
                    key={i + 1}
                    value={i + 1}
                  >
                    Tháng {i + 1}
                  </option>
                )
              )
            }
          </select>

          {/* Năm */}

          <select
            value={filters.year}
            className={styles.yearSelect}
            onChange={(e) =>
              dispatch(
                updateFilter({
                  year: Number(e.target.value)
                })
              )
            }
          >
            {
              Array.from(
                {
                  length:
                    currentYear -
                    2024 +
                    2
                },
                (_, i) => {

                  const year =
                    2024 + i;

                  return (
                    <option
                      key={year}
                      value={year}
                    >
                      {year}
                    </option>
                  );
                }
              )
            }
          </select>


        </div>

        <select
          value={filters.assignedUserId}
          onChange={(e) =>
            dispatch(
              updateFilter({
                assignedUserId:
                  e.target.value
              })
            )
          }
        >
          <option value="">
            Tất cả nhân viên
          </option>

          {consultants.map((item) => (
            <option
              key={item.id}
              value={item.id}
            >
              {item.fullName}
            </option>
          ))}
        </select>

        <select
          value={filters.collectionStatus}
          onChange={(e) =>
            dispatch(
              updateFilter({
                collectionStatus:
                  e.target.value
              })
            )
          }
        >
          <option value="">
            Tất cả trạng thái thu
          </option>

          <option value="CHUA_THU">
            Chưa thu
          </option>

          <option value="DA_THANH_TOAN">
            Đã thanh toán
          </option>
        </select>

        <select
          value={filters.debtStatus}
          onChange={(e) =>
            dispatch(
              updateFilter({
                debtStatus:
                  e.target.value
              })
            )
          }
        >
          <option value="">
            Tất cả trạng thái gạch nợ
          </option>

          <option value="CHUA_GACH_NO">
            Chưa gạch nợ
          </option>

          <option value="DA_GACH_NO">
            Đã gạch nợ
          </option>
        </select>

      </div>

      {/* Hàng tìm kiếm */}
      <div className={styles.searchRow}>

        <input
          placeholder="
  Tìm mã KH, tên KH, số TB, SĐT, địa chỉ...
  "
          value={searchText}
          onChange={(e) =>
            setSearchText(
              e.target.value
            )
          }
        />

        {/* <input
          placeholder="Tìm mã KH, tên KH, số TB, SĐT, địa chỉ..."
          value={filters.search}
          onChange={(e) =>
            dispatch(
              updateFilter({
                search: e.target.value
              })
            )
          }
        /> */}

        <button
          className={styles.resetBtn}
          onClick={handleReset}
        >
          Làm mới
        </button>


      </div>

    </div>
  );
};

export default CollectionFilter;