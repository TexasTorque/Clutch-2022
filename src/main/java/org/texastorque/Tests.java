/**
 * Copyright 2022 Texas Torque.
 *
 * This file is part of Clutch-2022, which is not licensed for distribution.
 * For more details, see ./license.txt or write <jus@gtsbr.org>.
 */
package org.texastorque;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.texastorque.subsystems.Shooter;

public final class Tests {

    public static final double calculateAngleWithOdometry(final Pose2d pose) {
        System.out.println(pose.toString());
        final double x = Shooter.HUB_CENTER_POSITION.getX() - pose.getX();
        final double y = Shooter.HUB_CENTER_POSITION.getY() - pose.getY();
        final Rotation2d angle = new Rotation2d(Math.atan2(y, x));
        final Rotation2d combined = pose.getRotation().minus(angle);
        System.out.println(combined.getDegrees());
        System.out.println(combined.times(-1).getDegrees());
        return combined.getDegrees();
    }

    public final static void main(final String[] args) {
        final double x = 2;
        final double y = 2;
        final double r = 90;

        final Pose2d pose = new Pose2d(x, y, Rotation2d.fromDegrees(r));
        final double angle = calculateAngleWithOdometry(pose);
        System.out.println(angle);
    }
}
