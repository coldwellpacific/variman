//$Id: ClobImpl.java,v 1.8 2004/06/04 01:27:42 steveebersole Exp $
package net.sf.hibernate.lob;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * A dummy implementation of <tt>java.sql.Clob</tt> that
 * may be used to insert new data into a CLOB.
 * @author Gavin King
 */
public class ClobImpl implements Clob {
	
	private Reader reader;
	private int length;
	
	public ClobImpl(String string) {
		reader = new StringReader(string);
		length = string.length();
	}

	public ClobImpl(Reader reader, int length) {
		this.reader = reader;
		this.length = length;
	}
	
	/**
	 * @see java.sql.Clob#length()
	 */
	public long length() throws SQLException {
		return length;
	}
	
	/**
	 * @see java.sql.Clob#truncate(long)
	 */
	public void truncate(long pos) throws SQLException {
		excep();
	}
	
	/**
	 * @see java.sql.Clob#getAsciiStream()
	 */
	public InputStream getAsciiStream() throws SQLException {
		excep(); return null;
	}
	
	/**
	 * @see java.sql.Clob#setAsciiStream(long)
	 */
	public OutputStream setAsciiStream(long pos) throws SQLException {
		excep(); return null;
	}
	
	/**
	 * @see java.sql.Clob#getCharacterStream()
	 */
	public Reader getCharacterStream() throws SQLException {
		return reader;
	}
	
	/**
	 * @see java.sql.Clob#setCharacterStream(long)
	 */
	public Writer setCharacterStream(long pos) throws SQLException {
		excep(); return null;
	}
	
	/**
	 * @see java.sql.Clob#getSubString(long, int)
	 */
	public String getSubString(long pos, int len) throws SQLException {
		excep(); return null;
	}
	
	/**
	 * @see java.sql.Clob#setString(long, String)
	 */
	public int setString(long pos, String string) throws SQLException {
		excep(); return 0;
	}
	
	/**
	 * @see java.sql.Clob#setString(long, String, int, int)
	 */
	public int setString(long pos, String string, int i, int j)
	throws SQLException {
		excep(); return 0;
	}
	
	/**
	 * @see java.sql.Clob#position(String, long)
	 */
	public long position(String string, long pos) throws SQLException {
		excep(); return 0;
	}
	
	/**
	 * @see java.sql.Clob#position(Clob, long)
	 */
	public long position(Clob colb, long pos) throws SQLException {
		excep(); return 0;
	}
	
	
	private static void excep() {
		throw new UnsupportedOperationException("Blob may not be manipulated from creating session");
	}
	
}






