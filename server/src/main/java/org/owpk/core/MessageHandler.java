package org.owpk.core;

import io.netty.channel.*;
import javafx.application.Platform;
import org.owpk.auth.User;
import org.owpk.message.DataInfo;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<Message<?>> {
  private static final ConcurrentHashMap<Channel, User> activeUsers = new ConcurrentHashMap<>();
  private final Map<String, DataInfo[]> files = new HashMap<>();
  private User user;
  private static int counter;

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("active");
    String path = "./server/client_folder/user" + counter;
    File f = new File(path);
    if (!f.exists())
      System.out.println(f.mkdirs());
    user = new User(
        1,
        f.getAbsolutePath(),
        12345,
        "User",
        "test@email.ru");
    activeUsers.put(ctx.channel(), user);
    counter++;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message<?> msg) throws Exception {
    System.out.println(msg);
    switch (msg.getType()) {
      case DOWNLOAD:
        downloadRequest(ctx.channel(), (DataInfo) msg);
        break;
      case UPLOAD:
        System.out.println("Upload");
        uploadRequest((DataInfo) msg);
        break;
      case DIR:
        List<FileInfo> list = FileUtility.getDirectories(activeUsers.get(ctx.channel()).getUserFolder());
        ctx.channel().writeAndFlush(new Message<>(MessageType.DIR, list));
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }

  private void downloadRequest(Channel channel, DataInfo ms) throws IOException {
    File f = new File(user.getUserFolder() + "\\" + ms.getFile());
    if (f.exists()) {
    DataInfo[] bufferedData = FileUtility.getChunkedFile(f, MessageType.DOWNLOAD);
      for (DataInfo bufferedDatum : bufferedData) {
        channel.writeAndFlush(bufferedDatum);
      }
    }
  }

  private void uploadRequest(DataInfo ms) throws IOException {
    FileUtility.assembleChunkedFile(ms, files, user.getUserFolder());
  }

}
