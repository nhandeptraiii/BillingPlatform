import axiosClient from "../config/axios";

export const getCollectionProgress = async (
  params
) => {

  const response =
    await axiosClient.get(
      "/records",
      {
        params
      }
    );

  return response.data.data;
};
export const printBill = async (
  recordId,
  collectedAmount
) => {

  const response =
    await axiosClient.patch(
      `/records/${recordId}/print-bill`,
      {
        collectedAmount
      }
    );

  return response.data;
};

export const markDebt = async (
  recordId
) => {

  const response =
    await axiosClient.patch(
      `/records/${recordId}/mark-debt`
    );

  return response.data;
};

export const exportCollectionProgress =
  async (params) => {

    const response =
      await axiosClient.get(
        "/records/export",
        {
          params,

          responseType:
            "blob"
        }
      );

    return response.data;
  };

  export const bulkMarkDebt = async (params) => {

  const response =
    await axiosClient.patch(
      "/records/bulk-mark-debt/filter",
      null,
      {
        params
      }
    );

  return response.data.data;
};