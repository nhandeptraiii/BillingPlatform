import { createSlice }
from "@reduxjs/toolkit";

const now =
  new Date();

const initialState = {

  selectedMonth:
    now.getMonth() + 1,

  selectedYear:
    now.getFullYear()

};

const paidCustomerImportSlice =
createSlice({

  name:
    "paidCustomerImport",

  initialState,

  reducers: {

    setSelectedMonth(
      state,
      action
    ) {

      state.selectedMonth =
        Number(
          action.payload
        );

    },

    setSelectedYear(
      state,
      action
    ) {

      state.selectedYear =
        Number(
          action.payload
        );

    }

  }

});

export const {

  setSelectedMonth,
  setSelectedYear

}
=
paidCustomerImportSlice.actions;

export default
paidCustomerImportSlice.reducer;