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

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.DataTypeException;
import net.rim.device.api.database.Database;
import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.database.DatabaseFactory;
import net.rim.device.api.database.Row;
import net.rim.device.api.database.Statement;
import net.rim.device.api.io.URI;

import org.linphone.core.LinphoneAddress;

/**
 * Store conversation threads and associated messages.
 * Use base class for thread handling.
 * 
 * @author guillaume Beraudo
 */
public final class PersistentMessageStorage extends MessageStorage {

	public static final String DATABASE_SCHEMA = 
			"CREATE TABLE Message("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT"
					+ ", peer TEXT NOT NULL"
					+ ", direction INTEGER"
					+ ", message TEXT NOT NULL"
					+ ", time NUMERIC"
					+ ", status INTEGER"
					+ ", unread INTEGER"
					+")";
	private Database db;


	PersistentMessageStorage() {
		openDb();
	}


	public synchronized ThreadItem[] listThreads() {
		mThreadItems=new Vector();

		Statement st=null;
		try {
			st=db.createStatement("SELECT peer, sum(unread), count(*) "
					+ "FROM Message GROUP BY peer");
			st.prepare();

			Cursor c=st.getCursor();
			while (c.next()) {
				Row r=c.getRow();
				String peer=r.getString(0);
				SimpleThreadItem item = new SimpleThreadItem(peer, peer);
				item.unreadCount=r.getInteger(1);
				item.totalCount=r.getInteger(2);
				mThreadItems.addElement(item);
			}
		} catch (Exception e) {
			fatal("Couldn't retrieve messages",e);
		} finally {
			try {
				if (st !=null) st.close();
			} catch (DatabaseException e) {
			}
		}
		return arrayItems();
	}

	public synchronized void removeThread(String uri) {
		super.removeThread(uri);
		Statement st=null;
		try {
			st=db.createStatement("DELETE FROM Message where peer = ?");
			st.prepare();
			st.bind(1, uri);
			st.execute();
		} catch (DatabaseException e) {
			fatal("Couldn't remove thread " + uri,e);
		} finally {
			try {
				if (st !=null) st.close();
			} catch (DatabaseException e) {
			}
		}
	}


	public synchronized void removeAllThreads() {
		super.removeAllThreads();
		Statement st=null;
		try {
			st=db.createStatement("DELETE ALL FROM Message");
			st.prepare();
			st.execute();
		} catch (DatabaseException e) {
			fatal("Couldn't remove threads",e);
		} finally {
			try {
				if (st !=null) st.close();
			} catch (DatabaseException e) {
			}
		}
	}

	public synchronized MessageItem sendMsg(String uri, String msg) {
		SimpleMessageItem item = (SimpleMessageItem) super.sendMsg(uri, msg);
		insertMessageInDatabase(item);
		return item;
	}


	public synchronized MessageItem receivedMsg(String uri, String message, Date now) {
		SimpleMessageItem item = (SimpleMessageItem) super.receivedMsg(uri, message, now);
		insertMessageInDatabase(item);
		return item;
	}

	public synchronized void updateSentMsg(Object opaque, LinphoneAddress to, int event, String phrase) {
		Long id=(Long)opaque;
		Statement st=null;
		try {
			st=db.createStatement("UPDATE Message SET status=? where id = ?");
			st.prepare();
			st.bind(1, event);
			st.bind(2, id.longValue());
			st.execute();
		} catch (DatabaseException e) {
			fatal("Couldn't update message " + id,e);
		} finally {
			try {
				if (st !=null) st.close();
			} catch (DatabaseException e) {
			}
		}
	}


	public Vector getAllMessages(LinphoneAddress peer) {
		Vector messages=new Vector();

		Statement st=null;
		try {
			st=db.createStatement("SELECT id,peer,direction,message,time,status,unread"
					+ " FROM Message where peer = ?");
			st.prepare();
			st.bind(1, peer.asStringUriOnly());

			Cursor c=st.getCursor();
			while (c.next()) {
				Row r=c.getRow();
				SimpleMessageItem item=parseMessageRow(r);
				messages.addElement(item);
			}
		} catch (DatabaseException e) {
			fatal("Couldn't retrieve messages",e);
		} finally {
			try {
				if (st !=null) st.close();
			} catch (DatabaseException e) {
			}
		}

		return messages;
	}


	public void setActivePeer(LinphoneAddress peer) {
		super.setActivePeer(peer);
		if (activePeer != null) {
			Statement st=null;
			try {
				st=db.createStatement("UPDATE Message SET unread=0 where peer = ? and unread!=0");
				st.prepare();
				st.bind(1, activePeer);
				st.execute();
			} catch (DatabaseException e) {
				fatal("Couldn't mark messages as read " +activePeer,e);
			} finally {
				try {
					if (st !=null) st.close();
				} catch (DatabaseException e) {
				}
			}
		}
	}





	// Helper functions

	private SimpleMessageItem parseMessageRow(Row r) {
		SimpleMessageItem item=new SimpleMessageItem();
		int i=0;
		try {// r.getInteger(i)
			item.id=r.getLong(i++);
			item.uri=r.getString(i++);
			item.direction=r.getInteger(i++);
			item.message=r.getString(i++);
			item.time=r.getLong(i++);
			item.status=r.getInteger(i++);
			item.read=r.getInteger(i++) == 0;
		} catch (DataTypeException e) {
			fatal("Parsing message row", e);
			return null;
		}
		return item;
	}

	private void insertMessageInDatabase(SimpleMessageItem item) {
		Statement st=null;
		try {
			st=db.createStatement("INSERT INTO Message "
					+ "(peer,direction,message,time,status,unread)"
					+ " VALUES (?,?,?,?,?,?);");
			st.prepare();
			st.bind(1, item.uri);
			st.bind(2, item.direction);
			st.bind(3, item.message);
			st.bind(4, new Date().getTime());
			st.bind(5, item.status);
			st.bind(6, item.read?0:1);
			st.execute();
			item.id=db.lastInsertedRowID();
		} catch (DatabaseException e) {
			fatal("Couldn't insert message",e);
		} finally {
			try {
				if (st !=null) st.close();
			} catch (DatabaseException e) {
			}
		}
	}

	private void openDb() {
		try {
			if (DatabaseFactory.exists(dbUri)) {
				db=DatabaseFactory.open(dbUri);				
			} else {
				throw new RuntimeException("Database " + dbUri + " doesn't exists");
			}
		} catch (Exception e) {
			fatal("Couldn't open db", e);
		}
	}


	static void createDatabase(URI dbUri) throws DatabaseException {
		if (DatabaseFactory.exists(dbUri)) return;
		Database db=null;
		Statement st=null;
		try {
			db = DatabaseFactory.create(dbUri);
			st=db.createStatement(DATABASE_SCHEMA);
			st.prepare();
			st.execute();
		} finally {
			if (st !=null) st.close();
			if (db !=null) db.close();
		}
	}


}
