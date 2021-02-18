package de.micromata.paypal.data;

import java.util.ArrayList;

public class PaymentUpdate {
    public static enum Operation {
        REPLACE, ADD;
    }

    private final Operation op;
    private final String path;
    private final ArrayList<? extends Updatable> value;

    public PaymentUpdate(Operation op, String path, ArrayList<? extends Updatable> value) {
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

    public ArrayList<? extends Updatable> getValue() {
        return value;
    }
}
