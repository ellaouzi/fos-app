package com.fosagri.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

@Route("home")
public class HomeView extends VerticalLayout {

    public HomeView() {

        add(new H1("Welcome to your new application"));
        add(new Paragraph("This is the home view"));

        add(new Paragraph("You can edit this view in src/main/java/com/fosagri/application/views/HomeView.java"));

        FormLayout form = new FormLayout();
        TextArea comments = new TextArea("Comments");
        comments.setPlaceholder("Enter your comments here...");
        comments.setWidthFull();
        comments.setMaxLength(500);
        form.add(comments);
        add(form);

    }
}
