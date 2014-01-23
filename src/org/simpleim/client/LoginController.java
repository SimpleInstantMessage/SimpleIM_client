package org.simpleim.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import org.simpleim.client.model.netty.Client;
import org.simpleim.common.message.NewAccountRequest;
import org.simpleim.common.message.Request;

import com.google.gson.Gson;

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
		String server = this.server.getText();
		int port = Integer.parseInt(this.port.getText());
		ChannelFuture f = new Client(server, port, new NewAccountRequestClientHandler()).run();
		f.channel().closeFuture().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						mainApp.showChatView();
					}
				});
			}
		});
	}

	public static class NewAccountRequestClientHandler extends ChannelHandlerAdapter {

		private static final Logger logger = Logger.getLogger(NewAccountRequestClientHandler.class.getName());
		//TODO delete it(debug code)
		private static final Gson DUMPER = new Gson();
		private static final Request NEW_ACCOUNT_REQUEST = new NewAccountRequest();

		@Override
		public void channelActive(ChannelHandlerContext ctx) {
			ctx.writeAndFlush(NEW_ACCOUNT_REQUEST);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println(DUMPER.toJson(msg));
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
}
