import axiosClient from "../config/axios";

const storeConfigService = {
  getConfig: async () => {
    const response = await axiosClient.get("/store-config");
    return response.data;
  },

  updateConfig: async (payload) => {
    const response = await axiosClient.put("/store-config", payload);
    return response.data;
  }
};

export default storeConfigService;