package org.realtors.rets.server.protocol;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.realtors.rets.server.RetsServerException;

public class PatternObjectSetTest extends TestCase
{
    public void testFindObject() throws RetsServerException
    {
        ObjectDescriptor expected =
            new ObjectDescriptor("abc123", 2, localUrl("abc123-2.jpg"));

        PatternObjectSet objectSet = createObjectSet();
        ObjectDescriptor actual = objectSet.findObject("Photo", 2);
        assertEquals(expected, actual);
    }

    public void testNonexistantObjectId() throws RetsServerException
    {
        PatternObjectSet objectSet = createObjectSet();
        assertNull(objectSet.findObject("Photo", 3));
    }

    public void testNonexistantType() throws RetsServerException
    {
        PatternObjectSet objectSet = createObjectSet();
        assertNull(objectSet.findObject("Video", 0));
    }

    public void testFindDefaultObject() throws RetsServerException
    {
        ObjectDescriptor expected =
            new ObjectDescriptor("abc123", 1, localUrl("abc123-1.jpg"));

        PatternObjectSet objectSet = createObjectSet();
        ObjectDescriptor actual = objectSet.findObject("Photo", 0);
        assertEquals(expected, actual);
    }

    public void testFindAllObjects() throws RetsServerException
    {
        List expected = new ArrayList();
        expected.add(
            new ObjectDescriptor("abc123", 1, localUrl("abc123-1.jpg")));
        expected.add(
            new ObjectDescriptor("abc123", 2, localUrl("abc123-2.jpg")));

        PatternObjectSet objectSet = createObjectSet();
        List actual = objectSet.findAllObjects("Photo");
        assertEquals(expected, actual);
    }

    private PatternObjectSet createObjectSet()
    {
        // The file name will get stripped off, so all that's import is that it
        // exists.
        String imageDirectory = directoryOfResource(GIF_FILE);
        PatternObjectSet objectSet =
            new PatternObjectSet(imageDirectory, "%k-%i.jpg", "abc123");
        return objectSet;
    }

    private String directoryOfResource(String resourceName)
    {
        URL imageUrl = getClass().getResource(resourceName);
        String imagePath = imageUrl.getPath();
        String imageDirectory =
            imagePath.substring(0, imagePath.lastIndexOf(resourceName) - 1);
        return imageDirectory;
    }

    private URL localUrl(String resourceName)
    {
        return getClass().getResource(resourceName);
    }

    public static final String GIF_FILE = "abc123-1.gif";
}
