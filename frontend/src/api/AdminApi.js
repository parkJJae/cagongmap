import axios from "axios";
import { tokenStore } from "./tokenStore";

const adminApi = axios.create({
    baseURL: "http://localhost:8080/api",
});

// ── 요청 인터셉터: 매 요청에 Bearer AT 부착 ──
adminApi.interceptors.request.use((config) => {
    const token = tokenStore.getAccessToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// ── 응답 인터셉터: 401 → RT로 자동 갱신 → 원요청 재시도 ──

// 동시에 여러 요청이 401을 받았을 때, refresh는 딱 한 번만 돌게 하는 잠금장치.
// 백엔드가 RT Rotation이라 refresh가 두 번 돌면 두 번째는 무조건 실패함.
let isRefreshing = false;
// refresh 끝나길 기다리는 요청들의 콜백 큐.
let pendingQueue = [];

// refresh 완료 후 대기 중이던 요청들을 깨움.
const flushQueue = (error, newToken) => {
    pendingQueue.forEach(({ resolve, reject }) => {
        if (error) reject(error);
        else resolve(newToken);
    });
    pendingQueue = [];
};

// 토큰 다 날리고 로그인 페이지로 보내는 공통 처리.
const forceLogout = () => {
    tokenStore.clear();
    // 라우터 밖(인터셉터)이라 navigate를 못 써서 location으로 이동.
    window.location.href = "/admin/login";
};

adminApi.interceptors.response.use(
    (response) => {
        // 기존 CafeApi처럼 { success, data, message }에서 data만 꺼내 반환.
        if (
            response.data &&
            typeof response.data === "object" &&
            "data" in response.data
        ) {
            response.data = response.data.data;
        }
        return response;
    },
    async (error) => {
        const originalRequest = error.config;
        const status = error.response?.status;

        // 401이 아니거나, 이미 재시도한 요청이면 그냥 에러 반환.
        if ((status !== 401 && status !== 403) || originalRequest._retry) {
            const message =
                error.response?.data?.message ||
                error.message ||
                "요청 중 오류가 발생했습니다.";
            return Promise.reject(new Error(message));
        }

        // refresh 자체가 401이면 (RT 만료/무효) → 로그아웃.
        if (originalRequest.url?.includes("/auth/refresh")) {
            forceLogout();
            return Promise.reject(new Error("세션이 만료되었습니다. 다시 로그인해주세요."));
        }

        originalRequest._retry = true;

        // 이미 다른 요청이 refresh 중이면, 큐에 넣고 기다림.
        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                pendingQueue.push({ resolve, reject });
            }).then((newToken) => {
                originalRequest.headers.Authorization = `Bearer ${newToken}`;
                return adminApi(originalRequest);
            });
        }

        // 내가 refresh 담당.
        isRefreshing = true;
        const refreshToken = tokenStore.getRefreshToken();

        if (!refreshToken) {
            forceLogout();
            return Promise.reject(new Error("로그인이 필요합니다."));
        }

        try {
            // refresh는 AT가 필요 없고, 인터셉터 꼬임 방지를 위해 순수 axios 사용.
            const res = await axios.post(
                "http://localhost:8080/api/auth/refresh",
                { refreshToken }
            );
            // 이 응답은 인터셉터를 안 거치므로 { success, data, message } 원형 그대로.
            const { accessToken, refreshToken: newRefreshToken } = res.data.data;

            tokenStore.setTokens(accessToken, newRefreshToken);
            isRefreshing = false;
            flushQueue(null, accessToken); // 대기 요청들 깨우기

            // 원래 요청 재시도.
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            return adminApi(originalRequest);
        } catch (refreshError) {
            isRefreshing = false;
            flushQueue(refreshError, null);
            forceLogout();
            return Promise.reject(new Error("세션이 만료되었습니다. 다시 로그인해주세요."));
        }
    }
);

// ── 관리자 API 호출 함수 ──

// 로그인 (인증 전이라 순수 axios — adminApi 인터셉터 불필요)
export const adminLogin = (username, password) =>
    axios.post("http://localhost:8080/api/auth/login", { username, password });

// 로그아웃 (RT 폐기)
export const adminLogout = (refreshToken) =>
    axios.post("http://localhost:8080/api/auth/logout", { refreshToken });

// 검토 필요 카페 목록
export const getFlaggedCafes = () => adminApi.get("/admin/reports");

// 특정 카페 신고 상세
export const getReportDetail = (cafeVisitId) =>
    adminApi.get(`/admin/reports/${cafeVisitId}`);

// 검토 완료 처리
export const markReviewed = (cafeVisitId) =>
    adminApi.post(`/admin/reports/${cafeVisitId}/review`);

// 악성 카페 삭제
export const deleteCafeByAdmin = (cafeVisitId) =>
    adminApi.delete(`/admin/cafes/${cafeVisitId}`);

export default adminApi;