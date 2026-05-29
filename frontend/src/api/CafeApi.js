import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080/api",
});

// 백엔드 응답이 { success, data, message } 형식이라
// data 필드만 꺼내서 호출하는 쪽이 편하게 쓰도록 처리
api.interceptors.response.use(
    (response) => {
        if (
            response.data &&
            typeof response.data === "object" &&
            "data" in response.data
        ) {
            response.data = response.data.data;
        }
        return response;
    },
    (error) => {
        const message =
            error.response?.data?.message ||
            error.message ||
            "요청 중 오류가 발생했습니다.";
        return Promise.reject(new Error(message));
    }
);

// 카페 등록
export const createCafe = (data) => api.post("/cafes", data);

// 카페 전체 조회
export const getAllCafes = () => api.get("/cafes");

// 카페 단건 조회
export const getCafe = (id) => api.get(`/cafes/${id}`);

// 카페 수정
export const updateCafe = (id, data) => api.put(`/cafes/${id}`, data);

// 카페 삭제
export const deleteCafe = (id) => api.delete(`/cafes/${id}`);

// 카페 신고 (비회원, IP 기반 중복 방지)
export const reportCafe = (cafeVisitId, reason) =>
    api.post(`/cafes/${cafeVisitId}/reports`, { reason });

export default api;