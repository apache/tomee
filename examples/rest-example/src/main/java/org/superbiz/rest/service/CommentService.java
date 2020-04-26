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

import jakarta.ejb.EJB;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import java.util.List;

@Path("/api/comment")
@Produces({"text/xml", "application/json"})
public class CommentService {

    @EJB
    private CommentDAO commentDao;

    @Path("/create")
    @PUT
    public Comment create(@QueryParam("author") String author,
                          @QueryParam("content") String content,
                          @QueryParam("postId") long postId) {
        return commentDao.create(author, content, postId);
    }

    @Path("/list/{postId}")
    @GET
    public List<Comment> list(@PathParam("postId") long postId) {
        return commentDao.list(postId);
    }

    @Path("/delete/{id}")
    @DELETE
    public void delete(@PathParam("id") long id) {
        commentDao.delete(id);
    }

    @Path("/update/{id}")
    @POST
    public Comment update(@PathParam("id") long id,
                          @QueryParam("author") String author,
                          @QueryParam("content") String content) {
        return commentDao.update(id, author, content);
    }
}
