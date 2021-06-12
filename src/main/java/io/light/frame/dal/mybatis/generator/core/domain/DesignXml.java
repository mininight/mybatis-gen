package io.light.frame.dal.mybatis.generator.core.domain;

import lombok.Getter;
import org.dom4j.Document;

import java.io.File;

/**
 * Design xml
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-12 15:52
 */
@Getter
public class DesignXml {
    private final File file;
    private final Document doc;

    public DesignXml(File file, Document doc) {
        this.file = file;
        this.doc = doc;
    }
}
