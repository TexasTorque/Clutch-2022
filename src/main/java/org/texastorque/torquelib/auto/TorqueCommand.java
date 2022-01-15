package org.texastorque.torquelib.auto;

public abstract class TorqueCommand {

    private boolean ended = false, started = false;

    public boolean run() {
        if (ended) return ended;
        if (!started) {
            init();
            started = true;
        }
        continuous();
        if (endCondition()) {
            end();
            ended = true;
        }
        return ended;
    }

    protected abstract void init();

    protected abstract void continuous();

    protected abstract boolean endCondition();

    protected abstract void end();
}
