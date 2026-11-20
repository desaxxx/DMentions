package dev.dash.dmentions.mention;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CooldownTrackerTest {

    private static final UUID ALEX = UUID.randomUUID();
    private static final UUID BOB = UUID.randomUUID();
    private static final String STEVE = UUID.randomUUID().toString();
    private static final String HEROBRINE = UUID.randomUUID().toString();
    private static final Duration FIVE_SECONDS = Duration.ofSeconds(5);

    private final CooldownTracker tracker = new CooldownTracker();

    @Test
    void unknownEntryHasNoCooldown() {
        assertEquals(0, tracker.remaining(ALEX, "player", STEVE, 1_000));
    }

    @Test
    void remainingCountsDown() {
        tracker.start(ALEX, "player", STEVE, FIVE_SECONDS, 1_000);
        assertEquals(5_000, tracker.remaining(ALEX, "player", STEVE, 1_000));
        assertEquals(1_500, tracker.remaining(ALEX, "player", STEVE, 4_500));
    }

    @Test
    void expiredCooldownIsZeroNotNegative() {
        tracker.start(ALEX, "player", STEVE, FIVE_SECONDS, 1_000);
        tracker.start(BOB, "player", STEVE, FIVE_SECONDS, 1_000);
        assertEquals(0, tracker.remaining(ALEX, "player", STEVE, 6_000));
        assertEquals(0, tracker.remaining(BOB, "player", STEVE, 60_000));
    }

    @Test
    void expiredEntryIsRemovedAndCanRestart() {
        tracker.start(ALEX, "player", STEVE, FIVE_SECONDS, 1_000);
        tracker.remaining(ALEX, "player", STEVE, 10_000);
        tracker.start(ALEX, "player", STEVE, FIVE_SECONDS, 10_000);
        assertEquals(5_000, tracker.remaining(ALEX, "player", STEVE, 10_000));
    }

    @Test
    void otherSendersAreNotAffected() {
        tracker.start(ALEX, "player", STEVE, FIVE_SECONDS, 1_000);
        assertEquals(0, tracker.remaining(BOB, "player", STEVE, 1_000));
    }

    @Test
    void otherTargetsAreNotAffected() {
        tracker.start(ALEX, "player", STEVE, FIVE_SECONDS, 1_000);
        assertEquals(0, tracker.remaining(ALEX, "player", HEROBRINE, 1_000));
    }

    @Test
    void otherTypesAreNotAffected() {
        tracker.start(ALEX, "player", STEVE, FIVE_SECONDS, 1_000);
        assertEquals(0, tracker.remaining(ALEX, "group", STEVE, 1_000));
    }

    @Test
    void zeroDurationStartsNothing() {
        tracker.start(ALEX, "player", STEVE, Duration.ZERO, 1_000);
        assertEquals(0, tracker.remaining(ALEX, "player", STEVE, 1_000));
    }

    @Test
    void restartExtendsCooldown() {
        tracker.start(ALEX, "player", STEVE, FIVE_SECONDS, 1_000);
        tracker.start(ALEX, "player", STEVE, FIVE_SECONDS, 3_000);
        assertEquals(5_000, tracker.remaining(ALEX, "player", STEVE, 3_000));
    }
}
