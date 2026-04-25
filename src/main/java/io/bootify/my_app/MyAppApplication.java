package io.bootify.my_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;


@SpringBootApplication
@EnableScheduling
@Push(PushMode.AUTOMATIC)
@CssImport("./global.css")
public class MyAppApplication implements AppShellConfigurator {

    public static void main(final String[] args) {
        SpringApplication.run(MyAppApplication.class, args);
    }

}
