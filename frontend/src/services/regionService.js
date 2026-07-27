import axiosClient from "../config/axios";

const regionService = {

  getAll() {
    return axiosClient.get("/regions");
  }

};

export default regionService;