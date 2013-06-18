package org.jenkinsci.plugins.gravatar.cache;

import com.google.common.collect.Lists;
import hudson.model.TaskListener;
import hudson.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PeriodicGravatarImageResolutionCacheFillingWorkerTest {

	private List<User> users = Lists.newArrayList();

	@Spy
	PeriodicGravatarImageResolutionCacheFillingWorker loader = new PeriodicGravatarImageResolutionCacheFillingWorker();

	@Mock
	GravatarImageResolutionCache cache;

	@Mock
	TaskListener taskListener;

	@Before
	public void setUp() throws Exception {
		for (int i = 0; i < 100; i++) {
			User user = mock(User.class);
			users.add(user);
		}
		doReturn(users).when(loader).getAllUsers();
		doReturn(cache).when(loader).cache();
	}

	@Test
	public void itShouldForceLoadOfAllUsers() throws Exception {
		loader.execute(taskListener);
		for (User user : users) {
			verify(cache, atLeastOnce()).loadIfUnknown(same(user));
		}
	}

	@Test
	public void itShouldNotForceLoadOfAnyOtherUsers() throws Exception {
		loader.execute(taskListener);
		verify(cache, times(users.size())).loadIfUnknown(any(User.class));
	}
}
