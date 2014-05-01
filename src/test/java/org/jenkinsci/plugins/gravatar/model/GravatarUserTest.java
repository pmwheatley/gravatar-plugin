package org.jenkinsci.plugins.gravatar.model;


import com.google.common.base.Optional;
import com.google.common.testing.EqualsTester;
import hudson.model.User;
import hudson.tasks.Mailer;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(User.class)
public class GravatarUserTest {

	public static final String EMAIL = "myid@mail.com";
	public static final String USER_ID = "myid";
	@Mock
	User user;

	@Mock
	Mailer.UserProperty mailProperty;

	@Before
	public void setUp() throws Exception {
		when(user.getId()).thenReturn(USER_ID);
		when(mailProperty.getAddress()).thenReturn(EMAIL);
		PowerMockito.mockStatic(User.class);
		when(User.get(eq(USER_ID))).thenReturn(user);
	}

	@Test(expected = NullPointerException.class)
	public void itShouldNotAllowNullUsers() throws Exception {
		GravatarUser.gravatarUser(null);
	}

	@Test
	public void itShouldAllowNonNullUsers() throws Exception {
		GravatarUser.gravatarUser(user);
	}

	@Test
	public void itShouldNotGiveAnEMailAddressIfNoneAvailable() throws Exception {
		GravatarUser grUser = GravatarUser.gravatarUser(user);
		assertThat(grUser.emailAddress(), equalTo(Optional.<String>absent()));
	}

	@Test
	public void itShouldReturnEMailAddressReturnedByProperty() throws Exception {
		when(user.getProperty(Mailer.UserProperty.class)).thenReturn(mailProperty);

		GravatarUser grUser = GravatarUser.gravatarUser(user);
		final Optional<String> email = grUser.emailAddress();

		assertThat(email.isPresent(), is(true));
		assertThat(email.get(), is(equalTo(EMAIL)));
	}

	@Test
	public void anEmptyEmailAddressIsTreatedAsNull() throws Exception {
		when(mailProperty.getAddress()).thenReturn("");
		when(user.getProperty(Mailer.UserProperty.class)).thenReturn(mailProperty);

		GravatarUser grUser = GravatarUser.gravatarUser(user);
		final Optional<String> email = grUser.emailAddress();
		assertThat(email, is(equalTo(Optional.<String>absent())));
	}

	@Test
	public void itShouldOnlyCallEMailPropertyOnce() throws Exception {
		GravatarUser grUser = GravatarUser.gravatarUser(user);
		grUser.emailAddress();
		grUser.emailAddress();
		grUser.emailAddress();
		verify(user, times(1)).getProperty(Mailer.UserProperty.class);
	}

	@Test
	public void itShouldBehaveCorrectlyWithEquals() throws Exception {
		GravatarUser user1 = user("userid1");
		GravatarUser user1_2 = user("userid1");
		GravatarUser user1_3 = user("userid1");
		GravatarUser user2 = user("userid2");
		GravatarUser user2_2 = user("userid2");
		GravatarUser user2_3 = user("userid2");
		new EqualsTester()
				.addEqualityGroup(user1, user1_2, user1_3)
				.addEqualityGroup(user2, user2_2, user2_3)
				.testEquals();
	}

	private GravatarUser user(String userId) {
		User user = mock(User.class);
		when(user.getId()).thenReturn(userId);
		return GravatarUser.gravatarUser(user);
	}


}
