import axiosClient from "../config/axios";

const consultantService = {
  getList(params) {
    return axiosClient.get("/users", {
      params
    });
  },

  create(data) {
    return axiosClient.post("/users", data);
  },

  update(id, data) {
    return axiosClient.put(`/users/${id}`, data);
  },

  delete(id) {
    return axiosClient.delete(`/users/${id}`);
  }
};

export default consultantService;