package com.prj.learnvocabularybe.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.prj.learnvocabularybe.entity.ReviewLogsEntity;
import com.prj.learnvocabularybe.entity.UserEntity;
import com.prj.learnvocabularybe.entity.UserWordProgressEntity;
import com.prj.learnvocabularybe.entity.WordMeaningEntity;
import com.prj.learnvocabularybe.repository.ReviewLogsRepository;
import com.prj.learnvocabularybe.repository.UserRepository;
import com.prj.learnvocabularybe.repository.UserWordProgressRepository;
import com.prj.learnvocabularybe.repository.WordMeaningRepository;

import lombok.RequiredArgsConstructor;

@Component
@Profile("local")
@RequiredArgsConstructor
public class DemoStudyDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoStudyDataSeeder.class);
    private static final Long DEMO_USER_ID = 8L;
    private static final int TARGET_PROGRESS_COUNT = 8;
    private static final int TARGET_STREAK_DAYS = 7;
    private static final String DEMO_USERNAME = "demo.study.user8";
    private static final String DEMO_EMAIL = "demo.study.user8@example.com";
    private static final String DEMO_PASSWORD = "demo-user-8";

    private final UserRepository userRepository;
    private final WordMeaningRepository wordMeaningRepository;
    private final UserWordProgressRepository progressRepository;
    private final ReviewLogsRepository reviewLogsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        UserEntity user = ensureDemoUser();
        List<WordMeaningEntity> meanings = loadSeedMeanings();

        if (meanings.size() < TARGET_PROGRESS_COUNT) {
            log.warn("Skip seeding study demo data for user {} because only {} word meanings exist", DEMO_USER_ID, meanings.size());
            return;
        }

        seedProgress(user, meanings);
        seedReviewLogs(user, meanings);
    }

    private UserEntity ensureDemoUser() {
        return userRepository.findById(DEMO_USER_ID)
                .orElseGet(() -> {
                    String encodedPassword = passwordEncoder.encode(DEMO_PASSWORD);
                    jdbcTemplate.update(
                            """
                            INSERT INTO users (id, username, email, password, avatar_url, notification_enabled, notification_time, created_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                            DEMO_USER_ID,
                            DEMO_USERNAME,
                            DEMO_EMAIL,
                            encodedPassword,
                            null,
                            false,
                            "08:00",
                            LocalDateTime.now()
                    );

                    try {
                        jdbcTemplate.execute(
                                """
                                SELECT setval(
                                    pg_get_serial_sequence('users', 'id'),
                                    GREATEST((SELECT COALESCE(MAX(id), 0) FROM users), 8),
                                    true
                                )
                                """
                        );
                    } catch (DataAccessException error) {
                        log.debug("Unable to reset users sequence after demo insert", error);
                    }

                    return userRepository.findById(DEMO_USER_ID)
                            .orElseThrow(() -> new IllegalStateException("Cannot create demo user 8"));
                });
    }

    private List<WordMeaningEntity> loadSeedMeanings() {
        return wordMeaningRepository.findAll().stream()
                .filter(meaning -> meaning.getVocabulary() != null)
                .sorted(Comparator.comparing(WordMeaningEntity::getId))
                .limit(TARGET_PROGRESS_COUNT)
                .toList();
    }

    private void seedProgress(UserEntity user, List<WordMeaningEntity> meanings) {
        List<ProgressSeed> seeds = List.of(
                new ProgressSeed(0, 4, 12, 2.7f, 0, 1, 6),
                new ProgressSeed(1, 3, 8, 2.6f, 1, 2, 4),
                new ProgressSeed(2, 2, 6, 2.45f, 0, 3, 3),
                new ProgressSeed(3, 2, 5, 2.35f, 1, 4, 2),
                new ProgressSeed(4, 5, 18, 2.85f, 0, 5, 9),
                new ProgressSeed(5, 1, 1, 1.9f, 3, 2, -1),
                new ProgressSeed(6, 0, 1, 1.7f, 4, 3, -2),
                new ProgressSeed(7, 1, 2, 1.6f, 5, 4, -3)
        );

        LocalDateTime now = LocalDateTime.now();

        for (ProgressSeed seed : seeds) {
            WordMeaningEntity meaning = meanings.get(seed.meaningIndex());
            UserWordProgressEntity progress = progressRepository
                    .findByUserIdAndWordMeaningId(user.getId(), meaning.getId())
                    .orElseGet(UserWordProgressEntity::new);

            progress.setUser(user);
            progress.setWordMeaning(meaning);
            progress.setRepetitionCount(seed.repetitionCount());
            progress.setIntervalDays(seed.intervalDays());
            progress.setEaseFactor(seed.easeFactor());
            progress.setLapses(seed.lapses());
            progress.setLastReviewDate(LocalDate.now().minusDays(seed.lastReviewDaysAgo()).atTime(19, 30));
            progress.setNextReviewDate(now.plusDays(seed.nextReviewDaysFromNow()));

            progressRepository.save(progress);
        }
    }

    private void seedReviewLogs(UserEntity user, List<WordMeaningEntity> meanings) {
        int existingStreak = calculateStreak(reviewLogsRepository.findDistinctReviewDates(user.getId()));
        if (existingStreak >= TARGET_STREAK_DAYS) {
            return;
        }

        List<ReviewLogsEntity> logs = new ArrayList<>();
        for (int dayOffset = 6; dayOffset >= 0; dayOffset -= 1) {
            LocalDate reviewDate = LocalDate.now().minusDays(dayOffset);

            WordMeaningEntity morningMeaning = meanings.get(dayOffset % meanings.size());
            WordMeaningEntity eveningMeaning = meanings.get((dayOffset + 3) % meanings.size());

            logs.add(buildLog(user, morningMeaning, reviewDate.atTime(8, 15), switch (dayOffset % 4) {
                case 0 -> 4;
                case 1 -> 3;
                case 2 -> 2;
                default -> 4;
            }, 1800L + (dayOffset * 240L)));

            logs.add(buildLog(user, eveningMeaning, reviewDate.atTime(20, 40), switch (dayOffset % 3) {
                case 0 -> 4;
                case 1 -> 3;
                default -> 2;
            }, 3200L + (dayOffset * 310L)));
        }

        reviewLogsRepository.saveAll(logs);
    }

    private ReviewLogsEntity buildLog(
            UserEntity user,
            WordMeaningEntity meaning,
            LocalDateTime reviewedAt,
            Integer grade,
            Long timeTakenMs
    ) {
        return ReviewLogsEntity.builder()
                .user(user)
                .wordMeaning(meaning)
                .grade(grade)
                .timeTakenMs(timeTakenMs)
                .reviewedAt(reviewedAt)
                .build();
    }

    private int calculateStreak(List<LocalDate> reviewDates) {
        if (reviewDates == null || reviewDates.isEmpty()) {
            return 0;
        }

        List<LocalDate> distinctDates = reviewDates.stream().distinct().sorted(Comparator.reverseOrder()).toList();
        LocalDate cursor = distinctDates.get(0);
        int streak = 0;

        while (distinctDates.contains(cursor)) {
            streak += 1;
            cursor = cursor.minusDays(1);
        }

        return streak;
    }

    private record ProgressSeed(
            int meaningIndex,
            int repetitionCount,
            int intervalDays,
            float easeFactor,
            int lapses,
            int lastReviewDaysAgo,
            int nextReviewDaysFromNow
    ) {
    }
}