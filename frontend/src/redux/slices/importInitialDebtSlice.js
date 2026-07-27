import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  previewData: [],
  loading: false,
};

const importInitialDebtSlice = createSlice({
  name: "importInitialDebt",

  initialState,

  reducers: {
    setPreviewData: (state, action) => {
      state.previewData = action.payload;
    },

    setLoading: (state, action) => {
      state.loading = action.payload;
    },

    clearImportData: (state) => {
      state.selectedFile = null;
      state.previewData = [];
      state.loading = false;
    },
  },
});

export const {
  setPreviewData,
  setLoading,
  clearImportData,
} = importInitialDebtSlice.actions;

export default importInitialDebtSlice.reducer;