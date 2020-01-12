package undeadborn.suikoden.tools.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

public class BinFile {

    private String name;
    private int id;
    private int offset;
    private int length;
    private List<String> errors;

    public BinFile(String name, int id, int offset, int length) {
        this.name = name;
        this.id = id;
        this.offset = offset;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public List<String> getErrors() {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        return errors;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

}
