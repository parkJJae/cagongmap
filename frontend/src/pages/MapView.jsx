// src/pages/MapView.jsx
import { useEffect, useMemo, useRef, useState } from "react";
import { getAllCafes } from "../api/CafeApi";
import { loadKakaoMaps } from "../lib/kakaoLoader";
import ReportModal from "../components/ReportModal";

export default function MapView() {
    const [cafes, setCafes] = useState([]);
    const [selected, setSelected] = useState(null);
    const [search, setSearch] = useState("");
    const [reportTarget, setReportTarget] = useState(null);

    // 카카오 지도 관련
    const mapRef = useRef(null);          // 실제 지도 div
    const [map, setMap] = useState(null); // kakao.maps.Map 인스턴스
    const markerRef = useRef(null);       // 선택된 카페 마커 1개만 관리

    // ─────────────────────────────────────
    // 1. 첫 로딩 시 전체 카페 목록 불러오기
    // ─────────────────────────────────────
    useEffect(() => {
        getAllCafes()
            .then((res) => {
                const list = res.data || [];
                setCafes(list);
                if (list.length > 0) {
                    setSelected(list[0]);
                }
            })
            .catch((err) => {
                console.error("카페 목록 불러오기 실패:", err);
            });
    }, []);

    // ─────────────────────────────────────
    // 2. 카카오 지도 초기화
    // ─────────────────────────────────────
    useEffect(() => {
        let mapInstance = null;

        loadKakaoMaps()
            .then((kakao) => {
                if (!mapRef.current) return;

                const center = new kakao.maps.LatLng(37.5665, 126.9780); // 서울 시청 근처
                mapInstance = new kakao.maps.Map(mapRef.current, {
                    center,
                    level: 5,
                });
                setMap(mapInstance);
            })
            .catch((err) => {
                console.error("카카오 지도 로드 실패:", err);
            });

        return () => {
            // 필요하면 언마운트 시 정리 로직 추가
        };
    }, []);

    // ─────────────────────────────────────
    // 3. 선택된 카페가 바뀔 때마다 지도 위치/마커 갱신
    // ─────────────────────────────────────
    useEffect(() => {
        if (!map || !selected) return;
        if (!selected.lat || !selected.lng) return;

        const kakao = window.kakao;
        const pos = new kakao.maps.LatLng(selected.lat, selected.lng);

        if (!markerRef.current) {
            markerRef.current = new kakao.maps.Marker({
                map,
                position: pos,
            });
        } else {
            markerRef.current.setPosition(pos);
            markerRef.current.setMap(map);
        }

        map.panTo(pos);
    }, [map, selected]);

    // ─────────────────────────────────────
    // 4. 검색어 기반 필터링
    // ─────────────────────────────────────
    const filteredCafes = useMemo(() => {
        if (!search.trim()) return cafes;

        const q = search.trim().toLowerCase();
        return cafes.filter((cafe) => {
            const name = cafe.name?.toLowerCase() ?? "";
            const addr = cafe.address?.toLowerCase() ?? "";
            return name.includes(q) || addr.includes(q);
        });
    }, [cafes, search]);

    useEffect(() => {
        if (
            filteredCafes.length > 0 &&
            (!selected || !filteredCafes.some((c) => c.id === selected.id))
        ) {
            setSelected(filteredCafes[0]);
        }
    }, [filteredCafes, selected]);

    // ─────────────────────────────────────
    // 5. 렌더링
    // ─────────────────────────────────────
    return (
        <div className="map-page">
            {/* 왼쪽 사이드바 */}
            <section className="map-sidebar">
                <div className="top-bar" style={{ paddingLeft: 0, paddingRight: 0 }}>
                    <button
                        type="button"
                        className="top-back"
                        onClick={() => {
                            window.location.href = "/";
                        }}
                    >
                        ← 홈으로
                    </button>
                    <span className="top-title">지도로 한눈에 보기</span>
                </div>

                <h2 className="sidebar-title">카페 방문 기록 지도</h2>
                <p className="sidebar-sub">
                    등록한 카페들을 지도 위에서 한 번에 볼 수 있어요.
                    <br />
                    이름이나 지점으로 검색해서 콘센트·와이파이·공부 분위기까지 빠르게 확인해보세요.
                </p>

                <div className="map-search-row">
                    <input
                        className="map-search-input"
                        type="text"
                        placeholder="예) 스타벅스 강남역점, 투썸플레이스 평택역점..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>

                <div className="sidebar-list">
                    {filteredCafes.map((cafe) => (
                        <button
                            key={cafe.id}
                            className={
                                "cafe-item" +
                                (selected && selected.id === cafe.id ? " cafe-item-active" : "")
                            }
                            onClick={() => setSelected(cafe)}
                        >
                            <div className="cafe-name-row">
                                <span className="cafe-name">{cafe.name}</span>
                                {cafe.rating && (
                                    <span className="cafe-rating">★ {cafe.rating}</span>
                                )}
                            </div>
                            <div className="cafe-address">{cafe.address}</div>
                            <div className="cafe-meta">
                                {cafe.hasOutlet && <span>🔌 콘센트 있음</span>}
                                {cafe.wifiSpeed && <span>📶 {cafe.wifiSpeed}</span>}
                                {cafe.registeredBy && (
                                    <span>👤 {cafe.registeredBy}</span>
                                )}
                            </div>
                        </button>
                    ))}

                    {filteredCafes.length === 0 && (
                        <div className="cafe-empty">
                            검색 결과가 없어요.
                            <br />
                            다른 이름이나 지점으로 검색해보세요.
                        </div>
                    )}
                </div>
            </section>

            {/* 오른쪽: 선택된 카페 카드 + 실제 카카오 지도 */}
            <section className="map-container-wrapper">
                <div className="selected-cafe-card">
                    {selected ? (
                        <>
                            <div className="fake-map-chip">선택된 카페</div>

                            <h3 className="fake-map-title">{selected.name}</h3>
                            <p className="fake-map-address">{selected.address}</p>

                            <div className="fake-map-meta">
                                {selected.rating && (
                                    <span>⭐ {selected.rating} / 5</span>
                                )}
                                {selected.hasOutlet && <span>🔌 콘센트</span>}
                                {selected.wifiSpeed && <span>📶 {selected.wifiSpeed}</span>}
                            </div>

                            {selected.memo && (
                                <div className="fake-map-memo">
                                    <span className="fake-map-memo-label">메모</span>
                                    <span>{selected.memo}</span>
                                </div>
                            )}

                            {selected.registeredBy && (
                                <div className="fake-map-footer">
                                    {selected.registeredBy} 님의 기록
                                </div>
                            )}

                            <button
                                className="report-trigger"
                                type="button"
                                onClick={() => setReportTarget(selected)}
                            >
                                🚩 이 카페 신고하기
                            </button>
                        </>
                    ) : (
                        <div className="fake-map-card-empty">
                            아직 선택된 카페가 없어요.
                            <br />
                            왼쪽 목록에서 카페를 골라보세요.
                        </div>
                    )}
                </div>

                <div className="map-real-container">
                    <div ref={mapRef} className="real-map" />
                </div>
            </section>

            {reportTarget && (
                <ReportModal
                    cafe={reportTarget}
                    onClose={() => setReportTarget(null)}
                />
            )}
        </div>
    );
}