import axiosClient from "../config/axios";

const paidCustomerImportService = {

  importReconciliation(
    month,
    year,
    file
  ) {

    const formData =
      new FormData();

    formData.append(
      "file",
      file
    );

    return axiosClient.post(
      `/records/import-reconciliation?month=${month}&year=${year}`,
      formData,
      {
        headers: {
          "Content-Type":
            "multipart/form-data"
        }
      }
    );

  },

  downloadTemplate() {

    return axiosClient.get(
      "/records/import-reconciliation/template",
      {
        responseType: "blob"
      }
    );

  }

};

export default paidCustomerImportService;