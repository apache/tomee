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
package org.apache.openejb.core;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class JavaMailTest {

    @EJB
    private Orange orange;

    @Module
    public Class<?>[] module() {
        return new Class[]{Orange.class};
    }

    @Test
    public void test() throws Exception {
        orange.test();
    }

    public static class ObjectSet<T> {
        final Map<T, T> map = new HashMap<T, T>();

        public ObjectSet() {
        }

        public T get(T t) {
            final T existing = map.get(t);
            if (existing != null) return existing;
            map.put(t, t);
            return t;
        }

        public Collection<T> values() {
            return map.values();
        }
    }

    public static class Domains {
        final ObjectSet<Domain> domains = new ObjectSet<Domain>();

        public Domain get(EmailAddress address) {
            return domains.get(new Domain(address.getDomain()));
        }

        public Collection<Domain> get() {
            return domains.values();
        }
    }

    public static class Domain {

        final ObjectSet<EmailAddress> addresses = new ObjectSet<EmailAddress>();

        final String name;

        public Domain(String name) {
            if (name == null) throw new IllegalArgumentException("Name cannot be null");
            this.name = name.toLowerCase();
        }

        public EmailAddress get(EmailAddress t) {
            return addresses.get(t);
        }

        public int getEmails() {
            int i = 0;
            for (EmailAddress address : addresses.values()) {
                i += address.getEmails();
            }

            return i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Domain domain = (Domain) o;

            if (!name.equals(domain.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public static class EmailAddress {
        private final String address;
        private final String user;
        private final String domain;
        private final String name;
        private int emails;

        public EmailAddress(InternetAddress internetAddress) {
            this(internetAddress.getAddress(), internetAddress.getPersonal());
        }

        public EmailAddress(String address, String name) {
            if (address == null) throw new IllegalArgumentException("Address cannot be null");
            this.address = address.toLowerCase();
            final String[] split = address.split("@");
            this.user = split[0];
            this.domain = split[1];
            this.name = name;
        }

        public String getUser() {
            return user;
        }

        public String getDomain() {
            return domain;
        }

        public String getAddress() {
            return address;
        }

        public String getName() {
            return name;
        }

        public int getEmails() {
            return emails;
        }

        public void increment() {
            emails += 1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EmailAddress that = (EmailAddress) o;

            if (!address.equals(that.address)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return address.hashCode();
        }
    }

    @XmlRootElement
    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class Messages {

        @XmlElement
        private List<Msg> messages = new ArrayList<Msg>();

        public List<Msg> getMessages() {
            return messages;
        }
    }

    @XmlRootElement(name = "message")
    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class Msg {

        private final List<Addr> from = new ArrayList<Addr>();
        private final List<Addr> to = new ArrayList<Addr>();
        private String subject;
        private Date sentDate;
        private Date receivedDate;
        private int messageNumber;
        private String messageId;

        public Msg() {
        }

        public Msg(Message message) throws MessagingException {
            for (Address address : message.getFrom()) {
                this.from.add(new Addr(address));
            }
            for (Address address : message.getAllRecipients()) {
                this.to.add(new Addr(address));
            }
            this.subject = message.getSubject();
            sentDate = message.getSentDate();
            receivedDate = message.getReceivedDate();
            messageNumber = message.getMessageNumber();
            try {
                messageId = message.getHeader("Message-Id")[0];
            } catch (Exception e) {
            }
        }

        public List<Addr> getFrom() {
            return from;
        }

        public List<Addr> getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public Date getSentDate() {
            return sentDate;
        }

        public void setSentDate(Date sentDate) {
            this.sentDate = sentDate;
        }

        public Date getReceivedDate() {
            return receivedDate;
        }

        public void setReceivedDate(Date receivedDate) {
            this.receivedDate = receivedDate;
        }

        @XmlAccessorType(value = XmlAccessType.FIELD)
        public static class Addr {

            @XmlAttribute
            private String address;

            @XmlAttribute
            private String name;

            @XmlAttribute
            private String domain;

            @XmlAttribute
            private String user;

            public Addr() {
            }

            public Addr(Address address) {
                if (address instanceof InternetAddress) {
                    InternetAddress internetAddress = (InternetAddress) address;
                    this.address = internetAddress.getAddress().toLowerCase();
                    this.name = internetAddress.getPersonal();

                    try {
                        final String[] split = this.address.split("@");
                        this.user = split[0];
                        this.domain = split[1];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getDomain() {
                return domain;
            }

            public void setDomain(String domain) {
                this.domain = domain;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final File saved = new File("/Users/dblevins/work/tomitribe/userlist");
        final List<File> files = Files.collect(saved, ".*.xml");

        final Domains domains = new Domains();

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2012);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        final Date date = calendar.getTime();

        final JAXBContext jaxbContext = JAXBContext.newInstance(Messages.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        for (File file : files) {
            try {
                final Msg message = (Msg) unmarshaller.unmarshal(file);
                if (message.getReceivedDate().before(date)) continue;

                final Msg.Addr addr = message.getFrom().get(0);
                final EmailAddress address = new EmailAddress(addr.getAddress(), addr.getName());
                final Domain domain = domains.get(address);
                domain.get(address).increment();
            } catch (JAXBException e) {
                System.out.println("FAILED - " + file);
            }
        }

        for (Domain domain : domains.get()) {
            System.out.printf("%s %s\n", domain.getEmails(), domain.name);
        }
    }

    public static void _main(String[] args) throws Exception {
        final Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        final Session session = Session.getDefaultInstance(props, null);

        final Store store = session.getStore("imaps");
        store.connect("imap.googlemail.com", "david.blevins@gmail.com", "Sn0wmany");

        for (Folder folder : store.getDefaultFolder().list()) {
            System.out.println(folder.getURLName());
        }

        final Folder folder = store.getFolder("openejb/openejb-users");

        if (!folder.isOpen()) folder.open(Folder.READ_WRITE);

        final List<Message> list = new ArrayList<Message>(Arrays.asList(folder.getMessages()));
        Collections.reverse(list);

        final Domains domains = new Domains();

        final File saved = new File("/Users/dblevins/work/tomitribe/userlist");

        final JAXBContext jaxbContext = JAXBContext.newInstance(Messages.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

//        int i = 0;
        for (Message message : list) {
            System.out.println(message.getMessageNumber());
            final File file = new File(saved, "message-" + message.getMessageNumber() + ".xml");
            final OutputStream write = IO.write(file);
            try {
                marshaller.marshal(new Msg(message), write);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                write.close();
            }

//            if (i++ > 1) break;
        }

    }


    private static final Map<String, AtomicInteger> map = new HashMap<String, AtomicInteger>();

    private static AtomicInteger get(String domain) {
        {
            final AtomicInteger integer = map.get(domain);
            if (integer != null) return integer;
        }

        final AtomicInteger value = new AtomicInteger();
        map.put(domain, value);
        return value;
    }

    @Singleton
    public static class Orange {

        @Resource
        private Session session;

        public void test() throws Exception {
            final URLName name = null;
            session.getFolder(name).getMessages();
        }
    }
}
