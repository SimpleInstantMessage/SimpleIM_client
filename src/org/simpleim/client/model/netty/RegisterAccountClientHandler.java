package org.simpleim.client.model.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.simpleim.common.message.NewAccountOkResponse;
import org.simpleim.common.message.NewAccountRequest;
import org.simpleim.common.message.Request;

public class RegisterAccountClientHandler extends ChannelHandlerAdapter {
	private static final Logger logger = Logger.getLogger(RegisterAccountClientHandler.class.getName());
	private static final Request NEW_ACCOUNT_REQUEST = new NewAccountRequest();

	private NewAccountOkResponse response;

	public NewAccountOkResponse getOkResponse() {
		return response;
	}

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
		// TODO inform user the exception
		logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
		// Close the connection when an exception is raised.
		ctx.close();
	}
}
