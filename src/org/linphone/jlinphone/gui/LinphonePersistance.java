package org.linphone.jlinphone.gui;

import java.util.Hashtable;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.Persistable;

public final class LinphonePersistance implements Persistable {

	private static LinphonePersistance instance;
	private Hashtable data;
	private PersistentObject mPersistentObject;

	private LinphonePersistance() {
		mPersistentObject = PersistentStore.getPersistentObject( "org.jlinphone.persistance".hashCode());
		if (mPersistentObject.getContents() != null && mPersistentObject.getContents() instanceof Hashtable) {
			data = (Hashtable) mPersistentObject.getContents();
		} else {
			data = new Hashtable();
		}
	}

	public static final synchronized LinphonePersistance instance() {
		if (instance == null) instance = new LinphonePersistance();
		return instance;
	}

	public synchronized void commit() {
		synchronized (mPersistentObject) {
			mPersistentObject.setContents(data);
			mPersistentObject.commit();
		}
	}

	public void put(Object key, String value) {
		data.put(key, value);
	}
	public void put(Object key, boolean value) {
		data.put(key, new Boolean(value));
	}

	public boolean getBoolean(String key,boolean defaultValue) {
		boolean lResult = defaultValue;
		if (data != null) {
			Boolean value = (Boolean) data.get(key);
			if (value != null) {
				return value.booleanValue();
			}
		}
		return lResult;
	}
	
	public String getString(String key,String defaultValue) {
		String lResult = defaultValue;
		String value = (String) data.get(key);
		if (value != null) {
			return value;
		}
		return lResult;
	}
}
