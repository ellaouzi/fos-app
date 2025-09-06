package com.fosagri.application.views.autoform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.forms.FormRenderer;
import com.fosagri.application.forms.FormSchema;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;

@PageTitle("Auto Form")
@Route("auto-form")
public class AutoFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final Div formContainer = new Div();
    private final TextArea output = new TextArea("Réponses (JSON)");

    public AutoFormView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Auto Form"), formContainer, output);

        output.setWidthFull();
        output.setMinHeight("200px");
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String key) {
        formContainer.removeAll();
        try {
            FormSchema schema;
            if (key == null || key.isBlank()) {
                schema = FormSchema.loadFromClasspath("example");
            } else {
                schema = FormSchema.loadFromClasspath(key);
            }

            formContainer.add(FormRenderer.createForm(schema, answers -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    output.setValue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answers));
                } catch (Exception ex) {
                    output.setValue("Erreur: " + ex.getMessage());
                }
            }));
        } catch (Exception ex) {
            Notification.show("Schema introuvable: " + ex.getMessage());
        }
    }
}
