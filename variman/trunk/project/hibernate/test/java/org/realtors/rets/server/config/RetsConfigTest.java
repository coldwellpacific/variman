/*
 */
package org.realtors.rets.server.config;

import org.realtors.rets.server.LinesEqualTestCase;
import org.realtors.rets.server.RetsServerException;

public class RetsConfigTest extends LinesEqualTestCase
{
    public void testToXml()
        throws RetsServerException
    {
        RetsConfig retsConfig = new RetsConfig();
        retsConfig.setGetObjectRoot("/tmp/pictures");
        retsConfig.setGetObjectPattern("%k-%i.jpg");
        retsConfig.setNonceInitialTimeout(5);
        retsConfig.setNonceSuccessTimeout(10);
        retsConfig.setPort(7103);

        DatabaseConfig database = new DatabaseConfig();
        database.setDriver("org.postgresql.Driver");
        database.setUrl("jdbc:postgresql://localhost/rex_test");
        database.setUsername("dave");
        database.setPassword("");
        database.setMaxActive(100);
        database.setMaxWait(120000);
        database.setMaxIdle(10);
        database.setMaxPsActive(50);
        database.setMaxPsWait(60000);
        database.setMaxPsIdle(5);
        retsConfig.setDatabase(database);
        String xml = retsConfig.toXml();
        assertLinesEqual(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rets-config>\n" +
            "  <port>7103</port>\n" +
            "  <get-object-pattern>%k-%i.jpg</get-object-pattern>\n" +
            "  <get-object-root>/tmp/pictures</get-object-root>\n" +
            "  <nonce-initial-timeout>5</nonce-initial-timeout>\n" +
            "  <nonce-success-timeout>10</nonce-success-timeout>\n" +
            "  <database>\n" +
            "    <driver>org.postgresql.Driver</driver>\n" +
            "    <url>jdbc:postgresql://localhost/rex_test</url>\n" +
            "    <username>dave</username>\n" +
            "    <password></password>\n" +
            "    <max-active>100</max-active>\n" +
            "    <max-idle>10</max-idle>\n" +
            "    <max-wait>120000</max-wait>\n" +
            "    <max-ps-active>50</max-ps-active>\n" +
            "    <max-ps-idle>5</max-ps-idle>\n" +
            "    <max-ps-wait>60000</max-ps-wait>\n" +
            "  </database>\n" +
            "</rets-config>\n" +
            "\n",
            xml
        );
    }

    public void testFromXml()
        throws RetsServerException
    {
        String xml =
            "<?xml version='1.0' ?>\n" +
            "<rets-config>\n" +
            "  <port>7103</port>\n" +
            "  <get-object-pattern>%k-%i.jpg</get-object-pattern>\n" +
            "  <get-object-root>/tmp/pictures</get-object-root>\n" +
            "  <nonce-initial-timeout>5</nonce-initial-timeout>\n" +
            "  <nonce-success-timeout>10</nonce-success-timeout>\n" +
            "  <database>\n" +
            "    <driver>org.postgresql.Driver</driver>\n" +
            "    <url>jdbc:postgresql://localhost/rex_test</url>\n" +
            "    <username>dave</username>\n" +
            "    <password/>" +
            "    <max-active>100</max-active>\n" +
            "    <max-wait>120000</max-wait>\n" +
            "    <max-idle>10</max-idle>\n" +
            "    <max-ps-active>50</max-ps-active>\n" +
            "    <max-ps-wait>60000</max-ps-wait>\n" +
            "    <max-ps-idle>5</max-ps-idle>\n" +
            "  </database>\n" +
            "</rets-config>";
        RetsConfig retsConfig = RetsConfig.initFromXml(xml);
        assertEquals(7103, retsConfig.getPort());
        assertEquals("%k-%i.jpg", retsConfig.getGetObjectPattern());
        assertEquals("/tmp/pictures", retsConfig.getGetObjectRoot());
        assertEquals(5, retsConfig.getNonceInitialTimeout());
        assertEquals(10, retsConfig.getNonceSuccessTimeout());

        DatabaseConfig database = retsConfig.getDatabase();
        assertEquals("org.postgresql.Driver", database.getDriver());
        assertEquals("jdbc:postgresql://localhost/rex_test",
                     database.getUrl());
        assertEquals("dave", database.getUsername());
        assertEquals("", database.getPassword());
        assertEquals(100, database.getMaxActive());
        assertEquals(120000, database.getMaxWait());
        assertEquals(10, database.getMaxIdle());
        assertEquals(50, database.getMaxPsActive());
        assertEquals(60000, database.getMaxPsWait());
        assertEquals(5, database.getMaxPsIdle());
    }

    public void testFromXmlDefaults()
        throws RetsServerException
    {
        String xml =
            "<?xml version='1.0' ?>\n" +
            "<rets-config>\n" +
            "</rets-config>";
        RetsConfig retsConfig = RetsConfig.initFromXml(xml);
        assertNull(retsConfig.getGetObjectPattern());
        assertNull(retsConfig.getGetObjectRoot());
        assertEquals(-1, retsConfig.getNonceInitialTimeout());
        assertEquals(-1, retsConfig.getNonceSuccessTimeout());
    }

    public void testGetDefaults()
    {
        RetsConfig retsConfig = new RetsConfig();
        assertEquals("/", retsConfig.getGetObjectRoot("/"));
        assertEquals("%i.jpg", retsConfig.getGetObjectPattern("%i.jpg"));
        assertEquals(2, retsConfig.getNonceInitialTimeout(2));
        assertEquals(2, retsConfig.getNonceSuccessTimeout(2));

        retsConfig.setGetObjectRoot("/tmp/pictures");
        retsConfig.setGetObjectPattern("%k-%i.jpg");
        retsConfig.setNonceInitialTimeout(5);
        retsConfig.setNonceSuccessTimeout(10);

        assertEquals("/tmp/pictures", retsConfig.getGetObjectRoot("/"));
        assertEquals("%k-%i.jpg", retsConfig.getGetObjectPattern("%i.jpg"));
        assertEquals(5, retsConfig.getNonceInitialTimeout(2));
        assertEquals(10, retsConfig.getNonceSuccessTimeout(2));
    }
}
