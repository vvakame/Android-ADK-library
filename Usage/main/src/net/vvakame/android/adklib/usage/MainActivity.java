package net.vvakame.android.adklib.usage;

import net.vvakame.android.adklib.driver.AdkDigitalFromDevice;
import net.vvakame.android.adklib.driver.AdkDigitalToDevice;
import net.vvakame.android.adklib.usage.driver.LedDriver;
import net.vvakame.android.adklib.usage.driver.SwitchDriver;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class MainActivity extends FragmentActivity {

	MyAdkFragment mAdkFragment;

	CheckBox mCheck;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
		mAdkFragment = new MyAdkFragment();
		{
			ButtonToLedBridge bridge = new ButtonToLedBridge();
			LedDriver driver = new LedDriver(bridge);
			mAdkFragment.addDriver(driver);
		}
		{
			mCheck = new CheckBox(this);
			SwitchToCheckboxBridge bridge = new SwitchToCheckboxBridge();
			SwitchDriver driver = new SwitchDriver(bridge);
			mAdkFragment.addDriver(driver);
		}

		tx.add(mAdkFragment, null);
		tx.commit();
	}

	class ButtonToLedBridge implements OnClickListener, AdkDigitalToDevice {
		boolean value = false;

		@Override
		public void onClick(View v) {
			value = !value;
			write(new byte[] { value ? (byte) 0 : (byte) 1 });
		}

		@Override
		public void write(byte[] value) {
		}
	}

	class SwitchToCheckboxBridge implements AdkDigitalFromDevice {

		@Override
		public void onChange(byte[] value) {
			mCheck.setChecked(value[0] == 0);
		}
	}
}
