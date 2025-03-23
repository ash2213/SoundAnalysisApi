package dat.exceptions;

public class ApiException extends RuntimeException {

    private int errCode;

    public ApiException(int errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }

}
