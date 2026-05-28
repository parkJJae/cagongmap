
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { adminLogin } from "../../api/AdminApi";
import { tokenStore } from "../../api/tokenStore";

export default function AdminLogin() {
    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [status, setStatus] = useState(null); // null | "loading" | "error"
    const [errorMsg, setErrorMsg] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setStatus("loading");
        setErrorMsg("");

        try {
            const res = await adminLogin(username, password);
            // adminLogin은 순수 axios라 인터셉터를 안 탐 → { success, data, message } 원형.
            // res.data = 응답 body, res.data.data = ApiResponse의 data 필드.
            const { accessToken, refreshToken } = res.data.data;

            tokenStore.setTokens(accessToken, refreshToken);
            navigate("/admin"); // 로그인 성공 → 대시보드로
        } catch (err) {
            // 백엔드가 계정 열거 방지로 통일한 메시지를 그대로 노출.
            const message =
                err.response?.data?.message ||
                "로그인 중 오류가 발생했습니다.";
            setErrorMsg(message);
            setStatus("error");
        }
    };

    return (
        <div className="admin-login-page">
            <form className="admin-login-form" onSubmit={handleSubmit}>
                <h2>관리자 로그인</h2>
                <p className="admin-login-sub">
                    카공맵 운영자 전용 페이지입니다.
                </p>

                <label className="field">
                    <span>아이디</span>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        placeholder="관리자 아이디"
                        autoComplete="username"
                        required
                    />
                </label>

                <label className="field">
                    <span>비밀번호</span>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="비밀번호"
                        autoComplete="current-password"
                        required
                    />
                </label>

                <button
                    className="home-btn primary"
                    type="submit"
                    disabled={status === "loading"}
                >
                    {status === "loading" ? "로그인 중..." : "로그인"}
                </button>

                {status === "error" && (
                    <p className="status error">{errorMsg}</p>
                )}
            </form>
        </div>
    );
}