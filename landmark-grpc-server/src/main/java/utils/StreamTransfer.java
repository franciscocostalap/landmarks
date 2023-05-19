package storageoperations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Class that transfers data from an input stream to an output stream.
 */
public class StreamTransfer {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * Transfers all data from the input stream to the output stream.
     * This method closes both streams.
     * @param in the input stream
     * @param out the output stream
     * @throws IOException if an I/O error occurs
     */
    public static void transfer(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while((bytesRead = in.read(buffer)) != -1){
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.close();
    }

    public static void transfer(InputStream in, OutputStream out) throws IOException {
        transfer(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Transfers all data from the input stream to the output channel.
     *
     * Transfers the data by chunks of the given buffer size.
     *
     * @param in the input stream
     * @param outChannel the output channel
     * @param bufferSize the size of the buffer of each chunk
     * @throws IOException if an I/O error occurs
     */
    public static void transfer(InputStream in, WritableByteChannel outChannel, int bufferSize) throws IOException {

        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while((bytesRead = in.read(buffer)) != -1){
            outChannel.write(java.nio.ByteBuffer.wrap(buffer, 0, bytesRead));
        }
        in.close();
        outChannel.close();

    }

    public static void transfer(InputStream in, WritableByteChannel outChannel) throws IOException {
        transfer(in, outChannel, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Reads all data from the input channel and writes them to the output stream.
     * This method closes both streams.
     *
     * @param inChannel the input channel
     * @param out the output stream
     * @throws IOException if an I/O error occurs
     */
    public static void transfer(ReadableByteChannel inChannel, OutputStream out) throws IOException {
        transfer(inChannel, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Reads all data from the input channel and writes them to the output stream.
     * This method closes both streams.
     *
     * @param inChannel the input channel
     * @param out the output stream
     * @param bufferSize the size of the buffer of each chunk read from the channel
     * @throws IOException if an I/O error occurs
     */
    public static void transfer(ReadableByteChannel inChannel, OutputStream out, int bufferSize) throws IOException {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(bufferSize);
        int bytesRead;
        while((bytesRead = inChannel.read(buffer)) != -1){
            out.write(buffer.array(), 0, bytesRead);
            buffer.clear();
        }
        inChannel.close();
        out.close();
    }

}
