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
package org.apache.openejb.data.test.repo;

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Repository;
import org.apache.openejb.data.test.entity.Task;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {

    // StartsWith operator
    List<Task> findByTitleStartsWith(String prefix);

    // EndsWith operator
    List<Task> findByTitleEndsWith(String suffix);

    // Between operator
    List<Task> findByPriorityBetween(int low, int high);

    // In operator
    List<Task> findByPriorityIn(List<Integer> priorities);

    // Or connector
    List<Task> findByCompletedTrueOrPriorityGreaterThan(int priority);

    // True operator
    List<Task> findByCompletedTrue();

    // False operator
    List<Task> findByCompletedFalse();

    // Not operator
    List<Task> findByTitleNot(String title);

    // Combined And + Or
    List<Task> findByTitleStartsWithAndCompletedFalse(String prefix);

    // OrderBy with descending
    @OrderBy(value = "priority", descending = true)
    List<Task> findByCompletedFalseOrderByPriorityDesc();

    // Limit parameter support
    List<Task> findByCompletedFalseOrderByPriorityAsc(Limit limit);

    // Sort parameter support
    List<Task> findByCompletedFalse(Sort<Task> sort);

    // Order parameter support
    List<Task> findByCompletedFalse(Order<Task> order);

    // PageRequest parameter support returning Page
    Page<Task> findByCompletedFalse(PageRequest pageRequest, Order<Task> order);

    // Multiple order clauses in method name
    List<Task> findByCompletedFalseOrderByPriorityDescTitleAsc();
}
