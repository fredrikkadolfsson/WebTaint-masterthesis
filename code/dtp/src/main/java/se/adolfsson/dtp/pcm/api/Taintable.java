package se.adolfsson.dtp.pcm.api;

public interface Taintable {
  void setTaint(boolean value);

  boolean isTainted();
}
