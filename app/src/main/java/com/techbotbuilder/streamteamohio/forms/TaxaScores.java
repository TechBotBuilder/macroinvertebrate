package com.techbotbuilder.streamteamohio.forms;

public enum TaxaScores {
    WATERPENNY_LARVA(3, 4),
    MAYFLY_NYMPH(3, 4),
    STONEFLY_NYMPH(3, 4),
    DOBSONFLY_LARVA(3, 4),
    CADDISFLY_LARVA(3, 4),
    RIFFLEBEETLE_ADULT(3, 4),
    GILLEDSNAIL(3, 4),

    DAMSELFLY_NYMPH(2, 3),
    DRAGONFLY_NYMPH(2, 3),
    CRANEFLY_NYMPH(2, 3),
    CRAYFISH(2, 3),
    SCUD(2, 3),
    CLAM(2, 3),
    SOWBUG(2, 3),
    BEETLE_LARVA(2, 0),

    BLACKFLY_LARVA(1, 2),
    MIDGE_LARVA(1, 2),
    LEECH(1, 2),
    AQUATICWORM(1, 1),
    BLOODMIDGE_LARVA(1,1),
    POUCHSNAIL(1, 1),

    PLANARIA(0,2);

    public int threeGroupScore;
    public int fourGroupScore;

    TaxaScores(int threeGroupScore, int fourGroupScore){
        this.threeGroupScore = threeGroupScore;
        this.fourGroupScore = fourGroupScore;
    }
}
