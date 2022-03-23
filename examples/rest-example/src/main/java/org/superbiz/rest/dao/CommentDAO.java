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

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;

@Singleton
@Lock(LockType.READ)
public class CommentDAO {

    @Inject
    private DAO dao;

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
