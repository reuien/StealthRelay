package mpc;

import controllerNettyServer.PrivacyController;

import java.util.ArrayList;

public class User {

    private String name;
    private PrivacyController controller;
    private long key0;
    private long key1;
    private long p;
    private ArrayList<Long> mpcKeys;

    public User() {
    }

    public User(String name, PrivacyController controller) {
        this.name = name;
        this.controller = controller;
    }

    public String getName() {
        return name;
    }

    public PrivacyController getController() {
        return controller;
    }

    public long getKey0() {
        return key0;
    }

    public void setKey0(long key0) {
        this.key0 = key0;
    }

    public long getKey1() {
        return key1;
    }

    public void setKey1(long key1) {
        this.key1 = key1;
    }

    public long getP() {
        return p;
    }

    public ArrayList<Long> getMpcKeys() {
        return mpcKeys;
    }

    public void setMpcKeys(ArrayList<Long> mpcKeys) {
        this.mpcKeys = mpcKeys;
    }

    public void setP() {
        this.p = key0 -key1;
    }
}


