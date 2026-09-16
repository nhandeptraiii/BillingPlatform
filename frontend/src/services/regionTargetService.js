import axiosClient from "../config/axios";

const regionTargetService = {
  getAll(params) {
    return axiosClient.get("/region-targets", { params });
  },
  save(data) {
    return axiosClient.post("/region-targets", data);
  }
};

export default regionTargetService;
