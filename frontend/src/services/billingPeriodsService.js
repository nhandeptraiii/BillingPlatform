import axiosClient from "../config/axios";

const billingPeriodService = {

  getBillingPeriods(params) {

    return axiosClient.get(
      "/billing-periods",
      {
        params
      }
    );

  }

};

export default billingPeriodService;