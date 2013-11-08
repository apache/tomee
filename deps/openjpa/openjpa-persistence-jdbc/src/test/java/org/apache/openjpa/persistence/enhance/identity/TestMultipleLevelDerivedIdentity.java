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
package org.apache.openjpa.persistence.enhance.identity;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests entities with compound keys that include entity relationship at
 * more than one level.
 * 
 * Page has a compound identity to Book which in turn uses a compound identity 
 * to Library.
 * 
 * Test case and domain classes were originally part of the reported issue 
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-207">OPENJPA-207</A>
 * 
 * @author Jeffrey Blattman
 * @author Pinaki Poddar
 * 
 */
@SuppressWarnings("unchecked")
public class TestMultipleLevelDerivedIdentity extends SingleEMFTestCase {
	private static String LIBRARY_NAME = "LIB";
	private static String BOOK_NAME    = "foo";
	private static int    NUM_PAGES    = 3;
	
	public void setUp() throws Exception {
        super.setUp(CLEAR_TABLES, Library.class, Book.class, Page.class,
                "openjpa.RuntimeUnenhancedClasses", "unsupported");
		create();
	}
	
	public void testMerge() {
        EntityManager em = emf.createEntityManager();
        
        Library lib = new Library();
        lib.setName("Congress Library");

        Book book = new Book();
        book.setName("Kite Runner");
        book.setLibrary(lib);
        em.merge(book);
        em.getTransaction().begin();
        em.getTransaction().commit();
        em.clear();
        try {
            em.merge(book);
            em.getTransaction().begin();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Fail to merge twice: " + e.getMessage());
        }
        em.close();
	}
	
	public void testPersist() {
		create();
	}

	public void testQueryRootLevel() {
		EntityManager em = emf.createEntityManager();
		List<Library> list = em.createQuery("SELECT p FROM Library p")
							   .getResultList();
		assertFalse(list.isEmpty());
		Library lib = (Library) list.get(0);
		assertNotNull(lib.getBook(BOOK_NAME));
		assertNotNull(lib.getBook(BOOK_NAME).getPage(1));
		em.close();
	}
	
	public void testQueryIntermediateLevel() {
		EntityManager em = emf.createEntityManager();
		List<Book> list = em.createQuery("SELECT p FROM Book p")
							   .getResultList();
		assertFalse(list.isEmpty());
		Book book = list.get(0);
		Library lib = book.getLibrary();
		for (int i=1; i<=NUM_PAGES; i++) {
			Page page = book.getPage(i);
			assertNotNull(page);
			assertEquals(book, page.getBook());
			assertEquals(lib, page.getBook().getLibrary());
            assertEquals(page, page.getBook().getPage(page.getNumber()));
		}
		em.close();
	}
	
	public void testQueryLeafLevel() {
		EntityManager em = emf.createEntityManager();
		List<Page> list = em.createQuery("SELECT p FROM Page p")
							   .getResultList();
		assertFalse(list.isEmpty());
		Book book = list.get(0).getBook();
		Library lib = book.getLibrary();
		for (Page page : list) {
			assertEquals(book, page.getBook());
			assertEquals(lib, page.getBook().getLibrary());
            assertEquals(page, page.getBook().getPage(page.getNumber()));
		}
		em.close();
	}

	public void testFindRootNode() {
		EntityManager em = emf.createEntityManager();
		Library lib = em.find(Library.class, LIBRARY_NAME);
		assertNotNull(lib);
		assertNotNull(lib.getBook(BOOK_NAME));
		assertNotNull(lib.getBook(BOOK_NAME).getPage(1));
		em.close();
	}
	
	public void testFindIntermediateNode() {
		EntityManager em = emf.createEntityManager();
		
		BookId bookId = new BookId();
		bookId.setLibrary(LIBRARY_NAME);
		bookId.setName(BOOK_NAME);
		Book book = em.find(Book.class, bookId);
		assertNotNull(book);
		em.close();
	}
	
	public void testFindLeafNode() {
		EntityManager em = emf.createEntityManager();
		
		BookId bookId = new BookId();
		bookId.setLibrary(LIBRARY_NAME);
		bookId.setName(BOOK_NAME);
		PageId pageId = new PageId();
		pageId.setBook(bookId);
		pageId.setNumber(2);
		Page page = em.find(Page.class, pageId);
		assertNotNull(page);
		em.close();
	}
	
	public void testUpdate() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		BookId bookId = new BookId();
		bookId.setLibrary(LIBRARY_NAME);
		bookId.setName(BOOK_NAME);
		Book book = em.find(Book.class, bookId);
		assertNotNull(book);
		book.setAuthor("modifiy Author");
		em.getTransaction().commit();
		em.close();
	}
	
	public void testDeleteRoot() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Library lib = em.find(Library.class, LIBRARY_NAME);
		em.remove(lib);
		em.getTransaction().commit();
		
	    assertEquals(0, count(Library.class));
	    assertEquals(0, count(Book.class));
	    assertEquals(0, count(Page.class));
	    em.close();
	}
	
	public void testDeleteLeafObtainedByQuery() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
        Page page = (Page)em.createQuery(
                "SELECT p FROM Page p WHERE p.number=2").getSingleResult();
		assertNotNull(page);
		em.remove(page);
		em.getTransaction().commit();
		
	    assertEquals(1, count(Library.class));
	    assertEquals(1, count(Book.class));
	    assertEquals(NUM_PAGES-1, count(Page.class));
	    em.close();
	}
	
	public void testDeleteLeafObtainedByFind() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		BookId bookId = new BookId();
		bookId.setLibrary(LIBRARY_NAME);
		bookId.setName(BOOK_NAME);
		PageId pageId = new PageId();
		pageId.setBook(bookId);
		pageId.setNumber(2);
		Page page = em.find(Page.class, pageId);
		assertNotNull(page);
		em.remove(page);
		em.getTransaction().commit();
		
	    assertEquals(1, count(Library.class));
	    assertEquals(1, count(Book.class));
	    assertEquals(NUM_PAGES-1, count(Page.class));
	    em.close();
	}

	
	/**
	 * Create a Library with a Book and three Pages.
	 */
	public void create() {
		if (count(Library.class) > 0)
			return;
		
		EntityManager em = null;
		em = emf.createEntityManager();
		em.getTransaction().begin();
		
		Library lib = new Library();
		lib.setName(LIBRARY_NAME);

		Book book = new Book();
		book.setName(BOOK_NAME);
		lib.addBook(book);
		for (int i = 1; i <= NUM_PAGES; i++) {
			Page page = new Page();
			page.setNumber(i);
			book.addPage(page);
		}
		em.persist(lib);
		em.getTransaction().commit();

		em.clear();
		em.close();
	}
}
