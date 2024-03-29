package org.hibernate.test;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.hibernate.CompositeUserType;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.engine.SessionImplementor;
import net.sf.hibernate.type.Type;

public class MultiplicityType implements CompositeUserType {

	private static final String[] PROP_NAMES = new String[] { 
		"count", "glarch" 
	};
	private static final int[] SQL_TYPES = new int[] { 
		Hibernate.INTEGER.sqlType(), Hibernate.STRING.sqlType()
	};
	private static final Type[] TYPES = new Type[] { 
		Hibernate.INTEGER, Hibernate.entity(Glarch.class)
	};
	
	public String[] getPropertyNames() {
		return PROP_NAMES;
	}

	public Type[] getPropertyTypes() {
		return TYPES;
	}

	public Object getPropertyValue(Object component, int property) {
		Multiplicity o = (Multiplicity) component;
		return property==0 ? 
			(Object) new Integer(o.count) : 
			(Object) o.glarch;
	}

	public void setPropertyValue(
		Object component,
		int property,
		Object value) {
		
		Multiplicity o = (Multiplicity) component;
		if (property==0) {
			o.count = ( (Integer) value ).intValue();
		}
		else {
			o.glarch = (Glarch) value;
		}
	}

	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	public Class returnedClass() {
		return Multiplicity.class;
	}

	public boolean equals(Object x, Object y) {
		Multiplicity mx = (Multiplicity) x;
		Multiplicity my = (Multiplicity) y;
		if (mx==my) return true;
		if (mx==null || my==null) return false;
		return mx.count==my.count && mx.glarch==my.glarch;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
		throws HibernateException, SQLException {
		
		Integer c = (Integer) Hibernate.INTEGER.nullSafeGet( rs, names[0] );
		GlarchProxy g = (GlarchProxy) Hibernate.entity(Glarch.class).nullSafeGet(rs, names[1], session, owner);
		Multiplicity m = new Multiplicity();
		m.count = c==null ? 0 : c.intValue();
		m.glarch = g;
		return m;
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
		throws HibernateException, SQLException {
		
		Multiplicity o = (Multiplicity) value;
		GlarchProxy g;
		Integer c;
		if (o==null) {
			g=null;
			c=new Integer(0);
		}
		else {
			g = o.glarch;
			c = new Integer(o.count);
		}
		Hibernate.INTEGER.nullSafeSet(st, c, index, session);
		Hibernate.entity(Glarch.class).nullSafeSet(st, g, index+1, session);

	}

	public Object deepCopy(Object value) {
		if (value==null) return null;
		Multiplicity v = (Multiplicity) value;
		Multiplicity m = new Multiplicity();
		m.count = v.count;
		m.glarch = v.glarch;
		return m;
	}

	public boolean isMutable() {
		return true;
	}

	public Object assemble(
		Serializable cached,
		SessionImplementor session,
		Object owner) throws HibernateException {
		if (cached==null) return null;
		Serializable[] o = (Serializable[]) cached;
		Multiplicity m = new Multiplicity();
		m.count = ( (Integer) o[0] ).intValue();
		m.glarch = o[1]==null ? null : (GlarchProxy) session.internalLoad( Glarch.class, o[1] );
		return m;
	}

	public Serializable disassemble(Object value, SessionImplementor session) 
	throws HibernateException {
		if (value==null) return null;
		Multiplicity m = (Multiplicity) value;
		return new Serializable[] { new Integer(m.count), session.getEntityIdentifierIfNotUnsaved(m.glarch) } ;
	}

}
