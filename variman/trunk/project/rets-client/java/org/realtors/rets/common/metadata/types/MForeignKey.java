package org.realtors.rets.common.metadata.types;

import java.util.Map;

import org.realtors.rets.common.metadata.MetaObject;
import org.realtors.rets.common.metadata.MetadataType;

public class MForeignKey extends MetaObject {
	public static final String FOREIGNKEYID = "ForeignKeyID";
	public static final String PARENTRESOURCEID = "ParentResourceID";
	public static final String PARENTCLASSID = "ParentClassID";
	public static final String PARENTSYSTEMNAME = "ParentSystemName";
	public static final String CHILDRESOURCEID = "ChildResourceID";
	public static final String CHILDCLASSID = "ChildClassID";
	public static final String CHILDSYSTEMNAME = "ChildSystemName";
	// 1.7.2
	public static final String CONDITIONALPARENTFIELD = "ConditionalParentField";
	public static final String CONDITIONALPARENTVALUE = "ConditionalParentValue";

	public MForeignKey() {
		this(DEFAULT_PARSING);
	}

	public MForeignKey(boolean strictParsing) {
		super(strictParsing);
	}

	public String getForeignKeyID() {
		return getStringAttribute(FOREIGNKEYID);
	}

	public String getParentResourceID() {
		return getStringAttribute(PARENTRESOURCEID);
	}

	public String getParentClassID() {
		return getStringAttribute(PARENTCLASSID);
	}

	public String getParentSystemName() {
		return getStringAttribute(PARENTSYSTEMNAME);
	}

	public String getChildResourceID() {
		return getStringAttribute(CHILDRESOURCEID);
	}

	public String getChildClassID() {
		return getStringAttribute(CHILDCLASSID);
	}

	public String getChildSystemName() {
		return getStringAttribute(CHILDSYSTEMNAME);
	}
	
	public String getConditionalParentField() {
		return getStringAttribute(CONDITIONALPARENTFIELD);
	}
	
	public String getConditionalParentValue() {
		return getStringAttribute(CONDITIONALPARENTVALUE);
	}

	@Override
	public MetadataType[] getChildTypes() {
		return sNoChildren;
	}

	@Override
	protected String getIdAttr() {
		return FOREIGNKEYID;
	}

	@Override
	protected void addAttributesToMap(Map attributeMap) {
		attributeMap.put(FOREIGNKEYID, sRETSID);
		attributeMap.put(PARENTRESOURCEID, sRETSID);
		attributeMap.put(PARENTCLASSID, sRETSID);
		attributeMap.put(PARENTSYSTEMNAME, sRETSNAME);
		attributeMap.put(CHILDRESOURCEID, sRETSID);
		attributeMap.put(CHILDCLASSID, sRETSID);
		attributeMap.put(CHILDSYSTEMNAME, sRETSNAME);
	
		// 1.7.2
		attributeMap.put(CONDITIONALPARENTFIELD, sRETSNAME);
		attributeMap.put(CONDITIONALPARENTVALUE, sRETSNAME);
	}

}
