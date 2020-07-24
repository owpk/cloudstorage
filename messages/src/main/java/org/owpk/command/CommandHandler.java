package org.owpk.command;

import java.io.IOException;

public interface CommandHandler {
  void listen(String cmd) throws IOException;
}
