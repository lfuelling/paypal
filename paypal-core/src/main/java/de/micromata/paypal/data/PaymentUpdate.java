package de.micromata.paypal.data;

import java.util.List;

public class PaymentUpdate {
    public static enum Operation {
        REPLACE, ADD;
    }

    private final Operation op;
    private final String path;
    private final List<? extends Updatable> value;

    public PaymentUpdate(Operation op, String path, List<? extends Updatable> value) {
        this.op = op;
        this.path = path;
        this.value = value;
    }

    public Operation getOp() {
        return op;
    }

    public String getPath() {
        return path;
    }

    public List<? extends Updatable> getValue() {
        return value;
    }
}
