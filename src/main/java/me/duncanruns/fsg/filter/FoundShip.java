package me.duncanruns.fsg.filter;

import kaptainwutax.mcutils.util.pos.CPos;

import java.util.Objects;

// Class to store information about a shipwreck's position and emerald count.
// Unused as only one shipwreck is checked rather than multiple.

public final class FoundShip {
    private final CPos pos;
    private final int emeralds;

    FoundShip(CPos pos, int emeralds) {
        this.pos = pos;
        this.emeralds = emeralds;
    }

    public CPos pos() {
        return pos;
    }

    public int emeralds() {
        return emeralds;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        FoundShip that = (FoundShip) obj;
        return Objects.equals(this.pos, that.pos) &&
                this.emeralds == that.emeralds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, emeralds);
    }

    @Override
    public String toString() {
        CPos pos = pos();
        return "Shipwreck at chunk " + pos.getX() + ", " + pos.getZ() + " with " + emeralds() + " emeralds.";
    }
}
