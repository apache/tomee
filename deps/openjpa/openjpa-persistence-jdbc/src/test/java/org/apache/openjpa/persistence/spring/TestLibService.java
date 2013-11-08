/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence.spring;

import java.util.*;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.*;
import org.apache.openjpa.persistence.models.library.*;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestLibService extends SingleEMFTestCase 
        implements TransactionalEntityManagerFactory {

    private static EnumSet<AutoDetachType> txScope = 
            EnumSet.allOf(AutoDetachType.class);
    
    private LibService service;
    
    public EntityManager getTransactionalEntityManager() {
        // return a transactionally scoped entity manager
        OpenJPAEntityManager em = emf.createEntityManager();
        txScope.remove(AutoDetachType.NONE);
        em.setAutoDetach(txScope);
        return em;
    }
    
    public void setUp() {
        // declare the library model classes
        super.setUp(DROP_TABLES, Book.class, Borrower.class, Subject.class,
            Volunteer.class);
        
        // put golden data in database
        LibTestingService libTestingService = new LibTestingService();
        libTestingService.setEntityManager(emf.createEntityManager());
        libTestingService.repopulateDB();
        libTestingService.close();
        
        // create the LibService
        service = new LibServiceImpl();
        service.setTransactionalEntityManagerFactory(this);
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        service = null;
    }
        
    /**
     * Using known data, test the LibraryService.findBookByTitle() method and 
     * verify the information returned.
     */
    public void testFindBookByTitle() {
        String title = "Gone Sailing";
        String qTitle = "\"" + title + "\"";
        String bName = "Dick";

        try {
            // find the book
            Book book = service.findBookByTitle(title);
            assertNotNull("could not find the book " + qTitle, book);
            assertEquals("the book found was not the book " + qTitle, title,
                    book.getTitle());

            // get the book's borrower
            Borrower borrower = book.getBorrower();
            assertNotNull("could not find the borrower " + bName, borrower);
            assertEquals("the borrower found was not " + bName, bName, 
                    borrower.getName());

            // get the borrower's volunteer status
            Volunteer volunteer = borrower.getVolunteer();
            assertNotNull("could not find " + bName + "'s volunteer status",
                    volunteer);
            assertTrue("could not find the reference from " + bName
                    + "'s volunteer status back to " + bName, 
                    volunteer.getBorrower() == borrower);

            // get the book's subjects
            List<Subject> subjects = book.getSubjects();
            assertNotNull("no subjects for the book " + qTitle, subjects);
            assertEquals(
                    "unexpected number of subjects for the book " + qTitle, 2,
                    subjects.size());
        } catch (Exception e) {
            fail("Unable to findBookByTitle");
        }
    }

    /**
     * Using known data, test the LibraryService.findBorrowerByName method and
     * verify the information returned.
     */
    public void testFindBorrowerByName() {
        String bName = "Harry";
        try {
            Borrower harry = service.findBorrowerByName(bName);
            assertNotNull("Could not find " + bName, harry);
            assertEquals("the borrower found is not " + bName, bName, 
                    harry.getName());
        } catch (Exception e) {
            fail("Unable to find borrower by name");
        }
    }
    
    /**
     * Using known data, test the LibraryService.borrowBook() operation.
     * <ul>
     * <li>Can we find Tom, and has he borrowed one book?</li>
     * <li>Can we find the book entitled "Gone Visiting"?</li>
     * <li>After Tom borrows the new book, has he borrowed two books?</li>
     * </ul>
     */
    public void testBorrowBook() {
        String bName = "Tom";
        String title = "Gone Visiting";

        try {
            // find the borrower Tom
            Borrower borrower = service.findBorrowerByName(bName);
            assertNotNull("Could not find " + bName, borrower);
            List<Book> books = borrower.getBooks();
            assertEquals(bName + " has borrowed an unexpected number of books",
                    1, (books == null ? 0 : borrower.getBooks().size()));

            // find the book "Gone Visiting"
            Book book = service.findBookByTitle(title);
            assertNotNull("Could not find the book " + title, book);

            // have Tom borrow the book
            service.borrowBook(borrower, book);
            List<Book> borrowedBooks = borrower.getBooks();
            assertEquals("Unexpected number of books borrowed", 2,
                    borrowedBooks.size());

            // Verify that the update is in the database
            borrower = service.findBorrowerByName(bName);
            assertNotNull("Could not find " + bName, borrower);
            List<Book> booksBorrowed2 = borrower.getBooks();
            assertEquals(bName + " has borrowed an unexpected number of books",
                    2, booksBorrowed2.size());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to borrow a book");
        }
    }
    
    /**
     * Test the LibraryService.returnBook() operation, etc.
     */
    public void testReturnBook() {
        String bName = "Harry";

        try {
            // find the borrower Harry
            Borrower borrower = service.findBorrowerByName(bName);
            assertNotNull("Could not find " + bName, borrower);
            List<Book> books = borrower.getBooks();
            assertEquals(bName + " has borrowed an unexpected number of books",
                    1, (books == null ? 0 : borrower.getBooks().size()));

            // find the one book Harry has borrowed
            Book book = borrower.getBooks().get(0);
            service.returnBook(book);

            // Verify that the update is in the database
            borrower = service.findBorrowerByName(bName);
            assertNotNull("Could not find " + bName, borrower);
            assertEquals(bName + " has borrowed an unexpected number of books",
                    0, borrower.getBooks().size());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unable to return a book");
        }
    }
}
