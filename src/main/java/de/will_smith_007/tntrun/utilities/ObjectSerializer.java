package de.will_smith_007.tntrun.utilities;

import com.google.inject.Singleton;
import lombok.Cleanup;

import java.io.*;
import java.util.concurrent.CompletableFuture;

@Singleton
public class ObjectSerializer {

    /**
     * Converts an object into a byte array asynchronously.
     *
     * @param serializableObject The object which should be serialized.
     * @return A {@link CompletableFuture} which contains the byte array.
     * @apiNote The object must implement the {@link Serializable} interface.
     * Please also note that all data in this object must be {@link Serializable}.
     */
    public <T extends Serializable> CompletableFuture<byte[]> serializeObjectAsync(T serializableObject) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                @Cleanup final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                @Cleanup final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(serializableObject);
                objectOutputStream.flush();
                return byteArrayOutputStream.toByteArray();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return new byte[0];
        });
    }

    /**
     * Converts a byte array into their original form as an object asynchronously.
     *
     * @param objectByteArray The byte array of the former object.
     * @return A {@link CompletableFuture} which contains the deserialized object.
     */
    public CompletableFuture<Object> deserializeObjectAsync(byte[] objectByteArray) {
        return CompletableFuture.supplyAsync(() -> deserializeObject(objectByteArray));
    }

    /**
     * Converts a byte array into their original form as an object synchronously.
     *
     * @param objectByteArray The byte array of the former object.
     * @return The deserialized object.
     */
    public Object deserializeObject(byte[] objectByteArray) {
        try {
            @Cleanup final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(objectByteArray);
            @Cleanup final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
