package com.fosagri.application.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormField {
    private String name;
    private String label;
    private String type; // text, number, date, select, checkbox, file
    private String placeholder;
    private Boolean required;
    private List<FieldOption> options; // for select
    private Condition condition; // simple show/hide condition
    private Integer order; // display order
    private Integer maxFiles; // maximum number of files for file type
    private String acceptedFileTypes; // accepted file types (e.g., ".pdf,.doc,.docx")

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public List<FieldOption> getOptions() { return options; }
    public void setOptions(List<FieldOption> options) { this.options = options; }

    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    public Integer getMaxFiles() { return maxFiles; }
    public void setMaxFiles(Integer maxFiles) { this.maxFiles = maxFiles; }

    public String getAcceptedFileTypes() { return acceptedFileTypes; }
    public void setAcceptedFileTypes(String acceptedFileTypes) { this.acceptedFileTypes = acceptedFileTypes; }
}
