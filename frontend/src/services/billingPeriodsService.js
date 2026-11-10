import axiosClient from "../config/axios";

const billingPeriodService = {
  getBillingPeriods(params) {
    return axiosClient.get("/billing-periods", { params });
  },

  closeBillingPeriod(id) {
    return axiosClient.patch(`/billing-periods/${id}/close`);
  },

  deleteBillingPeriod(id) {
    return axiosClient.delete(`/billing-periods/${id}`);
  }
};

export default billingPeriodService;
