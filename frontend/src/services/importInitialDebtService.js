import axiosClient from "../config/axios";

const importInitialDebtService = {

  // tải file mẫu
  downloadTemplate: async () => {

    const response = await axiosClient.get(
      "/billing-periods/import/template",
      {
        responseType: "blob"
      }
    );

    return response;
  },

  // import file excel
  importInitialDebt: async (file) => {

    const formData = new FormData();

    formData.append("file", file);

    const response = await axiosClient.post(
      "/billing-periods/import",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data"
        }
      }
    );

    return response.data;
  }
};

export default importInitialDebtService;