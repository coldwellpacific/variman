/*
 * Created on Jul 28, 2003
 *
 */
package org.realtors.rets.server.importer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.hibernate.HibernateException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.log4j.Logger;
import org.realtors.rets.server.metadata.MClass;
import org.realtors.rets.server.metadata.Resource;
import org.realtors.rets.server.metadata.Table;

/**
 * Creates a database schema based on the metadata currently in the database.
 * Currently optimized for postgres, but should be fairly portable.
 * 
 * @author kgarner
 */
public class CreateSchema extends RetsHelpers
{
    /**
     * Constructor that inits hibernate and loads the type mappings.
     * 
     * @throws HibernateException if hibernate gets ticked
     */
    public CreateSchema()
        throws HibernateException
    {
        super();
        mLs = System.getProperty("line.separator");
        mTypeMappings = new HashMap();
        loadTypeMapping();
        mFileName = null;
    }

    public String createTables()
    {
        Iterator i = mClasses.values().iterator();
        StringBuffer sb = new StringBuffer();
        while (i.hasNext())
        {
            MClass clazz = (MClass) i.next();
            Resource resource = clazz.getResource();
            StringBuffer tmp = new StringBuffer("rets_");
            tmp.append(resource.getResourceID()).append("_");
            tmp.append(clazz.getClassName());
            String sqlTableName = tmp.toString();
            sb.append("CREATE TABLE ").append(sqlTableName);
            sb.append(" (").append(mLs);
            sb.append("\tid INT8,").append(mLs);
            Set needsIndex = new HashSet();
            Iterator j = clazz.getTables().iterator();
            while (j.hasNext())
            {
                Table table = (Table) j.next();
                sb.append("\t").append(table.getDbName()).append(" ");
                switch (table.getDataType().toInt())
                {
                    case 0:
                        sb.append(mTypeMappings.get("boolean"));
                        break;
                    case 1:
                        sb.append(mTypeMappings.get("character")).append("(");
                        sb.append(table.getMaximumLength()).append(")");
                        break;
                    case 2:
                        sb.append(mTypeMappings.get("date"));
                        break;
                    case 3:
                        sb.append(mTypeMappings.get("datetime"));
                        break;
                    case 4:
                        sb.append(mTypeMappings.get("time"));
                        break;
                    case 5:
                        sb.append(mTypeMappings.get("tiny"));
                        break;
                    case 6:
                        sb.append(mTypeMappings.get("small"));
                        break;
                    case 7:
                        sb.append(mTypeMappings.get("int"));
                        break;
                    case 8:
                        sb.append(mTypeMappings.get("long"));
                        break;
                    case 9:
                        sb.append(mTypeMappings.get("decimal"));
                        break;
                }
                sb.append(",").append(mLs);
                if (table.getIndex() > 0)
                {
                    needsIndex.add(table);
                }
            }
            sb.append("\tprimary key(id)").append(mLs);
            sb.append(");").append(mLs);
            j = needsIndex.iterator();
            while (j.hasNext())
            {
                Table table = (Table) j.next();
                String dbName = table.getDbName();
                sb.append("create index ").append(sqlTableName).append("_");
                sb.append(dbName).append("_index on ").append(sqlTableName);
                sb.append("(").append(dbName).append(");").append(mLs);
            }
        }

        return sb.toString();
    }

    /**
     * 
     * @return
     */
    public String getFileName()
    {
        return mFileName;
    }
    
    private void loadTypeMapping()
    {
        Properties props = new Properties();
        try
        {
            InputStream is =
                getClass().getResourceAsStream("retstypemappings.properties");
            if (is != null)
            {
                props.load(is);
            }
        }
        catch (IOException e)
        {
            LOG.warn("Error loading retstypemappings.properties", e);
        }

        mTypeMappings.put("boolean",
                          props.getProperty("rets.db.boolean", "BOOL"));
        mTypeMappings.put("character",
                          props.getProperty("rets.db.character", "VARCHAR"));
        mTypeMappings.put("date", props.getProperty("rets.db.date", "DATE"));
        mTypeMappings.put("datetime",
                          props.getProperty("rets.db.datetime", "TIMESTAMP"));
        mTypeMappings.put("time",
                          props.getProperty("rets.db.time", "TIME"));
        mTypeMappings.put("tiny", props.getProperty("rets.db.tiny", "INT2"));
        mTypeMappings.put("small", props.getProperty("rets.db.small", "INT2"));
        mTypeMappings.put("int", props.getProperty("rets.db.int", "INT4"));
        mTypeMappings.put("long", props.getProperty("rets.db.long", "INT8"));
        mTypeMappings.put("decimal",
                          props.getProperty("rets.db.decimal", "NUMERIC"));
    }
    
    private void parseArgs(CommandLine cmd)
    {
        if (cmd.hasOption('f'))
        {
            mFileName = cmd.getOptionValue('f', "schema.out");
        }
    }
    
    private static Options getOptions()
    {
        Options ops = new Options();
        ops.addOption("f", "file", true, "output file");
        return ops;
    }
    
    public static void main(String[] args)
        throws HibernateException, ParseException, IOException
    {
        Parser parser = new GnuParser();
        Options opts = getOptions();
        CommandLine cmdl = null;
        try
        {
            cmdl = parser.parse(opts, args);
        }
        catch (Exception e)
        {
            printHelp(opts);
            System.exit(1);
        }
        CreateSchema cs = new CreateSchema();
        cs.parseArgs(cmdl);
        
        cs.loadMetadata();
        String schema = cs.createTables();
        if (cs.getFileName() != null)
        {
            writeFile(cs.getFileName(), schema);
        }
        else
        {
            System.out.print(schema);
        }
    }

    private static void printHelp(Options opt)
    {
        HelpFormatter fs = new HelpFormatter();
        fs.printHelp("CreateSchema [options]", opt);
    }

    /**
     * 
     * @param string
     * @param schema
     */
    private static void writeFile(String fileName, String schema)
        throws IOException
    {
        PrintWriter pw = new PrintWriter(new FileWriter(fileName));
        pw.print(schema);
        pw.close();
    }

    private String mFileName;
    /** The Line Seperator */
    private String mLs;
    private Map mTypeMappings;
    private static Logger LOG = Logger.getLogger(CreateSchema.class);

}