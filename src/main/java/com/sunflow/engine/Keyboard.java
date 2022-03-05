package com.sunflow.engine;

public class Keyboard {
    protected char keyNew;
    protected int keyCodeNew;

    protected char key;
    protected int keyCode;
    protected boolean[] newKeys = new boolean[65536];
    protected boolean[] keys = new boolean[newKeys.length];
    protected boolean[] lastKeys = new boolean[newKeys.length];

    public void update() {
        key = keyNew;
        keyCode = keyCodeNew;
        // for (int i = 0; i < keysNew.length; i++) keysOld[i] = keys[i];
        // for (int i = 0; i < keysNew.length; i++) keys[i] = keysNew[i];
        System.arraycopy(keys, 0, lastKeys, 0, newKeys.length);
        System.arraycopy(newKeys, 0, keys, 0, newKeys.length);
    }

    public void updateKey(char key, int keyCode, boolean newState) {
        this.keyNew = key;
        this.keyCodeNew = keyCode;
        this.newKeys[key] = newState;
    }

    public char key() { return key; }

    public int keyCode() { return keyCode; }

    // public boolean[] keys() { return keys; }

    public boolean isKeyDown(int key) {
        if (key < 0 || key > keys.length) return false;
        return keys[key];
    }

    public boolean isKeyPressed(int key) {
        if (key < 0 || key > keys.length) return false;
        return !lastKeys[key] && keys[key];
    }

    public boolean isKeyHeld(int key) {
        if (key < 0 || key > keys.length) return false;
        return lastKeys[key] && keys[key];
    }

    public boolean isKeyReleased(int key) {
        if (key < 0 || key > keys.length) return false;
        return lastKeys[key] && !keys[key];
    }
}
