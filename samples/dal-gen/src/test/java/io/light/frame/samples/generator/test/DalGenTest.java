package io.light.frame.samples.generator.test;

import io.light.frame.dal.mybatis.generator.core.MybatisGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TODO
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-13 16:20
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DalGenTest {

    @Autowired
    private MybatisGenerator mybatisGenerator;

    @Test
    public void process() {
        mybatisGenerator.process("cms_content");
        mybatisGenerator.process("module-level-0", "cms_content");
        mybatisGenerator.process("sub/module-level-1", "cms_content");
    }
}
