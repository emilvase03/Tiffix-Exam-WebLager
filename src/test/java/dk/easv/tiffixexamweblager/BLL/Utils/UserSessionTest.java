package dk.easv.tiffixexamweblager.BLL.Utils;

import dk.easv.tiffixexamweblager.BE.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UserSessionTest {

    private UserSession session;

    @BeforeEach
    void setUp() {
        session = UserSession.getInstance();
        session.clear();
    }

    // getInstance

    @Test
    void getInstance_returnsSameInstance() {
        UserSession first  = UserSession.getInstance();
        UserSession second = UserSession.getInstance();
        assertSame(first, second, "getInstance() should always return the same singleton");
    }

    // setCurrentUser

    @Test
    void setCurrentUser_withValidUser_doesNotThrow() {
        User mockUser = mock(User.class);
        assertDoesNotThrow(() -> session.setCurrentUser(mockUser));
    }

    @Test
    void setCurrentUser_withNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> session.setCurrentUser(null));
    }

    // getCurrentUser

    @Test
    void getCurrentUser_whenUserIsSet_returnsCorrectUser() {
        User mockUser = mock(User.class);
        session.setCurrentUser(mockUser);
        assertSame(mockUser, session.getCurrentUser());
    }

    @Test
    void getCurrentUser_whenNoUserSet_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> session.getCurrentUser());
    }

    // isLoggedIn

    @Test
    void isLoggedIn_whenUserIsSet_returnsTrue() {
        session.setCurrentUser(mock(User.class));
        assertTrue(session.isLoggedIn());
    }

    @Test
    void isLoggedIn_whenNoUserSet_returnsFalse() {
        assertFalse(session.isLoggedIn());
    }

    // clear

    @Test
    void clear_afterSettingUser_logsOutUser() {
        session.setCurrentUser(mock(User.class));
        session.clear();
        assertFalse(session.isLoggedIn());
    }

    @Test
    void clear_afterSettingUser_preventsGetCurrentUser() {
        session.setCurrentUser(mock(User.class));
        session.clear();
        assertThrows(IllegalStateException.class, () -> session.getCurrentUser());
    }
}