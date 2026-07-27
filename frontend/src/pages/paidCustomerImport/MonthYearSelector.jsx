import { setSelectedMonth, setSelectedYear } from "../../redux/slices/paidCustomerImportSlice";
import styles
from "./MonthYearSelector.module.scss";

import {
  useDispatch,
  useSelector
}
from "react-redux";

const MonthYearSelector =
() => {

  const dispatch =
    useDispatch();

  const {

    selectedMonth,
    selectedYear

  }
  =
  useSelector(
    state =>
      state
      .paidCustomerImport
  );

const currentYear = new Date().getFullYear();

const years = [
  currentYear - 2,
  currentYear - 1,
  currentYear,
  currentYear + 1
];

  return (

    <div
      className={
        styles.wrapper
      }
    >

      <div
        className={
          styles.item
        }
      >

        <label>
          Tháng
        </label>

        <select

          value={
            selectedMonth
          }

          onChange={e =>
            dispatch(
              setSelectedMonth(
                e.target.value
              )
            )
          }

        >

          {
            Array.from(
              {
                length: 12
              }
            ).map(
              (
                _,
                index
              ) => (

                <option
                  key={
                    index + 1
                  }
                  value={
                    index + 1
                  }
                >

                  Tháng {
                    index + 1
                  }

                </option>

              )
            )
          }

        </select>

      </div>

      <div
        className={
          styles.item
        }
      >

        <label>
          Năm
        </label>

        <select

          value={
            selectedYear
          }

          onChange={e =>
            dispatch(
              setSelectedYear(
                e.target.value
              )
            )
          }

        >

          {
            years.map(
              year => (

                <option
                  key={year}
                  value={year}
                >
                  {year}
                </option>

              )
            )
          }

        </select>

      </div>

    </div>

  );

};

export default
MonthYearSelector;