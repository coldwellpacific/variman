/*
 */
package org.realtors.rets.server.dmql;

import org.apache.commons.lang.builder.EqualsBuilder;

public class ConstantStringComponent extends Object
    implements DmqlStringComponent
{
    public ConstantStringComponent(String constant)
    {
        mConstant = constant;
    }

    public String toString()
    {
        return mConstant;
    }

    public String toSql()
    {
        return mConstant;
    }

    public boolean conatinsWildcards()
    {
        return false;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof ConstantStringComponent))
        {
            return false;
        }
        ConstantStringComponent rhs = (ConstantStringComponent) o;
        return new EqualsBuilder()
            .append(mConstant, rhs.mConstant)
            .isEquals();
    }

    protected String mConstant;
}
