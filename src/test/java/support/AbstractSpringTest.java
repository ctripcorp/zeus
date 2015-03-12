package support;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
@ContextConfiguration("/test-spring-context.xml")
public abstract class AbstractSpringTest extends AbstractJUnit4SpringContextTests{
}
