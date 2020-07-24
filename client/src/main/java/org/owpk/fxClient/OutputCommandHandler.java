package org.owpk.fxClient;

import lombok.Data;
import org.owpk.command.AbsCommandHandler;
import org.owpk.command.Command;
import org.owpk.message.Messages;
import org.owpk.util.FileUtility;
import org.owpk.util.NetworkUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.owpk.message.MessageType.DIR;
import static org.owpk.message.MessageType.DOWNLOAD;

public class OutputCommandHandler extends AbsCommandHandler {
  private Network net;

  public OutputCommandHandler(Network network) {
    this.net = network;
  }

  @Override
  protected Map<String, Command> fillCommands() {
    Map<String, Command> map = new HashMap<>();
    map.put(DIR.getCmd(), this::getDirsCmd);
    map.put(DOWNLOAD.getCmd(), this::downloadCmd);
    return map;
  }

  private void getDirsCmd() throws IOException {
    NetworkUtils.sendObj(net.getOut(), new Messages("", DIR));
  }

  private void downloadCmd() throws IOException {
    final String fileName = parsePayload(rowCommand, 1);
    NetworkUtils.sendObj(net.getOut(), new Messages(fileName, DOWNLOAD));
  }
}
