package com.fosagri.application.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormSchema {
    private String key;
    private String title;
    private List<FormField> fields = new ArrayList<>();

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<FormField> getFields() { return fields; }
    public void setFields(List<FormField> fields) { this.fields = fields; }

    // Utility to load schema from classpath forms/<key>.json
    public static FormSchema loadFromClasspath(String key) throws IOException {
        String path = String.format("/forms/%s.json", key);
        try (InputStream is = FormSchema.class.getResourceAsStream(path)) {
            if (is == null) throw new IOException("Schema not found: " + path);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, FormSchema.class);
        }
    }

    // Serialize to JSON
    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    // Deserialize from JSON
    public static FormSchema fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, FormSchema.class);
    }
}
