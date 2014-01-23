package org.simpleim.client;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class LoginController {
	private MainApp mainApp;
	@FXML
	private TextField server;
	@FXML
	private TextField port;

	/**
	 * Is called by the main application to give a reference back to itself.
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	private void handleLogin() {
		mainApp.showChatView();
	}
}
