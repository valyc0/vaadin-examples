package io.bootify.my_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;


@SpringBootApplication
@Push(PushMode.AUTOMATIC)
public class MyAppApplication implements AppShellConfigurator {

    public static void main(final String[] args) {
        SpringApplication.run(MyAppApplication.class, args);
    }

}
