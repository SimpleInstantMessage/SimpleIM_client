package org.simpleim.client;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.util.Callback;

import org.simpleim.client.model.container.Account;
import org.simpleim.client.model.netty.ChatClientHandler;

public class ChatController extends Controller {
	private ChatClientHandler mChatClientHandler;
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
		mChatClientHandler = handler;
		//TODO add chatListener
		return this;
	}
	public ChatController setUserList(ObservableList<Account> userList) {
		mUserList = userList;
		this.userList.setItems(mUserList);
		return this;
	}

	@FXML
	private void initialize() {
		userList.setCellFactory(new Callback<ListView<Account>, ListCell<Account>>() {
			@Override
			public ListCell<Account> call(ListView<Account> list) {
				return new AccountFormatListCell();
			}
		});
	}

	private static class AccountFormatListCell extends ListCell<Account> {
		@Override
		protected void updateItem(Account item, boolean empty) {
			super.updateItem(item, empty);
			setText(item == null ? "" : item.getNickname() == null ? item.getId() : item.getNickname());
			// TODO online?
		}
	}
}
