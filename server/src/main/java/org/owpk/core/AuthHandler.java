package org.owpk.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.auth.User;
import org.owpk.auth.UserDAO;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.message.UserInfo;
import org.owpk.util.Config;
import org.owpk.util.FileUtility;
import org.owpk.util.ServerConfig;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class AuthHandler extends SimpleChannelInboundHandler<Message<?>> {
  private final Logger log = LogManager.getLogger(AuthHandler.class.getName());
  private static final ConcurrentHashMap<Channel, User> activeUsers = new ConcurrentHashMap<>();
  private final User testUser = new User(1, "user", "1234", "user" + Config.getLineSeparator(), "");
  private UserDAO userDAO;

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    userDAO = new UserDAO();
    log.info("user accepted " + ctx.channel().remoteAddress());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message<?> msg) throws Exception {
    log.info("Auth request: " + msg + " : " + ctx.channel().remoteAddress());
    UserInfo info = (UserInfo) msg;
    if (msg.getType() == MessageType.AUTH &&
        info.getLogin().equals(testUser.getLogin())) {
      ctx.writeAndFlush(new Message<>(MessageType.OK, "Auth ok"));
      FileUtility.createDirectory(ServerConfig.getConfig().getRoot() + Config.getLineSeparator() + testUser.getServer_folder());
      ctx.pipeline().addLast(new MessageHandler(testUser));
      ctx.pipeline().remove(this);
      new File(ServerConfig.getConfig().getRoot() + Config.getLineSeparator() + testUser.getServer_folder()).mkdirs();
    } else {
      switch (msg.getType()) {
        case AUTH:
          auth(info, ctx.channel());
          break;
        case SIGN:
          sign(info, ctx.channel());
          break;
      }
    }
  }

  private void auth(UserInfo info, Channel channel) {
    User user = userDAO.getUserByLoginAndPassword(info.getLogin(), info.getPassword());
    if (user != null) {
      activeUsers.put(channel, user);
      channel.writeAndFlush(new Message<>(MessageType.OK, "Auth ok"));
      channel.pipeline().addLast(new MessageHandler(user));
      channel.pipeline().remove(this);
      log.info(channel.remoteAddress() + " verified : user " + user);
    } else {
      log.info("Message send to user");
      channel.writeAndFlush(new Message<>(MessageType.ERROR, "User not found, try to sign"));
    }
  }

  private void sign(UserInfo msg, Channel channel) throws IOException {
    log.info("new sign request");
    final User temp = userDAO.getUserByLogin(msg.getLogin());
    if (temp == null) {
      final User user = new User(
          msg.getLogin(),
          msg.getPassword(),
              Config.getLineSeparator()+ "user_folder_" + msg.getLogin() + Config.getLineSeparator(),
          msg.getEmail());
      userDAO.addUser(user);
      FileUtility.createDirectory(ServerConfig.getConfig().getRoot() + Config.getLineSeparator() + user.getServer_folder());
      log.info("user added: " + user);
      channel.writeAndFlush(new Message<>(MessageType.OK, "OK, Try to auth now"));
    } else {
      channel.writeAndFlush(new Message<>(MessageType.ERROR, "User already exist"));
    }
  }

  public static ConcurrentHashMap<Channel, User> getActiveUsers() {
    return activeUsers;
  }

}
