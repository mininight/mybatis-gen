package io.light.frame.dal.mybatis.generator.exceptions;

/**
 * TODO
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-09 13:01
 */
public class MybatisGenException extends RuntimeException {

    public MybatisGenException(String message) {
        super(message);
    }

    public MybatisGenException(String message, Throwable cause) {
        super(message, cause);
    }
}
