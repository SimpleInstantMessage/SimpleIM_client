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

	/*package*/ void showLoginView() {
		LoginController controller = showScene("view/Login.fxml");
		controller.setMainApp(this);
	}

	/*package*/ void showChatView() {
		showScene("view/Chat.fxml");
	}

	private <T> T showScene(String rootNotePath) {
		try {
			FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(rootNotePath));
			mRootLayout = (Parent) loader.load();
			Scene scene = new Scene(mRootLayout);
			mPrimaryStage.setScene(scene);
			mPrimaryStage.sizeToScene();
			mPrimaryStage.show();
			return loader.getController();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
