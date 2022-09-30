package jLHS.exceptions;

import org.junit.Assert;
import org.junit.Test;

public class MalformedRequestExceptionTest {
    @Test
    public void testConstructor() {
        Assert.assertThrows(MalformedRequestException.class, () -> {
            throw new MalformedRequestException("test exception", null);
        });
    }
}
