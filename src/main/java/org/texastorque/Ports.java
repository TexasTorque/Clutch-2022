package org.texastorque;

public class Ports {

    public class DRIVEBASE {
        public class TRANSLATIONAL {
            public class RIGHT {
                public static final int FRONT = 1;
                public static final int BACK = 2;
            }
            public class LEFT {
                public static final int FRONT = 3;
                public static final int BACK = 4;
            }
        }
        public class ROTATIONAL {
            public class RIGHT {
                public static final int FRONT = 5;
                public static final int BACK = 6;
            }
            public class LEFT {
                public static final int FRONT = 7;
                public static final int BACK = 8;
            }
        }
    }

    public class INTAKE {
        public static final int ROTARY = 9;
        public class ROLLER {
            public static final int LEFT = 19;
            public static final int RIGHT = 10;
        }   
        public static final int SWITCH = 3;
    }

    public final class MAGAZINE {
        public static final int BELT = 11;
        public static final int GATE = 12;
    }

    public final class SHOOTER {
        public static final int HOOD = 13;
        public final class FLYWHEEL {
            public static final int LEFT = 14;
            public static final int RIGHT = 15;
        }
    }

    public final class CLIMBER {
        public final class WINCH {
            public static final int LEFT = 16;
            public static final int RIGHT = 17;
        }
        public final class SERVO {
            public static final int LEFT = 5;
            public static final int RIGHT = 6;
        }
        public final class CLAW {
            public static final int LEFT = 1;
            public static final int RIGHT = 2;
        }
    }

    public static final int TURRET = 18;

    public static final int LIGHTS = 3;
}