package org.texastorque.torquelib.util;

public class TorqueLock<E> {
    private boolean locked;
    private E value;

    public TorqueLock(boolean locked) {
        this.locked = locked;
    }

    public E calculate(E requested) {
        if (!locked)
            value = requested;
        return value;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public E getValue() {
        return value;
    }

}
