//$Id: ProxoolConnectionProvider.java,v 1.8 2004/06/26 02:33:27 steveebersole Exp $
package net.sf.hibernate.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.util.PropertiesHelper;
import net.sf.hibernate.util.StringHelper;
import net.sf.hibernate.util.ConfigHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;

/**
 * A connection provider that uses a Proxool connection pool. Hibernate will use this by
 * default if the <tt>hibernate.proxool.*</tt> properties are set.
 * @see ConnectionProvider
 */
public class ProxoolConnectionProvider implements ConnectionProvider {
	

	private static final String PROXOOL_JDBC_STEM = "proxool.";

	private static final String HIBERNATE_ALIAS_STEM = PROXOOL_JDBC_STEM + "hibernate-";

	private static final Log log = LogFactory.getLog(ProxoolConnectionProvider.class);

	private static int aliasCount;

	private String proxoolAlias;

	// TRUE if the pool is borrowed from the outside, FALSE if we used to create it
	private boolean existingPool;

	// Not null if the Isolation level has been specified in the configuration file.
	// Otherwise, it is left to the Driver's default value.
	private Integer isolation;
	
	/**
	 * Grab a connection
	 * @return a JDBC connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
	    // get a connection from the pool (thru DriverManager, cfr. Proxool doc)
		Connection c = DriverManager.getConnection(proxoolAlias);
		
		// set the Transaction Isolation if defined
		if (isolation!=null) c.setTransactionIsolation( isolation.intValue() );
		
		// toggle autoCommit to false if set
		if ( c.getAutoCommit() ) c.setAutoCommit(false);
		
		// return the connection
		return c;
	}

	/**
	 * Dispose of a used connection.
	 * @param conn a JDBC connection
	 * @throws SQLException
	 */
	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
	}

	/**
	 * Initialize the connection provider from given properties.
	 * @param props <tt>SessionFactory</tt> properties
	 */
	public void configure(Properties props) throws HibernateException {

		// Get the configurator files (if available)
		String jaxpFile = props.getProperty(Environment.PROXOOL_XML);
		String propFile = props.getProperty(Environment.PROXOOL_PROPERTIES);
		String externalConfig = props.getProperty(Environment.PROXOOL_EXISTING_POOL);

		// Default the Proxool alias setting
		proxoolAlias = props.getProperty(Environment.PROXOOL_POOL_ALIAS);

		// Configured outside of Hibernate (i.e. Servlet container, or Java Bean Container
		// already has Proxool pools running, and this provider is to just borrow one of these
		if ( "true".equals(externalConfig) ) {

			// Validate that an alias name was provided to determine which pool to use
			if ( !StringHelper.isNotEmpty(proxoolAlias) ) {
				String msg = "Cannot configure Proxool Provider to use an existing in memory pool without the " + Environment.PROXOOL_POOL_ALIAS + " property set.";
				log.fatal(msg);
				throw new HibernateException(msg);
			}
			// Append the stem to the proxool pool alias
			proxoolAlias = PROXOOL_JDBC_STEM + proxoolAlias;

			// Set the existing pool flag to true
			existingPool = true;

			log.info("Configuring Proxool Provider using existing pool in memory: " + proxoolAlias);

			// Configured using the JAXP Configurator
		}
		else if ( StringHelper.isNotEmpty(jaxpFile) ) {

			log.info("Configuring Proxool Provider using JAXPConfigurator: " + jaxpFile);

			// Validate that an alias name was provided to determine which pool to use
			if ( !StringHelper.isNotEmpty(proxoolAlias) ) {
				String msg = "Cannot configure Proxool Provider to use JAXP without the " + Environment.PROXOOL_POOL_ALIAS + " property set.";
				log.fatal(msg);
				throw new HibernateException(msg);
			}

			try {
				JAXPConfigurator.configure( ConfigHelper.getConfigStreamReader(jaxpFile), false );
			}
			catch (ProxoolException e) {
				String msg = "Proxool Provider unable to load JAXP configurator file: " + jaxpFile;
				log.fatal(msg, e);
				throw new HibernateException(msg, e);
			}

			// Append the stem to the proxool pool alias
			proxoolAlias = PROXOOL_JDBC_STEM + proxoolAlias;
			log.info("Configuring Proxool Provider to use pool alias: " + proxoolAlias);

			// Configured using the Properties File Configurator
		}
		else if ( StringHelper.isNotEmpty(propFile) ) {

			log.info("Configuring Proxool Provider using Properties File: " + propFile);

			// Validate that an alias name was provided to determine which pool to use
			if ( !StringHelper.isNotEmpty(proxoolAlias) ) {
				String msg = "Cannot configure Proxool Provider to use Properties File without the " + Environment.PROXOOL_POOL_ALIAS + " property set.";
				log.fatal(msg);
				throw new HibernateException(msg);
			}

			try {
				PropertyConfigurator.configure( ConfigHelper.getConfigProperties(propFile) );
			}
			catch (ProxoolException e) {
				String msg = "Proxool Provider unable to load load Property configurator file: " + propFile;
				log.fatal(msg, e);
				throw new HibernateException(msg, e);
			}

			// Append the stem to the proxool pool alias
			proxoolAlias = PROXOOL_JDBC_STEM + proxoolAlias;
			log.info("Configuring Proxool Provider to use pool alias: " + proxoolAlias);
		}
		
		// Remember Isolation level
		isolation = PropertiesHelper.getInteger(Environment.ISOLATION, props);
		if (isolation!=null) {
		    log.info("JDBC isolation level: " + Environment.isolationLevelToString( isolation.intValue() ) );
		}
	}

	/**
	 * Release all resources held by this provider. JavaDoc requires a second sentence.
	 * @throws HibernateException
	 */
	public void close() throws HibernateException {

		// If the provider was leeching off an existing pool don't close it
		if (existingPool) {
			return;
		}

		// We have created the pool ourselves, so shut it down
		try {
	        ProxoolFacade.shutdown(0);
		}
		catch (Exception e) {
			// If you're closing down the ConnectionProvider chances are an
			// is not a real big deal, just warn
			log.warn("Exception occured when closing the Proxool pool", e);
			throw new HibernateException("Exception occured when closing the Proxool pool", e);
		}
	}

	// Creates a new alais and is thread safe.
	private static String newAlias() {
		synchronized(ProxoolConnectionProvider.class) {

			// Increment the alias counter
			aliasCount++;
	
			// Build the alias using a stringbuffer, just easier to read and faster to execute
			StringBuffer buff = new StringBuffer(HIBERNATE_ALIAS_STEM);
			buff.append(aliasCount);
			return buff.toString();
		}
	}

}
