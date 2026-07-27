import { Navigate } from "react-router-dom";
import { toast } from "react-toastify";

const getNormalizedRole = (user) => {

  const directRole =
    user?.role || user?.roleName;

  if (
    typeof directRole === "string" &&
    directRole.trim()
  ) {
    return directRole.trim().toUpperCase();
  }

  const firstRole = user?.roles?.[0];

  if (
    typeof firstRole === "string"
  ) {
    return firstRole.trim().toUpperCase();
  }

  if (
    firstRole?.name
  ) {
    return firstRole.name.trim().toUpperCase();
  }

  return "";
};

const ProtectedRoute = ({ children }) => {

  const token =
    localStorage.getItem("token");


if (!token) {
  return <Navigate to="/login" replace />;
}

const userStr = localStorage.getItem("user");

if (!userStr) {
  return <Navigate to="/login" replace />;
}

let user = null;

try {
  user = JSON.parse(userStr);
} catch {
  localStorage.removeItem("token");
  localStorage.removeItem("user");

  return <Navigate to="/login" replace />;
}

  const role =
    getNormalizedRole(user);

    if (role === "CONSULTANT") {

  localStorage.removeItem("token");
  localStorage.removeItem("user");

  toast.warning(
    "Tài khoản CONSULTANT chỉ được phép sử dụng trên Mobile App."
  );

  return <Navigate to="/login" replace />;
}

  if (role !== "ADMIN" &&
  role !== "MANAGER") {

    localStorage.removeItem("token");
    localStorage.removeItem("user");

    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;