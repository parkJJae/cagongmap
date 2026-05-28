import { Navigate } from "react-router-dom";
import { tokenStore } from "../../api/tokenStore";

export default function ProtectedRoute({ children }) {
    if (!tokenStore.isLoggedIn()) {
        // replace: 뒤로가기 눌렀을 때 다시 /admin으로 안 돌아오게 히스토리 교체
        return <Navigate to="/admin/login" replace />;
    }
    return children;
}