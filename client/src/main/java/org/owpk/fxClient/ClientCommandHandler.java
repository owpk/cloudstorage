package org.owpk.fxClient;

import org.owpk.command.AbsCommandHandler;
import org.owpk.command.Command;
import org.owpk.message.MessageType;
import org.owpk.message.Messages;
import org.owpk.util.NetworkUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.owpk.message.MessageType.*;

public class ClientCommandHandler extends AbsCommandHandler {
  private Network net;

  public ClientCommandHandler(Network network) {
    this.net = network;
  }

  @Override
  protected Map<String, Command> fillCommands() {
    Map<String, Command> map = new HashMap<>();
    map.put(DIR.getCmd(), this::getDirsCmd);
    map.put(DOWNLOAD.getCmd(), this::getDirsCmd);
    return map;
  }

  private void getDirsCmd() throws IOException {
    NetworkUtils.sendObj(net.getOut(), new Messages<>("", DIR));
  }

}
