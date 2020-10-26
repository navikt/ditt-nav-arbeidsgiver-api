package no.nav.tag.dittNavArbeidsgiver.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;

public class ClockMock extends Clock {
    private Instant now;

    public ClockMock(Instant now) {
        this.now = now;
    }

    public void forward(TemporalAmount timeToAdd) {
        now = now.plus(timeToAdd);
    }

    @Override
    public Instant instant() {
        return now;
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }
}
