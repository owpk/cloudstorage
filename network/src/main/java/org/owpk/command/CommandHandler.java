package org.owpk.command;

import java.io.IOException;

public interface CommandHandler {
  void listen() throws IOException;
}
