package org.simpleim.client;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import org.simpleim.client.model.container.Account;

public class ChatController extends Controller {
	private ObservableList<Account> mUserList;
	@FXML
	private ListView<Account> userList;
	@FXML
	private TextArea chatLog;
	@FXML
	private TextArea inputMessage;
	@FXML
	private Button send;

	public ChatController setUserList(ObservableList<Account> userList) {
		mUserList = userList;
		this.userList.setItems(mUserList);
		return this;
	}
}
