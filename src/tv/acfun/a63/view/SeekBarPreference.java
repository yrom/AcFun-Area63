package tv.acfun.a63.view;

import tv.acfun.a63.R;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
/**
 * seekbar 
 * @author Matthew Wiggins
 *
 */
public class SeekBarPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener {
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar mSeekBar;
	private TextView mValueText;

	private String mDialogMessage, mSuffix;
	private int mDefault, mMax, mValue = 0;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		mSuffix = attrs.getAttributeValue(androidns, "text");
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100);

	}
	
	@Override
	protected View onCreateDialogView() {
	    setDialogLayoutResource(R.layout.dialog_seekbar);
		View layout = super.onCreateDialogView();
		TextView message = (TextView) layout.findViewById(R.id.message);
		if (mDialogMessage != null){
		    message.setText(mDialogMessage);
		}else
		    message.setVisibility(View.GONE);

		mValueText = (TextView) layout.findViewById(R.id.text);
		mSeekBar = (SeekBar) layout.findViewById(R.id.seek_bar);
		mSeekBar.setOnSeekBarChangeListener(this);

		if (shouldPersist())
		      mValue = getPersistedInt(mDefault);

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		return layout;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		if (restore)
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		else
			mValue = (Integer) defaultValue;
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		String t = String.valueOf(value);
		mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
		if (shouldPersist())
		      persistInt(value);
		callChangeListener(Integer.valueOf(value));
	}

	public void onStartTrackingTouch(SeekBar seek) {
	}

	public void onStopTrackingTouch(SeekBar seek) {
	}

	public void setMax(int max) {
		mMax = max;
	}

	public int getMax() {
		return mMax;
	}

	public void setProgress(int progress) {
		mValue = progress;
		if (mSeekBar != null)
			mSeekBar.setProgress(progress);
	}

	public int getProgress() {
		return mValue;
	}
}
