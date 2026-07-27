import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import consultantService from "../../services/consultantService";

export const fetchConsultants = createAsyncThunk(
  "consultant/fetch",
  async ({
    page = 0,
    size = 5,
    keyword = "",
    role = ""
  }) => {

    const params = {
      page,
      size
    };

    if (keyword?.trim()) {
      params.keyword = keyword;
    }

    if (role) {
      params.role = role;
    }

    const res =
      await consultantService.getList(params);

    return res.data.data;
  }
);

const consultantSlice = createSlice({
  name: "consultant",

initialState: {
  list: [],
  loading: false,

  page: 0,
  size: 5,

  totalPages: 0,
  totalElements: 0
},

  reducers: {},

  extraReducers: (builder) => {
    builder

      .addCase(fetchConsultants.pending, (state) => {
        state.loading = true;
      })

.addCase(fetchConsultants.fulfilled, (state, action) => {

  state.loading = false;

  state.list = action.payload.content;

  state.page = action.payload.number;

  state.totalPages = action.payload.totalPages;

  state.totalElements = action.payload.totalElements;

})

      .addCase(fetchConsultants.rejected, (state) => {
        state.loading = false;
      });
  }
});

export default consultantSlice.reducer;