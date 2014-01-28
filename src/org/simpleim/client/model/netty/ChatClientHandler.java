package org.simpleim.client.model.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.simpleim.client.model.container.Account;
import org.simpleim.common.message.LoginFailureResponse;
import org.simpleim.common.message.LoginOkResponse;
import org.simpleim.common.message.LoginRequest;
import org.simpleim.common.message.ReceiveMessageNotification;
import org.simpleim.common.message.SendMessageRequest;
import org.simpleim.common.message.UpdateFinishedNotification;

public class ChatClientHandler extends ChannelHandlerAdapter {
	private static final Logger logger = Logger.getLogger(ChatClientHandler.class.getName());
	private static final UpdateFinishedNotification UPDATE_FINISHED_NOTIFICATION = new UpdateFinishedNotification();
	private final Account mAccount = new Account();
	private final CopyOnWriteArrayList<ChatClientListener> mListeners = new CopyOnWriteArrayList<>();
	private ChannelHandlerContext mChannelHandlerContext;

	public ChatClientHandler(String id, String password) {
		super();
		if(id == null || password == null)
			throw new NullPointerException("id and password shouldn't be null.");
		if(id.isEmpty())
			throw new IllegalArgumentException("id shouldn't be empty.");
		mAccount.setId(id).setPassword(password);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(new LoginRequest().setId(mAccount.getId()).setPassword(mAccount.getPassword()));
		mChannelHandlerContext = ctx;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof LoginOkResponse) {
			for(ChatClientListener listener : mListeners)
				listener.onLoginOk(this, (LoginOkResponse) msg);
			ctx.writeAndFlush(UPDATE_FINISHED_NOTIFICATION);
		} else if (msg instanceof LoginFailureResponse) {
			for(ChatClientListener listener : mListeners)
				listener.onLoginFailure(this, (LoginFailureResponse) msg);
			//TODO close?
		} else if (msg instanceof ReceiveMessageNotification) {
			for(ChatClientListener listener : mListeners)
				listener.onReceiveMessage(this, (ReceiveMessageNotification) msg);
		} else {
			//TODO unknown failure
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		//TODO inform user the exception
		logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
		// Close the connection when an exception is raised.
		ctx.close();
	}

	public void send(SendMessageRequest request) {
		if(mChannelHandlerContext == null)
			throw new IllegalStateException("channel isn't active now.");
		mChannelHandlerContext.writeAndFlush(request);
	}

	public Account getAccount() {
		return mAccount;
	}

	public boolean addListenerIfAbsent(ChatClientListener listener) {
		return mListeners.addIfAbsent(listener);
	}
	public boolean removeListener(ChatClientListener listener) {
		return mListeners.remove(listener);
	}

	public static interface ChatClientListener {
		public void onLoginOk(ChatClientHandler handler, LoginOkResponse response);
		public void onLoginFailure(ChatClientHandler handler, LoginFailureResponse response);
		public void onReceiveMessage(ChatClientHandler handler, ReceiveMessageNotification message);
	}
	public static class ChatClientListenerAdapter implements ChatClientListener {
		@Override
		public void onLoginOk(ChatClientHandler handler, LoginOkResponse response) {}
		@Override
		public void onLoginFailure(ChatClientHandler handler, LoginFailureResponse response) {}
		@Override
		public void onReceiveMessage(ChatClientHandler handler, ReceiveMessageNotification message) {}
	}
}
