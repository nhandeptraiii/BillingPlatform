export const getRole = (user) => String(
  user?.role || user?.roleName || user?.roles?.[0]?.name || user?.roles?.[0] || ""
).trim().toUpperCase();

export const hasRole = (user, ...roles) => roles.includes(getRole(user));

export const canManageUsers = (user) => hasRole(user, "ADMIN", "MANAGER", "NVKD");
export const canImportEmployees = (user) => hasRole(user, "ADMIN", "MANAGER");
export const canManageTargets = (user) => hasRole(user, "ADMIN", "MANAGER");
