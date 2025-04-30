package edu.chorn.myproject.repository;

/*
    @author chorn
    @project myproject
    @class UserRepository
    @version 1.0.0
    @since 08.04.2025 - 17.34
*/

import edu.chorn.myproject.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByName(String name);

    @Query("{'description': { $regex: ?0 }}")
    List<User> findByDescriptionContaining(String descriptionSubstring);
}
