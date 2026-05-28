
const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";

export const tokenStore = {
    getAccessToken() {
        return localStorage.getItem(ACCESS_TOKEN_KEY);
    },
    getRefreshToken() {
        return localStorage.getItem(REFRESH_TOKEN_KEY);
    },
    // 로그인/갱신 성공 시 AT+RT 동시 저장
    setTokens(accessToken, refreshToken) {
        localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    },
    // 로그아웃 시 둘 다 제거
    clear() {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
    },
    // 로그인 여부 간단 체크 (AT 존재 여부로 판단)
    isLoggedIn() {
        return !!localStorage.getItem(ACCESS_TOKEN_KEY);
    },
};