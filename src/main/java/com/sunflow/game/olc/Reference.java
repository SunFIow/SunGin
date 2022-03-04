package com.sunflow.game.olc;

public class Reference<T> {
	public T value;

	public Reference() {}

	public Reference(T value) { this.value = value; }

	public T get() { return value; }

	public void set(T value) { this.value = value; }

	@Override
	public String toString() {
		if (value == null) return "null";
		return value.toString();
	}
}
