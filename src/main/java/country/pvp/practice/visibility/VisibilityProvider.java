package country.pvp.practice.visibility;

import country.pvp.practice.match.Match;
import country.pvp.practice.party.Party;
import country.pvp.practice.player.PracticePlayer;

public class VisibilityProvider {

    /**
     * Decides the visibility of a player
     *
     * @param observer   player who is looking
     * @param observable player who is looked on
     * @return visibility
     */
    public Visibility provide( PracticePlayer observer, PracticePlayer observable) {
        switch (observer.getState()) {
            case QUEUING:
            case IN_LOBBY:
                if (observer.hasParty() && observable.hasParty()) {
                    Party party = observer.getParty();

                    return party.hasPlayer(observable) ? Visibility.SHOWN : Visibility.HIDDEN;
                }
                return Visibility.HIDDEN;
            case IN_MATCH:
                if (observable.isInMatch()) {
                    Match match = observable.getCurrentMatch();
                    return match.isInMatch(observer) && match.isAlive(observable) ? Visibility.SHOWN : Visibility.HIDDEN;
                }
                return Visibility.HIDDEN;
            case SPECTATING:
                if (observable.isInMatch()) {
                    Match match = observer.getCurrentMatch();
                    return match.isInMatch(observable) ? Visibility.SHOWN : Visibility.HIDDEN;
                }
                return Visibility.HIDDEN;
            default:
                return Visibility.HIDDEN;
        }
    }
}
