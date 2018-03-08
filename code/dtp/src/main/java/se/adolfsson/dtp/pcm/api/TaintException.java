package se.adolfsson.dtp.pcm.api;


public class TaintException extends RuntimeException {
  String source;

  public TaintException(String message, String source) {
    super(message);
    this.source = source;
  }
}
