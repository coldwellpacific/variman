/*
 */
package org.realtors.rets.server.metadata;

import java.util.HashSet;
import java.util.Set;

/**
 * Utilize ObjectMother test pattern.
 */
public class ObjectMother
{
    public static MSystem createSystem()
    {
        MSystem system = new MSystem();
        return system;
    }

    public static Resource createResource()
    {
        Resource resource = new Resource();
        MSystem system = createSystem();
        resource.setSystem(system);
        resource.setResourceID("Property");
        resource.updateLevel();

        Set resources = new HashSet();
        resources.add(resource);
        system.setResources(resources);
        return resource;
    }

    public static MClass createClass()
    {
        MClass clazz = new MClass();
        Resource resource = createResource();
        clazz.setClassName("RES");
        resource.setClasses(new HashSet());
        resource.addClass(clazz);
        return clazz;
    }

    public static Table createTable()
    {
        Table table = new Table();
        MClass clazz = createClass();
        table.setSystemName("E_SCHOOL");
        clazz.setTables(new HashSet());
        clazz.addTable(table);
        return table;
    }

    public static Update createUpdate()
    {
        Update update = new Update();
        update.setMClass(createClass());
        update.setUpdateName("Change");
        update.updateLevel();
        return update;
    }

    public static UpdateType createUpdateType()
    {
        UpdateType updateType = new UpdateType();
        updateType.setUpdate(createUpdate());
        updateType.updateLevel();
        return updateType;
    }

    public static MObject createMObject()
    {
        MObject object = new MObject();
        object.setResource(createResource());
        return object;
    }

    public static SearchHelp createSearchHelp()
    {
        SearchHelp searchHelp = new SearchHelp();
        searchHelp.setResource(createResource());
        searchHelp.updateLevel();
        return searchHelp;
    }

    public static EditMask createEditMask()
    {
        EditMask editMask = new EditMask();
        editMask.setResource(createResource());
        editMask.updateLevel();
        return editMask;
    }

    public static Lookup createLookup()
    {
        Resource resource = createResource();
        Lookup lookup = new Lookup();
        lookup.setLookupName("E_SCHOOL");
        resource.setLookups(new HashSet());
        resource.addLookup(lookup);
        return lookup;
    }

    public static LookupType createLookupType()
    {
        Lookup lookup = createLookup();
        LookupType lookupType = new LookupType();
        lookupType.setValue("303");
        lookup.setLookupTypes(new HashSet());
        lookup.addLookupType(lookupType);
        return lookupType;
    }

    public static ValidationLookup createValidationLookup()
    {
        ValidationLookup validationLookup = new ValidationLookup();
        validationLookup.setResource(createResource());
        validationLookup.setValidationLookupName("School");
        validationLookup.updateLevel();
        return validationLookup;
    }

    public static ValidationLookupType createValidationLookupType()
    {
        ValidationLookupType validationLookupType = new ValidationLookupType();
        validationLookupType.setValidationLookup(createValidationLookup());
        validationLookupType.updateLevel();
        return validationLookupType;
    }

    public static ValidationExternal createValidationExternal()
    {
        ValidationExternal validationExternal = new ValidationExternal();
        validationExternal.setResource(createResource());
        validationExternal.setValidationExternalName("VET1");
        validationExternal.updateLevel();
        return validationExternal;
    }

    public static ValidationExternalType createValidationExternalType()
    {
        ValidationExternalType validationExternalType =
            new ValidationExternalType();
        validationExternalType.setValidationExternal(
            createValidationExternal());
        validationExternalType.updateLevel();
        return validationExternalType;
    }

    public static ValidationExpression createValidationExpression()
    {
        ValidationExpression validationExpression = new ValidationExpression();
        validationExpression.setResource(createResource());
        validationExpression.updateLevel();
        return validationExpression;
    }

    public static ForeignKey createForeignKey()
    {
        ForeignKey foreignKey = new ForeignKey();
        foreignKey.setSystem(createSystem());
        return foreignKey;
    }
}
