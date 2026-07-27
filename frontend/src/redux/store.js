import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slices/authSlice";
import importInitialDebtReducer from "./slices/importInitialDebtSlice";
import paidCustomerImportReducer from "./slices/paidCustomerImportSlice";
import initialDebtReducer from "./slices/initialDebtSlice"
import collectionProgressReducer from "./slices/collectionProgressSlice"
import consultantReducer from "./slices/consultantSlice"
import storeConfigReducer from "./slices/storeConfigSlice"
import dashboardReducer from "./slices/dashboardSlice"
import { injectStore } from "../config/axios";

export const store = configureStore({
  reducer: {
    auth: authReducer,
    importInitialDebt: importInitialDebtReducer,
    paidCustomerImport: paidCustomerImportReducer,
    initialDebt : initialDebtReducer,
    collectionProgress: collectionProgressReducer,
    consultant: consultantReducer,
    storeConfig: storeConfigReducer,
    dashboard: dashboardReducer,
  }
});

// inject store vào axios
injectStore(store);