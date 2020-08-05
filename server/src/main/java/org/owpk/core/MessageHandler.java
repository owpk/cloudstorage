package org.owpk.core;

import io.netty.channel.*;
import org.owpk.auth.User;
import org.owpk.message.DataInfo;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<Message<?>> {
  private static final ConcurrentHashMap<Channel, User> activeUsers = new ConcurrentHashMap<>();
  private final Map<String, DataInfo[]> files = new HashMap<>();
  private User user;

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("active");
    user = new User(
        1,
        "C:\\Test\\out\\",
        12345,
        "User",
        "test@email.ru");
    activeUsers.put(ctx.channel(), user);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message<?> msg) throws Exception {
    System.out.println("read key");
    System.out.println(msg);
    switch (msg.getType()) {
      case DOWNLOAD:
        download((DataInfo) msg);
        break;
      case UPLOAD:
        System.out.println("Upload");
        upload((DataInfo) msg, activeUsers.get(ctx.channel()).getUserFolder());
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

  private void download(DataInfo ms) {
    //TODO
  }

  private void upload(DataInfo ms, String root) throws IOException {
    System.out.println("Package accepted: " + ms.getChunkIndex());
    String userFolder = user.getUserFolder();
    String fileName = ms.getFile();
    File f = new File(userFolder + fileName);

    files.computeIfAbsent(fileName, k -> new DataInfo[ms.getChunkCount()]);
    files.get(fileName)[ms.getChunkIndex()] = ms;
    if (Arrays.stream(files.get(fileName)).allMatch(Objects::nonNull)) {
      try(FileOutputStream fos = new FileOutputStream(f)) {
        for (DataInfo data : files.get(fileName))
          fos.write(data.getPayload());
      }
      files.remove(fileName);
    }
  }

}
