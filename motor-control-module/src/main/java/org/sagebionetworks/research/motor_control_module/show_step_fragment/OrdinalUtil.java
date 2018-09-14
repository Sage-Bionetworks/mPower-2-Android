package org.sagebionetworks.research.motor_control_module.show_step_fragment;


public class OrdinalUtil {
    private OrdinalUtil() {}

    public static String getNumberOrdinal(Number number) {
        if (number == null) {
            return null;
        }

        String format = "{0,ordinal}";
        return com.ibm.icu.text.MessageFormat.format(format, number);
    }
}
