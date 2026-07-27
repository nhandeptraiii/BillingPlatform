import { createSlice } from "@reduxjs/toolkit";
import { debtData } from "../../mock/debtData";

const initialDebtSlice = createSlice({
  name: "initialDebt",

  initialState: {
    loading: false,
    debts: debtData,
    selectedRows: [],
    filters: {
      keyword: "",
      period: "",
      status: ""
    }
  },

  reducers: {
    setSelectedRows: (state, action) => {
      state.selectedRows = action.payload;
    },

    setFilters: (state, action) => {
      state.filters = {
        ...state.filters,
        ...action.payload
      };
    }
  }
});

export const {
  setSelectedRows,
  setFilters
} = initialDebtSlice.actions;

export default initialDebtSlice.reducer;