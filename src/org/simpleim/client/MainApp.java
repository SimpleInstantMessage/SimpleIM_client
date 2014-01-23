package org.simpleim.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
	private Stage mPrimaryStage;
	private Parent mRootLayout;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		mPrimaryStage = primaryStage;
		mPrimaryStage.setTitle("SimpleIM");

		showLoginView();
	}

	private void showLoginView() {
		showScene("view/Login.fxml");
	}

	private void showChatView() {
		showScene("view/Chat.fxml");
	}

	private void showScene(String rootNotePath) {
		try {
			FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(rootNotePath));
			mRootLayout = (Parent) loader.load();
			Scene scene = new Scene(mRootLayout);
			mPrimaryStage.setScene(scene);
			mPrimaryStage.show();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
