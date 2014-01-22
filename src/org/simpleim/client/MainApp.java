package org.simpleim.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {
	private Stage mPrimaryStage;
	private BorderPane mRootLayout;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		mPrimaryStage = primaryStage;
		mPrimaryStage.setTitle("SimpleIM");
		try {
			FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/RootLayout.fxml"));
			mRootLayout = (BorderPane) loader.load();
			Scene scene = new Scene(mRootLayout);
			mPrimaryStage.setScene(scene);
			mPrimaryStage.show();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		showChatView();
	}

	private void showChatView() {
		try {
			FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/Chat.fxml"));
			mRootLayout.setCenter((Node) loader.load());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
