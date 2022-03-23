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
package org.superbiz.rest.dao;

import org.superbiz.rest.model.Post;
import org.superbiz.rest.model.User;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import java.util.List;

@Singleton
@Lock(LockType.READ)
public class PostDAO {

    @Inject
    private DAO dao;

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
            throw new IllegalArgumentException("user id " + id + " not found");
        }

        Post post = dao.find(Post.class, id);
        if (post == null) {
            throw new IllegalArgumentException("post id " + id + " not found");
        }

        post.setTitle(title);
        post.setContent(content);
        post.setUser(user);
        return dao.update(post);
    }
}
