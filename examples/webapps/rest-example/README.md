[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building OpenEJB :: Web Examples :: REST Example 1.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ rest-example ---
[INFO] Deleting /Users/dblevins/examples/webapps/rest-example/target
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ rest-example ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ rest-example ---
[INFO] Compiling 12 source files to /Users/dblevins/examples/webapps/rest-example/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ rest-example ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /Users/dblevins/examples/webapps/rest-example/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ rest-example ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.7.2:test (default-test) @ rest-example ---
[INFO] No tests to run.
[INFO] Surefire report directory: /Users/dblevins/examples/webapps/rest-example/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
There are no tests to run.

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-war-plugin:2.1.1:war (default-war) @ rest-example ---
[INFO] Packaging webapp
[INFO] Assembling webapp [rest-example] in [/Users/dblevins/examples/webapps/rest-example/target/rest-example-1.0]
[INFO] Processing war project
[INFO] Copying webapp resources [/Users/dblevins/examples/webapps/rest-example/src/main/webapp]
[INFO] Webapp assembled in [32 msecs]
[INFO] Building war: /Users/dblevins/examples/webapps/rest-example/target/rest-example-1.0.war
[INFO] 
[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ rest-example ---
[INFO] Installing /Users/dblevins/examples/webapps/rest-example/target/rest-example-1.0.war to /Users/dblevins/.m2/repository/org/superbiz/rest-example/1.0/rest-example-1.0.war
[INFO] Installing /Users/dblevins/examples/webapps/rest-example/pom.xml to /Users/dblevins/.m2/repository/org/superbiz/rest-example/1.0/rest-example-1.0.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2.318s
[INFO] Finished at: Fri Oct 28 17:03:55 PDT 2011
[INFO] Final Memory: 10M/81M
[INFO] ------------------------------------------------------------------------
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
    package org.superbiz.rest.dao;
    
    import org.superbiz.rest.model.Comment;
    import org.superbiz.rest.model.Post;
    
    import javax.ejb.EJB;
    import javax.ejb.Stateless;
    import java.util.Collections;
    import java.util.List;
    
    /**
     * @author Romain Manni-Bucau
     */
    @Stateless
    public class CommentDAO extends DAO {
        @EJB private DAO dao;
    
        public List<Comment> list(long postId) {
            Post post = dao.find(Post.class, postId);
            if (post == null) {
                throw new IllegalArgumentException("post with id " + postId + " not found");
            }
            return Collections.unmodifiableList(post.getComments());
        }
    
        public Comment create(String author, String content, long postId) {
            Post post = dao.find(Post.class, postId);
            if (post == null) {
                throw new IllegalArgumentException("post with id " + postId + " not found");
            }
    
            Comment comment = new Comment();
            comment.setAuthor(author);
            comment.setContent(content);
            dao.create(comment);
            comment.setPost(post);
            return comment;
        }
    
        public void delete(long id) {
            dao.delete(Comment.class, id);
        }
    
        public Comment update(long id, String author, String content) {
            Comment comment = dao.find(Comment.class, id);
            if (comment == null) {
                throw new IllegalArgumentException("comment with id " + id + " not found");
            }
    
            comment.setAuthor(author);
            comment.setContent(content);
            return dao.update(comment);
        }
    }
    /*
     *     Licensed to the Apache Software Foundation (ASF) under one or more
     *     contributor license agreements.  See the NOTICE file distributed with
     *     this work for additional information regarding copyright ownership.
     *     The ASF licenses this file to You under the Apache License, Version 2.0
     *     (the "License"); you may not use this file except in compliance with
     *     the License.  You may obtain a copy of the License at
     *
     *        http://www.apache.org/licenses/LICENSE-2.0
     *
     *     Unless required by applicable law or agreed to in writing, software
     *     distributed under the License is distributed on an "AS IS" BASIS,
     *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *     See the License for the specific language governing permissions and
     *     limitations under the License.
     */
    package org.superbiz.rest.dao;
    
    import javax.ejb.Stateless;
    import javax.persistence.EntityManager;
    import javax.persistence.PersistenceContext;
    import javax.persistence.Query;
    import java.util.List;
    
    /**
     * Simply maps the entitymanager.
     * It simplifies refactoring (unitName change) and wraps some logic (limited queries).
     *
     * @author Romain Manni-Bucau
     */
    @Stateless
    public class DAO {
        @PersistenceContext(unitName = "blog") private EntityManager em;
    
        public <E> E create(E e) {
            em.persist(e);
            return e;
        }
    
        public <E> E update(E e) {
            return em.merge(e);
        }
    
        public <E> void delete(Class<E> clazz, long id) {
            em.remove(em.find(clazz, id));
        }
    
        public <E> E find(Class<E> clazz, long id) {
            return em.find(clazz, id);
        }
    
        public <E> List<E> find(Class<E> clazz, String query, int min, int max) {
            return queryRange(em.createQuery(query, clazz), min, max).getResultList();
        }
    
        public <E> List<E> namedFind(Class<E> clazz, String query, int min, int max) {
            return queryRange(em.createNamedQuery(query, clazz), min, max).getResultList();
        }
    
        private static Query queryRange(Query query, int min, int max) {
            if (max >= 0) {
                query.setMaxResults(max);
            }
            if (min >= 0) {
                query.setFirstResult(min);
            }
            return query;
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
    package org.superbiz.rest.dao;
    
    import org.superbiz.rest.model.Post;
    import org.superbiz.rest.model.User;
    
    import javax.ejb.EJB;
    import javax.ejb.Stateless;
    import java.util.List;
    
    /**
     * @author Romain Manni-Bucau
     */
    @Stateless
    public class PostDAO {
        @EJB private DAO dao;
    
        public Post create(String title, String content, long userId) {
            User user = dao.find(User.class, userId);
            Post post = new Post();
            post.setTitle(title);
            post.setContent(content);
            post.setUser(user);
            return dao.create(post);
        }
    
        public Post find(long id) {
            return dao.find(Post.class, id);
        }
    
        public List<Post> list(int first, int max) {
            return dao.namedFind(Post.class, "post.list", first, max);
        }
    
        public void delete(long id) {
            dao.delete(Post.class, id);
        }
    
        public Post update(long id, long userId, String title, String content) {
            User user = dao.find(User.class, userId);
            if (user == null) {
                throw  new IllegalArgumentException("user id " + id + " not found");
            }
    
            Post post = dao.find(Post.class, id);
            if (post == null) {
                throw  new IllegalArgumentException("post id " + id + " not found");
            }
    
            post.setTitle(title);
            post.setContent(content);
            post.setUser(user);
            return dao.update(post);
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
    package org.superbiz.rest.dao;
    
    import org.superbiz.rest.model.User;
    
    import javax.ejb.EJB;
    import javax.ejb.Stateless;
    import java.util.List;
    
    /**
     * @author Romain Manni-Bucau
     */
    @Stateless
    public class UserDAO {
        @EJB private DAO dao;
    
        public User create(String name, String pwd, String mail) {
            User user = new User();
            user.setFullname(name);
            user.setPassword(pwd);
            user.setEmail(mail);
            return dao.create(user);
        }
    
        public List<User> list(int first, int max) {
            return dao.namedFind(User.class, "user.list", first, max);
        }
    
        public User find(long id) {
            return dao.find(User.class, id);
        }
    
        public void delete(long id) {
            dao.delete(User.class, id);
        }
    
        public User update(long id, String name, String pwd, String mail) {
            User user = dao.find(User.class, id);
            if (user == null) {
                throw  new IllegalArgumentException("setUser id " + id + " not found");
            }
    
            user.setFullname(name);
            user.setPassword(pwd);
            user.setEmail(mail);
            return dao.update(user);
        }
    }
    /*
     * Licensed to the Apache Software Foundation (ASF) under one or more
     *     contributor license agreements.  See the NOTICE file distributed with
     *     this work for additional information regarding copyright ownership.
     *     The ASF licenses this file to You under the Apache License, Version 2.0
     *     (the "License"); you may not use this file except in compliance with
     *     the License.  You may obtain a copy of the License at
     *
     *        http://www.apache.org/licenses/LICENSE-2.0
     *
     *     Unless required by applicable law or agreed to in writing, software
     *     distributed under the License is distributed on an "AS IS" BASIS,
     *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *     See the License for the specific language governing permissions and
     *     limitations under the License.
     */
    
    package org.superbiz.rest.model;
    
    import javax.persistence.Entity;
    import javax.persistence.JoinColumn;
    import javax.persistence.Lob;
    import javax.persistence.ManyToOne;
    import javax.persistence.NamedQueries;
    import javax.persistence.NamedQuery;
    import javax.validation.Valid;
    import javax.validation.constraints.NotNull;
    import javax.validation.constraints.Size;
    import javax.xml.bind.annotation.XmlRootElement;
    import javax.xml.bind.annotation.XmlTransient;
    
    /**
     * @author Romain Manni-Bucau
     */
    @Entity
    @NamedQueries({
        @NamedQuery(name = "comment.list", query = "select c from Comment c")
    })
    @XmlRootElement(name = "comment")
    public class Comment extends Model {
        @NotNull @Size(min = 1) private String author;
        @NotNull @Size(min = 1) @Lob private String content;
        @ManyToOne @JoinColumn(name = "post_id") @Valid @XmlTransient private Post post;
    
        public void setAuthor(final String author) {
            this.author = author;
        }
    
        public void setContent(final String content) {
            this.content = content;
        }
    
        public void setPost(Post post) {
            post.addComment(this);
            this.post = post;
        }
    
        public String getAuthor() {
            return author;
        }
    
        public String getContent() {
            return content;
        }
    
        public Post getPost() {
            return post;
        }
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
    package org.superbiz.rest.model;
    
    import javax.persistence.MappedSuperclass;
    import javax.persistence.PrePersist;
    import java.util.Date;
    
    /**
     * @author Romain Manni-Bucau
     */
    @MappedSuperclass
    public abstract class DatedModel extends Model {
        private Date created;
    
        @PrePersist public void create() {
            created = new Date();
        }
    
        public Date getCreated() {
            return created;
        }
    
        public void setCreated(Date created) {
            this.created = created;
        }
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
    package org.superbiz.rest.model;
    
    import javax.persistence.Access;
    import javax.persistence.AccessType;
    import javax.persistence.GeneratedValue;
    import javax.persistence.Id;
    import javax.persistence.MappedSuperclass;
    import javax.xml.bind.annotation.XmlAccessType;
    import javax.xml.bind.annotation.XmlAccessorType;
    
    /**
     * @author Romain Manni-Bucau
     */
    @MappedSuperclass
    @Access(AccessType.FIELD)
    @XmlAccessorType(XmlAccessType.FIELD)
    public abstract class Model {
        @Id @GeneratedValue protected long id;
    
        public long getId() {
            return id;
        }
    
        public void setId(long id) {
            this.id = id;
        }
    }
    /*
     * Licensed to the Apache Software Foundation (ASF) under one or more
     *     contributor license agreements.  See the NOTICE file distributed with
     *     this work for additional information regarding copyright ownership.
     *     The ASF licenses this file to You under the Apache License, Version 2.0
     *     (the "License"); you may not use this file except in compliance with
     *     the License.  You may obtain a copy of the License at
     *
     *        http://www.apache.org/licenses/LICENSE-2.0
     *
     *     Unless required by applicable law or agreed to in writing, software
     *     distributed under the License is distributed on an "AS IS" BASIS,
     *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *     See the License for the specific language governing permissions and
     *     limitations under the License.
     */
    
    package org.superbiz.rest.model;
    
    import javax.persistence.Entity;
    import javax.persistence.FetchType;
    import javax.persistence.Lob;
    import javax.persistence.ManyToOne;
    import javax.persistence.NamedQueries;
    import javax.persistence.NamedQuery;
    import javax.persistence.OneToMany;
    import javax.validation.Valid;
    import javax.validation.constraints.NotNull;
    import javax.validation.constraints.Size;
    import javax.xml.bind.annotation.XmlRootElement;
    import java.util.ArrayList;
    import java.util.List;
    
    /**
     * @author Romain Manni-Bucau
     */
    @Entity
    @NamedQueries({
        @NamedQuery(name = "post.list", query = "select p from Post p")
    })
    @XmlRootElement(name = "post")
    public class Post extends DatedModel {
        @NotNull @Size(min = 1) private String title;
        @NotNull @Size(min = 1) @Lob private String content;
        @ManyToOne @Valid private User user;
        @OneToMany(mappedBy = "post", fetch = FetchType.EAGER) private List<Comment> comments = new ArrayList<Comment>();
    
        public void setTitle(final String title) {
            this.title = title;
        }
    
        public void setContent(final String content) {
            this.content = content;
        }
    
        public void setUser(final User user) {
            this.user = user;
        }
    
        public String getTitle() {
            return title;
        }
    
        public String getContent() {
            return content;
        }
    
        public User getUser() {
            return user;
        }
    
        public List<Comment> getComments() {
            return comments;
        }
    
        public void setComments(List<Comment> comments) {
            this.comments = comments;
        }
    
        public void addComment(final Comment comment) {
            getComments().add(comment);
        }
    }
    /*
     * Licensed to the Apache Software Foundation (ASF) under one or more
     *     contributor license agreements.  See the NOTICE file distributed with
     *     this work for additional information regarding copyright ownership.
     *     The ASF licenses this file to You under the Apache License, Version 2.0
     *     (the "License"); you may not use this file except in compliance with
     *     the License.  You may obtain a copy of the License at
     *
     *        http://www.apache.org/licenses/LICENSE-2.0
     *
     *     Unless required by applicable law or agreed to in writing, software
     *     distributed under the License is distributed on an "AS IS" BASIS,
     *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *     See the License for the specific language governing permissions and
     *     limitations under the License.
     */
    
    package org.superbiz.rest.model;
    
    import javax.persistence.Entity;
    import javax.persistence.NamedQueries;
    import javax.persistence.NamedQuery;
    import javax.validation.constraints.NotNull;
    import javax.validation.constraints.Pattern;
    import javax.validation.constraints.Size;
    import javax.xml.bind.annotation.XmlRootElement;
    
    /**
     * @author Romain Manni-Bucau
     */
    @Entity
    @NamedQueries({
        @NamedQuery(name = "user.list", query = "select u from User u")
    })
    @XmlRootElement(name = "user")
    public class User extends Model {
        @NotNull @Size(min = 3, max = 15) private String fullname;
        @NotNull @Size(min = 5, max = 15) private String password;
        @NotNull @Pattern(regexp = ".+@.+\\.[a-z]+") private String email;
    
        public void setFullname(final String fullname) {
            this.fullname = fullname;
        }
    
        public void setPassword(final String password) {
            this.password = password;
        }
    
        public void setEmail(final String email) {
            this.email = email;
        }
    
        public String getFullname() {
            return fullname;
        }
    
        public String getPassword() {
            return password;
        }
    
        public String getEmail() {
            return email;
        }
    }
    /*
     * Licensed to the Apache Software Foundation (ASF) under one or more
     *     contributor license agreements.  See the NOTICE file distributed with
     *     this work for additional information regarding copyright ownership.
     *     The ASF licenses this file to You under the Apache License, Version 2.0
     *     (the "License"); you may not use this file except in compliance with
     *     the License.  You may obtain a copy of the License at
     *
     *        http://www.apache.org/licenses/LICENSE-2.0
     *
     *     Unless required by applicable law or agreed to in writing, software
     *     distributed under the License is distributed on an "AS IS" BASIS,
     *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *     See the License for the specific language governing permissions and
     *     limitations under the License.
     */
    
    package org.superbiz.rest.service;
    
    import org.superbiz.rest.dao.CommentDAO;
    import org.superbiz.rest.model.Comment;
    
    import javax.ejb.EJB;
    import javax.ws.rs.DELETE;
    import javax.ws.rs.GET;
    import javax.ws.rs.POST;
    import javax.ws.rs.PUT;
    import javax.ws.rs.Path;
    import javax.ws.rs.PathParam;
    import javax.ws.rs.Produces;
    import javax.ws.rs.QueryParam;
    import java.util.List;
    
    /**
     * @author Romain Manni-Bucau
     */
    @Path("/api/comment")
    @Produces({"text/xml", "application/json"})
    public class CommentService {
        @EJB private CommentDAO commentDao;
    
        @Path("/create") @PUT public Comment create(@QueryParam("author") String author,
                                                    @QueryParam("content") String content,
                                                    @QueryParam("postId") long postId) {
            return commentDao.create(author, content, postId);
        }
    
        @Path("/list/{postId}") @GET public List<Comment> list(@PathParam("postId") long postId) {
            return commentDao.list(postId);
        }
    
        @Path("/delete/{id}") @DELETE public void delete(@PathParam("id") long id) {
            commentDao.delete(id);
        }
    
        @Path("/update/{id}") @POST public Comment update(@PathParam("id") long id,
                                                          @QueryParam("author") String author,
                                                          @QueryParam("content") String content) {
            return commentDao.update(id, author, content);
        }
    }
    /*
     * Licensed to the Apache Software Foundation (ASF) under one or more
     *     contributor license agreements.  See the NOTICE file distributed with
     *     this work for additional information regarding copyright ownership.
     *     The ASF licenses this file to You under the Apache License, Version 2.0
     *     (the "License"); you may not use this file except in compliance with
     *     the License.  You may obtain a copy of the License at
     *
     *        http://www.apache.org/licenses/LICENSE-2.0
     *
     *     Unless required by applicable law or agreed to in writing, software
     *     distributed under the License is distributed on an "AS IS" BASIS,
     *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *     See the License for the specific language governing permissions and
     *     limitations under the License.
     */
    package org.superbiz.rest.service;
    
    import org.superbiz.rest.dao.PostDAO;
    import org.superbiz.rest.model.Post;
    
    import javax.ejb.EJB;
    import javax.ws.rs.DELETE;
    import javax.ws.rs.DefaultValue;
    import javax.ws.rs.GET;
    import javax.ws.rs.POST;
    import javax.ws.rs.PUT;
    import javax.ws.rs.Path;
    import javax.ws.rs.PathParam;
    import javax.ws.rs.Produces;
    import javax.ws.rs.QueryParam;
    import java.util.List;
    
    /**
     * @author Romain Manni-Bucau
     */
    @Path("/api/post")
    @Produces({ "text/xml", "application/json" })
    public class PostService {
        @EJB private PostDAO dao;
    
        @Path("/create") @PUT public Post create(@QueryParam("title") String title,
                                            @QueryParam("content") String content,
                                            @QueryParam("userId") long userId) {
            return dao.create(title, content, userId);
        }
    
        @Path("/list") @GET public List<Post> list(@QueryParam("first") @DefaultValue("0") int first,
                                              @QueryParam("max") @DefaultValue("20") int max) {
            return dao.list(first, max);
        }
    
        @Path("/show/{id}") @GET public Post show(@PathParam("id") long id) {
            return dao.find(id);
        }
    
        @Path("/delete/{id}") @DELETE public void delete(@PathParam("id") long id) {
            dao.delete(id);
        }
    
        @Path("/update/{id}") @POST public Post update(@PathParam("id") long id,
                                            @QueryParam("userId") long userId,
                                            @QueryParam("title") String title,
                                            @QueryParam("content") String content) {
            return dao.update(id, userId, title, content);
        }
    }
    /*
     * Licensed to the Apache Software Foundation (ASF) under one or more
     *     contributor license agreements.  See the NOTICE file distributed with
     *     this work for additional information regarding copyright ownership.
     *     The ASF licenses this file to You under the Apache License, Version 2.0
     *     (the "License"); you may not use this file except in compliance with
     *     the License.  You may obtain a copy of the License at
     *
     *        http://www.apache.org/licenses/LICENSE-2.0
     *
     *     Unless required by applicable law or agreed to in writing, software
     *     distributed under the License is distributed on an "AS IS" BASIS,
     *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *     See the License for the specific language governing permissions and
     *     limitations under the License.
     */
    package org.superbiz.rest.service;
    
    import org.superbiz.rest.dao.UserDAO;
    import org.superbiz.rest.model.Post;
    import org.superbiz.rest.model.User;
    
    import javax.ejb.EJB;
    import javax.ws.rs.DELETE;
    import javax.ws.rs.DefaultValue;
    import javax.ws.rs.GET;
    import javax.ws.rs.POST;
    import javax.ws.rs.PUT;
    import javax.ws.rs.Path;
    import javax.ws.rs.PathParam;
    import javax.ws.rs.Produces;
    import javax.ws.rs.QueryParam;
    import java.util.List;
    
    /**
     * @author Romain Manni-Bucau
     */
    @Path("/api/user")
    @Produces({ "text/xml", "application/json" })
    public class UserService {
        @EJB private UserDAO dao;
    
        @Path("/create") @PUT public User create(@QueryParam("name") String name,
                                            @QueryParam("pwd") String pwd,
                                            @QueryParam("mail") String mail) {
            return dao.create(name, pwd, mail);
        }
    
        @Path("/list") @GET public List<User> list(@QueryParam("first") @DefaultValue("0") int first,
                                              @QueryParam("max") @DefaultValue("20") int max) {
            return dao.list(first, max);
        }
    
        @Path("/show/{id}") @GET public User show(@PathParam("id") long id) {
            return dao.find(id);
        }
    
        @Path("/delete/{id}") @DELETE public void delete(@PathParam("id") long id) {
            dao.delete(id);
        }
    
        @Path("/update/{id}") @POST public User update(@PathParam("id") long id,
                                            @QueryParam("name") String name,
                                            @QueryParam("pwd") String pwd,
                                            @QueryParam("mail") String mail) {
            return dao.update(id, name, pwd, mail);
        }
    }
    package org.superbiz.rest.dao;
    
    import org.junit.AfterClass;
    import org.junit.BeforeClass;
    import org.junit.Test;
    import org.superbiz.rest.model.User;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.NamingException;
    
    import static junit.framework.Assert.assertNotNull;
    
    /**
     * @author rmannibucau
     */
    public class UserDaoTest {
        private static EJBContainer container;
    
        @BeforeClass public static void start() {
            container = EJBContainer.createEJBContainer();
        }
    
        @AfterClass public static void stop() {
            if (container != null) {
                container.close();
            }
        }
    
        @Test public void create() throws NamingException {
            UserDAO dao = (UserDAO) container.getContext().lookup("java:global/rest-example/UserDAO");
            User user = dao.create("foo", "dummy", "foo@bar.org");
            assertNotNull(dao.find(user.getId()));
        }
    }
    package org.superbiz.rest.dao;
    
    import org.apache.commons.io.FileUtils;
    import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
    import org.apache.tomee.embedded.EmbeddedTomEEContainer;
    import org.junit.AfterClass;
    import org.junit.BeforeClass;
    import org.junit.Test;
    import org.superbiz.rest.model.User;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.NamingException;
    import javax.ws.rs.GET;
    import javax.ws.rs.Path;
    import javax.ws.rs.PathParam;
    import javax.ws.rs.Produces;
    import java.io.File;
    import java.io.IOException;
    import java.util.Properties;
    
    import static junit.framework.Assert.assertEquals;
    import static junit.framework.Assert.assertNotNull;
    
    /**
     * @author rmannibucau
     */
    public class UserServiceTest {
        private static EJBContainer container;
        private static File webApp;
    
        @BeforeClass public static void start() throws IOException {
            webApp = createWebApp();
            Properties p = new Properties();
            p.setProperty(EJBContainer.APP_NAME, "test");
            p.setProperty(EJBContainer.PROVIDER, "tomee-embedded"); // need web feature
            p.setProperty(EJBContainer.MODULES, webApp.getAbsolutePath());
            p.setProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, "-1"); // random port
            container = EJBContainer.createEJBContainer(p);
        }
    
        @AfterClass public static void stop() {
            if (container != null) {
                container.close();
            }
            if (webApp != null) {
                try {
                    FileUtils.forceDelete(webApp);
                } catch (IOException e) {
                    FileUtils.deleteQuietly(webApp);
                }
            }
        }
    
        @Test public void create() throws NamingException {
            UserDAO dao = (UserDAO) container.getContext().lookup("java:global/" + webApp.getName() + "/UserDAO");
            User user = dao.create("foo", "dummy", "foo@dummy.org");
            assertNotNull(dao.find(user.getId()));
    
            String uri = "http://127.0.0.1:" + System.getProperty(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT) + "/" + webApp.getName();
            UserServiceClientAPI client = JAXRSClientFactory.create(uri, UserServiceClientAPI.class);
            User retrievedUser = client.show(user.getId());
            assertNotNull(retrievedUser);
            assertEquals("foo", retrievedUser.getFullname());
            assertEquals("dummy", retrievedUser.getPassword());
            assertEquals("foo@dummy.org", retrievedUser.getEmail());
        }
    
        private static File createWebApp() throws IOException {
            File file = new File(System.getProperty("java.io.tmpdir") + "/tomee-" + Math.random());
            if (!file.mkdirs() && !file.exists()) {
                throw new RuntimeException("can't create " + file.getAbsolutePath());
            }
    
            FileUtils.copyDirectory(new File("target/classes"), new File(file, "WEB-INF/classes"));
    
            return file;
        }
    
        /**
           * a simple copy of the unique method i want to use from my service.
           * It allows to use cxf proxy to call remotely our rest service.
           * Any other way to do it is good.
           */
        @Path("/api/user")
        @Produces({ "text/xml", "application/json" })
        public static interface UserServiceClientAPI {
            @Path("/show/{id}") @GET User show(@PathParam("id") long id);
        }
    }
