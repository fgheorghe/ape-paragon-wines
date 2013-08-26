package src.com.paragon;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: fgheorghe
 * Date: 25/08/13
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class UtilTest {
    @Test
    public void testCurrentTimeMillis() throws Exception {
        Util util = new Util();
        assertEquals( util.currentTimeMillis(), System.currentTimeMillis() );
    }
}
