
<center>
<a href="https://vaadin.com">
 <img src="https://vaadin.com/images/hero-reindeer.svg" width="200" height="200" /></a>
</center>

# Vaadin V8 (LTS) - Simple WebApp in Java

This demo will show how to start with a simple Vaadin V8 webapp,
based on pure Java API running on TomEE (webprofile)

The Vaadin Framework is OpenSource and available at [Github](https://github.com/vaadin/framework)

## Build this example
To build this example, just run *mvn clean install tomee:run*
You will find the app running under [http://localhost:8080/](http://localhost:8080/)

## Implementation
This implementation is using the [Vaadin 8 API](https://vaadin.com/framework).

```java
public class HelloVaadin {

    public static class MyUI extends UI {
        @Override
        protected void init(VaadinRequest request) {

            //create the components you want to use
            // and set the main component with setContent(..)
            final Layout layout = new VerticalLayout();
            layout
                .addComponent(new Button("click me",
                                         event -> layout.addComponents(new Label("clicked again"))
                ));

            //set the main Component
            setContent(layout);
        }

        @WebServlet("/*")
        @VaadinServletConfiguration(productionMode = false, ui = MyUI.class)
        public static class MyProjectServlet extends VaadinServlet { }
    }
}

```

The documentation of the Vaadin Framework is available [here](https://vaadin.com/docs/v8/framework/tutorial.html)


## Support Information's
Vaadin Framework 8 is the latest version based on GWT. V8 itself is a LTS version.

The new Vaadin Platform is based on WebComponents.
As of Vaadin 10, Vaadin is moving to a release train model with four major releases every year. 
This allows them to ship new features faster to developers. 
Vaadin is continuing their commitment to long-term stability with long-term support (LTS) releases. 
The LTS releases will come out approximately every two years and offer 5 years of support.