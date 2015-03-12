package support;

import com.ctrip.zeus.util.S;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
@ContextConfiguration("/test-spring-context.xml")
public abstract class AbstractSpringTest extends AbstractJUnit4SpringContextTests{
    @BeforeClass
    public static void setup(){
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
    }
}
