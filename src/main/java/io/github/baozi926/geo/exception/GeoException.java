package io.github.baozi926.geo.exception;

/**
 * @author 蔡惠民
 */
public class GeoException extends Exception {

    private static final long serialVersionUID = -4866550466373959848L;

    //无参构造方法
    public GeoException() {

        super();
    }

    //有参的构造方法
    public GeoException(String message) {
        super(message);

    }

    // 用指定的详细信息和原因构造一个新的异常
    public GeoException(String message, Throwable cause) {

        super(message, cause);
    }

    //用指定原因构造一个新的异常
    public GeoException(Throwable cause) {

        super(cause);
    }

}
