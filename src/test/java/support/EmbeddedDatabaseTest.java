package support;

import org.junit.Test;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author:xingchaowang
 * @date: 3/11/2015.
 */
public class EmbeddedDatabaseTest extends AbstractSpringTest {
    @Resource(name = "embeddedDataSource")
    private DataSource embeddedDataSource;

    @Test
    public void test(){

        System.out.println("helloWorld");

    }
}
