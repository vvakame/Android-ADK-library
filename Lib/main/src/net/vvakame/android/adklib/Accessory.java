package net.vvakame.android.adklib;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.vvakame.android.adklib.AccessoryFragment.OnAccessoryCallback;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public final class Accessory implements Closeable {
	static final String TAG = Accessory.class.getSimpleName();

	Handler mLocalHandler;

	/** ADKとの入出力 */
	final ParcelFileDescriptor mParcelFileDescriptor;
	final InputStream mInputStream;
	final OutputStream mOutputStream;

	@SuppressWarnings("resource")
	static void setupInstance(OnAccessoryCallback callback,
			UsbManager usbManager, UsbAccessory accessory) {
		new Accessory(callback, usbManager, accessory);
	}

	private Accessory(OnAccessoryCallback callback, UsbManager usbManager,
			UsbAccessory accessory) throws IllegalStateException {
		mParcelFileDescriptor = usbManager.openAccessory(accessory);
		if (mParcelFileDescriptor == null) {
			Log.e(TAG, "accessory open failed");
			throw new IllegalStateException();
		}
		FileDescriptor fd = mParcelFileDescriptor.getFileDescriptor();
		mInputStream = new FileInputStream(fd);
		mOutputStream = new FileOutputStream(fd);

		new LooperThread().start();

		callback.onAccessoryConnected(this);
	}

	public void write(byte... data) throws IOException {
		mOutputStream.write(data);
	}

	public boolean isConnected() {
		return mLocalHandler != null
				&& mLocalHandler.getLooper().getThread().isAlive();
	}

	@Override
	public void close() throws IOException {
		Log.d(TAG, "accessory close");
		try {
			if (mParcelFileDescriptor != null) {
				mParcelFileDescriptor.close();
			}
			if (mLocalHandler != null) {
				mLocalHandler.getLooper().quit();
				mLocalHandler = null;
			}
		} catch (IOException e) {
			Log.e(TAG, "in close method", e);
		} finally {
		}
	}

	class LooperThread extends Thread {

		@Override
		public void run() {
			Looper.prepare();

			mLocalHandler = new Handler();

			Looper.loop();
		}
	}
}
