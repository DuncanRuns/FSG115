package me.duncanruns.fsg;

import kaptainwutax.mcutils.util.pos.CPos;

public record FoundShip(CPos pos, int emeralds) {

    @Override
    public String toString() {
        CPos pos = pos();
        return "Shipwreck at chunk " + pos.getX() + ", " + pos.getZ() + " with " + emeralds() + " emeralds.";
    }
}
