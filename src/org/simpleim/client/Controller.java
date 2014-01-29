package org.simpleim.client;

import javafx.application.Platform;


public class Controller {
	protected MainApp mainApp;

	/**
	 * Is called by the main application to give a reference back to itself.
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	protected static void runInJavaFXApplicationThread(Runnable run) {
		if(Platform.isFxApplicationThread()) {
			run.run();
		} else {
			Platform.runLater(run);
		}
	}
}
