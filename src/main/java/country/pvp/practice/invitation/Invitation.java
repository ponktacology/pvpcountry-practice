package country.pvp.practice.invitation;

import country.pvp.practice.Expiring;
import country.pvp.practice.player.PracticePlayer;
import country.pvp.practice.time.TimeUtil;
import lombok.Data;

import java.util.UUID;

@Data
public abstract class Invitation implements Expiring {

    private final UUID id = UUID.randomUUID();
    private final long timeStamp = System.currentTimeMillis();
    private final String message;
    private final PracticePlayer inviter;

    public void accept() {
        onAccept();
    }

    public void decline() {
        onDecline();
    }

    protected abstract void onAccept();

    protected abstract void onDecline();

    @Override
    public boolean hasExpired() {
        return TimeUtil.elapsed(timeStamp) > 15_000L;
    }
}