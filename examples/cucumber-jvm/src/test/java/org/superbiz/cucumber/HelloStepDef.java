package org.superbiz.cucumber;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class HelloStepDef {
    @Inject
    private Hello hello;

    private String name;

    @Given("^A name '([a-z]*)'$")
    public void initName(final String name) {
        this.name = name;
    }

    @Then("^The bean says '([ a-z]*)'$")
    public void checkResult(final String result) {
        assertEquals(result, hello.hello(name));
    }
}
