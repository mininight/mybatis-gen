package io.light.frame.dal.mybatis.generator.exceptions;

/**
 * Initialization exception
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-13 14:34
 */
public class InitializationException extends MybatisGenException{

    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
