package net.vvakame.android.adklib.usage.driver;

public enum Type {

	LED1(0), SWITCH1(1);

	int command;

	private Type(int command) {
		this.command = command;
	}

	public int getCommand() {
		return command;
	}
}
