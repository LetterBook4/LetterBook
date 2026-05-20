package com.letterbook.book;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {
    List<Book> findByOwnerId(String ownerId);

    @Query("{ 'ownerId': ?0, $or: [ {'titulo': {$regex: ?1, $options:'i'}}, {'autor': {$regex: ?1, $options:'i'}} ] }")
    List<Book> searchByOwner(String ownerId, String term);
}
