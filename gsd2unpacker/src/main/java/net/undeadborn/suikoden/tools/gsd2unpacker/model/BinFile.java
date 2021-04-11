package net.undeadborn.suikoden.tools.gsd2unpacker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BinFile {
    private String name;
    private int id;
    private int offset;
    private int size;
    private int length;
    private List<String> errors;
}
