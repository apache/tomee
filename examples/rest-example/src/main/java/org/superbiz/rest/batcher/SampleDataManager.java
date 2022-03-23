/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.rest.batcher;

import org.superbiz.rest.dao.CommentDAO;
import org.superbiz.rest.dao.PostDAO;
import org.superbiz.rest.dao.UserDAO;
import org.superbiz.rest.model.Post;
import org.superbiz.rest.model.User;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.logging.Logger;

@Startup
@DependsOn({"CommentDAO", "PostDAO", "UserDAO"})
@Singleton
@Lock(LockType.READ)
public class SampleDataManager {

    private static final Logger LOGGER = Logger.getLogger(SampleDataManager.class.getName());

    @PersistenceContext(unitName = "blog")
    private EntityManager em;

    @Inject
    private CommentDAO comments;

    @Inject
    private PostDAO posts;

    @Inject
    private UserDAO users;

    @PostConstruct
    public void createSomeData() {
        final User tomee = users.create("tomee", "tomee", "tomee@apache.org");
        final User openejb = users.create("openejb", "openejb", "openejb@apache.org");
        final Post tomeePost = posts.create("TomEE", "TomEE is a cool JEE App Server", tomee.getId());
        posts.create("OpenEJB", "OpenEJB is a cool embedded container", openejb.getId());
        comments.create("visitor", "nice post!", tomeePost.getId());
    }

    // a bit ugly but at least we clean data
    @Schedule(second = "0", minute = "30", hour = "*", persistent = false)
    private void cleanData() {
        LOGGER.info("Cleaning data");
        deleteAll();
        createSomeData();
        LOGGER.info("Data reset");
    }

    private void deleteAll() {
        em.createQuery("delete From Comment").executeUpdate();
        em.createQuery("delete From Post").executeUpdate();
        em.createQuery("delete From User").executeUpdate();
    }
}
