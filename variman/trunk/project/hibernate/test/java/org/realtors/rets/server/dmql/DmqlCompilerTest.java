/*
 */
package org.realtors.rets.server.dmql;

import antlr.ANTLRException;
import junit.framework.TestCase;

public class DmqlCompilerTest extends TestCase
{
    public void testSimple() throws ANTLRException
    {
        String sql = DmqlCompiler.dmqlToSql("(STATUS=A)");
        assertEquals("(STATUS = 'A')", sql);
    }
}
