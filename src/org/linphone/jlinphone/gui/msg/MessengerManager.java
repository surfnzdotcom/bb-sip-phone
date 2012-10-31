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

import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;
import net.rim.device.api.util.StringProvider;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatMessage.State;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.jlinphone.core.LinphoneAddressImpl;
import org.linphone.jlinphone.core.LinphoneChatMessageImpl;
import org.linphone.jlinphone.core.LinphoneCoreImpl;
import org.linphone.jlinphone.core.LinphoneCoreImpl.OnMessageListener;
import org.linphone.jlinphone.gui.AdvancedSearchableContactList;
import org.linphone.jlinphone.gui.LinphoneMain;
import org.linphone.jlinphone.gui.LinphoneResource;
import org.linphone.jlinphone.gui.LinphoneScreen;
import org.linphone.jlinphone.gui.TabFieldItem;
import org.linphone.jlinphone.gui.msg.MessageStorage.MessageItem;
import org.linphone.jlinphone.gui.msg.MessageStorage.ThreadItem;

/**
 * @author guillaume Beraudo
 *
 */
public class MessengerManager extends VerticalFieldManager implements TabFieldItem, LinphoneResource, OnMessageListener {

	private static Bitmap successIcon = Bitmap.getBitmapResource("chat_message_delivered.png");
	private static Bitmap progressIcon = Bitmap.getBitmapResource("chat_message_inprogress.png");
	private static Bitmap failureIcon = Bitmap.getBitmapResource("chat_message_not_delivered.png");


	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private LinphoneCoreImpl mCore;
	private MessageStorage mMessageStorage=MessageStorage.getInstance();

	private ConversationField mConversationMgr;

	private Manager mThreadsListMgr = new VerticalFieldManager();
	private ThreadsListField mThreadsListField;

	private ContactChooser mContactChooserMgr;
	private final Font mBaseFont;

	private static final int CONVERSATION =1;
	private static final int THREADS=2;
	private static final int CHOOSER=3;

	public MessengerManager(LinphoneCore core) {
		super();
		mBaseFont=Font.getDefault();
		mConversationMgr = new ConversationField();
		mContactChooserMgr = new ContactChooser();
		mCore=(LinphoneCoreImpl)core;
		initializeThreadContent();
		mCore.setMessageListener(this);
	}


	private void onTextReceived(final LinphoneAddress from, final String message) {
		if (shouldNotifyUserOfIncomingMsg(from)) {
			NotificationsManager.triggerImmediateEvent(LinphoneMain.NOTIF_ID, 0, null, null);
		}
		if (isMgrDisplayed(mThreadsListMgr)) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					if (!mThreadsListField.contains(from)) {
						mThreadsListField.refresh();
					} else {
						mThreadsListField.invalidate();
					}
				}
			});
		}

		if (isMgrDisplayed(mConversationMgr) && LinphoneAddressImpl.equalsUserAndDomain(from, mConversationMgr.getPeer())) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					mConversationMgr.addReceivedMsg(message);
					mConversationMgr.scrollDown();
				}
			});
		}

	}


	MenuItem composeMenu=new MenuItem(provideString(MSG_COMPOSE), 110, 10) {
		public void run() {
			display(CHOOSER);
		}
	};

	MenuItem deleteMenu=new MenuItem(provideString(MSG_DELETE), 110, 10) {
		public void run() {
			Dialog.inform("Delete");
		}
	};

	MenuItem deleteAllMenu=new MenuItem(provideString(MSG_DELETE_ALL), 110, 10) {
		public void run() {
			Dialog.inform("Delete all");
		}
	};

	protected void makeContextMenu(ContextMenu cm) {
		cm.addItem(composeMenu);
		cm.addItem(deleteMenu);
		cm.addItem(deleteAllMenu);
	}

	public void onSelected() {
		// Display conversation if in call or threads otherwise
		NotificationsManager.cancelImmediateEvent(LinphoneMain.NOTIF_ID, 0, this, null);
		LinphoneCall currentCall=mCore.getCurrentCall();
		if (currentCall != null) {
			if (!isMgrDisplayed(mConversationMgr)) {
				mConversationMgr.reset(currentCall.getRemoteAddress());
				display(CONVERSATION);
			}
		} else if (!isMgrDisplayed(mThreadsListMgr)) {
			display(THREADS);
		}
	}

	public void onUnSelected() {
		// TODO Auto-generated method stub

	}

	public boolean keyChar(char ch, int status, int time) {
		return super.keyChar(ch, status, time);
	}


	/**
	 * Back to threads from conversation if not in call.
	 */
	public boolean navigateBack() {
		if (mCore.isIncall()) return false;
		if (isMgrDisplayed(mConversationMgr)) {
			mMessageStorage.setActivePeer(null);
			display(THREADS);
			return true;
		}
		return false;
	}







	private static interface MessageField {
		long getMessageId();
	}



	/**
	 * Displays a given conversation, passed or active.
	 * @author guillaume
	 */
	private class ConversationField extends VerticalFieldManager implements LinphoneChatMessage.StateListener{
		private ContactField mRemoteContact= new ContactField();
		private LinphoneChatRoom mChatRoom;
		private Manager mMessages= new VerticalFieldManager(VERTICAL_SCROLL|USE_ALL_WIDTH|USE_ALL_HEIGHT) {
			boolean wasAlreadyFocused;
			protected void sublayout(int maxWidth, int maxHeight) {
				super.sublayout(maxWidth, maxHeight);
			};
			protected void onFocus(int direction) {
				if (wasAlreadyFocused) super.onFocus(direction);
				else if (getFieldCount() > 0){
					wasAlreadyFocused=true;
					getField(getFieldCount()-1).setFocus();
				}
			};
			protected void onUndisplay() {
				wasAlreadyFocused=false;
			};
		};
		private ConversationEditText mTextInput= new ConversationEditText();

		public ConversationField() {
			super(USE_ALL_WIDTH);
			mRemoteContact.setFont(mBaseFont.derive(mBaseFont.getStyle(), mBaseFont.getHeight()+4));
			add(mRemoteContact);
			add(mMessages);
			add(mTextInput);
		}

		protected void sublayout(int maxWidth, int maxHeight) {
			int width= Math.min(Display.getWidth(), getPreferredWidth());
			//int height= Math.min(Display.getHeight(), getPreferredHeight());
			int height= Display.getHeight() - getTitleHeight();

			int contactHeight=mRemoteContact.getPreferredHeight();
			layoutChild(mRemoteContact,width,contactHeight);
			contactHeight=mRemoteContact.getHeight();
			setPositionChild(mRemoteContact, 0, 0);

			int inputHeight=mTextInput.getPreferredHeight();
			layoutChild(mTextInput,width,inputHeight);
			inputHeight=mTextInput.getHeight();
			int inputYPos=height-inputHeight;
			setPositionChild(mTextInput, 0, inputYPos);


			int remainingHeight=height-inputHeight-contactHeight;
			layoutChild(mMessages,width,remainingHeight);
			setPositionChild(mMessages, 0, contactHeight);

			setExtent(width, height);
		}
		
		public LinphoneAddress getPeer() {
			return mRemoteContact.mPeer;
		}

		private void scrollDown() {
			int preferred=mMessages.getPreferredHeight();
			mMessages.setVerticalScroll(preferred, true);
		}

		public void addSentMsg(MessageItem msg) {
			SentMessageField f=new SentMessageField(msg, FIELD_RIGHT);
			mMessages.add(f);
		}
		public void addReceivedMsg(String str) {
			ReceivedMessageField f=new ReceivedMessageField(str, FIELD_LEFT);
			mMessages.add(f);
		}
		public void reset(LinphoneAddress peer) {
			mRemoteContact.setContact(peer);
			mMessages.deleteAll();
			mMessageStorage.setActivePeer(peer);
			Vector msgs=mMessageStorage.getAllMessages(peer);
			MessageItem item=null;
			for (int i =0; i < msgs.size(); i++) {
				item = (MessageItem) msgs.elementAt(i);
				if (item.getDirection() == MessageItem.DIR_RECEIVED) {
					addReceivedMsg(item.getContent());
				} else {
					addSentMsg(item);
				}
			}
			mTextInput.setText("");
		}

		public void updateMessage(Object opaque, State state) {
			long id=((Long)opaque).longValue();
			for (int i=mMessages.getFieldCount()-1; i >= 0; --i) {
				MessageField f=(MessageField)mMessages.getField(i);
				if (f.getMessageId() == id) {
					SentMessageField sent=(SentMessageField) f;
					sent.onEvent(state);
					return;
				}
			}
			// Error
		}
		private int getTitleHeight() {
			return ((LinphoneScreen)getScreen()).getTitlePreferredHeight();
		}

		

		private class ContactField extends HorizontalFieldManager {
			private LabelField field=new LabelField("",USE_ALL_WIDTH);
			private LinphoneAddress mPeer;

			public ContactField() {
				super(USE_ALL_WIDTH);
				field.setBackground(BackgroundFactory.createSolidBackground(Color.DARKGRAY));
				//				field.setBorder(BorderFactory.createSimpleBorder(new XYEdges(1, 1, 1, 1)));
				add(field);
				field.setFont(mBaseFont.derive(mBaseFont.getStyle(), mBaseFont.getHeight()+4));
			}

			void setContact(LinphoneAddress peer) {
				if (!LinphoneAddressImpl.equalsUserAndDomain(peer, mPeer)) {
					mChatRoom=mCore.createChatRoom(peer.asStringUriOnly());
					if (mChatRoom == null) {
						Status.show("Error creating chat room", 500);
						navigateBack();
						return;
					}
				}
				mPeer=peer;
				String display=mPeer.getDisplayName();

				if (display != null && display.length() > 0) {
					field.setText(display + " [" + mPeer.asStringUriOnly() + "]");					
				} else {
					field.setText(mPeer.asStringUriOnly());
				}
			}
		}

		private void boxize(Field field) {
			field.setBorder(BorderFactory.createRoundedBorder(
					new XYEdges(3, 3, 3, 3),
					Color.BLACK,
					Border.STYLE_FILLED));
			field.setBackground(BackgroundFactory.createSolidBackground(Color.DARKGRAY));
			field.setPadding(2, 2, 2, 2);
			field.setMargin(3, 3, 3, 3);
			field.setFont(mBaseFont.derive(mBaseFont.getStyle(), mBaseFont.getHeight()+2));
		}

		private class SentMessageField extends LabelField implements MessageField {
			MessageItem item;
			LinphoneChatMessage.State status;

			Bitmap getIcon() {
				if (status == LinphoneChatMessage.State.Delivered) {
					return successIcon;
				} else if (status == LinphoneChatMessage.State.InProgress) {
					return progressIcon;
				} else if (status == LinphoneChatMessage.State.NotDelivered) {
					return failureIcon;
				} else {
					return progressIcon;
				}
			}

			private String labelize(MessageItem msg, State status) {
				String label= "    " + msg.getContent();
				String error=msg.getErrorMsg();
				if (error != null && error.length() > 0) label+= "\n" + error;
				return label;
			}

			public SentMessageField(MessageItem item, long style) {
				super("", style|FOCUSABLE);
				this.item = item;
				onEvent(item.getState());
				boxize(this);
			}

			protected void paint(Graphics g) {
				super.paint(g);
				try {
					int yorig=(getFont().getHeight()-17)/2;
					g.drawBitmap(3, yorig, 18, 17, getIcon(), 0, 0);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
			}
			public long getMessageId() {
				return item.getId();
			}

			public void onEvent(State event) {
				// Hopefully the item has been updated
				status=event;
				setText(labelize(item, event));
				//				updateLayout();
			}
		}


		private class ReceivedMessageField extends LabelField implements MessageField {
			public ReceivedMessageField(String msg, long style) {
				super(msg, style|FOCUSABLE);
				boxize(this);
			}

			public long getMessageId() {
				return -1;
			}
		}

		private class ConversationEditText extends AutoTextEditField {

			private static final boolean simulateRecvdMsg = false;
			public ConversationEditText() {
				super("->", "", 150, FOCUSABLE|EDITABLE);
				setBackground(BackgroundFactory.createSolidBackground(Color.WHITE));
			}

			void sendMessage(String msg) {
				if (!simulateRecvdMsg || !msg.startsWith(" ")) {
					String uri=mRemoteContact.mPeer.asStringUriOnly();
					MessageItem item = mMessageStorage.sendMsg(uri, msg);
					mConversationMgr.addSentMsg(item);
					LinphoneChatMessageImpl lcm=LinphoneChatMessageImpl.createSentMessage(item, null, mRemoteContact.mPeer);
					mChatRoom.sendMessage(lcm, ConversationField.this);
				} else {
					mConversationMgr.addReceivedMsg(msg);
				}
				scrollDown();
				setText("");
			}
			protected boolean keyChar(char key, int status, int time) {
				if (key =='\n') {
					if (mTextInput.getText().length() > 0) sendMessage(getText());
					return true;
				}
				return super.keyChar(key, status, time);
			}
		}

		public void onLinphoneChatMessageStateChanged(final LinphoneChatMessage msg,
				final State state) {
			//		getLinphoneScreen().displayStatus(mCore, "Message event " + event + " from " + to.asStringUriOnly());
			if (!isMgrDisplayed(mConversationMgr)) return;

			LinphoneAddress p1=msg.getPeerAddress();
			LinphoneAddress p2=mConversationMgr.getPeer();
			if (!LinphoneAddressImpl.equalsUserAndDomain(p1, p2)) return;

			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					mConversationMgr.updateMessage(msg.getUserData(), state);
				}
			});
		}
	}






	/**
	 * Displays the list of past conversations.
	 * @author guillaume
	 */
	private class ThreadsListField extends ListField {
		private ThreadItem[] mThreadItems;

		public ThreadsListField() {
			super();
			setCallback(new Cb());
			mThreadItems=new ThreadItem[0];
		}

		public boolean contains(LinphoneAddress from) {
			for (int i=0; i < mThreadItems.length; ++i) {
				ThreadItem item=mThreadItems[i];
				if (item.getUri().equalsIgnoreCase(from.asStringUriOnly())) {
					return true;
				}
			}
			return false;
		}

		private class Cb implements ListFieldCallback {

			public void drawListRow(ListField list, Graphics g, int index, int y, int w) { 
				if (list.getSelectedIndex() !=index) {
					g.setBackgroundColor(index%2==0?Color.LIGHTGRAY:Color.DARKGRAY);
					g.clear();
				}
				ThreadItem item = (ThreadItem) get(list,index);
				String dn=item.getDisplayName();
				g.drawText(dn, 0,y,0,w);
				String count = String.valueOf(item.getMsgCount());
				g.drawText(dn, 0,y,0,w);
				//				int widthLeft=Math.min(0, w - getFont().getAdvance(dn));
				String stats= count + (item.hasUnread() ? "*" : ""); 
				int neededWidth=getFont().getAdvance(stats);
				g.drawText(stats, w-neededWidth, y, 0, neededWidth);
			} 
			public Object get(ListField list, int index) {
				return mThreadItems[index]; 
			} 
			public int indexOfList(ListField list, String prefix, int string) { 
				return -1;
			} 
			public int getPreferredWidth(ListField list) { 
				return Display.getWidth(); 
			}
		}

		protected void makeContextMenu(ContextMenu contextMenu) {
			int selected=getSelectedIndex();

			MenuItem SendMenu=new MenuItem(provideString(MSG_COMPOSE), 110, 10) {
				public void run() {
					display(CHOOSER);
				}
			};
			contextMenu.addItem(SendMenu);

			if (selected > 0) {
				final ThreadItem selectedThread=mThreadItems[selected];
				MenuItem openThreadMenu=new MenuItem(provideString(MSG_OPEN_THREAD, selectedThread.getDisplayName()), 110, 10) {
					public void run() {
						openConversation(selectedThread);
					}
				};
				contextMenu.addItem(openThreadMenu);

				MenuItem deleteMenu=new MenuItem(provideString(MSG_DELETE, selectedThread.getDisplayName()), 110, 10) {
					public void run() {
						mMessageStorage.removeThread(selectedThread.getUri());
						refresh();
					}
				};
				contextMenu.addItem(deleteMenu);
			}

			MenuItem deleteAllMenu=new MenuItem(provideString(MSG_DELETE_ALL), 110, 10) {
				public void run() {
					mMessageStorage.removeAllThreads();
					refresh();
				}
			};
			contextMenu.addItem(deleteAllMenu);

		}

		public void refresh() {
			mThreadItems=mMessageStorage.listThreads();
			setSize(mThreadItems.length);
			invalidate();
		}

		protected boolean keyChar(char key, int status, int time) {
			if (key !='\n') {
				return super.keyChar(key, status, time);
			} else {
				navigationClick(0,0);
				return true;
			}
		}

		private void openConversation(ThreadItem item) {
			LinphoneAddress peer=lcf().createLinphoneAddress(item.getUri());
			peer.setDisplayName(item.getDisplayName());
			mConversationMgr.reset(peer);
			display(CONVERSATION);
		}

		protected boolean navigationUnclick(int status, int time) {
			int selected=getSelectedIndex();
			if (selected >= 0) {
				ThreadItem item=mThreadItems[getSelectedIndex()];
				openConversation(item);
			} else {
				display(CHOOSER);
			}
			return true;
		}
	}



	private class ContactChooser extends AdvancedSearchableContactList {
		protected void onContactChosen(String uri, String displayName) {
			LinphoneAddress peer;
			try {
				peer = mCore.interpretUrl(uri);
			} catch (LinphoneCoreException e) {
				Status.show("Error " + e.getMessage());
				return;
			}
			peer.setDisplayName(displayName);
			mConversationMgr.reset(peer);
			display(CONVERSATION);
		}
	}













	private boolean shouldNotifyUserOfIncomingMsg(LinphoneAddress from) {
		LinphoneScreen ls=getLinphoneScreen();
		if (ls==null) return true;
		if (ls.isDisplayedTab(LinphoneScreen.MESSENGER_TAB_INDEX)) {
			if (isMgrDisplayed(mThreadsListMgr)) return false;
			if (isMgrDisplayed(mConversationMgr)) {
				if (LinphoneAddressImpl.equalUris(from, mConversationMgr.getPeer()))
					return false;
			}
		}
		return true;
		//		if (!mCore.isIncall()) Beeper.beep(Beeper.simpleToneSequence());
	}

	private void initializeThreadContent() {
		/*mThreadsListMgr.add(new LabelField("Thread list!", FIELD_BOTTOM|FOCUSABLE) {
			protected void makeContextMenu(ContextMenu contextMenu) {
				MessengerManager.this.makeContextMenu(contextMenu);
			}
		});*/
		mThreadsListField=new ThreadsListField();
		mThreadsListMgr.add(mThreadsListField);
	}


	private void deleteManager() {
		if (isMgrDisplayed(mThreadsListMgr)) delete(mThreadsListMgr);
		if (isMgrDisplayed(mConversationMgr)) delete(mConversationMgr);
		if (isMgrDisplayed(mContactChooserMgr)) delete(mContactChooserMgr);
		getLinphoneScreen().setFocusToTab(LinphoneScreen.MESSENGER_TAB_INDEX);
	}

	private void display(int code) {
		switch (code) {
		case CONVERSATION:
			deleteManager();
			//		mConversationRemote.setText("Remote is " + currentRemote());
			add(mConversationMgr);
			mConversationMgr.setFocus();
			break;
		case THREADS:
			deleteManager();
			mThreadsListField.refresh();
			add(mThreadsListMgr);
			mThreadsListField.setFocus();
			break;
		case CHOOSER:
			deleteManager();
			add(mContactChooserMgr=new ContactChooser());
			mContactChooserMgr.setFocus();
			break;
		default:
			break;
		}

	}


	private boolean isMgrDisplayed(Manager mgr) {
		return mgr.getManager() == this;
	}

	private StringProvider provideString(int res) {
		return new StringProvider(mRes.getString(res));
	}
	private StringProvider provideString(int res, String trailing) {
		return new StringProvider(mRes.getString(res)+ " " + trailing);
	}

	private LinphoneCoreFactory lcf() { return LinphoneCoreFactory.instance(); }

	private LinphoneScreen getLinphoneScreen() {
		return (LinphoneScreen)getScreen();
	}


	public void onMessageReceived(LinphoneCore lc, LinphoneChatRoom cr,
			LinphoneChatMessage message) {
		onTextReceived(message.getPeerAddress(), message.getMessage());
		
	}


}
