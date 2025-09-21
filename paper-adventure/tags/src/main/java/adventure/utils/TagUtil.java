package adventure.utils;

import java.text.MessageFormat;

public class TagUtil {
    private static final MessageFormat ERROR_FORMAT = new MessageFormat("No argument {0} key provided");

    public static String parseErrorMsg(String tagName) {
        return ERROR_FORMAT.format(tagName);
    }
}
