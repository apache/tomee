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

import jakarta.data.repository.By;
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import org.apache.openejb.data.test.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface ProductCatalog extends DataRepository<Product, String> {

    @Insert
    Product add(Product product);

    @Insert
    Product[] addMultiple(Product... products);

    @Find
    Optional<Product> get(@By("productNum") String productNum);

    @Update
    Product modify(Product product);

    @Update
    Product[] modifyMultiple(Product... products);

    @Delete
    void remove(Product product);

    @Delete
    void removeMultiple(Product... products);

    @Save
    void save(Product product);

    @Delete
    void deleteById(@By("productNum") String productNum);

    long deleteByProductNumLike(String pattern);

    long countByPriceGreaterThanEqual(Double price);

    List<Product> findByNameLike(String name);

    Stream<Product> findByPriceNotNullAndPriceLessThanEqual(double maxPrice);

    List<Product> findByPriceNull();

    List<Product> findByProductNumLike(String pattern);

    @Query("WHERE LENGTH(name) = ?1 AND price < ?2 ORDER BY name")
    List<Product> findByNameLengthAndPriceBelow(int nameLength, double maxPrice);

    @Query("FROM Product WHERE (:rate * price <= :max AND :rate * price >= :min) ORDER BY name")
    Stream<Product> withTaxBetween(@Param("min") double min, @Param("max") double max, @Param("rate") double rate);
}
