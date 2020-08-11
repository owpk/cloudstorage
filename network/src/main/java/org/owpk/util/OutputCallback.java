package org.owpk.util;

import org.owpk.message.Message;

@FunctionalInterface
public interface OutputCallback<T> {
  void call(T t);
}
