Title: Moviefun

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/webapps/moviefun) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/webapps/moviefun). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.ejb.EJB;
    import javax.servlet.ServletException;
    import javax.servlet.http.HttpServlet;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import javax.servlet.http.HttpSession;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.ListIterator;
    
    /**
     * @version $Revision$ $Date$
     */
    public class ActionServlet extends HttpServlet {
    
        @EJB(name = "movies")
        private Movies moviesBean;
    
    	@Override
    	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		process(request, response);
    	}
    
    	@Override
    	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		process(request, response);
    	}
    
    	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    		HttpSession session = request.getSession();
    		
    	    List<Movie> movies = null;
    	    ListIterator<Movie> listIterator = null;
    	    int display = 5;
    	    
    	    String action = request.getParameter("action");
    
    	    
    		if ("Add".equals(action)) {
    
    	        String title = request.getParameter("title");
    	        String director = request.getParameter("director");
    	        String genre = request.getParameter("genre");
    	        int rating = Integer.parseInt(request.getParameter("rating"));
    	        int year = Integer.parseInt(request.getParameter("year"));
    
    	        Movie movie = new Movie(title, director, genre, rating, year);
    
    	        moviesBean.addMovie(movie);
    
    	    } else if ("Remove".equals(action)) {
    
    	        String[] ids = request.getParameterValues("id");
    	        for (String id : ids) {
    	            moviesBean.deleteMovieId(new Long(id));
    	        }
    
    	    } else if (">>".equals(action)) {
    
    	        movies = (List) session.getAttribute("movies.collection");
    	        listIterator = (ListIterator) session.getAttribute("movies.iterator");
    
    	    } else if ("<<".equals(action)) {
    
    	        movies = (List) session.getAttribute("movies.collection");
    	        listIterator = (ListIterator) session.getAttribute("movies.iterator");
    	        for (int i = display * 2; i > 0 && listIterator.hasPrevious(); i--) {
    	            listIterator.previous(); // backup
    	        }
    
    	    } else if ("findByTitle".equals(action)) {
    
    	        movies = moviesBean.findByTitle(request.getParameter("key"));
    
    	    } else if ("findByDirector".equals(action)) {
    
    	        movies = moviesBean.findByDirector(request.getParameter("key"));
    
    	    } else if ("findByGenre".equals(action)) {
    
    	        movies = moviesBean.findByGenre(request.getParameter("key"));
    	    }
    
    	    if (movies == null) {
    	        try {
    	            movies = moviesBean.getMovies();
    	        } catch (Throwable e) {
    	            // We must not have run setup yet
    	            response.sendRedirect("setup.jsp");
    	            return;
    	        }
    	    }
    
    	    if (listIterator == null) {
    	        listIterator = movies.listIterator();
    	    }
    
    	    session.setAttribute("movies.collection", movies);
    	    session.setAttribute("movies.iterator", listIterator);
    	    
    	    List<Movie> moviesToShow = new ArrayList<Movie>();
    	    
    	    boolean hasPrevious = listIterator.hasPrevious();
    	    
    	    int start = listIterator.nextIndex();
            
    	    for (int i=display; i > 0 && listIterator.hasNext(); i-- ) {
    	    	    Movie movie = (Movie) listIterator.next();
    	    	    moviesToShow.add(movie);
    	    }
            
    	    boolean hasNext = listIterator.hasNext();
    			
    	    int end = listIterator.nextIndex();
    	    request.setAttribute("movies", moviesToShow);
    	    request.setAttribute("start", start);
    	    request.setAttribute("end", end);
    	    request.setAttribute("total", movies.size());
    	    request.setAttribute("display", display);
    	    request.setAttribute("hasNext", hasNext);
    	    request.setAttribute("hasPrev", hasPrevious);
    		
    	    request.getRequestDispatcher("WEB-INF/moviefun.jsp").forward(request, response);
    	}
    
    }
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import org.superbiz.moviefun.util.JsfUtil;
    import org.superbiz.moviefun.util.PaginationHelper;
    
    import javax.ejb.EJB;
    import javax.faces.bean.ManagedBean;
    import javax.faces.bean.SessionScoped;
    import javax.faces.component.UIComponent;
    import javax.faces.context.FacesContext;
    import javax.faces.convert.Converter;
    import javax.faces.convert.FacesConverter;
    import javax.faces.model.DataModel;
    import javax.faces.model.ListDataModel;
    import javax.faces.model.SelectItem;
    import java.io.Serializable;
    import java.util.ResourceBundle;
    
    @ManagedBean(name = "movieController")
    @SessionScoped
    public class MovieController implements Serializable {
    
    
        private Movie current;
        private DataModel items = null;
        @EJB
        private MoviesImpl ejbFacade;
        private PaginationHelper pagination;
        private int selectedItemIndex;
    
        public MovieController() {
        }
    
        public Movie getSelected() {
            if (current == null) {
                current = new Movie();
                selectedItemIndex = -1;
            }
            return current;
        }
    
        private MoviesImpl getFacade() {
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
            } catch (Exception e) {
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
            } catch (Exception e) {
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
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    
        private void updateCurrentItem() {
            int count = getFacade().count();
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
    
            public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
                if (value == null || value.length() == 0) {
                    return null;
                }
                MovieController controller = (MovieController) facesContext.getApplication().getELResolver().
                        getValue(facesContext.getELContext(), null, "movieController");
                return controller.ejbFacade.find(getKey(value));
            }
    
            long getKey(String value) {
                long key;
                key = Long.parseLong(value);
                return key;
            }
    
            String getStringKey(long value) {
                StringBuffer sb = new StringBuffer();
                sb.append(value);
                return sb.toString();
            }
    
            public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
                if (object == null) {
                    return null;
                }
                if (object instanceof Movie) {
                    Movie o = (Movie) object;
                    return getStringKey(o.getId());
                } else {
                    throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + MovieController.class.getName());
                }
            }
    
        }
    
    }
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.ejb.EJB;
    import javax.ejb.LocalBean;
    import javax.ejb.Stateless;
    import javax.jws.WebService;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.TypedQuery;
    import javax.persistence.criteria.CriteriaBuilder;
    import javax.persistence.criteria.CriteriaQuery;
    import javax.persistence.criteria.Path;
    import javax.persistence.criteria.Predicate;
    import javax.persistence.criteria.Root;
    import javax.persistence.metamodel.EntityType;
    import java.util.List;
    
    @LocalBean
    @Stateless(name = "Movies")
    @WebService(portName = "MoviesPort",
            serviceName = "MoviesWebService",
            targetNamespace = "http://superbiz.org/wsdl")
    public class MoviesImpl implements Movies, MoviesRemote {
    
        @EJB
        private Notifier notifier;
    
        @PersistenceContext(unitName = "movie-unit")
        private EntityManager entityManager;
    
        @Override
        public Movie find(Long id) {
            return entityManager.find(Movie.class, id);
        }
    
        @Override
        public void addMovie(Movie movie) {
            entityManager.persist(movie);
        }
    
        @Override
        public void editMovie(Movie movie) {
            entityManager.merge(movie);
        }
    
        @Override
        public void deleteMovie(Movie movie) {
            entityManager.remove(movie);
            notifier.notify("Deleted Movie \"" + movie.getTitle() + "\" (" + movie.getYear() + ")");
        }
    
        @Override
        public void deleteMovieId(long id) {
            Movie movie = entityManager.find(Movie.class, id);
            deleteMovie(movie);
        }
    
        @Override
        public List<Movie> getMovies() {
            CriteriaQuery<Movie> cq = entityManager.getCriteriaBuilder().createQuery(Movie.class);
            cq.select(cq.from(Movie.class));
            return entityManager.createQuery(cq).getResultList();
        }
    
        @Override
        public List<Movie> findByTitle(String title) {
            return findByStringField("title", title);
        }
    
        @Override
        public List<Movie> findByGenre(String genre) {
            return findByStringField("genre", genre);
        }
    
        @Override
        public List<Movie> findByDirector(String director) {
            return findByStringField("director", director);
        }
    
        private List<Movie> findByStringField(String fieldname, String param) {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Movie> query = builder.createQuery(Movie.class);
            Root<Movie> root = query.from(Movie.class);
            EntityType<Movie> type = entityManager.getMetamodel().entity(Movie.class);
    
            Path<String> path = root.get(type.getDeclaredSingularAttribute(fieldname, String.class));
            Predicate condition = builder.like(path, "%" + param + "%");
    
            query.where(condition);
    
            return entityManager.createQuery(query).getResultList();
        }
    
        @Override
        public List<Movie> findRange(int[] range) {
            CriteriaQuery<Movie> cq = entityManager.getCriteriaBuilder().createQuery(Movie.class);
            cq.select(cq.from(Movie.class));
            TypedQuery<Movie> q = entityManager.createQuery(cq);
            q.setMaxResults(range[1] - range[0]);
            q.setFirstResult(range[0]);
            return q.getResultList();
        }
    
        @Override
        public int count() {
            CriteriaQuery<Long> cq = entityManager.getCriteriaBuilder().createQuery(Long.class);
            Root<Movie> rt = cq.from(Movie.class);
            cq.select(entityManager.getCriteriaBuilder().count(rt));
            TypedQuery<Long> q = entityManager.createQuery(cq);
            return (q.getSingleResult()).intValue();
        }
    
    }/**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    /**
     * @version $Revision$ $Date$
     */
    public interface Notifier {
        void notify(String message);
    }
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.annotation.Resource;
    import javax.ejb.Stateless;
    import javax.jms.Connection;
    import javax.jms.ConnectionFactory;
    import javax.jms.DeliveryMode;
    import javax.jms.JMSException;
    import javax.jms.MessageProducer;
    import javax.jms.Session;
    import javax.jms.TextMessage;
    import javax.jms.Topic;
    
    @Stateless
    public class NotifierImpl implements Notifier {
    
        @Resource
        private ConnectionFactory connectionFactory;
    
        @Resource(name = "notifications")
        private Topic notificationsTopic;
    
        public void notify(String message) {
            try {
                Connection connection = null;
                Session session = null;
    
                try {
                    connection = connectionFactory.createConnection();
                    connection.start();
    
                    // Create a Session
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
                    // Create a MessageProducer from the Session to the Topic or Queue
                    MessageProducer producer = session.createProducer(notificationsTopic);
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    
                    // Create a message
                    TextMessage textMessage = session.createTextMessage(message);
    
                    // Tell the producer to send the message
                    producer.send(textMessage);
                } finally {
                    // Clean up
                    if (session != null) session.close();
                    if (connection != null) connection.close();
                }
            } catch (JMSException e) {
                throw new IllegalStateException(e);
            }
    
        }
    
    }
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun.util;
    
    import javax.faces.application.FacesMessage;
    import javax.faces.component.UIComponent;
    import javax.faces.context.FacesContext;
    import javax.faces.convert.Converter;
    import javax.faces.model.SelectItem;
    import java.util.List;
    
    public class JsfUtil {
    
        public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne) {
            int size = selectOne ? entities.size() + 1 : entities.size();
            SelectItem[] items = new SelectItem[size];
            int i = 0;
            if (selectOne) {
                items[0] = new SelectItem("", "---");
                i++;
            }
            for (Object x : entities) {
                items[i++] = new SelectItem(x, x.toString());
            }
            return items;
        }
    
        public static void addErrorMessage(Exception ex, String defaultMsg) {
            String msg = ex.getLocalizedMessage();
            if (msg != null && msg.length() > 0) {
                addErrorMessage(msg);
            } else {
                addErrorMessage(defaultMsg);
            }
        }
    
        public static void addErrorMessages(List<String> messages) {
            for (String message : messages) {
                addErrorMessage(message);
            }
        }
    
        public static void addErrorMessage(String msg) {
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
            FacesContext.getCurrentInstance().addMessage(null, facesMsg);
        }
    
        public static void addSuccessMessage(String msg) {
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
            FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
        }
    
        public static String getRequestParameter(String key) {
            return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
        }
    
        public static Object getObjectFromRequestParameter(String requestParameterName, Converter converter, UIComponent component) {
            String theId = JsfUtil.getRequestParameter(requestParameterName);
            return converter.getAsObject(FacesContext.getCurrentInstance(), component, theId);
        }
    
    }/**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun.util;
    
    import javax.faces.model.DataModel;
    
    public abstract class PaginationHelper {
    
        private int pageSize;
        private int page;
    
        public PaginationHelper(int pageSize) {
            this.pageSize = pageSize;
        }
    
        public abstract int getItemsCount();
    
        public abstract DataModel createPageDataModel();
    
        public int getPageFirstItem() {
            return page * pageSize;
        }
    
        public int getPageLastItem() {
            int i = getPageFirstItem() + pageSize - 1;
            int count = getItemsCount() - 1;
            if (i > count) {
                i = count;
            }
            if (i < 0) {
                i = 0;
            }
            return i;
        }
    
        public boolean isHasNextPage() {
            return (page + 1) * pageSize + 1 <= getItemsCount();
        }
    
        public void nextPage() {
            if (isHasNextPage()) {
                page++;
            }
        }
    
        public boolean isHasPreviousPage() {
            return page > 0;
        }
    
        public void previousPage() {
            if (isHasPreviousPage()) {
                page--;
            }
        }
    
        public int getPageSize() {
            return pageSize;
        }
    
    }
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import com.gargoylesoftware.htmlunit.WebClient;
    import com.gargoylesoftware.htmlunit.html.DomNodeList;
    import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
    import com.gargoylesoftware.htmlunit.html.HtmlElement;
    import com.gargoylesoftware.htmlunit.html.HtmlPage;
    import org.junit.Test;
    
    import java.util.Iterator;
    
    import static org.junit.Assert.assertTrue;
    
    public class MoviesIT {
    
        @Test
        public void testShouldMakeSureWebappIsWorking() throws Exception {
            WebClient webClient = new WebClient();
            HtmlPage page = webClient.getPage("http://localhost:9999/moviefun/setup.jsp");
    
            assertMoviesPresent(page);
    
            page = webClient.getPage("http://localhost:9999/moviefun/faces/movie/List.xhtml");
    
            assertMoviesPresent(page);
            webClient.closeAllWindows();
        }
    
        private void assertMoviesPresent(HtmlPage page) {
            String pageAsText = page.asText();
            assertTrue(pageAsText.contains("Wedding Crashers"));
            assertTrue(pageAsText.contains("Starsky & Hutch"));
            assertTrue(pageAsText.contains("Shanghai Knights"));
            assertTrue(pageAsText.contains("I-Spy"));
            assertTrue(pageAsText.contains("The Royal Tenenbaums"));
            assertTrue(pageAsText.contains("Zoolander"));
            assertTrue(pageAsText.contains("Shanghai Noon"));
        }
    
        private void clickOnLink(HtmlPage page, String lookFor) throws Exception {
            DomNodeList<HtmlElement> links = page.getElementsByTagName("a");
            Iterator<HtmlElement> iterator = links.iterator();
            while (iterator.hasNext()) {
                HtmlAnchor anchor = (HtmlAnchor) iterator.next();
    
                if (lookFor.equals(anchor.getTextContent())) {
                    anchor.click();
                    break;
                }
            }
        }
    
    }
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import junit.framework.TestCase;
    import org.apache.openejb.api.LocalClient;
    
    import javax.annotation.Resource;
    import javax.ejb.EJB;
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.transaction.UserTransaction;
    import java.util.List;
    import java.util.Properties;
    
    @LocalClient
    public class MoviesTest extends TestCase {
    
        @EJB
        private Movies movies;
    
        @Resource
        private UserTransaction userTransaction;
    
        @PersistenceContext
        private EntityManager entityManager;
    
        public void setUp() throws Exception {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.localclient.LocalInitialContextFactory");
            p.put("movieDatabase", "new://Resource?type=DataSource");
            p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
            p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
    
            InitialContext initialContext = new InitialContext(p);
    
            // Here's the fun part
            initialContext.bind("inject", this);
        }
    
        public void test() throws Exception {
    
            userTransaction.begin();
    
            try {
                entityManager.persist(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
                entityManager.persist(new Movie("Joel Coen", "Fargo", 1996));
                entityManager.persist(new Movie("Joel Coen", "The Big Lebowski", 1998));
    
                List<Movie> list = movies.getMovies();
                assertEquals("List.size()", 3, list.size());
    
                for (Movie movie : list) {
                    movies.deleteMovie(movie);
                }
    
                assertEquals("Movies.getMovies()", 0, movies.getMovies().size());
    
            } finally {
                userTransaction.commit();
            }
        }
    }/**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.GenerationType;
    import javax.persistence.Id;
    import java.io.Serializable;
    
    @Entity
    public class Movie implements Serializable {
    
        private static final long serialVersionUID = 1L;
    
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;
    
        private String director;
        private String title;
        private int year;
        private String genre;
        private int rating;
    
    
        public Movie() {
        }
    
        public Movie(String title, String director, String genre, int rating, int year) {
            this.director = director;
            this.title = title;
            this.year = year;
            this.genre = genre;
            this.rating = rating;
        }
    
        public Movie(String director, String title, int year) {
            this.director = director;
            this.title = title;
            this.year = year;
        }
    
        public long getId() {
            return id;
        }
    
        public void setId(long id) {
            this.id = id;
        }
    
        public String getDirector() {
            return director;
        }
    
        public void setDirector(String director) {
            this.director = director;
        }
    
        public String getTitle() {
            return title;
        }
    
        public void setTitle(String title) {
            this.title = title;
        }
    
        public int getYear() {
            return year;
        }
    
        public void setYear(int year) {
            this.year = year;
        }
    
        public String getGenre() {
            return genre;
        }
    
        public void setGenre(String genre) {
            this.genre = genre;
        }
    
        public int getRating() {
            return rating;
        }
    
        public void setRating(int rating) {
            this.rating = rating;
        }
    }/**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.ejb.Local;
    import java.util.List;
    
    @Local
    public interface Movies {
        public int count();
    
        public List<Movie> findRange(int[] range);
    
        public List<Movie> findByDirector(String director);
    
        public List<Movie> findByGenre(String genre);
    
        public List<Movie> findByTitle(String title);
    
        public List<Movie> getMovies();
    
        public void deleteMovieId(long id);
    
        public void deleteMovie(Movie movie);
    
        public void editMovie(Movie movie);
    
        public void addMovie(Movie movie);
    
        public Movie find(Long id);
    }
    /**
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     *  Unless required by applicable law or agreed to in writing, software
     *  distributed under the License is distributed on an "AS IS" BASIS,
     *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *  See the License for the specific language governing permissions and
     *  limitations under the License.
     */
    package org.superbiz.moviefun;
    
    import javax.ejb.Remote;
    import java.util.List;
    
    @Remote
    public interface MoviesRemote {
        public int count();
    
        public List<Movie> findRange(int[] range);
    
        public List<Movie> findByDirector(String director);
    
        public List<Movie> findByGenre(String genre);
    
        public List<Movie> findByTitle(String title);
    
        public List<Movie> getMovies();
    
        public void deleteMovieId(long id);
    
        public void deleteMovie(Movie movie);
    
        public void editMovie(Movie movie);
    
        public void addMovie(Movie movie);
    
        public Movie find(Long id);
    }
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
    
    import javax.naming.InitialContext;
    import javax.naming.NamingException;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.net.MalformedURLException;
    import java.net.URL;
    
    public class NotificationMonitor {
        private static TrayIcon trayIcon;
    
        public static void main(String[] args) throws NamingException, InterruptedException, AWTException, MalformedURLException {
            addSystemTrayIcon();
    
            // Boot the embedded EJB Container 
            new InitialContext();
    
            System.out.println("Starting monitor...");
        }
    
        private static void addSystemTrayIcon() throws AWTException, MalformedURLException {
            SystemTray tray = SystemTray.getSystemTray();
    
            URL moviepng = NotificationMonitor.class.getClassLoader().getResource("movie.png");
            Image image = Toolkit.getDefaultToolkit().getImage(moviepng);
    
            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting monitor...");
                    System.exit(0);
                }
            };
    
            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
    
            trayIcon = new TrayIcon(image, "Notification Monitor", popup);
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
    
    
        }
    
        public static void showAlert(String message) {
            synchronized (trayIcon) {
                trayIcon.displayMessage("Alert received", message, TrayIcon.MessageType.WARNING);
            }
        }
    }/*
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
    
    import javax.ejb.ActivationConfigProperty;
    import javax.ejb.MessageDriven;
    import javax.jms.JMSException;
    import javax.jms.Message;
    import javax.jms.MessageListener;
    import javax.jms.TextMessage;
    
    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "notifications")})
    public class NotificationsBean implements MessageListener {
    
        public void onMessage(Message message) {
            try {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
    
                System.out.println("");
                System.out.println("====================================");
                System.out.println("Notification received: " + text);
                System.out.println("====================================");
                System.out.println("");
    
                NotificationMonitor.showAlert(text);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }