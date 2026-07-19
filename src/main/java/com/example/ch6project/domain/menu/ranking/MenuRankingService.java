package com.example.ch6project.domain.menu.ranking;

import com.example.ch6project.common.exception.CustomException;
import com.example.ch6project.common.exception.ErrorCode;
import com.example.ch6project.domain.menu.dto.PopularMenuResponse;
import com.example.ch6project.domain.menu.entity.Menu;
import com.example.ch6project.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuRankingService {

    private static final String MENU_RANKING_DAILY_KEY = "menu:ranking:";

    private final StringRedisTemplate stringRedisTemplate;
    private final MenuRepository menuRepository;

    @Transactional
    public void increaseMenuRanking(Long menuId, LocalDate date) {
        String key = MENU_RANKING_DAILY_KEY + date;

        stringRedisTemplate.opsForZSet()
                .incrementScore(key, String.valueOf(menuId), 1);
    }

    public List<PopularMenuResponse> getPopularMenus() {
        Map<Long, Long> menuOrderCounts = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            String key = MENU_RANKING_DAILY_KEY + date;

            Set<ZSetOperations.TypedTuple<String>> ranking =
                    stringRedisTemplate.opsForZSet()
                            .reverseRangeWithScores(key, 0, -1);

            if (ranking == null || ranking.isEmpty()) {
                continue;
            }

            for (ZSetOperations.TypedTuple<String> tuple : ranking) {
                if (tuple.getValue() == null || tuple.getScore() == null) {
                    continue;
                }

                Long menuId = Long.valueOf(tuple.getValue());
                Long orderCount = tuple.getScore().longValue();

                menuOrderCounts.merge(menuId, orderCount, Long::sum);
            }
        }

        List<Map.Entry<Long, Long>> popularMenuEntries = menuOrderCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(3)
                .toList();

        List<Long> popularMenuIds = popularMenuEntries.stream()
                .map(Map.Entry::getKey)
                .toList();

        Map<Long, Menu> menuMap = menuRepository.findAllById(popularMenuIds).stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        return popularMenuEntries.stream()
                .map(entry -> {
                    Menu menu = menuMap.get(entry.getKey());
                    if (menu == null) {
                        throw new CustomException(ErrorCode.MENU_NOT_FOUND);
                    }

                    return PopularMenuResponse.from(menu, entry.getValue());
                })
                .toList();
    }
}
