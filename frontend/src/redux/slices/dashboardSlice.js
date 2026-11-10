import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import dashboardService from "../../services/dashboardService";

export const fetchDashboard = createAsyncThunk(
  "dashboard/fetchDashboard",
  async ({ month, year, date }) => {
    const [overview, consultants, dailyStats] =
      await Promise.all([
        dashboardService.getOverview(month, year),
        dashboardService.getConsultants(month, year),
        dashboardService.getDailyStats(date),
      ]);

    return {
      overview: overview.data.data || overview.data,
      consultants: consultants.data.data || consultants.data,
      dailyStats: dailyStats.data.data || dailyStats.data,
    };
  }
);

const dashboardSlice = createSlice({
  name: "dashboard",

  initialState: {
    loading: false,

    overview: {},

    consultants: [],

    dailyStats: [],

  },

  reducers: {},

  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboard.pending, (state) => {
        state.loading = true;
      })

      .addCase(fetchDashboard.fulfilled, (state, action) => {
        state.loading = false;

        state.overview = action.payload.overview;

        state.consultants = action.payload.consultants;

        state.dailyStats = action.payload.dailyStats;

      })

      .addCase(fetchDashboard.rejected, (state) => {
        state.loading = false;
      });
  },
});

export default dashboardSlice.reducer;
