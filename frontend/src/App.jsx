import { BrowserRouter, Routes, Route } from "react-router-dom";
import UserApp from "./pages/UserApp";
import AdminLogin from "./pages/admin/AdminLogin";
import AdminDashboard from "./pages/admin/AdminDashboard";
import ProtectedRoute from "./pages/admin/ProtectedRoute";

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* 관리자 영역 */}
                <Route path="/admin/login" element={<AdminLogin />} />
                <Route
                    path="/admin"
                    element={
                        <ProtectedRoute>
                            <AdminDashboard />
                        </ProtectedRoute>
                    }
                />
                {/* 일반 사용자 영역 — 기존 useState 전환 그대로 */}
                <Route path="/*" element={<UserApp />} />
            </Routes>
        </BrowserRouter>
    );
}