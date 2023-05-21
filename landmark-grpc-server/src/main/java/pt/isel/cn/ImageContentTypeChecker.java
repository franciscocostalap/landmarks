package pt.isel.cn;

public class ImageContentTypeChecker {
    private static final byte[] PNG_HEADER = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    private static final byte[] JPEG_HEADER = { (byte) 0xFF, (byte) 0xD8 };

    public static String getContentType(byte[] imageData) {
        if (imageData.length < 2) {
            return null; // Invalid or unsupported image format
        }

        // Check for PNG header
        if (compareBytes(imageData, PNG_HEADER)) {
            return "image/png";
        }

        // Check for JPEG header
        if (compareBytes(imageData, JPEG_HEADER)) {
            return "image/jpeg";
        }

        return null; // Invalid or unsupported image format
    }

    private static boolean compareBytes(byte[] arr1, byte[] arr2) {
        if (arr1.length < arr2.length) {
            return false;
        }

        for (int i = 0; i < arr2.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }

        return true;
    }
}