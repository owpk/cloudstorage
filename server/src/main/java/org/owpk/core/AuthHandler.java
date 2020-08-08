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

import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class AuthHandler extends SimpleChannelInboundHandler<Message<?>> {
  private final Logger log = LogManager.getLogger(AuthHandler.class.getName());
  private static final ConcurrentHashMap<Channel, User> activeUsers = new ConcurrentHashMap<>();
  private UserDAO userDAO;

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    userDAO = new UserDAO();
    log.info("user accepted " + ctx.channel().remoteAddress());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message<?> msg) throws Exception {
    if (msg.getType() == MessageType.AUTH) {
      log.info("Auth request: " + msg + " : " + ctx.channel().remoteAddress());
      UserInfo info = (UserInfo) msg;
      User user = userDAO.getUserByLoginAndPassword(info.getLogin(), info.getPassword());
      if (user != null) {
        activeUsers.put(ctx.channel(), user);
        ctx.writeAndFlush(new Message<>(MessageType.OK, "Auth ok"));
        ctx.pipeline().addLast(new MessageHandler(user));
        ctx.pipeline().remove(this);
        log.info(ctx.channel().remoteAddress() + " accepted : user " + user);
      } else ctx.writeAndFlush(new Message<>(MessageType.ERROR, "Not found, try to sign"));
    } else if (msg.getType() == MessageType.SIGN) {
      log.info("Sign request");
      final User user = new User();
      userDAO.addUser(user);
      ctx.writeAndFlush(new Message<>(MessageType.OK, "OK, Try to auth now"));
    }
  }


  public static ConcurrentHashMap<Channel, User> getActiveUsers() {
    return activeUsers;
  }

}
