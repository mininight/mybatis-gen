package io.light.frame.dal.mybatis.generator.util;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.spring.VelocityEngineFactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.Writer;
import java.util.function.Consumer;

/**
 * Velocity engine helper
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-10 12:10
 */
public class VelocityEngineHelper implements InitializingBean {

    private final VelocityEngineFactoryBean factoryBean;

    private static VelocityEngine original;

    public VelocityEngineHelper(VelocityEngineFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        factoryBean.afterPropertiesSet();
        original = factoryBean.getObject();
    }

    public static void exec(Consumer<VelocityEngine> consumer) {
        consumer.accept(original);
    }

    /**
     * merges a template and puts the rendered stream into the writer
     *
     * @param templateName name of template to be used in merge
     * @param encoding     encoding used in template
     * @param context      filled context to be used in merge
     * @param writer       writer to write template into
     * @return true if successful, false otherwise.  Errors
     * logged to velocity log
     * @throws ResourceNotFoundException resource not found Exception
     * @throws ParseErrorException       parse error exception
     * @throws MethodInvocationException method invocation exception
     * @see VelocityEngine#mergeTemplate(String, String, Context, Writer)
     */
    public static boolean mergeTemplate(String templateName, String encoding, Context context, Writer writer)
            throws ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        return original.mergeTemplate(templateName, encoding, context, writer);
    }
}
