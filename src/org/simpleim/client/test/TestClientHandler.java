package org.simpleim.client.test;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.simpleim.common.message.NewAccountRequest;
import org.simpleim.common.message.Request;

import com.google.gson.Gson;

public class TestClientHandler extends ChannelHandlerAdapter {

	private static final Logger logger = Logger.getLogger(TestClientHandler.class.getName());
	//TODO delete it(debug code)
	private static final Gson DUMPER = new Gson();
	private static final Request TEST_REQUEST = new NewAccountRequest();

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(TEST_REQUEST);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println(DUMPER.toJson(msg));
		ctx.close();
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
