package org.texastorque.subsystems.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.texastorque.subsystems.Climber.*;

/**
 * Test for the climber algorithm.
 * 
 * Helped me catch one bug: this shit fucking works!!
 * 
 * Excuse the shitty code quality - its just a test so I don't care.
 * 
 * I'm also a 'lil toasted rn 🙃
 */
public class ClimberTest {

    public static final String TEST_FILE_PATH =
            "/Users/justuslanguell/TexasTorque/TexasTorque2022/src/main/java/org/texastorque/subsystems/test/ClimberTest.txt";
    // Yea its my filepath. No I don't care. Change it yourself dumbass.

    public static final void main(final String[] arguments) { new ClimberTest(); }

    public static final String input() {
        try {
            final Scanner s = new Scanner(new File(TEST_FILE_PATH));
            if (!s.hasNext()) return "";
            return s.next();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
            return "";
        }
    }

    private ClimberTest() {
        init();

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> update(), 0, 500, TimeUnit.MILLISECONDS);
    }

    private Winch left, right;

    private final void init() {
        left = new Winch(8);
        right = new Winch(8);
    }

    private ClimberState state = ClimberState.OFF;

    private final void update() {
        if (input().equals("up"))
            state = ClimberState.BOTH_UP;
        else if (input().equals("down"))
            state = ClimberState.BOTH_DOWN;
        else if (input().equals("right"))
            state = ClimberState.ZERO_RIGHT;
        else if (input().equals("left"))
            state = ClimberState.ZERO_LEFT;
        else 
            state = ClimberState.OFF;

        double l = state.getLeft().calculate(left.getPosition());
        left.setPercent(l);
        double r = state.getRight().calculate(right.getPosition());
        right.setPercent(r);

        System.out.printf("%s: (%03.2f, %03.2f) & (%03.2f, %03.2f)\n", state,
                l, r, left.getPosition(), right.getPosition());
    }

    /**
     * A class for adding one number to another fucking number.
     * 
     * @author 🤡
     */
    private static final class Winch {
        private final double speed;
        private double position;

        private Winch(final double speed) {
            this.speed = speed;
            this.position = 0;
        }

        public final void setPercent(final double percent) {
            position += speed * percent;
        }

        public final double getPosition() {
            return position;
        }
    }
}
