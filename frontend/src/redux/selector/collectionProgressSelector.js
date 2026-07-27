export const selectCollectionProgress =
  (state) =>
    state.collectionProgress;

export const selectRecords =
  (state) =>
    state.collectionProgress.records;

export const selectLoading =
  (state) =>
    state.collectionProgress.loading;

export const selectConsultants =
  (state) =>
    state.collectionProgress.consultants;

export const selectBillingPeriods =
  (state) =>
    state.collectionProgress.billingPeriods;

export const selectFilters =
  (state) =>
    state.collectionProgress.filters;

export const selectPagination =
  (state) => ({
    page:
      state.collectionProgress.page,

    size:
      state.collectionProgress.size,

    totalPages:
      state.collectionProgress.totalPages,

    totalElements:
      state.collectionProgress.totalElements
  });