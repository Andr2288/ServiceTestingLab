package edu.chorn.myproject.service;

import edu.chorn.myproject.model.User;
import edu.chorn.myproject.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import request.UserCreateRequest;
import request.UserUpdateRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/*
    @author chorn
    @project myproject
    @class UserServiceTest
    @version 1.0.0
    @since 30.04.2025 - 16.43
*/

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService underTest;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void whenInsertNewItem_ThenCreateDateIsPresent() {

        //given
        UserCreateRequest request = new UserCreateRequest("Till Lindemman", "Rammstein", "poet");
        LocalDateTime now = LocalDateTime.now();

        // when
        User createdUser = underTest.create(request);

        // then
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("Till Lindemman", createdUser.getName());
        assertEquals("Rammstein", createdUser.getCode());
        assertEquals("poet", createdUser.getDescription());
        assertNotNull(createdUser.getCreateDate());
        assertSame(LocalDateTime.class, createdUser.getCreateDate().getClass());
        assertNotNull(createdUser.getUpdateDate());
        assertSame(ArrayList.class, createdUser.getUpdateDate().getClass());
        assertTrue(createdUser.getUpdateDate().isEmpty());
    }

    @Test
    void whenGetAllUsers_ThenReturnAllUsers() {

        // given
        userRepository.deleteAll();
        User user1 = new User("test1", "code1", "desc1");
        User user2 = new User("test2", "code2", "desc2");
        userRepository.saveAll(List.of(user1, user2));

        // when
        List<User> result = underTest.getAll();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("test1")));
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("test2")));
    }

    @Test
    void whenGetUserById_ThenReturnCorrectUser() {

        // given
        User user = new User("testName", "testCode", "testDesc");
        user = userRepository.save(user);
        String userId = user.getId();

        // when
        User result = underTest.getById(userId);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testName", result.getName());
        assertEquals("testCode", result.getCode());
        assertEquals("testDesc", result.getDescription());
    }

    @Test
    void whenGetUserByNonExistentId_ThenReturnNull() {

        // given
        String nonExistentId = "non-existent-id";

        // when
        User result = underTest.getById(nonExistentId);

        // then
        assertNull(result);
    }

    @Test
    void whenCreateUser_ThenReturnSavedUser() {

        // given
        UserCreateRequest request = new UserCreateRequest("New User", "NU123", "New user description");

        // when
        User result = underTest.create(request);

        // then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("New User", result.getName());
        assertEquals("NU123", result.getCode());
        assertEquals("New user description", result.getDescription());
    }

    @Test
    void whenUpdateExistingUser_ThenUpdateAllFields() {

        // given
        User user = new User("Original", "ORG", "Original description");
        user.setCreateDate(LocalDateTime.now());
        user.setUpdateDate(new ArrayList<>());
        user = userRepository.save(user);

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                user.getId(), "Updated", "UPD", "Updated description"
        );

        // when
        User result = underTest.update(updateRequest);

        // then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals("Updated", result.getName());
        assertEquals("UPD", result.getCode());
        assertEquals("Updated description", result.getDescription());
        assertEquals(1, result.getUpdateDate().size());
    }

    @Test
    void whenUpdateNonExistentUser_ThenReturnNull() {

        // given
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "non-existent-id", "Updated", "UPD", "Updated description"
        );

        // when
        User result = underTest.update(updateRequest);

        // then
        assertNull(result);
    }

    @Test
    void whenDeleteExistingUser_ThenUserIsRemoved() {

        // given
        User user = new User("ToDelete", "DEL", "To be deleted");
        user = userRepository.save(user);
        String userId = user.getId();

        // when
        underTest.delById(userId);

        // then
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void whenMultipleUpdates_ThenUpdateDatesStackUp() {

        // given
        User user = new User("Original", "ORG", "Original description");
        user.setCreateDate(LocalDateTime.now());
        user.setUpdateDate(new ArrayList<>());
        user = userRepository.save(user);

        UserUpdateRequest firstUpdate = new UserUpdateRequest(
                user.getId(), "First Update", "FU", "First update desc"
        );

        // when
        User afterFirstUpdate = underTest.update(firstUpdate);

        UserUpdateRequest secondUpdate = new UserUpdateRequest(
                user.getId(), "Second Update", "SU", "Second update desc"
        );

        User afterSecondUpdate = underTest.update(secondUpdate);

        // then
        assertEquals(1, afterFirstUpdate.getUpdateDate().size());
        assertEquals(2, afterSecondUpdate.getUpdateDate().size());
    }

    @Test
    void whenCreateUser_ThenUpdateDateListIsEmpty() {

        // given
        UserCreateRequest request = new UserCreateRequest("Test User", "TU", "Test description");

        // when
        User result = underTest.create(request);

        // then
        assertNotNull(result.getUpdateDate());
        assertTrue(result.getUpdateDate().isEmpty());
    }

    @Test
    void whenCreateMultipleUsers_ThenAllAreSavedCorrectly() {

        // given
        UserCreateRequest request1 = new UserCreateRequest("User 1", "U1", "First user");
        UserCreateRequest request2 = new UserCreateRequest("User 2", "U2", "Second user");

        // when
        User user1 = underTest.create(request1);
        User user2 = underTest.create(request2);

        // then
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotEquals(user1.getId(), user2.getId());

        User fromDb1 = userRepository.findById(user1.getId()).orElse(null);
        User fromDb2 = userRepository.findById(user2.getId()).orElse(null);

        assertNotNull(fromDb1);
        assertNotNull(fromDb2);
        assertEquals("User 1", fromDb1.getName());
        assertEquals("User 2", fromDb2.getName());
    }

    @Test
    void whenDeleteUser_ThenCannotRetrieveById() {

        // given
        User user = new User("ToBeDeleted", "TBD", "User to delete");
        user = userRepository.save(user);
        String userId = user.getId();

        // when
        underTest.delById(userId);
        User retrievedUser = underTest.getById(userId);

        // then
        assertNull(retrievedUser);
    }

    @Test
    void whenUpdateUserName_ThenOnlyNameIsChanged() {
        // given
        User originalUser = new User("Original Name", "ON", "Original description");
        originalUser.setCreateDate(LocalDateTime.now());
        originalUser.setUpdateDate(new ArrayList<>());
        originalUser = userRepository.save(originalUser);

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                originalUser.getId(), "New Name", originalUser.getCode(), originalUser.getDescription()
        );

        // when
        User updatedUser = underTest.update(updateRequest);

        // then
        assertEquals("New Name", updatedUser.getName());
        assertEquals("ON", updatedUser.getCode());
        assertEquals("Original description", updatedUser.getDescription());
    }

    @Test
    void whenUpdateUserCode_ThenOnlyCodeIsChanged() {

        // given
        User originalUser = new User("Original Name", "ON", "Original description");
        originalUser.setCreateDate(LocalDateTime.now());
        originalUser.setUpdateDate(new ArrayList<>());
        originalUser = userRepository.save(originalUser);

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                originalUser.getId(), originalUser.getName(), "NC", originalUser.getDescription()
        );

        // when
        User updatedUser = underTest.update(updateRequest);

        // then
        assertEquals("Original Name", updatedUser.getName());
        assertEquals("NC", updatedUser.getCode());
        assertEquals("Original description", updatedUser.getDescription());
    }

    @Test
    void whenUpdateUserDescription_ThenOnlyDescriptionIsChanged() {

        // given
        User originalUser = new User("Original Name", "ON", "Original description");
        originalUser.setCreateDate(LocalDateTime.now());
        originalUser.setUpdateDate(new ArrayList<>());
        originalUser = userRepository.save(originalUser);

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                originalUser.getId(), originalUser.getName(), originalUser.getCode(), "New description"
        );

        // when
        User updatedUser = underTest.update(updateRequest);

        // then
        assertEquals("Original Name", updatedUser.getName());
        assertEquals("ON", updatedUser.getCode());
        assertEquals("New description", updatedUser.getDescription());
    }

    @Test
    void whenCreateUser_ThenCreateDateIsCloseToCurrentTime() {
        // given
        UserCreateRequest request = new UserCreateRequest("Time Test", "TT", "Testing time");
        LocalDateTime before = LocalDateTime.now();

        // when
        User createdUser = underTest.create(request);
        LocalDateTime after = LocalDateTime.now();

        // then
        assertNotNull(createdUser.getCreateDate());
        assertTrue(
                !createdUser.getCreateDate().isBefore(before) &&
                        !createdUser.getCreateDate().isAfter(after)
        );
    }

    @Test
    void whenUpdateUser_ThenUpdateDateIsCloseToCurrentTime() {

        // given
        User user = new User("Time Update Test", "TUT", "Testing update time");
        user.setCreateDate(LocalDateTime.now().minusDays(1));
        user.setUpdateDate(new ArrayList<>());
        user = userRepository.save(user);

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                user.getId(), "Updated Name", user.getCode(), user.getDescription()
        );

        LocalDateTime before = LocalDateTime.now();

        // when
        User updatedUser = underTest.update(updateRequest);

        LocalDateTime after = LocalDateTime.now();

        // then
        assertFalse(updatedUser.getUpdateDate().isEmpty());
        LocalDateTime updateTime = updatedUser.getUpdateDate().get(0);
        assertTrue(!updateTime.isBefore(before) && !updateTime.isAfter(after));
    }

    @Test
    void whenGetAll_ThenReturnsEmptyListIfNoUsers() {

        // given
        userRepository.deleteAll();

        // when
        List<User> result = underTest.getAll();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void whenCreateUser_ThenIdIsGenerated() {

        // given
        UserCreateRequest request = new UserCreateRequest("ID Test", "IDT", "Testing ID generation");

        // when
        User createdUser = underTest.create(request);

        // then
        assertNotNull(createdUser.getId());
        assertFalse(createdUser.getId().isEmpty());
    }

    @Test
    void whenCreateUserWithoutOptionalFields_ThenUserIsStillCreated() {

        // given
        UserCreateRequest request = new UserCreateRequest("Minimal User", null, null);

        // when
        User createdUser = underTest.create(request);

        // then
        assertNotNull(createdUser);
        assertEquals("Minimal User", createdUser.getName());
        assertNull(createdUser.getCode());
        assertNull(createdUser.getDescription());
    }

    @Test
    void whenUpdateNonExistentUser_ThenNoUserIsUpdated() {

        // given
        long countBefore = userRepository.count();
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "non-existent-id", "Some Name", "SC", "Some description"
        );

        // when
        User result = underTest.update(updateRequest);

        // then
        assertNull(result);
        assertEquals(countBefore, userRepository.count());
    }
}