package io.light.frame.dal.mybatis.generator.test;

import io.light.frame.dal.mybatis.generator.core.MybatisGenerator;
import io.light.frame.dal.mybatis.generator.test.base.TestStarter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestStarter.class)
public class TestGen {

    @Autowired
    private MybatisGenerator generator;

    @Test
    public void mapper() throws Exception {
        generator.process("cms_content");
    }
}
