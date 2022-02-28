package me.duncanruns.fsg;

interface AngleMathHelper {

    // Returns the absolute difference of 2 angles in radians. The returned value will be between 0 and pi.
    static double getAngleDifference(double angle1, double angle2) {
        double diff = getRealAngle(angle1) - getRealAngle(angle2);
        return Math.abs(diff > Math.PI ? diff - Math.PI * 2 : diff);
    }

    // Converts an angle in radians to an angle in radians between 0 and 2pi.
    static double getRealAngle(double angle) {
        return angle % (Math.PI * 2);
    }
}
