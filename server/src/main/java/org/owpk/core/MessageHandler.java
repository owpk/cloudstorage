package org.owpk.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.auth.User;
import org.owpk.message.DataInfo;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.util.Config;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;
import org.owpk.util.ServerConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<Message<?>> {
  private final Logger log = LogManager.getLogger(MessageHandler.class.getName());
  private final File userFolder;
  private User user;


  public MessageHandler(User user) {
    this.user = user;
    this.userFolder = new File(ServerConfig.getConfig().getRoot().toAbsolutePath() + Config.getLineSeparator() + user.getServer_folder());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message<?> msg) throws Exception {
    log.info(msg);
    switch (msg.getType()) {
      case DOWNLOAD:
        downloadRequest(ctx.channel(), msg);
        break;
      case UPLOAD:
        uploadRequest((DataInfo) msg);
        break;
      case DELETE:
        deleteRequest(msg.getPayload());
      case DIR:
        List<FileInfo> list = FileUtility.getDirectories(userFolder.getAbsolutePath());
        ctx.channel().writeAndFlush(new Message<>(MessageType.DIR, list));
        log.info(userFolder);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error(cause);
    cause.printStackTrace();
    ctx.close();
  }

  private void deleteRequest(Object payload) {
    try {
      String path = userFolder + Config.getLineSeparator() + payload;
      FileUtility.deleteFile(Paths.get(path));
    } catch (IOException e) {
      log.error(e);
    }
  }

  private void downloadRequest(Channel channel, Message<?> ms) throws IOException {
    log.debug(ms);
    final File f = new File(userFolder + Config.getLineSeparator() + ms.getPayload());
    if (f.exists()) {
    FileUtility.sendFileByChunks(channel::writeAndFlush, f, MessageType.DOWNLOAD);
    }
  }

  private void uploadRequest(DataInfo ms) throws IOException {
    log.debug("package accepted: " + ms);
    FileUtility.FileWriter writer = FileUtility.FileWriter.getWriter(userFolder + Config.getLineSeparator() + ms.getFile());
    writer.assembleChunkedFile(ms);
  }

}
