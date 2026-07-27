import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  config: {
    storeName: "",
    address: "",
    hotline: "",
    adsText: ""
  },
  loading: false
};

const storeConfigSlice = createSlice({
  name: "storeConfig",

  initialState,

  reducers: {
    setConfig: (state, action) => {
      state.config = action.payload;
    },

    setLoading: (state, action) => {
      state.loading = action.payload;
    }
  }
});

export const {
  setConfig,
  setLoading
} = storeConfigSlice.actions;

export default storeConfigSlice.reducer;