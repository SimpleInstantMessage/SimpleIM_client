package org.simpleim.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import org.simpleim.client.model.container.Account;
import org.simpleim.client.model.netty.Client;
import org.simpleim.client.util.Constant;
import org.simpleim.common.message.NewAccountOkResponse;
import org.simpleim.common.message.NewAccountRequest;
import org.simpleim.common.message.Request;

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
			Task<Account> registerTask = new RegisterNewAccountTask(server, port) {
				@Override
				protected void succeeded() {
					super.succeeded();
					// login
					Account account = getValue();
					if(account != null  && account.isValid())
						new Thread(new LoginTask(server, port, account.getId(), account.getPassword())).start();
					else
						;// TODO register failure
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
	private static void writeAccount(Account account) {
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
				//TODO deal with situation if ACCOUNT_FILE_PATH is a directory and so on
				// retry
				e.printStackTrace();
			} catch (JsonIOException e) {
				// retry
				e.printStackTrace();
			} catch (IOException e) {
				// because of auto-close
				e.printStackTrace();
			}
		}
	}

	private static class NewAccountRequestClientHandler extends ChannelHandlerAdapter {
		private static final Logger logger = Logger.getLogger(NewAccountRequestClientHandler.class.getName());
		private static final Request NEW_ACCOUNT_REQUEST = new NewAccountRequest();

		private NewAccountOkResponse response;

		@Override
		public void channelActive(ChannelHandlerContext ctx) {
			ctx.writeAndFlush(NEW_ACCOUNT_REQUEST);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			response = (NewAccountOkResponse) msg;
			ctx.close();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			//TODO inform user the exception
			logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
			// Close the connection when an exception is raised.
			ctx.close();
		}
	}

	private static class RegisterNewAccountTask extends Task<Account> {
		protected final String server;
		protected final int port;

		public RegisterNewAccountTask(String server, int port) {
			super();
			this.server = server;
			this.port = port;
		}

		@Override
		protected Account call() throws InterruptedException {
			final NewAccountRequestClientHandler handler = new NewAccountRequestClientHandler();
			ChannelFuture f = new Client(server, port, handler).run();
			try {
				f.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				if(isCancelled())
					return null;
				else
					throw e;
			}
			// save new Account
			Account newAccount = null;
			if(handler.response != null && handler.response.isValid())
				newAccount = new Account().setId(handler.response.getId()).setPassword(handler.response.getPassword());
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
			// TODO login
			return true;
		}

		@Override
		protected void succeeded() {
			super.succeeded();
			mainApp.showChatView();
		}
	}
}
