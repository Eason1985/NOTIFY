package com.mdiaf.notify.utils;

import java.io.*;

/**
 * Created by Eason on 15/10/4.
 */
public class SerializationUtils {

    /**
     * Serialize the object provided.
     *
     * @param object the object to serialize
     * @return an array of bytes representing the object in a portable fashion
     */
    public static byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(stream).writeObject(object);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not serialize object of type: " + object.getClass(), e);
        }
        return stream.toByteArray();
    }

    /**
     * @param bytes a serialized object created
     * @return the result of deserializing the bytes
     */
    public static Object deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return deserialize(new ObjectInputStream(new ByteArrayInputStream(bytes)));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize object", e);
        }
    }

    /**
     * @param stream an object stream created from a serialized object
     * @return the result of deserializing the bytes
     */
    public static Object deserialize(ObjectInputStream stream) {
        if (stream == null) {
            return null;
        }
        try {
            return stream.readObject();
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize object", e);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not deserialize object type", e);
        }
    }

}

