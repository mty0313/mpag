package top.diff.mpag.common;

public class GlobalException extends Exception {

  public GlobalException() {
    super();
  }

  public GlobalException(String message) {
    super(message);
  }

  public GlobalException(String message, Throwable cause) {
    super(message, cause);
  }

  public GlobalException(Throwable cause) {
    super(cause);
  }

  protected GlobalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
