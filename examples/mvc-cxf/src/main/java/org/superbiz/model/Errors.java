package org.superbiz.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.mvc.binding.ParamError;

@Named("error")
@RequestScoped
public class Errors {

    private List<ParamError> errors = new ArrayList<>();

    public void setErrors(List<ParamError> messages) {
        this.errors = messages;
    }

    public String getErrors() {
        return errors.stream()
                     .map(ParamError::getMessage)
                     .collect(Collectors.joining("<br>"));
    }

    public String getMessage(String param) {
        return errors.stream()
                     .filter(v -> v.getParamName().equals(param))
                     .map(ParamError::getMessage)
                     .findFirst()
                     .orElse("");
    }
}