package org.simpleim.client;

import io.netty.channel.ChannelFuture;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Dialogs;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.simpleim.client.model.container.Account;
import org.simpleim.client.model.netty.ChatClientHandler;
import org.simpleim.client.model.netty.ChatClientHandler.ChatClientListener;
import org.simpleim.client.model.netty.ChatClientHandler.ChatClientListenerAdapter;
import org.simpleim.client.model.netty.Client;
import org.simpleim.client.model.netty.RegisterAccountClientHandler;
import org.simpleim.client.util.Constant;
import org.simpleim.common.message.LoginFailureResponse;
import org.simpleim.common.message.LoginOkResponse;
import org.simpleim.common.message.NewAccountOkResponse;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class LoginController extends Controller {
	private static final int NUMBER_OF_RETRIES = 5;
	private static final Gson GSON = new Gson();

	@FXML
	private TextField server;
	@FXML
	private TextField port;
	protected static Stage stage;

	@FXML
	private void handleLogin() {
		String server = this.server.getText();
		int port = Integer.parseInt(this.port.getText());//TODO deal with exception
		Account account = readAccount();
		if(account != null && account.isValid()) {
			// login
			new Thread(new LoginTask(server, port, account.getId(), account.getPassword())).start();
		} else {
			// register a new account and login
			Task<Account> registerTask = new RegisterAccountTask(server, port) {
				@Override
				protected void succeeded() {
					super.succeeded();
					// login
					Account account = getValue();
					if(account != null  && account.isValid())
						new Thread(new LoginTask(server, port, account.getId(), account.getPassword())).start();
					else
						Dialogs.showErrorDialog(stage, "New ccount registration failed! You can try again.", "REGISTRATION FAILED", "Error", getException());
				}
				@Override
				protected void failed() {
					super.failed();
					if(getException() instanceof FileNotFoundException)
						Dialogs.showErrorDialog(stage, 
								"Cannot create account file, bacause there already have a directory named "
										+ Constant.ACCOUNT_FILE_PATH + ". Please delete it and try again.",
								"CANNOT CREATE ACCOUNT FILE", "Error", getException());
					else
						Dialogs.showErrorDialog(stage, "New ccount registration failed! You can try again.", "REGISTRATION FAILED", "Error", getException());
				}
				
			};
			new Thread(registerTask).start();
		}
	}

	private static Account readAccount() {
		Account result = null;
		for(int i = NUMBER_OF_RETRIES ; i >= 1 ; i--) {
			try(Reader reader = new InputStreamReader(new FileInputStream(Constant.ACCOUNT_FILE_PATH), Constant.UTF8)) {
				result = GSON.fromJson(reader, Account.class);
				break;
			} catch (JsonSyntaxException e) {
				// TODO inform user
				e.printStackTrace();
				break;
			} catch (JsonIOException e) {
				// retry
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				//TODO deal with situation if ACCOUNT_FILE_PATH is a directory and so on
				System.out.println("no account, get a new account.");
//				e.printStackTrace();
				break;
			} catch (IOException e) {
				// because of auto-close
				e.printStackTrace();
			}
		}
		return result;
	}
	private static void writeAccount(Account account) throws FileNotFoundException {
		if(account == null)
			throw new NullPointerException("account shouldn't be null");
		if(!account.isValid())
			throw new IllegalArgumentException("account isn't valid: " + account);
		// insure the data directory
		for(int i = NUMBER_OF_RETRIES ; i >= 1 ; i--) {
			File directory = new File(Constant.DATA_DIRECTORY_PATH);
			if(!directory.exists())
				if(!directory.mkdirs())
					continue;
		}
		// save
		for(int i = NUMBER_OF_RETRIES ; i >= 1 ; i--) {
			try(Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(Constant.ACCOUNT_FILE_PATH), Constant.UTF8)) ) {
				GSON.toJson(account, writer);
				break;
			} catch (FileNotFoundException e) {
				throw e;
			} catch (JsonIOException e) {
				// retry
				e.printStackTrace();
			} catch (IOException e) {
				// because of auto-close
				e.printStackTrace();
			}
		}
	}

	private final ChatClientListener mChatClientListener = new ChatClientListenerAdapter() {
		@Override
		public void onLoginOk(final ChatClientHandler handler, final LoginOkResponse response) {
			runInJavaFXApplicationThread(new Runnable() {
				@Override
				public void run() {
					changeToChatScene(handler, response);
				}
			});
		}
		@Override
		public void onLoginFailure(ChatClientHandler handler, LoginFailureResponse response) {
			//TODO
			switch(response.getCause()) {
			case ID_NOT_FOUND:
				System.out.println("ID_NOT_FOUND");
				break;
			case PASSWORD_INCORRECT:
				System.out.println("PASSWORD_INCORRECT");
				break;
			}
		}

		/**
		 * <strong>Note: </strong> must be run in JavaFX Application Thread
		 * @see Platform#isFxApplicationThread()
		 */
		private void changeToChatScene(ChatClientHandler handler, LoginOkResponse response) {
			ChatController controller = mainApp.showChatView();
			controller.setChatClientHandler(handler);
			ObservableList<Account> users = FXCollections.observableArrayList();
			for(String userId : response.getOnlineUsersIds())
				users.add(new Account().setId(userId));
			controller.setUserList(users);
			handler.removeListener(mChatClientListener);
			handler.notifyUpdateFinished();
		}
	};

	private static class RegisterAccountTask extends Task<Account> {
		protected final String server;
		protected final int port;

		public RegisterAccountTask(String server, int port) {
			super();
			this.server = server;
			this.port = port;
		}

		@Override
		protected Account call() throws InterruptedException, FileNotFoundException {
			final RegisterAccountClientHandler handler = new RegisterAccountClientHandler();
			ChannelFuture f = new Client(server, port, handler).run();
			try {
				f.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				if(isCancelled())
					return null;
				else
					throw e;
			}
			if(f.cause() != null)
				throw new RuntimeException(f.cause());
			// save new Account
			Account newAccount = null;
			NewAccountOkResponse response = handler.getOkResponse();
			if(response != null && response.isValid())
				newAccount = new Account().setId(response.getId()).setPassword(response.getPassword());
			if(newAccount == null) {
				return null;
			}
			writeAccount(newAccount);
			return newAccount;
		}
	}

	private class LoginTask extends Task<Boolean> {
		private final String server;
		private final int port;
		private final String id;
		private final String password;

		public LoginTask(String server, int port, String id, String password) {
			super();
			this.server = server;
			this.port = port;
			this.id = id;
			this.password = password;
		}

		@Override
		protected Boolean call() throws Exception {
			ChatClientHandler handler = new ChatClientHandler(id, password);
			handler.addListenerIfAbsent(mChatClientListener);
			new Client(server, port, handler).run();
			return true;
		}
	}
}
