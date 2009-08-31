/*
 * Variman RETS Server
 *
 * Author: Dave Dribin
 * Copyright (c) 2004, The National Association of REALTORS
 * Distributed under a BSD-style license.  See LICENSE.TXT for details.
 */

/*
 */
package org.realtors.rets.server.metadata.format;

import java.util.Collection;
import java.util.Iterator;

import org.realtors.rets.client.RetsVersion;
import org.realtors.rets.common.util.DataRowBuilder;
import org.realtors.rets.common.util.TagBuilder;
import org.realtors.rets.server.metadata.Lookup;

public class CompactLookupFormatter extends MetadataFormatter
{
    public void format(FormatterContext context, Collection lookups,
                       String[] levels)
    {
        if (lookups.size() == 0)
        {
            return;
        }
        RetsVersion retsVersion = context.getRetsVersion();
        
        TagBuilder tag = new TagBuilder(context.getWriter(), "METADATA-LOOKUP")
            .appendAttribute("Resource", levels[RESOURCE_LEVEL])
            .appendAttribute("Version", context.getVersion())
            .appendAttribute("Date", context.getDate(), context.getRetsVersion())
            .beginContentOnNewLine();
        
        // Version and Date become LookupTypeVersion and LookupTypeDate in the 1.7.2 DTD.
        if (retsVersion.equals(RetsVersion.RETS_1_0) || retsVersion.equals(RetsVersion.RETS_1_5) ||
                retsVersion.equals(RetsVersion.RETS_1_7))
        {
            tag.appendColumns(COLUMNS);
        }
        else
        {
            tag.appendColumns(COLUMNS_1_7_2);
        }
        
        for (Iterator iterator = lookups.iterator(); iterator.hasNext();)
        {
            Lookup lookup = (Lookup) iterator.next();
            appendDataRow(context, lookup);
        }
        tag.close();

        if (context.isRecursive())
        {
            for (Iterator iterator = lookups.iterator(); iterator.hasNext();)
            {
                Lookup lookup = (Lookup) iterator.next();
                context.format(lookup.getLookupTypes(),
                               lookup.getPathAsArray());
            }
        }
    }

    private void appendDataRow(FormatterContext context, Lookup lookup)
    {
        DataRowBuilder row = new DataRowBuilder(context.getWriter());
        row.begin();
        row.append(lookup.getMetadataEntryID());
        row.append(lookup.getLookupName());
        row.append(lookup.getVisibleName());
        row.append(context.getVersion());
        row.append(context.getDate(), context.getRetsVersion());
        row.end();
    }

    private static final String[] COLUMNS = new String[] {
        "MetadataEntryID", "LookupName", "VisibleName", "Version", "Date",
    };
    private static final String[] COLUMNS_1_7_2 = new String[] {
        "MetadataEntryID", "LookupName", "VisibleName", "LookupTypeVersion", "LookupTypeDate",
    };
}
