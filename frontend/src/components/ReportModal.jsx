import { useState } from "react";
import { reportCafe } from "../api/CafeApi";
import { REPORT_REASON_LABEL } from "../api/reportReason";

const REASONS = ["MISINFORMATION", "INAPPROPRIATE", "SPAM", "OTHER"];

export default function ReportModal({ cafe, onClose }) {
    const [reason, setReason] = useState("MISINFORMATION");
    const [status, setStatus] = useState(null); // null | "loading" | "success" | "error"
    const [errorMsg, setErrorMsg] = useState("");

    const handleSubmit = async () => {
        setStatus("loading");
        setErrorMsg("");
        try {
            await reportCafe(cafe.id, reason);
            setStatus("success");
        } catch (err) {
            // 백엔드가 409로 내려주는 "이미 신고한 글입니다" 등의 메시지를 그대로 표시.
            setErrorMsg(err.message || "신고 중 오류가 발생했습니다.");
            setStatus("error");
        }
    };

    return (
        <div className="kakao-modal-backdrop">
            <div className="report-modal">
                <div className="kakao-modal-header">
                    <h2>카페 신고</h2>
                    <button
                        className="kakao-close-btn"
                        type="button"
                        onClick={onClose}
                    >
                        ✕
                    </button>
                </div>

                <p className="report-modal-target">
                    <strong>{cafe.name}</strong> 카페를 신고합니다.
                </p>

                {status !== "success" ? (
                    <>
                        <div className="report-modal-reasons">
                            {REASONS.map((r) => (
                                <label
                                    key={r}
                                    className={
                                        "report-reason" +
                                        (reason === r ? " report-reason--active" : "")
                                    }
                                >
                                    <input
                                        type="radio"
                                        name="reason"
                                        value={r}
                                        checked={reason === r}
                                        onChange={() => setReason(r)}
                                    />
                                    <span>{REPORT_REASON_LABEL[r]}</span>
                                </label>
                            ))}
                        </div>

                        <p className="report-modal-notice">
                            허위 신고가 반복되면 신고가 제한될 수 있어요.
                        </p>

                        {status === "error" && (
                            <p className="status error">{errorMsg}</p>
                        )}

                        <div className="report-modal-actions">
                            <button
                                className="home-btn secondary"
                                type="button"
                                onClick={onClose}
                            >
                                취소
                            </button>
                            <button
                                className="home-btn primary"
                                type="button"
                                onClick={handleSubmit}
                                disabled={status === "loading"}
                            >
                                {status === "loading" ? "신고 중..." : "신고하기"}
                            </button>
                        </div>
                    </>
                ) : (
                    // 성공 화면 — 같은 카페 또 신고 못 하니 닫기만 노출.
                    <>
                        <p className="status success">
                            신고가 접수되었습니다. 확인 후 운영자가 검토할 예정입니다.
                        </p>
                        <div className="report-modal-actions">
                            <button
                                className="home-btn primary"
                                type="button"
                                onClick={onClose}
                            >
                                닫기
                            </button>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}