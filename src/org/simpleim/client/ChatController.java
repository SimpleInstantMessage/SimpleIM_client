package org.simpleim.client;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import org.simpleim.client.model.container.Account;
import org.simpleim.client.model.netty.ChatClientHandler;
import org.simpleim.client.model.netty.ChatClientHandler.ChatClientListener;
import org.simpleim.client.model.netty.ChatClientHandler.ChatClientListenerAdapter;
import org.simpleim.common.message.ChatMessage;
import org.simpleim.common.message.LoginNotification;
import org.simpleim.common.message.LogoutNotification;
import org.simpleim.common.message.ReceiveMessageNotification;
import org.simpleim.common.message.SendMessageRequest;
import org.simpleim.common.message.User;

public class ChatController extends Controller {
	private static final String FORMATTER = "%1$tF %1$ta %1$tT %tZ \t%s%n%s%n%n";
	private ChatClientHandler mChatClientHandler;
	private boolean closing = false;
	private ObservableList<Account> mUserList;
	@FXML
	private ListView<Account> userList;
	@FXML
	private TextArea chatLog;
	@FXML
	private TextArea inputMessage;
	@FXML
	private Button send;

	public ChatController setChatClientHandler(ChatClientHandler handler) {
		if(mChatClientHandler == handler)
			return this;
		if(mChatClientHandler != null)
			mChatClientHandler.removeListener(mChatListener);
		mChatClientHandler = handler;
		if(mChatClientHandler != null)
			mChatClientHandler.addListenerIfAbsent(mChatListener);
		return this;
	}
	public ChatController setUserList(ObservableList<Account> userList) {
		mUserList = userList;
		this.userList.setItems(mUserList);
		if(!mUserList.isEmpty())
			this.userList.getSelectionModel().selectFirst();
		return this;
	}

	@Override
	public void setMainApp(MainApp mainApp) {
		super.setMainApp(mainApp);
		mainApp.getPrimaryStage().getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				mChatClientHandler.logout();
				closing = true;
				event.consume();
			}
		});
		KeyCodeCombination keyCombination = new KeyCodeCombination(KeyCode.ENTER, KeyCodeCombination.CONTROL_DOWN);
		mainApp.getPrimaryStage().getScene().getAccelerators().put(keyCombination,
				new Runnable() {
					@Override
					public void run() {
						send.fire();
					}
				});
		send.setText(send.getText() + "(" + keyCombination.getName() + ")");
	}
	@FXML
	private void initialize() {
		userList.setCellFactory(new Callback<ListView<Account>, ListCell<Account>>() {
			@Override
			public ListCell<Account> call(ListView<Account> list) {
				return new AccountFormatListCell();
			}
		});
		userList.getSelectionModel().selectedItemProperty().addListener(mSelectedItemChangeListener);
	}

	@FXML
	private void handleSend() { // TODO add a shortcut key
		String body = inputMessage.getText();
		if(body.isEmpty()) {
			// TODO inform Please input message in inputMessage TextArea first
			return;
		}
		Account target = userList.getSelectionModel().getSelectedItem();
		if(target == null) {
			// TODO inform Please select chat friend in userList ListView first
			return;
		}
		inputMessage.clear();
		SendMessageRequest request = new SendMessageRequest();
		request.setMessage(new ChatMessage().setBody(body))
				.setSender(new User().setId(mChatClientHandler.getAccount().getId()))
				.setTargetsIds(new String[]{target.getId()});
		mChatClientHandler.send(request);
		appendChatLog(body, request.getSender(), request.getMessage().getSendTime());
	}

	/**
	 * Refreshes the {@link #userList}. This is only necessary if an item that is already in
	 * the list is changed. New and deleted items are refreshed automatically.
	 * <p>
	 * This is a workaround because otherwise we would need to use property
	 * bindings in the model class and add a *property() method for each
	 * property. Maybe this will not be necessary in future versions of JavaFX
	 * (see http://javafx-jira.kenai.com/browse/RT-22599)
	 */
	private void refreshUserList() {
		userList.getSelectionModel().selectedItemProperty().removeListener(mSelectedItemChangeListener);
		int selectedIndex = userList.getSelectionModel().getSelectedIndex();
		userList.setItems(null);
		userList.layout();
		this.userList.setItems(mUserList);
		// Must set the selected index again (see http://javafx-jira.kenai.com/browse/RT-26291)
		userList.getSelectionModel().select(selectedIndex);
		userList.getSelectionModel().selectedItemProperty().addListener(mSelectedItemChangeListener);
	}

	private void appendChatLog(String body, User user, long time) {
		chatLog.appendText(formatChatMessage(body, user, time));
	}
	private static String formatChatMessage(String body, User user, long time) {
		return String.format(FORMATTER, time, user.getNikename() != null ? user.getNikename() : user.getId(), body);
	}

	private final ChangeListener<Account> mSelectedItemChangeListener = new ChangeListener<Account>() {
		@Override
		public void changed(ObservableValue<? extends Account> observable, Account oldValue, Account newValue) {
			ChatLog chatlog = null;
			if(oldValue != null) {
				chatlog = (ChatLog) oldValue.getAttachment();
				if(chatlog == null)
					oldValue.setAttachment(chatlog = new ChatLog());
				chatlog.chatLog = chatLog.getText();
			}
			if(newValue != null && ((chatlog = (ChatLog) newValue.getAttachment()) != null)) {
				chatLog.setText(chatlog.chatLog);
				chatlog.numberOfNewMessage = 0;
				refreshUserList();
			} else
				chatLog.clear();
			// TODO 更新标题栏
		}
	};

	private final ChatClientListener mChatListener = new ChatClientListenerAdapter() {
		@Override
		public void onReceiveChatMessage(final ChatClientHandler handler, final ReceiveMessageNotification message) {
			runInJavaFXApplicationThread(new Runnable() {
				@Override
				public void run() {
					String senderId = message.getSender().getId();
					if(senderId.equals(userList.getSelectionModel().getSelectedItem().getId())) {
						appendChatLog(message.getMessage().getBody(), message.getSender(), message.getMessage().getSendTime());
					} else {
						for(Account account : mUserList) {
							if(senderId.equals(account.getId())) {
								ChatLog chatlog = (ChatLog) account.getAttachment();
								if(chatlog == null)
									account.setAttachment(chatlog = new ChatLog());
								chatlog.chatLog += formatChatMessage(
										message.getMessage().getBody(), message.getSender(), message.getMessage().getSendTime());
								chatlog.numberOfNewMessage++;
								refreshUserList();
								break;
							}
						}
					}
				}
			});
		}

		@Override
		public void onReceiveLoginNotification(final ChatClientHandler handler, final LoginNotification message) {
			runInJavaFXApplicationThread(new Runnable() {
				@Override
				public void run() {
					mUserList.add(new Account().setId(message.getNewUserId()));
					if(mUserList.size() == 1)
						userList.getSelectionModel().selectFirst();
				}
			});
		}

		@Override
		public void onReceiveLogoutNotification(final ChatClientHandler handler, final LogoutNotification message) {
			if(message.getUserLoggedOutId().equals(handler.getAccount().getId())) {
//				Platform.exit();
			} else {
				runInJavaFXApplicationThread(new Runnable() {
					@Override
					public void run() {
						for(Account account : mUserList) {
							if(message.getUserLoggedOutId().equals(account.getId())) {
								mUserList.remove(account);
								break;
							}
						}
						if(!mUserList.isEmpty())
							userList.getSelectionModel().selectFirst();
					}
				});
			}
		}

		@Override
		public void onChannelInactive(ChatClientHandler handler) {
			runInJavaFXApplicationThread(new Runnable() {
				@Override
				public void run() {
					changeToLoginScene();
					if(!closing)
						;// TODO inform user lost connection
				}
			});
		}

		private void changeToLoginScene() {
			mChatClientHandler.removeListener(mChatListener);
			mainApp.getPrimaryStage().getScene().getWindow().setOnCloseRequest(null);
			mainApp.showLoginView();
		}
	};

	private class AccountFormatListCell extends ListCell<Account> {
		@Override
		protected void updateItem(Account item, boolean empty) {
			super.updateItem(item, empty);
			if(item == null)
				return;
			setText(item.getNickname() == null ? item.getId() : item.getNickname());
			if(item.getId().equals(mChatClientHandler.getAccount().getId())) {
				setText(getText() + " (me)");
				setTextFill(Color.ORANGE);
			}
			ChatLog chatlog = (ChatLog) item.getAttachment();
			if(chatlog != null) {
				if(chatlog.numberOfNewMessage > 1) {
					setText(getText() + " (" + chatlog.numberOfNewMessage + " messages)");
					setStyle("-fx-font-weight: bold;");
				} else if (chatlog.numberOfNewMessage == 1) {
					setText(getText() + " (1 message)");
					setStyle("-fx-font-weight: bold;");
				} else {
					setStyle("");
				}
			}
			// TODO is it online?
		}
	}

	private static class ChatLog {
		private String chatLog = "";
		private int numberOfNewMessage = 0;
	}
}
