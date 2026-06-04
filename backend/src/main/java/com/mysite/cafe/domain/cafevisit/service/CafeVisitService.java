package com.mysite.cafe.domain.cafevisit.service;

import com.mysite.cafe.domain.cafevisit.dto.CafeVisitRequest;
import com.mysite.cafe.domain.cafevisit.dto.CafeVisitResponse;
import com.mysite.cafe.domain.cafevisit.entity.CafeVisit;
import com.mysite.cafe.domain.cafevisit.entity.WifiSpeed;
import com.mysite.cafe.domain.cafevisit.repository.CafeVisitRepository;
import com.mysite.cafe.domain.user.entity.User;
import com.mysite.cafe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CafeVisitService {

    private final CafeVisitRepository cafeVisitRepository;
    private final UserRepository userRepository;

     //카페 방문 기록 등록
    @Transactional
    public CafeVisitResponse create(CafeVisitRequest request) {

        // 같은 좌표의 카페가 10초 안에 등록됐다면 중복 등록으로 간주
        LocalDateTime since = LocalDateTime.now().minusSeconds(10);
        if (request.lat() != null && request.lng() != null
                && cafeVisitRepository.existsRecentByLocation(request.lat(), request.lng(), since)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "방금 등록된 카페입니다. 잠시 후 다시 시도해주세요."
            );
        }
        // 닉네임으로 User 조회 (없으면생성)
        User user = findOrCreateUser(request.nickname());

        // DTO -> Entity
        CafeVisit cafeVisit = CafeVisit.builder()
                .name(request.name())
                .address(request.address())
                .lat(request.lat())
                .lng(request.lng())
                .rating(request.rating())
                .priceLevel(request.priceLevel())
                .hasOutlet(request.hasOutlet())
                .wifiSpeed(parseWifiSpeed(request.wifiSpeed()))
                .studyScore(request.studyScore())
                .memo(request.memo())
                .visitedAt(request.visitedAt())
                .user(user)
                .build();

        // 저장 후 Response로 변환
        CafeVisit saved = cafeVisitRepository.save(cafeVisit);
        return toResponse(saved);
    }

    //카페 방문 기록 전체 조회
    public List<CafeVisitResponse> findAll() {
        return cafeVisitRepository.findAllWithUser().stream()
                .map(this::toResponse)
                .toList();
    }


    private User findOrCreateUser(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return null;
        }
        return userRepository.findByNickname(nickname)
                .orElseGet(() -> userRepository.save(
                        User.builder().nickname(nickname).build()
                ));
    }

    // String -> Enum
    private WifiSpeed parseWifiSpeed(String wifiSpeed) {
        if (wifiSpeed == null || wifiSpeed.isBlank()) {
            return null;
        }
        try {
            return WifiSpeed.valueOf(wifiSpeed.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Entity -> DTO
    private CafeVisitResponse toResponse(CafeVisit v) {
        String nickname = (v.getUser() != null) ? v.getUser().getNickname() : null;
        String wifiSpeedStr = (v.getWifiSpeed() != null) ? v.getWifiSpeed().name() : null;

        return new CafeVisitResponse(
                v.getId(),
                v.getName(),
                v.getAddress(),
                v.getLat(),
                v.getLng(),
                v.getRating(),
                v.getPriceLevel(),
                v.getHasOutlet(),
                wifiSpeedStr,
                v.getStudyScore(),
                v.getMemo(),
                v.getVisitedAt(),
                nickname
        );
    }

    //카페 방문 기록 단건 조회
    public CafeVisitResponse findById(Long id) {
        CafeVisit cafeVisit = cafeVisitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "카페 방문 기록을 찾을 수 없습니다. id=" + id
                ));
        return toResponse(cafeVisit);
    }

    //카페 방문 기록 수정
    @Transactional
    public CafeVisitResponse update(Long id, CafeVisitRequest request) {
        CafeVisit cafeVisit = cafeVisitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "카페 방문 기록을 찾을 수 없습니다. id=" + id
                ));

        cafeVisit.update(
                request.name(),
                request.address(),
                request.lat(),
                request.lng(),
                request.rating(),
                request.priceLevel(),
                request.hasOutlet(),
                parseWifiSpeed(request.wifiSpeed()),
                request.studyScore(),
                request.memo(),
                request.visitedAt()
        );

        return toResponse(cafeVisit);
    }

    //카페 방문 기록 삭제
    @Transactional
    public void delete(Long id) {
        if (!cafeVisitRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카페 방문 기록을 찾을 수 없습니다. id=" + id
            );
        }
        cafeVisitRepository.deleteById(id);
    }

}