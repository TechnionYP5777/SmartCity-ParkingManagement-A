package il.ac.technion.cs.eldery.system.applications.api.exceptions;

/** @author RON
 * @since 31-12-2016 (yes, I'm working on new year's eve...) */
public class OnLoadException extends Exception {
    private static final long serialVersionUID = 5725783046384567488L;

    public enum ErrorCode {
        SENSOR_COM_NAME_NOT_FOUND("SENSOR_COM_NAME_NOT_FOUND"),
        SENSOR_NOT_FOUND_IN_LOCATION("SENSOR_NOT_FOUND_IN_LOCATION"),
        SENSOR_ID_NOT_FOUND("SENSOR_ID_NOT_FOUND")
        // TODO: RON - add more here, and fix the msgs!
        ;

        private final String msg;

        ErrorCode(final String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }
    }

    private final ErrorCode errorCode;
    private final String moreInfo;

    public OnLoadException(final ErrorCode errorCode) {
        this(errorCode, "");
    }

    public OnLoadException(final ErrorCode errorCode, final String moreInfo) {
        super(errorCode.getMsg() + "; " + moreInfo);
        this.errorCode = errorCode;
        this.moreInfo = moreInfo;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
