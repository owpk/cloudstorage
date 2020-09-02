package org.owpk.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.util.ServerConfig;

public class Server {
  private final Logger log = LogManager.getLogger(Server.class.getName());
  private final int PORT;

  public Server(int port) {
    this.PORT = port;
  }

  public void run(ChannelInboundHandlerAdapter adapter,
                  ByteToMessageDecoder... decoder) throws InterruptedException {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap boot = new ServerBootstrap();
      boot.group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
              socketChannel.pipeline().addLast(
                  new ObjectEncoder(),
                  new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                  adapter);
            }
          })
          .option(ChannelOption.SO_BACKLOG, 128)
          .childOption(ChannelOption.SO_KEEPALIVE, true);
      ChannelFuture future = boot.bind(PORT).sync();
      log.info("Server started at : " + PORT);
      future.channel().closeFuture().sync();
    } finally {
      workGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
      log.info("server shutdown");
    }
  }

  public static void main(String[] args) throws InterruptedException {
    new Server(ServerConfig.getConfig().getPort()).run(new AuthHandler());
  }
}
