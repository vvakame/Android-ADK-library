package net.vvakame.android.adklib;

import net.vvakame.android.adklib.AccessoryFragment.OnAccessoryCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

class AccessoryPermissionReceiver extends BroadcastReceiver {

	static final String TAG = AccessoryPermissionReceiver.class.getSimpleName();

	final OnAccessoryCallback mCallback;
	final UsbManager mUsbManager;

	public AccessoryPermissionReceiver(OnAccessoryCallback callback,
			UsbManager usbManager) {
		mCallback = callback;
		mUsbManager = usbManager;
	}

	public static String getPermissionAction(Context context) {
		return context.getPackageName() + ".action.USB_PERMISSION";
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String permissionAction = getPermissionAction(context);
		String action = intent.getAction();
		if (permissionAction.equals(action)) {
			// TODO チャタリング対策で syncronized が必要？
			UsbAccessory accessory = UsbManager.getAccessory(intent);
			if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
					false)) {
				Log.d(TAG, "permission grant for accessory " + accessory);
				Accessory.setupInstance(mCallback, mUsbManager, accessory);
			} else {
				Log.d(TAG, "permission denied for accessory " + accessory);
			}
		} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
			UsbAccessory accessory = UsbManager.getAccessory(intent);
			mCallback.onAccessoryDisconnected(accessory);
		}
	}
}
