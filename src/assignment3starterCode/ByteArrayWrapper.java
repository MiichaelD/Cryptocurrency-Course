package assignment3starterCode;

import java.util.Arrays;

/** a wrapper for byte array with hashCode and equals function implemented */
public class ByteArrayWrapper {

    private byte[] contents;

    public ByteArrayWrapper(byte[] b) {
        contents = new byte[b.length];
        for (int i = 0; i < contents.length; i++)
            contents[i] = b[i];
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        ByteArrayWrapper otherB = (ByteArrayWrapper) other;
        byte[] b = otherB.contents;
        if (contents == null) {
            return b == null;
        } else if (b != null && contents.length == b.length){
            for (int i = 0; i < b.length; i++) {
                if (contents[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(contents);
    }
}
