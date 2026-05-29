// src/pages/admin/AdminDashboard.jsx
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    getFlaggedCafes,
    getReportDetail,
    markReviewed,
    deleteCafeByAdmin,
    adminLogout,
} from "../../api/AdminApi";
import { tokenStore } from "../../api/tokenStore";
import { REPORT_REASON_LABEL } from "../../api/reportReason";

export default function AdminDashboard() {
    const navigate = useNavigate();

    const [cafes, setCafes] = useState([]);       // 검토 필요 카페 목록
    const [selectedId, setSelectedId] = useState(null);
    const [detail, setDetail] = useState(null);   // 선택된 카페 신고 상세
    const [listError, setListError] = useState("");
    const [actionMsg, setActionMsg] = useState(""); // 검토완료/삭제 결과 안내

    // ── 목록 불러오기 (재사용하려고 함수로 분리) ──
    const loadList = async () => {
        try {
            const res = await getFlaggedCafes();
            setCafes(res.data);
            setListError("");
        } catch (err) {
            setListError(err.message || "목록을 불러오지 못했습니다.");
        }
    };

    // 첫 진입 시 목록 로드
    useEffect(() => {
        loadList();
    }, []);

    // ── 카페 선택 → 상세 로드 ──
    const handleSelect = async (cafeVisitId) => {
        setSelectedId(cafeVisitId);
        setDetail(null);
        setActionMsg("");
        try {
            const res = await getReportDetail(cafeVisitId);
            setDetail(res.data);
        } catch (err) {
            setActionMsg(err.message || "상세를 불러오지 못했습니다.");
        }
    };

    // ── 검토 완료 처리 ──
    const handleReview = async () => {
        if (!detail) return;
        try {
            await markReviewed(detail.cafeVisitId);
            setActionMsg("검토 완료 처리했습니다.");
            // flag가 꺼졌으니 목록에서 사라짐 → 목록 갱신 + 상세 닫기
            await loadList();
            setSelectedId(null);
            setDetail(null);
        } catch (err) {
            setActionMsg(err.message || "검토 완료 처리에 실패했습니다.");
        }
    };

    // ── 카페 삭제 (되돌릴 수 없으니 확인) ──
    const handleDelete = async () => {
        if (!detail) return;
        const ok = window.confirm(
            `"${detail.cafeName}" 카페 기록을 삭제할까요?\n신고 내역도 함께 삭제되며 되돌릴 수 없습니다.`
        );
        if (!ok) return;

        try {
            await deleteCafeByAdmin(detail.cafeVisitId);
            setActionMsg("카페를 삭제했습니다.");
            await loadList();
            setSelectedId(null);
            setDetail(null);
        } catch (err) {
            setActionMsg(err.message || "삭제에 실패했습니다.");
        }
    };

    // ── 로그아웃 ──
    const handleLogout = async () => {
        try {
            await adminLogout(tokenStore.getRefreshToken());
        } catch {
            // 실패해도 클라이언트 토큰은 지움 (백엔드 멱등)
        }
        tokenStore.clear();
        navigate("/admin/login");
    };

    return (
        <div className="admin-page">
            {/* 상단 바 */}
            <header className="admin-topbar">
                <h1 className="admin-topbar-title">🛠 신고 관리 대시보드</h1>
                <button className="home-btn secondary" onClick={handleLogout}>
                    로그아웃
                </button>
            </header>

            <div className="admin-body">
                {/* 왼쪽: 검토 필요 카페 목록 */}
                <section className="admin-list">
                    <h2 className="admin-section-title">
                        검토 필요 카페 ({cafes.length})
                    </h2>
                    <p className="admin-section-sub">
                        신고 수가 많은 순으로 표시됩니다.
                    </p>

                    {listError && <p className="status error">{listError}</p>}

                    {cafes.length === 0 && !listError && (
                        <div className="admin-empty">
                            검토가 필요한 카페가 없습니다. 👍
                        </div>
                    )}

                    {cafes.map((cafe) => (
                        <button
                            key={cafe.cafeVisitId}
                            className={
                                "admin-list-item" +
                                (selectedId === cafe.cafeVisitId
                                    ? " admin-list-item--active"
                                    : "")
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
                </section>

                {/* 오른쪽: 신고 상세 */}
                <section className="admin-detail">
                    {!detail ? (
                        <div className="admin-detail-empty">
                            왼쪽에서 카페를 선택하면 신고 상세가 표시됩니다.
                        </div>
                    ) : (
                        <>
                            <h2 className="admin-detail-title">{detail.cafeName}</h2>
                            <div className="admin-detail-summary">
                                <span className="admin-badge">
                                    총 신고 {detail.reportCount}건
                                </span>
                                {detail.flaggedForReview && (
                                    <span className="admin-badge admin-badge--warn">
                                        검토 대기
                                    </span>
                                )}
                            </div>

                            <h3 className="admin-detail-subtitle">사유별 신고</h3>
                            <ul className="admin-reason-list">
                                {Object.entries(detail.reasonCounts).map(
                                    ([reason, count]) => (
                                        <li key={reason} className="admin-reason-item">
                                            <span>
                                                {REPORT_REASON_LABEL[reason] || reason}
                                            </span>
                                            <span className="admin-badge">{count}</span>
                                        </li>
                                    )
                                )}
                            </ul>

                            <div className="admin-actions">
                                <button
                                    className="home-btn primary"
                                    onClick={handleReview}
                                >
                                    검토 완료 (정상 글로 판단)
                                </button>
                                <button
                                    className="admin-btn-danger"
                                    onClick={handleDelete}
                                >
                                    카페 삭제 (악성 글)
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