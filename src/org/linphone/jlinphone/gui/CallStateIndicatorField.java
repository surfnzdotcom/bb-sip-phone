package org.linphone.jlinphone.gui;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

/**
 * @author guillaume
 *
 */
public class CallStateIndicatorField extends HorizontalFieldManager {

	public static final int CallStateINCALL=0;
	public static final int CallStateENDCALL=1;
	public static final int CallStateSECURE=2;
	public static final int CallStateUNSECURE=3;

	private Bitmap incallBM=Bitmap.getBitmapResource("incall.png");
	private Bitmap secureBM=Bitmap.getBitmapResource("security_ok.png");
	private Bitmap unsecureBM=Bitmap.getBitmapResource("security_ko.png");
	private BitmapField incallField = new BitmapField(incallBM);
	private BitmapField secureField = new BitmapField(secureBM);
	private BitmapField unsecureField = new BitmapField(unsecureBM);

	private int currentCallState=CallStateENDCALL;
	public boolean dontShowUnsecure;

	public CallStateIndicatorField(long style) {
		super(style);
		incallField.setSpace((40-24)/2, (40-19)/2);
		secureField.setSpace((40-24)/2, (40-19)/2);
		unsecureField.setSpace((40-24)/2, (40-19)/2);
	}

	private boolean isVisible(Field f) { return f.getIndex() != -1; }
	private void clean() {
		switch (currentCallState) {
		case CallStateENDCALL:
			break;
		case CallStateINCALL:
			if (isVisible(incallField)) delete(incallField);
			break;
		case CallStateSECURE:
			if (isVisible(secureField)) delete(secureField);
			break;
		case CallStateUNSECURE:
			if (isVisible(unsecureField)) delete(unsecureField);
			break;
		}
	}

	public void setCallState(int callState) {
		if (dontShowUnsecure && callState == CallStateUNSECURE) return;
		if (callState == currentCallState) return;
		if (callState == CallStateINCALL && currentCallState == CallStateSECURE) return;
		if (callState == CallStateINCALL && currentCallState == CallStateUNSECURE) return;

		clean();
		switch (callState) {
		case CallStateENDCALL:
			break;
		case CallStateINCALL:
			add(incallField);
			break;
		case CallStateSECURE:
			add(secureField);
			break;
		case CallStateUNSECURE:
			add(unsecureField);
			break;
		}
		currentCallState=callState;
	}
}
