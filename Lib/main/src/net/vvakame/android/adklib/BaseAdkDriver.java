package net.vvakame.android.adklib;

public abstract class BaseAdkDriver implements AdkDriver {

	@Override
	public abstract void onMessageArrive(byte[] data);

	public abstract void messageSend(byte[] data);
}
