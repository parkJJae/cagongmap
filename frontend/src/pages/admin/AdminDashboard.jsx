// src/pages/admin/AdminDashboard.jsx
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    getFlaggedCafes,
    getAllCafesForAdmin,
    getReportDetail,
    markReviewed,
    deleteCafeByAdmin,
    adminLogout,
} from "../../api/AdminApi";
import { tokenStore } from "../../api/tokenStore";
import { REPORT_REASON_LABEL } from "../../api/reportReason";

export default function AdminDashboard() {
    const navigate = useNavigate();

    const [activeTab, setActiveTab] = useState("flagged"); // "flagged" | "all"

    const [flaggedCafes, setFlaggedCafes] = useState([]);
    const [allCafes, setAllCafes] = useState([]);

    const [selectedId, setSelectedId] = useState(null);
    const [reportDetail, setReportDetail] = useState(null); // 신고 사유 breakdown
    const [listError, setListError] = useState("");
    const [actionMsg, setActionMsg] = useState("");

    // 현재 탭 기준 리스트 + 선택된 카페 객체
    const currentList = activeTab === "flagged" ? flaggedCafes : allCafes;
    const selectedCafe = currentList.find((c) => {
        const id = activeTab === "flagged" ? c.cafeVisitId : c.id;
        return id === selectedId;
    });

    // ── 목록 로딩 ──
    const loadFlagged = async () => {
        try {
            const res = await getFlaggedCafes();
            setFlaggedCafes(res.data);
            setListError("");
        } catch (err) {
            setListError(err.message || "목록을 불러오지 못했습니다.");
        }
    };

    const loadAll = async () => {
        try {
            const res = await getAllCafesForAdmin();
            setAllCafes(res.data);
            setListError("");
        } catch (err) {
            setListError(err.message || "전체 카페를 불러오지 못했습니다.");
        }
    };

    // 첫 진입 + 탭 전환 시 해당 탭 데이터 로드
    useEffect(() => {
        if (activeTab === "flagged") loadFlagged();
        else loadAll();
        setSelectedId(null);
        setReportDetail(null);
        setActionMsg("");
    }, [activeTab]);

    // ── 카페 선택 ──
    const handleSelect = async (cafeId) => {
        setSelectedId(cafeId);
        setReportDetail(null);
        setActionMsg("");
        // 신고가 한 건이라도 있으면 사유별 breakdown 가져옴 (둘 다 공용)
        try {
            const res = await getReportDetail(cafeId);
            setReportDetail(res.data);
        } catch {
            // 무시 — 디테일 못 가져와도 카페 정보는 리스트에서 보임
        }
    };

    // ── 액션: 검토 완료 (flagged 전용) ──
    const handleReview = async () => {
        if (!selectedCafe) return;
        try {
            await markReviewed(selectedId);
            setActionMsg("검토 완료 처리했습니다.");
            await loadFlagged();
            setSelectedId(null);
            setReportDetail(null);
        } catch (err) {
            setActionMsg(err.message || "검토 완료 처리에 실패했습니다.");
        }
    };

    // ── 액션: 삭제 (양쪽 공통) ──
    const handleDelete = async () => {
        if (!selectedCafe) return;
        const name = activeTab === "flagged" ? selectedCafe.cafeName : selectedCafe.name;
        const ok = window.confirm(
            `"${name}" 카페 기록을 삭제할까요?\n신고 내역도 함께 삭제되며 되돌릴 수 없습니다.`
        );
        if (!ok) return;

        try {
            await deleteCafeByAdmin(selectedId);
            setActionMsg("카페를 삭제했습니다.");
            // 양쪽 다 영향 받으니 둘 다 재로딩
            if (activeTab === "flagged") await loadFlagged();
            else await loadAll();
            setSelectedId(null);
            setReportDetail(null);
        } catch (err) {
            setActionMsg(err.message || "삭제에 실패했습니다.");
        }
    };

    const handleLogout = async () => {
        try {
            await adminLogout(tokenStore.getRefreshToken());
        } catch { /* 멱등 */ }
        tokenStore.clear();
        navigate("/admin/login");
    };

    return (
        <div className="admin-page">
            <header className="admin-topbar">
                <h1 className="admin-topbar-title">🛠 신고 관리 대시보드</h1>
                <button className="home-btn secondary" onClick={handleLogout}>
                    로그아웃
                </button>
            </header>

            <div className="admin-body">
                {/* 왼쪽: 탭 + 리스트 */}
                <section className="admin-list">
                    <div className="admin-tabs">
                        <button
                            className={
                                "admin-tab" + (activeTab === "flagged" ? " admin-tab--active" : "")
                            }
                            onClick={() => setActiveTab("flagged")}
                        >
                            검토 필요 ({flaggedCafes.length})
                        </button>
                        <button
                            className={
                                "admin-tab" + (activeTab === "all" ? " admin-tab--active" : "")
                            }
                            onClick={() => setActiveTab("all")}
                        >
                            전체 카페
                        </button>
                    </div>

                    {listError && <p className="status error">{listError}</p>}

                    {activeTab === "flagged" ? (
                        <>
                            <p className="admin-section-sub">신고 수가 많은 순으로 표시됩니다.</p>
                            {flaggedCafes.length === 0 && !listError && (
                                <div className="admin-empty">검토가 필요한 카페가 없습니다. 👍</div>
                            )}
                            {flaggedCafes.map((cafe) => (
                                <button
                                    key={cafe.cafeVisitId}
                                    className={
                                        "admin-list-item" +
                                        (selectedId === cafe.cafeVisitId ? " admin-list-item--active" : "")
                                    }
                                    onClick={() => handleSelect(cafe.cafeVisitId)}
                                >
                                    <div className="admin-list-item-name">{cafe.cafeName}</div>
                                    <div className="admin-list-item-meta">
                                        <span className="admin-badge">신고 {cafe.reportCount}</span>
                                        <span className="admin-list-item-date">
                                            {new Date(cafe.createdAt).toLocaleDateString()}
                                        </span>
                                    </div>
                                </button>
                            ))}
                        </>
                    ) : (
                        <>
                            <p className="admin-section-sub">
                                최근 등록순. 이상한 글이 보이면 바로 삭제할 수 있습니다.
                            </p>
                            {allCafes.length === 0 && !listError && (
                                <div className="admin-empty">등록된 카페가 없습니다.</div>
                            )}
                            {allCafes.map((cafe) => (
                                <button
                                    key={cafe.id}
                                    className={
                                        "admin-list-item" +
                                        (selectedId === cafe.id ? " admin-list-item--active" : "")
                                    }
                                    onClick={() => handleSelect(cafe.id)}
                                >
                                    <div className="admin-list-item-name">{cafe.name}</div>
                                    <div className="admin-list-item-meta">
                                        {cafe.flaggedForReview ? (
                                            <span className="admin-badge admin-badge--warn">
                                                검토 대기 ({cafe.reportCount})
                                            </span>
                                        ) : cafe.reportCount > 0 ? (
                                            <span className="admin-badge">신고 {cafe.reportCount}</span>
                                        ) : (
                                            <span className="admin-badge">정상</span>
                                        )}
                                        <span className="admin-list-item-date">
                                            {new Date(cafe.createdAt).toLocaleDateString()}
                                        </span>
                                    </div>
                                </button>
                            ))}
                        </>
                    )}
                </section>

                {/* 오른쪽: 상세 */}
                <section className="admin-detail">
                    {!selectedCafe ? (
                        <div className="admin-detail-empty">
                            왼쪽에서 카페를 선택하면 상세가 표시됩니다.
                        </div>
                    ) : (
                        <>
                            <h2 className="admin-detail-title">
                                {activeTab === "flagged" ? selectedCafe.cafeName : selectedCafe.name}
                            </h2>

                            {/* 전체 탭에서만 카페 정보 풀로 노출 (flagged 탭은 이미 신고 위주라 생략) */}
                            {activeTab === "all" && (
                                <div className="admin-cafe-info">
                                    <p className="fake-map-address">{selectedCafe.address}</p>
                                    <div className="fake-map-meta">
                                        {selectedCafe.rating && <span>⭐ {selectedCafe.rating} / 5</span>}
                                        {selectedCafe.hasOutlet && <span>🔌 콘센트</span>}
                                        {selectedCafe.wifiSpeed && <span>📶 {selectedCafe.wifiSpeed}</span>}
                                        {selectedCafe.registeredBy && <span>👤 {selectedCafe.registeredBy}</span>}
                                    </div>
                                    {selectedCafe.memo && (
                                        <div className="fake-map-memo">
                                            <span className="fake-map-memo-label">메모</span>
                                            <span>{selectedCafe.memo}</span>
                                        </div>
                                    )}
                                </div>
                            )}

                            <div className="admin-detail-summary">
                                <span className="admin-badge">
                                    총 신고 {reportDetail?.reportCount ?? selectedCafe.reportCount ?? 0}건
                                </span>
                                {(reportDetail?.flaggedForReview ?? selectedCafe.flaggedForReview) && (
                                    <span className="admin-badge admin-badge--warn">검토 대기</span>
                                )}
                            </div>

                            {reportDetail && Object.keys(reportDetail.reasonCounts).length > 0 && (
                                <>
                                    <h3 className="admin-detail-subtitle">사유별 신고</h3>
                                    <ul className="admin-reason-list">
                                        {Object.entries(reportDetail.reasonCounts).map(([reason, count]) => (
                                            <li key={reason} className="admin-reason-item">
                                                <span>{REPORT_REASON_LABEL[reason] || reason}</span>
                                                <span className="admin-badge">{count}</span>
                                            </li>
                                        ))}
                                    </ul>
                                </>
                            )}

                            <div className="admin-actions">
                                {activeTab === "flagged" && (
                                    <button className="home-btn primary" onClick={handleReview}>
                                        검토 완료 (정상 글로 판단)
                                    </button>
                                )}
                                <button className="admin-btn-danger" onClick={handleDelete}>
                                    카페 삭제
                                </button>
                            </div>
                        </>
                    )}

                    {actionMsg && (
                        <p className="status success" style={{ marginTop: 16 }}>
                            {actionMsg}
                        </p>
                    )}
                </section>
            </div>
        </div>
    );
}