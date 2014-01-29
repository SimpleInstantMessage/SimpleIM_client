package org.simpleim.client;

import java.util.Date;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
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
	}
	@FXML
	private void initialize() {
		userList.setCellFactory(new Callback<ListView<Account>, ListCell<Account>>() {
			@Override
			public ListCell<Account> call(ListView<Account> list) {
				return new AccountFormatListCell();
			}
		});
		userList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Account>() {
			@Override
			public void changed(ObservableValue<? extends Account> observable, Account oldValue, Account newValue) {
				chatLog.clear();
				// TODO 更新标题栏
			}
		});
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

	private void appendChatLog(String body, User user, long time) {
		chatLog.appendText(new Date(time).toString() + " \t");
		chatLog.appendText(user.getNikename() != null ? user.getNikename() : user.getId());
		chatLog.appendText('\n' + body + '\n');
	}

	private final ChatClientListener mChatListener = new ChatClientListenerAdapter() {
		@Override
		public void onReceiveChatMessage(final ChatClientHandler handler, final ReceiveMessageNotification message) {
			runInJavaFXApplicationThread(new Runnable() {
				@Override
				public void run() {
					if(userList.getSelectionModel().getSelectedItem().getId().equals(message.getSender().getId())) {
						appendChatLog(message.getMessage().getBody(), message.getSender(), message.getMessage().getSendTime());
					} // TODO else
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
			if(closing) {
				Platform.exit();
			} else {
				// TODO inform user
			}
		}

		private void runInJavaFXApplicationThread(Runnable run) {
			if(Platform.isFxApplicationThread()) {
				run.run();
			} else {
				Platform.runLater(run);
			}
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
			// TODO is it online?
		}
	}
}
