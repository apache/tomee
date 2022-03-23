/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.moviefun;

import org.superbiz.moviefun.util.JsfUtil;
import org.superbiz.moviefun.util.PaginationHelper;

import jakarta.ejb.EJB;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.SessionScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ResourceBundle;

@ManagedBean(name = "movieController")
@SessionScoped
public class MovieController implements Serializable {


    private Movie current;
    private DataModel items;
    @EJB
    private Movies ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public Movie getSelected() {
        if (current == null) {
            current = new Movie();
            selectedItemIndex = -1;
        }
        return current;
    }

    private Movies getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Movie) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Movie();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().addMovie(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("MovieCreated"));
            return prepareCreate();
        } catch (final Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Movie) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().editMovie(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("MovieUpdated"));
            return "View";
        } catch (final Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Movie) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().deleteMovieId(current.getId());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("MovieDeleted"));
        } catch (final Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        final int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.getMovies(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.getMovies(), true);
    }

    @FacesConverter(forClass = Movie.class)
    public static class MovieControllerConverter implements Converter {

        public Object getAsObject(final FacesContext facesContext, final UIComponent component, final String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            final MovieController controller = (MovieController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "movieController");
            return controller.ejbFacade.find(getKey(value));
        }

        long getKey(final String value) {
            final long key;
            key = Long.parseLong(value);
            return key;
        }

        String getStringKey(final long value) {
            final StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        public String getAsString(final FacesContext facesContext, final UIComponent component, final Object object) {
            if (object == null) {
                return null;
            }
            if (Movie.class.isInstance(object)) {
                final Movie o = Movie.class.cast(object);
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + MovieController.class.getName());
            }
        }

    }

}
