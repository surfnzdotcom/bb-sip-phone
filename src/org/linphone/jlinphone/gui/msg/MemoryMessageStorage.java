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

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatMessage.State;

/**
 * Store conversation threads and associated messages.
 * Use base class for threads handling.
 * 
 * @author guillaume Beraudo
 */
public final class MemoryMessageStorage extends MessageStorage {

	
	public ThreadItem getThreadItem(LinphoneAddress peer) {
		ThreadItem thread=loadThread(peer.asStringUriOnly());
		if (thread == null) {
			thread = new SimpleThreadItem(peer);
			mThreadItems.addElement(thread);
		}
		return thread;
	}

	MemoryMessageStorage() {
	}
	

	public synchronized MessageItem sendMsg(String uri, String msg) {
		return super.sendMsg(uri, msg);
	}


	public synchronized MessageItem receivedMsg(String uri, String message, Date now) {
		return super.receivedMsg(uri, message);
	}

	public synchronized void updateSentMsg(LinphoneChatMessage msg, State event, String phrase) {
		ThreadItem thread=loadThread(msg.getPeerAddress().asStringUriOnly());
		Long id=(Long) msg.getUserData();
		SimpleMessageItem message=findMessage(thread, id.longValue());
		if (message!=null) {
			message.state=event;
			message.error=phrase;
		}
	}


	public Vector getAllMessages(LinphoneAddress peer) {
		ThreadItem thread=getThreadItem(peer);
		return ((SimpleThreadItem)thread).getMessages();
	}

}
