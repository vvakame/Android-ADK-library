package net.vvakame.android.adklib;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class AccessoryFragment extends Fragment {

	public interface OnAccessoryCallback {
		public void onAccessoryConnected(Accessory accesory);

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

			// TODO Honeycomb 以前は com.android.future.usb.UsbManager
			mUsbManager = (UsbManager) activity
					.getSystemService(Context.USB_SERVICE);

			UsbAccessory[] accessoryList = mUsbManager.getAccessoryList();
			if (accessoryList != null && accessoryList.length == 1) {
				UsbAccessory accessory = accessoryList[0];
				if (mUsbManager.hasPermission(accessory)) {
					Accessory.setupInstance(mCallback, mUsbManager, accessory);
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
