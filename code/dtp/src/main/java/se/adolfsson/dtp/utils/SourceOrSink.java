package se.adolfsson.dtp.utils;

import lombok.Value;

@Value
public class SourceOrSink {
  private String clazz;
  private String[] methods;
  private String descriptor;
}
