export const getRole = (user) => String(
  user?.role || user?.roleName || user?.roles?.[0]?.name || user?.roles?.[0] || ""
).trim().toUpperCase();

export const hasRole = (user, ...roles) => roles.includes(getRole(user));

export const canManageUsers = (user) => hasRole(user, "ADMIN", "MANAGER", "NVKD");
export const canImportEmployees = (user) => hasRole(user, "ADMIN", "MANAGER");
export const canManageTargets = (user) => hasRole(user, "ADMIN", "MANAGER");

export const roleLabels = {
  ADMIN: "Quản trị viên",
  MANAGER: "Giám đốc khu vực",
  NVKD: "Nhân viên kinh doanh địa bàn",
  CONSULTANT: "Tư vấn viên",
};
