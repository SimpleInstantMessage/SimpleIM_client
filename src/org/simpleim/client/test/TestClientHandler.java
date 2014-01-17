package org.simpleim.client.test;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.simpleim.common.message.NewAccountOkResponse;

public class TestClientHandler extends ChannelHandlerAdapter {

	private static final Logger logger = Logger.getLogger(TestClientHandler.class.getName());

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(new NewAccountOkResponse().setId("4321").setPassword("6543210"));
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ctx.write(msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
		ctx.close();
	}
}
