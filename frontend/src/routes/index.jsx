import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { useSelector } from "react-redux";
import MainLayout from "../layouts/mainLayout/MainLayout";

import Login from "../pages/login/Login";
import Dashboard from "../pages/dashboard/Dashboard";
import Profile from "../pages/profile/Profile"
import ImportInitialDebt from "../pages/importInitialDebt/ImportInitialDebt";
import InitialDebtList from "../pages/initialDebtList/InitialDebtList";
import PaidCustomerImport from "../pages/paidCustomerImport/PaidCustomerImport";
import CollectionProgress from "../pages/collectionProgress/CollectionProgress";
import ConsultantManagement from "../pages/consultant/ConsultantManagement";
import StoreConfig from "../pages/storeConfig/StoreConfig";
import ProtectedRoute from "./ProtectedRoute";
import ConsultantProgress from "../pages/consultantProgress/ConsultantProgress";

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

function HomeByRole() {
  const user = useSelector((state) => state.auth.user);
  const role = getNormalizedRole(user);

  if (role === "USER" || role === "CONSULTANT") {
    return <Dashboard />;
  }

  return <Dashboard />;
}

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        {/* <Route path="/403" element={<Forbidden />} /> */}

        <Route
          element={
            <ProtectedRoute>
            <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/" element={<HomeByRole />} />
          <Route
            path="/profile"
            element={
              <Profile />
            }
          />
          <Route
            path="/importInitialDebt"
            element={
              <ImportInitialDebt />
            }
          />
           <Route
            path="/initialDebt"
            element={
              <InitialDebtList />
            }
          />
          <Route
           path="/paid-customer-import"
           element={
            <PaidCustomerImport />
           }
          />
          <Route
           path="/collectionProgress"
           element={
            <CollectionProgress />
           }
          />
          <Route
          path="/consultants"
          element={<ConsultantManagement />}
          />
          <Route
          path="store-config"
          element={
            <StoreConfig />
          }
          />
          <Route
           path="/consultant-progress"
           element={
            <ConsultantProgress />
           }
           />
        </Route>


      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;
