package org.apache.geode.management.internal.exceptions;

import org.apache.geode.GemFireException;

public class ConfigurationValidationException extends GemFireException {
  public ConfigurationValidationException(String message) {
    super(message);
  }
}
