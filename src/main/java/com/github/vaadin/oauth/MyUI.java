package com.github.vaadin.oauth;

import com.github.vaadin.oauth.util.OAuthProvider;
import com.github.vaadin.oauth.util.OAuthUtil;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.cdi.CDIUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Theme("mytheme")
@Widgetset("com.github.vaadin.oauth.MyAppWidgetset")
@CDIUI
public class MyUI extends UI {

    @Inject
    OAuthUtil oAuthUtil;

    private String email;
    private Label nameLabel;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);

        Button button = new Button("Click Me");
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                layout.addComponent(new Label("Thank you for clicking"));
            }
        });
        layout.addComponent(button);

        Button login = new Button("Login");
        login.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                doLogin();
            }
        });
        layout.addComponent(login);

        nameLabel = new Label();
        nameLabel.setImmediate(true);
        layout.addComponent(nameLabel);
        HttpServletRequest request = (HttpServletRequest) vaadinRequest;
        email = (String) request.getSession().getAttribute("EMAIL");
        nameLabel.setCaption(email);

    }

    public String getUserName() {
        return email;
    }

    public void setUserName(String userName) {
        this.email = userName;
        nameLabel.setCaption(userName);
    }

    private void doLogin() {

        OAuthProvider provider = oAuthUtil.getByName("GOOGLE");
        String url = provider.getOAuth().getAuthorizationUrl(provider.getScopes(),
                String.valueOf(System.currentTimeMillis()));
        getUI().getPage().setLocation(url);
    }

}
