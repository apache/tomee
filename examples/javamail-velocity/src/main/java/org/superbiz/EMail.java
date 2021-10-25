/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.Date;

@Data
@Getter
@Setter
@ToString
public class EMail {

    private MailType mailType;

    private String mailFrom;
    private String mailSubject;
    private String mailContent;

    private Collection<String> mailTo;
    private Collection<String> mailCc;
    private Collection<String> mailBcc;

    private Date sentDate;
    private String templateName;

    public EMail(MailType mailType, Collection<String> toRecipients, String subject, String mailContent, Collection<String> toCC, Collection<String> toBCC) {
        setMailTo(toRecipients);
        setMailSubject(subject);
        setMailContent(mailContent);
        setMailCc(toCC);
        setMailBcc(toBCC);
        setMailType(mailType);
    }

}