package org.jenkinsci.plugins.gravatar.cache;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hudson.model.User;
import org.jenkinsci.plugins.gravatar.model.GravatarUrlCreator;
import org.jenkinsci.plugins.gravatar.model.GravatarUser;

import java.util.concurrent.*;
import java.util.logging.Logger;

import static org.jenkinsci.plugins.gravatar.model.GravatarUser.gravatarUser;

public enum GravatarImageResolutionCacheInstance implements GravatarImageResolutionCache {
	 INSTANCE;

	final GravatarImageResolutionLoadingCache cache = new GravatarImageResolutionLoadingCache();

	public Optional<GravatarUrlCreator> urlCreatorFor(User user) {
		return cache.urlCreatorFor(user);
	}

	public void loadIfUnknown(User user) {
		cache.loadIfUnknown(user);
	}

	public boolean hasGravatarCreator(User user) {
		return cache.hasGravatarCreator(user);
	}

}
