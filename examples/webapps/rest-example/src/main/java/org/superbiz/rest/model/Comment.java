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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
    @NotNull
    @Size(min = 1)
    private String author;
    @NotNull
    @Size(min = 1)
    @Lob
    private String content;
    @ManyToOne
    @JoinColumn(name = "post_id")
    @Valid
    @XmlTransient
    private Post post;

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
