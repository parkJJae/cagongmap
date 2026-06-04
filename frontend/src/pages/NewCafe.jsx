// src/pages/NewCafe.jsx
import { useRef, useState } from "react";
import { createCafe } from "../api/CafeApi";
import KakaoSearchModal from "../components/KakaoSearchModal";

export default function NewCafe({ onBack }) {
    const [form, setForm] = useState({
        name: "",
        address: "",
        rating: 5,
        priceLevel: 2,
        hasOutlet: true,
        wifiSpeed: "FAST",
        studyScore: 3,
        memo: "",
        nickname: "",
        lat: null,
        lng: null,
    });

    const [status, setStatus] = useState(null);
    const [errorMsg, setErrorMsg] = useState("");
    const [isKakaoOpen, setIsKakaoOpen] = useState(false);

    // 더블클릭 방지용 잠금장치
    // setState는 비동기라 status가 "loading"으로 반영되기 전에 두 번째 클릭이 통과될 수 있음
    // useRef는 동기적으로 즉시 반영되므로 함수 진입 시점에 확실히 차단 가능
    const isSubmittingRef = useRef(false);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const handlePlaceSelect = (place) => {
        setForm((prev) => ({
            ...prev,
            name: place.name || prev.name,
            address: place.roadAddress || place.address || prev.address,
            lat: place.lat ?? prev.lat,
            lng: place.lng ?? prev.lng,
        }));
        setIsKakaoOpen(false);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // 이미 제출 중이면 즉시 차단
        if (isSubmittingRef.current) return;
        isSubmittingRef.current = true;

        setErrorMsg("");
        setStatus("loading");

        try {
            const payload = {
                name: form.name,
                address: form.address,
                rating: Number(form.rating) || null,
                priceLevel: Number(form.priceLevel) || null,
                hasOutlet: form.hasOutlet,
                wifiSpeed: form.wifiSpeed,
                studyScore: Number(form.studyScore) || null,
                memo: form.memo,
                nickname: form.nickname,
                lat: form.lat ?? 37.4979,
                lng: form.lng ?? 127.0276,
                visitedAt: new Date().toISOString(),
            };

            await createCafe(payload);
            setStatus("success");
        } catch (err) {
            console.error(err);
            setErrorMsg(err.message || "등록 중 오류가 발생했어요. 잠시 후 다시 시도해주세요.");
            setStatus("error");
        } finally {
            // 성공/실패 무관하게 잠금 해제
            isSubmittingRef.current = false;
        }
    };

    return (
        <div className="new-page">
            <header className="top-bar">
                {onBack && (
                    <button className="top-back" onClick={onBack}>
                        ← 홈으로
                    </button>
                )}
                <span className="top-title">카페 등록하기</span>
            </header>

            <main className="new-main">
                <form className="new-form" onSubmit={handleSubmit}>
                    <div>
                        <h2>새 카페 기록 추가</h2>
                        <p className="new-sub">
                            최소 정보만 적어도 괜찮아요. 나중에 다시 와서 수정해도 돼요.
                        </p>
                    </div>

                    <label className="field">
                        <div className="field-label-row">
                            <span>카페 이름</span>
                            <button
                                type="button"
                                className="kakao-inline-btn"
                                onClick={() => setIsKakaoOpen(true)}
                            >
                                🔍 카카오맵에서 찾기
                            </button>
                        </div>
                        <input
                            name="name"
                            value={form.name}
                            onChange={handleChange}
                            placeholder="예) 스타벅스 강남역점"
                            required
                        />
                    </label>

                    <label className="field">
                        <span>주소</span>
                        <input
                            name="address"
                            value={form.address}
                            onChange={handleChange}
                            placeholder="예) 서울 강남구 강남대로 123"
                            required
                        />
                    </label>

                    <div className="field-row">
                        <label className="field">
                            <span>만족도 (1~5)</span>
                            <input
                                type="number"
                                min="1"
                                max="5"
                                name="rating"
                                value={form.rating}
                                onChange={handleChange}
                            />
                        </label>

                        <label className="field">
                            <span>가격대 (1~3)</span>
                            <input
                                type="number"
                                min="1"
                                max="3"
                                name="priceLevel"
                                value={form.priceLevel}
                                onChange={handleChange}
                            />
                        </label>
                    </div>

                    <div className="field-row">
                        <label className="field">
                            <span>공부 점수 (1~5)</span>
                            <input
                                type="number"
                                min="1"
                                max="5"
                                name="studyScore"
                                value={form.studyScore}
                                onChange={handleChange}
                            />
                        </label>

                        <label className="field">
                            <span>와이파이 속도</span>
                            <select
                                name="wifiSpeed"
                                value={form.wifiSpeed}
                                onChange={handleChange}
                            >
                                <option value="FAST">FAST</option>
                                <option value="NORMAL">NORMAL</option>
                                <option value="SLOW">SLOW</option>
                            </select>
                        </label>
                    </div>

                    <label className="field checkbox">
                        <input
                            type="checkbox"
                            name="hasOutlet"
                            checked={form.hasOutlet}
                            onChange={handleChange}
                        />
                        🔌 콘센트 있음
                    </label>

                    <label className="field">
                        <span>닉네임</span>
                        <input
                            name="nickname"
                            value={form.nickname}
                            onChange={handleChange}
                            placeholder="기록을 남기는 사람"
                            required
                        />
                    </label>

                    <label className="field">
                        <span>메모</span>
                        <textarea
                            name="memo"
                            value={form.memo}
                            onChange={handleChange}
                            rows={3}
                            placeholder="좌석 분위기, 소음, 추천 메뉴 등 자유롭게 적어주세요"
                        />
                    </label>

                    <button
                        className="home-btn primary"
                        type="submit"
                        disabled={status === "loading"}
                    >
                        {status === "loading" ? "등록 중..." : "기록 저장하기"}
                    </button>

                    {status === "success" && (
                        <p className="status success">등록이 완료되었어요!</p>
                    )}
                    {status === "error" && (
                        <p className="status error">
                            {errorMsg}
                        </p>
                    )}
                </form>
            </main>

            {isKakaoOpen && (
                <KakaoSearchModal
                    onSelect={handlePlaceSelect}
                    onClose={() => setIsKakaoOpen(false)}
                />
            )}
        </div>
    );
}