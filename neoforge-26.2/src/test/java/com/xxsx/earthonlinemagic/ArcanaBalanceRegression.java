package com.xxsx.earthonlinemagic;

public final class ArcanaBalanceRegression {
    private static final double EPSILON = 0.000001D;

    private ArcanaBalanceRegression() {
    }

    public static void main(String[] args) {
        verifyMonotonicAndCapped();
        verifyInvalidValuesAreBounded();
        verifyExperienceCurve();
        verifyJourneyMask();
    }

    private static void verifyMonotonicAndCapped() {
        double previousExhaustion = 0.0D;
        double previousCombat = 0.0D;
        double previousBreath = 1.0D;
        double previousFall = 0.0D;
        for (int level = 0; level <= 100; level++) {
            double exhaustion = ArcanaBalance.exhaustionReduction(level * 0.10D);
            double combat = ArcanaBalance.combatDamageReduction(level * 0.09D);
            double breath = ArcanaBalance.breathMultiplier(level * 30.0D);
            double fall = ArcanaBalance.fallDamageReduction(level * 0.08D);
            require(exhaustion + EPSILON >= previousExhaustion, "exhaustion reduction regressed");
            require(combat + EPSILON >= previousCombat, "combat reduction regressed");
            require(breath + EPSILON >= previousBreath, "breath multiplier regressed");
            require(fall + EPSILON >= previousFall, "fall reduction regressed");
            previousExhaustion = exhaustion;
            previousCombat = combat;
            previousBreath = breath;
            previousFall = fall;
        }
        close(0.82D, ArcanaBalance.exhaustionReduction(1000.0D));
        close(0.30D, ArcanaBalance.combatDamageReduction(1000.0D));
        close(4.0D, ArcanaBalance.breathMultiplier(100000.0D));
        close(0.45D, ArcanaBalance.fallDamageReduction(1000.0D));
    }

    private static void verifyInvalidValuesAreBounded() {
        close(0.0D, ArcanaBalance.exhaustionReduction(Double.NaN));
        close(0.0D, ArcanaBalance.combatDamageReduction(Double.NEGATIVE_INFINITY));
        close(1.0D, ArcanaBalance.breathMultiplier(Double.POSITIVE_INFINITY));
        close(0.0D, ArcanaBalance.fallDamageReduction(-1.0D));
    }

    private static void verifyExperienceCurve() {
        int previous = 0;
        for (int level = 1; level < 10; level++) {
            int needed = ArcanaBalance.xpNeededForLevel(level, 10);
            require(needed > previous, "experience curve must increase");
            previous = needed;
        }
        require(ArcanaBalance.xpNeededForLevel(0, 10) == 0, "locked route must not require experience");
        require(ArcanaBalance.xpNeededForLevel(10, 10) == 0, "capped route must not require experience");
    }

    private static void verifyJourneyMask() {
        int total = 6;
        int mask = 0;
        for (int milestoneId = 0; milestoneId < total; milestoneId++) {
            require(JourneyProgress.nextId(mask, total) == milestoneId, "journey order changed");
            int next = JourneyProgress.apply(mask, milestoneId, total);
            require(JourneyProgress.count(next, total) == JourneyProgress.count(mask, total) + 1,
                    "journey count did not advance");
            require(JourneyProgress.apply(next, milestoneId, total) == next,
                    "journey milestone is not idempotent");
            mask = next;
        }
        require(JourneyProgress.isComplete(mask, total), "journey full mask is incomplete");
        require(JourneyProgress.nextId(mask, total) == -1, "complete journey still has a next step");
        require(JourneyProgress.count(mask, total) == total, "journey total mismatch");
        require(JourneyProgress.sanitize(mask | (1 << 20), total) == mask,
                "journey mask kept unknown bits");
    }

    private static void close(double expected, double actual) {
        require(Math.abs(expected - actual) <= EPSILON, "expected " + expected + ", got " + actual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
