package org.simpleim.client;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

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
}
