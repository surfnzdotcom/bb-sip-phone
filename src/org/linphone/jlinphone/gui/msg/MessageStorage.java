/*
Copyright (C) 2012  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package org.linphone.jlinphone.gui.msg;

import java.util.Date;
import java.util.Vector;

import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.database.DatabaseFactory;
import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.io.URI;

import org.linphone.core.LinphoneAddress;
import org.linphone.jlinphone.gui.Custom;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.sal.SalListener.MessageEvent;

/**
 * @author guillaume Beraudo
 */
public abstract class MessageStorage {

	protected static URI dbUri;

	protected Vector mThreadItems = new Vector();
	protected static final Logger sLogger=JOrtpFactory.instance().createLogger(Custom.APPNAME+" - MessageStorage");
	protected static final void fatal(String msg, Throwable e) {
		sLogger.fatal(msg, e);
		String eMsg=e.getMessage();
		throw new RuntimeException(msg + " " + eMsg);
	}
	protected String activePeer;

	private static MessageStorage instance;
	public synchronized static final MessageStorage getInstance() {
		if (instance == null) {
			dbUri=createUriQuietly(DatabaseFactory.getDefaultRoot()+"messages.db");
			if (dbUri != null) {
				try {
					PersistentMessageStorage.createDatabase(dbUri);
				} catch (DatabaseException e) {
					sLogger.error("Couldn't create messages database " + dbUri);
					dbUri=null;
				}
			}
			if (dbUri != null)
				instance = new PersistentMessageStorage();
			else {
				sLogger.debug("Using memory message storage");
				instance = new MemoryMessageStorage();
			}
		}
		return instance;
	}
	public interface ThreadItem {
		String getUri();
		String getDisplayName();
		boolean hasUnread();
		int getMsgCount();
	}

	public interface MessageItem {
		public static final int DIR_SENT =1;
		public static final int DIR_RECEIVED=2;

		String getUri();
		String getContent();
		int getDirection();
		int getStatus();
		long getId();
		String getErrorMsg();
	}

	public ThreadItem getThreadItem(LinphoneAddress peer) {
		ThreadItem thread=loadThread(peer.asStringUriOnly());
		if (thread == null) {
			thread = new SimpleThreadItem(peer);
			mThreadItems.addElement(thread);
		}
		return thread;
	}

	public ThreadItem[] listThreads() {
		return arrayItems();
	}

	public synchronized void removeThread(String uri) {
		int size=mThreadItems.size();
		for (int i=0; i < size; ++i) {
			ThreadItem item=(ThreadItem) mThreadItems.elementAt(i);
			if (item.getUri().equals(uri)) {
				mThreadItems.removeElementAt(i);
				return;
			}
		}
	}

	public synchronized void removeAllThreads() {
		mThreadItems.removeAllElements();
	}

	public synchronized MessageItem sendMsg(String uri, String msg) {
		SimpleThreadItem thread=loadThread(uri);
		if (thread == null) thread = new SimpleThreadItem(uri, uri);
		SimpleMessageItem item = new SimpleMessageItem();
		item.direction=MessageItem.DIR_SENT;
		item.message=msg;
		item.status=MessageEvent.PROGRESS;
		item.uri=uri;
		item.read=true;

		thread.messages.addElement(item);
		thread.totalCount++;
		return item;
	}

	public synchronized MessageItem receivedMsg(String uri, String message, Date now) {
		SimpleThreadItem thread=loadThread(uri);
		if (thread == null) {
			thread = new SimpleThreadItem(uri, uri);
			mThreadItems.addElement(thread);
		}
		SimpleMessageItem item = new SimpleMessageItem();
		item.direction=MessageItem.DIR_RECEIVED;
		item.message=message;
		item.status=MessageEvent.SUCCESS;
		item.uri=uri;
		item.thread=thread;
		item.read=uri.equalsIgnoreCase(activePeer);

		thread.messages.addElement(item);
		thread.totalCount++;
		if (!item.read) thread.unreadCount++;
		return item;
	}

	public abstract void updateSentMsg(Object opaque, LinphoneAddress to,
			int event, String phrase);

	public abstract Vector getAllMessages(LinphoneAddress peer);



	public void setActivePeer(LinphoneAddress peer) {
		if (peer != null) {
			SimpleThreadItem thread=(SimpleThreadItem)getThreadItem(peer);
			thread.unreadCount=0;
			activePeer=peer.asStringUriOnly();
		} else {
			activePeer=null;
		}

	}









	protected static class SimpleMessageItem implements MessageItem {
		private static long seq=0;
		String uri;
		String message;
		int direction;
		int status;
		String error;
		long id;
		ThreadItem thread;
		long time;
		boolean read=false;

		public SimpleMessageItem() {
			id=seq++;
		}

		public String getUri() {return uri;}
		public String getContent() {return message;}
		public int getDirection() {return direction;}
		public int getStatus() {return status;}
		public long getId() {return id;}
		public String getErrorMsg() {return error;}
	}






	protected static class SimpleThreadItem implements ThreadItem {
		String label;
		String uri;
		Vector messages = new Vector();
		public int unreadCount;
		public int totalCount;

		public SimpleThreadItem(String uri, String label) {
			super();
			this.uri=uri;
			this.label = label;
		}

		private static final String helperDisplay(LinphoneAddress peer) {
			String display=peer.getDisplayName();
			if (peer.getDisplayName() != null && peer.getDisplayName().length() > 0) {
				display=peer.getDisplayName();
			} else {
				display=peer.asStringUriOnly();
			}
			return display;
		}
		public SimpleThreadItem(LinphoneAddress peer) {
			uri=peer.asStringUriOnly();
			label=helperDisplay(peer);
		}
		public String getDisplayName() {
			return label;
		}
		public String getUri() {
			return uri;
		}
		protected Vector getMessages() {
			return messages;
		}

		public boolean hasUnread() {
			return unreadCount > 0;
		}

		public int getMsgCount() {
			return totalCount;
		}
	}








	// Helper methods
	protected SimpleThreadItem loadThread(String uri) {
		for (int i=0; i < mThreadItems.size(); ++i) {
			SimpleThreadItem item=(SimpleThreadItem) mThreadItems.elementAt(i);
			if (item.uri.equalsIgnoreCase(uri)) return item;
		}
		return null;
	}

	protected SimpleMessageItem findMessage(ThreadItem thread, Object opaque) {
		long opaqueLong=((Long)opaque).longValue();
		if (thread == null) return null;
		Vector messages=((SimpleThreadItem)thread).getMessages();
		for (int i=0; i < messages.size(); ++i) {
			SimpleMessageItem f=(SimpleMessageItem)messages.elementAt(i);
			if (f.id == opaqueLong) return f;
		}
		return null;
	}

	protected ThreadItem[] arrayItems() {
		int size=mThreadItems.size();
		ThreadItem[] result=new ThreadItem[size];
		for (int i=0; i < size; ++i) {
			result[i]=(ThreadItem) mThreadItems.elementAt(i);
		}
		return result;
	}


	private static URI createUriQuietly(String uri) {
		try {
			return URI.create(uri);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MalformedURIException e) {
			e.printStackTrace();
		}
		return null;
	}
}

