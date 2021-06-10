package io.light.frame.dal.mybatis.generator.exceptions;

/**
 * TODO
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-10 11:08
 */
public class MetaAccessException extends MybatisGenException {

    public MetaAccessException(String message) {
        super(message);
    }

    public MetaAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
