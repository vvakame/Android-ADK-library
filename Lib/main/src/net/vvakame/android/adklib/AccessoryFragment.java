package net.vvakame.android.adklib;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class AccessoryFragment extends Fragment {

	public interface OnAccessoryCallback {
		public void onAccessoryConnected(UsbManager usbManager,
				UsbAccessory accessory);

		public void onAccessoryDisconnected(UsbAccessory accessory);
	}

	public interface OnAccessoryCallbackPicker {
		public OnAccessoryCallback getOnAccessoryCallback();
	}

	OnAccessoryCallback mCallback;

	UsbManager mUsbManager;

	IntentFilter mFilter;
	AccessoryPermissionReceiver mPermissionReceiver;

	public AccessoryFragment() {
	}

	public AccessoryFragment(OnAccessoryCallback callback) {
		mCallback = callback;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (mCallback == null) {
			mCallback = ((OnAccessoryCallbackPicker) activity)
					.getOnAccessoryCallback();
		}
		{
			String permissionAction = AccessoryPermissionReceiver
					.getPermissionAction(activity);

			// TODO if (Build.VERSION_CODES.HONEYCOMB_MR1 <=
			// Build.VERSION.SDK_INT)
			// android.hardware.usb.UsbManager
			mUsbManager = UsbManager.getInstance(activity);

			UsbAccessory[] accessoryList = mUsbManager.getAccessoryList();
			if (accessoryList != null && accessoryList.length == 1) {
				UsbAccessory accessory = accessoryList[0];
				if (mUsbManager.hasPermission(accessory)) {
					mCallback.onAccessoryConnected(mUsbManager, accessory);
				} else {
					PendingIntent pendingIntent = PendingIntent.getBroadcast(
							activity, 0, new Intent(permissionAction), 0);
					mUsbManager.requestPermission(accessory, pendingIntent);
				}
			}

			mFilter = new IntentFilter(permissionAction);
			mFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
			mPermissionReceiver = new AccessoryPermissionReceiver(mCallback,
					mUsbManager);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(mPermissionReceiver, mFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mPermissionReceiver);
	}
}
