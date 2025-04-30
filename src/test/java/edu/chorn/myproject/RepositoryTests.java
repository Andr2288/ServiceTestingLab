package edu.chorn.myproject;

/*
    @author chorn
    @project myproject
    @class RepositoryTests
    @version 1.0.0
    @since 24.04.2025 - 12.45
*/

import edu.chorn.myproject.model.User;
import edu.chorn.myproject.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataMongoTest
public class RepositoryTests {

    @Autowired
    UserRepository underTest;

    @BeforeEach
    void setUp() {

        underTest.save(new User("1", "Freddy Mercury", "Queen", "##test"));
        underTest.save(new User("2", "Paul McCartney", "Beatles", "##test"));
        underTest.save(new User("3", "Mick Jagg", "Beatles", "##test"));
    }

    @AfterEach
    void tearDown() {

        List<User> usersToDelete = underTest.findAll().stream()
                .filter(user -> user.getDescription().contains("##test"))
                .toList();
        underTest.deleteAll(usersToDelete);
    }

    @AfterAll
    void afterAll() {

    }

    @Test
    void shouldGiveIdForNewRecord() {

        // given
        User john = new User("John Lennon", "Beatles", "##test");

        // when
        underTest.save(john);
        User userFromDb = underTest.findAll().stream()
                .filter(user -> user.getName().equals("John Lennon"))
                .findFirst().orElse(null);

        // then
        assertFalse(userFromDb.getId() == john.getId());
        assertNotNull(userFromDb);
        assertNotNull(userFromDb.getId());
        assertFalse(userFromDb.getId().isEmpty());
    }

    @Test
    void shouldSaveUserSuccessfully() {

        // given
        User user = new User("Alice Cooper", "Rock", "##test");

        // when
        User savedUser = underTest.save(user);

        // then
        assertNotNull(savedUser.getId());
        assertEquals("Alice Cooper", savedUser.getName());
        assertEquals("Rock", savedUser.getCode());
        assertEquals("##test", savedUser.getDescription());
    }

    @Test
    void shouldFindUserById() {

        // given
        User user = new User("Bob Dylan", "Folk", "##test");
        User savedUser = underTest.save(user);

        // when
        Optional<User> foundUser = underTest.findById(savedUser.getId());

        // then
        assertTrue(foundUser.isPresent());
        assertEquals("Bob Dylan", foundUser.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundById() {

        // given
        String nonExistentId = "999";

        // when
        Optional<User> foundUser = underTest.findById(nonExistentId);

        // then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void shouldFindAllUsers() {

        // given
        underTest.save(new User("Eddie Vedder", "PearlJam", "##test"));
        underTest.save(new User("Kurt Cobain", "Nirvana", "##test"));

        // when
        List<User> users = underTest.findAll();

        // then
        assertEquals(5, users.size());
    }

    @Test
    void shouldUpdateUserSuccessfully() {

        // given
        User user = new User("Chris Cornell", "Soundgarden", "##test");
        User savedUser = underTest.save(user);
        savedUser.setName("Chris Updated");
        savedUser.setCode("Audioslave");

        // when
        User updatedUser = underTest.save(savedUser);

        // then
        assertEquals("Chris Updated", updatedUser.getName());
        assertEquals("Audioslave", updatedUser.getCode());
        assertEquals(savedUser.getId(), updatedUser.getId());
    }

    @Test
    void shouldDeleteUserById() {

        // given
        User user = new User("Dave Grohl", "FooFighters", "##test");
        User savedUser = underTest.save(user);

        // when
        underTest.deleteById(savedUser.getId());
        Optional<User> deletedUser = underTest.findById(savedUser.getId());

        // then
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void shouldSaveMultipleUsers() {

        // given
        List<User> users = List.of(
                new User("Jim Morrison", "TheDoors", "##test"),
                new User("Janis Joplin", "Blues", "##test")
        );

        // when
        underTest.saveAll(users);
        List<User> allUsers = underTest.findAll();

        // then
        assertEquals(5, allUsers.size());
    }

    @Test
    void shouldFindUsersByCode() {

        // given
        underTest.save(new User("Axl Rose", "GunsNRoses", "##test"));
        underTest.save(new User("Slash", "GunsNRoses", "##test"));

        // when
        List<User> users = underTest.findAll().stream()
                .filter(user -> user.getCode().equals("GunsNRoses"))
                .toList();

        // then
        assertEquals(2, users.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersMatchCode() {

        // when
        List<User> users = underTest.findAll().stream()
                .filter(user -> user.getCode().equals("NonExistentCode"))
                .toList();

        // then
        assertTrue(users.isEmpty());
    }

    @Test
    void shouldReturnZeroCountWhenNoUsers() {

        // given
        underTest.deleteAll();

        // when
        long count = underTest.count();

        // then
        assertEquals(0, count);
    }

    @Test
    void shouldReturnTrueWhenUserExistsByName() {

        // given
        User user = new User("Alice Cooper", "Rock", "##test");
        underTest.save(user);

        // when
        boolean exists = underTest.existsByName("Alice Cooper");

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExistByName() {

        // when
        boolean exists = underTest.existsByName("Some Name");

        // then
        assertFalse(exists);
    }

    @Test
    void shouldFindUsersByDescriptionContaining() {

        // given
        underTest.save(new User("Eddie Vedder", "PearlJam", "description ##test"));
        underTest.save(new User("Kurt Cobain", "Nirvana", "another description ##test"));

        // when
        List<User> users = underTest.findByDescriptionContaining("description");

        // then
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(user -> user.getDescription().contains("description")));
    }

    @Test
    void shouldReturnEmptyListWhenNoDescriptionMatches() {

        // when
        List<User> users = underTest.findByDescriptionContaining("don't exist");

        // then
        assertTrue(users.isEmpty());
    }

    void printAllUsersToConsole() {

        List<User> users = underTest.findAll();

        System.out.println("=== All Users in Database ===");
        if (users.isEmpty()) {
            System.out.println("No users found in the database.");
        } else {
            users.forEach(user -> System.out.println(
                    "ID: " + user.getId() +
                            ", Name: " + user.getName() +
                            ", Code: " + user.getCode() +
                            ", Description: " + user.getDescription()
            ));
        }
        System.out.println("============================");
    }
}