package net.undeadborn.suikoden.tools.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class BinFile {
    private int id;
    private String name;
    private int offset;
    private int fSize;
    private int dSize;
    private int dummy;
    @JsonIgnore
    private File file;
    @JsonIgnore
    private List<String> errors;
}