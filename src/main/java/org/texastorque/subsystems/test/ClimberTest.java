package org.texastorque.subsystems.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.texastorque.subsystems.Climber.*;
import org.texastorque.torquelib.control.TorqueClick;

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

    public static final ArrayList<String> input() {
        try {
            final ArrayList<String> ls = new ArrayList<String>();
            final Scanner s = new Scanner(new File(TEST_FILE_PATH));
            while (s.hasNext()) ls.add(s.next());
            return ls;
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
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

    private AutoClimbState state = AutoClimbState.OFF;

    private boolean started = false, approved = false, climb = false;
    private TorqueClick approvalReset = new TorqueClick();
    public final void set(final boolean climb) {
        this.climb = climb;
        if (!started && climb) started = true;
        if (approvalReset.calculate(climb)) approved = true;
    }

    private final void update() {
        final ArrayList<String> in = input();

        set(in.get(0).equals("x"));

        System.out.printf("Climb: %1B, Approved: %1B %n", climb, approved);

        if (in.get(1).equals("x")) approved = false;

        //System.out.printf("%s: (%03.2f, %03.2f) & (%03.2f, %03.2f)\n", state,
                //l, r, left.getPosition(), right.getPosition());
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
