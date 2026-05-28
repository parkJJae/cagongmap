import { useNavigate } from "react-router-dom";
import { adminLogout } from "../../api/AdminApi";
import { tokenStore } from "../../api/tokenStore";

export default function AdminDashboard() {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await adminLogout(tokenStore.getRefreshToken());
        } catch {
            // 로그아웃은 실패해도 클라이언트 토큰은 지움 (멱등)
        }
        tokenStore.clear();
        navigate("/admin/login");
    };

    return (
        <div style={{ padding: 40 }}>
            <h2>관리자 대시보드 (임시)</h2>
            <p>로그인이 성공하면 이 화면이 보입니다.</p>
            <button className="home-btn secondary" onClick={handleLogout}>
                로그아웃
            </button>
        </div>
    );
}