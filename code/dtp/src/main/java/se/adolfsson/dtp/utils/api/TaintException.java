package se.adolfsson.dtp.utils.api;


public class TaintException extends RuntimeException {
  private String source;

  TaintException(String message, String source) {
    super(message);
    this.source = source;
  }
}
