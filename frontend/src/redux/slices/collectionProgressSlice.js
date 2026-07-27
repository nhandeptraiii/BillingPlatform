import {
  createSlice,
  createAsyncThunk
} from "@reduxjs/toolkit";

import {
  getCollectionProgress,
  markDebt,
  printBill,
  exportCollectionProgress,
  bulkMarkDebt
} from "../../services/collectionProgressService";

import consultantService
  from "../../services/consultantService";
import { toast } from "react-toastify";


export const fetchCollectionProgress =
  createAsyncThunk(
    "collectionProgress/fetch",

    async (_, thunkAPI) => {

      const state =
        thunkAPI.getState()
          .collectionProgress;

      return await getCollectionProgress({
        page: state.page,
        size: state.size,
        ...state.filters
      });
    }
  );

export const fetchConsultants =
  createAsyncThunk(
    "collectionProgress/fetchConsultants",

    async () => {

      const response =
        await consultantService.getList({
          page: 0,
          size: 100
        });

      return response.data.data;
    }
  );

export const printBillRecord =
  createAsyncThunk(

    "collectionProgress/printBill",

    async (
      {
        recordId,
        collectedAmount
      },
      thunkAPI
    ) => {

      try {

        await printBill(
          recordId,
          collectedAmount
        );

        toast.success(
          "Thanh toán thành công"
        );

        return {
          recordId,
          collectedAmount
        };

      } catch (error) {

        const message =
          error.response?.data?.message ||
          "Thanh toán thất bại";

        return thunkAPI.rejectWithValue(
          message
        );
      }
    }
  );
export const markDebtRecord =
  createAsyncThunk(

    "collectionProgress/markDebt",

    async (
      recordId,
      thunkAPI
    ) => {

      try {

        await markDebt(recordId);

        toast.success(
          "Gạch nợ thành công"
        );

        return recordId;

      } catch (error) {

        const message =
          error.response?.data?.message ||
          "Gạch nợ thất bại";

        return thunkAPI.rejectWithValue(
          message
        );
      }
    }
  );

export const exportExcel =
  createAsyncThunk(

    "collectionProgress/export",

    async (_, thunkAPI) => {

      const state =
        thunkAPI.getState()
          .collectionProgress;

      const blob =
        await exportCollectionProgress({

          month:
            state.filters.month,

          year:
            state.filters.year,

          assignedUserId:
            state.filters.assignedUserId,

          collectionStatus:
            state.filters.collectionStatus,

          debtStatus:
            state.filters.debtStatus
        });

      const url =
        window.URL.createObjectURL(
          blob
        );

      const a =
        document.createElement(
          "a"
        );

      a.href = url;

      a.download =
        `TienDoThuCuoc_${state.filters.month}_${state.filters.year}.xlsx`;

      a.click();

      window.URL
        .revokeObjectURL(
          url
        );
    }
  );

  export const bulkMarkDebtByFilter =
  createAsyncThunk(

    "collectionProgress/bulkMarkDebt",

    async (_, thunkAPI) => {

      try {

        const state =
          thunkAPI.getState()
            .collectionProgress;

        await bulkMarkDebt({

          month:
            state.filters.month,

          year:
            state.filters.year,

          assignedUserId:
            state.filters.assignedUserId || undefined
        });

        toast.success(
          "Gạch nợ tất cả thành công"
        );

        thunkAPI.dispatch(
          fetchCollectionProgress()
        );

      } catch (error) {

        toast.error(
          error.response?.data?.message ||
          "Gạch nợ thất bại"
        );

        return thunkAPI.rejectWithValue(
          error.response?.data?.message
        );
      }
    }
  );

const initialState = {

  loading: false,

  records: [],

  consultants: [],

  page: 0,

  size: 20,

  totalPages: 0,

  totalElements: 0,

  filters: {

    month: new Date().getMonth() + 1,

    year: new Date().getFullYear(),

    search: "",

    assignedUserId: "",

    collectionStatus: "",

    debtStatus: ""
  }
};

const collectionProgressSlice =
  createSlice({

    name: "collectionProgress",

    initialState,

    reducers: {

      updateFilter: (
        state,
        action
      ) => {

        state.filters = {

          ...state.filters,

          ...action.payload
        };
      },

      resetFilter: (state) => {

        state.filters = {

          month:
            new Date().getMonth() + 1,

          year:
            new Date().getFullYear(),

          search: "",

          assignedUserId: "",

          collectionStatus: "",

          debtStatus: ""
        };

        state.page = 0;
      },

      setPage: (
        state,
        action
      ) => {

        state.page =
          action.payload;
      },

      setSize: (
        state,
        action
      ) => {

        state.size =
          action.payload;
      }

    },

    extraReducers: (builder) => {

      builder

        .addCase(
          fetchCollectionProgress.pending,

          (state) => {

            state.loading = true;
          }
        )

        .addCase(
          fetchCollectionProgress.fulfilled,

          (state, action) => {

            state.loading = false;

            state.records =
              action.payload.content;

            state.totalPages =
              action.payload.totalPages;

            state.totalElements =
              action.payload.totalElements;
          }
        )

        .addCase(
          fetchCollectionProgress.rejected,

          (state) => {

            state.loading = false;
          }
        )

        .addCase(
          fetchConsultants.fulfilled,

          (state, action) => {

            state.consultants =
              action.payload.content;
          }
        )

        .addCase(
  printBillRecord.fulfilled,

  (state, action) => {

    const record =
      state.records.find(
        x =>
          x.id ===
          action.payload.recordId
      );

    if (record) {

      record.collectionStatus =
        "DA_THANH_TOAN";

      record.collectedAmount =
        action.payload.collectedAmount;

      record.collectedAt =
        new Date().toISOString();
    }
  }
)

.addCase(
  markDebtRecord.fulfilled,

  (state, action) => {

    const record =
      state.records.find(
        x =>
          x.id ===
          action.payload
      );

    if (record) {

      record.debtStatus =
        "DA_GACH_NO";
    }
  }
);
    }

  });

export const {

  updateFilter,

  resetFilter,

  setPage,

  setSize

} = collectionProgressSlice.actions;

export default
  collectionProgressSlice.reducer;