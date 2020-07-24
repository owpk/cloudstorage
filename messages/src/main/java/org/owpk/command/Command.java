package org.owpk.command;

import java.io.IOException;

public interface Command {
  void execute() throws IOException;
}
