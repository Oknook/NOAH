import { BrowserRouter, Routes, Route } from "react-router-dom";
import ScrollToTop from "./components/common/ScrollToTop";
import App from "./App";
import WelcomePage from "./pages/WelcomgPage";
import JoinPage from "./pages/JoinPage";
import HomePage from "./pages/HomePage";
import TransferPage from "./pages/TransferPage";
import TripCreatePage from "./pages/TripCreatePage";
import TripPage from "./pages/TripPage";
import BankBookCreatePage from "./pages/BankBookCreatePage";
import GoalPage from "./pages/GoalPage";
import NotificationPage from "./pages/NotificationPage";
import ParticipantManagementPage from "./pages/ParticipantManagementPage";
import ExchangePage from "./pages/ExchangePage";
import SpendingManagemnetPage from "./pages/SpendingManagementPage";
import PlanningCreatePage from "./pages/PlanningCreatePage";
import PlanningPage from "./pages/PlanningPage";
import MyPage from "./pages/MyPage";
import TravelHistoryPage from "./pages/TravelHistoryPage";
import AutomaticWithdrawalSettingPage from "./pages/AutomaticWithdrawalSettingPage";

export default function Main() {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <Routes>
        <Route path="/" element={<App />}>
          <Route index element={<WelcomePage />} />
          <Route path="join" element={<JoinPage />} />
          <Route path="home" element={<HomePage />} />
          <Route path="transfer" element={<TransferPage />} />
          <Route path="tripcreate" element={<TripCreatePage />} />
          <Route path="trip/:tripid">
            <Route index element={<TripPage />} />
            <Route path="bankbookcreate" element={<BankBookCreatePage />} />
            <Route path="goal" element={<GoalPage />} />
            <Route
              path="participantmanageMent"
              element={<ParticipantManagementPage />}
            />
            <Route path="exchange" element={<ExchangePage />} />
            <Route
              path="spendingmanagement"
              element={<SpendingManagemnetPage />}
            />
            <Route path="planningcreate" element={<PlanningCreatePage />} />
            <Route path="planning" element={<PlanningPage />} />
          </Route>
          <Route path="notification" element={<NotificationPage />} />
          <Route path="mypage" element={<MyPage />} />
          <Route path="travelhistory" element={<TravelHistoryPage />} />
          <Route
            path="automaticwithdrawalsetting"
            element={<AutomaticWithdrawalSettingPage />}
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}