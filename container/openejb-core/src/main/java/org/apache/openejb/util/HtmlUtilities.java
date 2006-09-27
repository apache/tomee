package org.apache.openejb.util;

public class HtmlUtilities {

    public static final String ANCHOR_NAME_TYPE = "name";

    public static final String ANCHOR_HREF_TYPE = "href";

    //we don't want anyone creating new instances of this class
    private HtmlUtilities() {
    }

    public static String createAnchor(String value, String display, String type) {
        //our type must be one of these two
        if (!(ANCHOR_HREF_TYPE.equals(type) || ANCHOR_NAME_TYPE.equals(type))) {
            throw new IllegalArgumentException("The type argument must be either \"name\" or \"href\"");
        }

        return new StringBuffer(100)
                .append("<a ")
                .append(type)
                .append("=\"")
                .append(value)
                .append("\">")
                .append(display)
                .append("</a>")
                .toString();
    }

    public static String createSelectFormField(String name, String onChange) {
        StringBuffer temp = new StringBuffer(60).append("<select name=\"").append(name);

        if (onChange != null) {
            temp.append("\" onChange=\"").append(onChange);
        }

        return temp.append("\">").toString();
    }

    public static String createSelectOption(String value, String display, boolean selected) {
        StringBuffer temp = new StringBuffer(65).append("<option value=\"").append(value).append("\"");

        if (selected) {
            temp.append(" selected");
        }

        return temp.append(">").append(display).append("</option>").toString();
    }

    public static String createTextFormField(String name, String value, int size, int maxLength) {
        return createInputFormField("text", name, value, size, maxLength, null, null, null, null, false, false, false);
    }

    public static String createFileFormField(String name, String value, int size) {
        return createInputFormField("file", name, value, size, 0, null, null, null, null, false, false, false);
    }

    public static String createHiddenFormField(String name, String value) {
        return createInputFormField("hidden", name, value, 0, 0, null, null, null, null, false, false, false);
    }

    public static String createSubmitFormButton(String name, String value) {
        return createInputFormField("submit", name, value, 0, 0, null, null, null, null, false, false, false);
    }

    public static String createInputFormField(
            String type,
            String name,
            String value,
            int size,
            int maxLength,
            String onFocus,
            String onBlur,
            String onChange,
            String onClick,
            boolean checked,
            boolean disabled,
            boolean readOnly) {

        StringBuffer temp = new StringBuffer(150)
                .append("<input type=\"")
                .append(type)
                .append("\" name=\"")
                .append(name)
                .append("\" value=\"")
                .append(value)
                .append("\"");

        if (size > 0) {
            temp.append(" size=\"").append(size).append("\"");
        }
        if (maxLength > 0) {
            temp.append(" maxlength=\"").append(maxLength).append("\"");
        }
        if (onFocus != null) {
            temp.append(" onfocus=\"").append(onFocus).append("\"");
        }
        if (onBlur != null) {
            temp.append(" onblur=\"").append(onBlur).append("\"");
        }
        if (onChange != null) {
            temp.append(" onchange=\"").append(onChange).append("\"");
        }
        if (onClick != null) {
            temp.append(" onclick=\"").append(onClick).append("\"");
        }
        if (checked) {
            temp.append(" checked");
        }
        if (disabled) {
            temp.append(" disabled");
        }
        if (readOnly) {
            temp.append(" readonly");
        }

        return temp.append(">").toString();
    }

    public static String createTextArea(
            String name,
            String content,
            int rows,
            int columns,
            String onFocus,
            String onBlur,
            String onChange) {
        StringBuffer temp = new StringBuffer(50);
        temp.append("<textarea name=\"").append(name).append("\" rows=\"").append(rows).append("\" cols=\"").append(
                columns).append(
                "\"");

        if (onFocus != null) {
            temp.append(" onfocus=\"").append(onFocus).append("\"");
        }
        if (onBlur != null) {
            temp.append(" onblur=\"").append(onBlur).append("\"");
        }
        if (onChange != null) {
            temp.append(" onchange=\"").append(onChange).append("\"");
        }

        return temp.append(">").append(content).append("</textarea>").toString();
    }
}
