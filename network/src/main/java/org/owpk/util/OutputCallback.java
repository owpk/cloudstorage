package org.owpk.util;

import org.owpk.message.Message;

@FunctionalInterface
public interface OutputCallback<T extends Message<?>> {
  void call(T t);
}
