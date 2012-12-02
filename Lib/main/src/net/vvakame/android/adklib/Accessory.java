package net.vvakame.android.adklib;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public final class Accessory implements Closeable {
	static final String TAG = Accessory.class.getSimpleName();

	/** データを受信するためのループ */
	final DriverThread mDriverThread;

	/** ADKとの入出力 */
	final ParcelFileDescriptor mParcelFileDescriptor;
	final InputStream mIs;
	final OutputStream mOs;

	Accessory(UsbManager usbManager, UsbAccessory accessory)
			throws IllegalStateException {
		mParcelFileDescriptor = usbManager.openAccessory(accessory);
		if (mParcelFileDescriptor == null) {
			Log.e(TAG, "accessory open failed");
			throw new IllegalStateException();
		}
		FileDescriptor fd = mParcelFileDescriptor.getFileDescriptor();
		mIs = new FileInputStream(fd);
		mOs = new FileOutputStream(fd);

		mDriverThread = new DriverThread();
		mDriverThread.start();
	}

	public boolean isConnected() {
		return mDriverThread.isAlive();
	}

	@Override
	public void close() throws IOException {
		Log.d(TAG, "accessory close");
		try {
			if (mParcelFileDescriptor != null) {
				mParcelFileDescriptor.close();
			}
			if (mDriverThread != null) {
				mDriverThread.disconnect();
			}
		} catch (IOException e) {
			Log.e(TAG, "in close method", e);
		} finally {
		}
	}

	class DriverThread extends Thread {
		boolean mRunning = true;

		@Override
		public void run() {
			int length = 0;
			byte[] buffer = new byte[16384];

			while (length >= 0 && mRunning) {
				try {
					length = mIs.read(buffer);
				} catch (IOException e) {
					break;
				}

				byte[] pass = new byte[length];
				System.arraycopy(buffer, 0, pass, 0, length);
				// TODO リスナにコールバック
			}
			mRunning = false;
		}

		public void disconnect() {
			mRunning = false;
		}
	}
}
