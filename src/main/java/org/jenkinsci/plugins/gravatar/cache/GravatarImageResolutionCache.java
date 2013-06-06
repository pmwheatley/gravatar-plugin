package org.jenkinsci.plugins.gravatar.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import hudson.model.User;
import org.jenkinsci.plugins.gravatar.model.GravatarUrlCreator;
import org.jenkinsci.plugins.gravatar.boundary.GravatarImageURLVerifier;
import org.jenkinsci.plugins.gravatar.model.GravatarUser;

import java.util.concurrent.*;
import java.util.logging.Logger;

import static org.jenkinsci.plugins.gravatar.model.GravatarUser.gravatarUser;

public enum GravatarImageResolutionCache {
	INSTANCE;

	private static final Logger LOG = Logger.getLogger(GravatarImageResolutionCache.class.getName());

	private final LoadingCache<GravatarUser, Optional<GravatarUrlCreator>> cache;

	private GravatarImageResolutionCache() {
		cache = CacheBuilder.newBuilder()
				.concurrencyLevel(2)
				.refreshAfterWrite(30, TimeUnit.MINUTES)
				.initialCapacity(User.getAll().size())
				.build(createUrlForUser());
	}

	private CacheLoader<GravatarUser, Optional<GravatarUrlCreator>> createUrlForUser() {
		return new CacheLoader<GravatarUser, Optional<GravatarUrlCreator>>() {

			final ListeningExecutorService reloader = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

			@Override
			public ListenableFuture<Optional<GravatarUrlCreator>> reload(final GravatarUser gravatarUser, Optional<GravatarUrlCreator> oldValue) throws Exception {
				//optimization: if it was previously known, it is rather safe to suspect, that it is still known (who removes a gravatar?)
				if(oldValue.isPresent()) {
					LOG.finer("Reusing old gravatar result value for " + gravatarUser + " because it has been known before.");
					return Futures.immediateFuture(oldValue);
				}
				//otherwise, we try it again, maybe it's there now?
				LOG.fine("Scheduling " + gravatarUser + " for reloading");
				ListenableFuture<Optional<GravatarUrlCreator>> future = reloader.submit(new Callable<Optional<GravatarUrlCreator>>() {
					public Optional<GravatarUrlCreator> call() throws Exception {
						return load(gravatarUser);
					}
				});
				return future;
			}

			@Override
			public Optional<GravatarUrlCreator> load(GravatarUser gravatarUser) throws Exception {
				if(!gravatarUser.emailAddress().isPresent()) {
					LOG.finer("Cannot check for gravatar for user " + gravatarUser + " since no e-mail address is known");
					return creator();
				}
				if(verifier().verify(gravatarUser.emailAddress().get())) {
					LOG.fine("Verified gravatar for "+gravatarUser);
					return creatorFor(gravatarUser);
				}
				return creator();
			}

			private Optional<GravatarUrlCreator> creator() {
				return Optional.absent();
			}

			private Optional<GravatarUrlCreator> creatorFor(GravatarUser user) {
				return Optional.of(GravatarUrlCreator.of(user));
			}

			private GravatarImageURLVerifier verifier() {
				return new GravatarImageURLVerifier();
			}
		};
	}

	public Optional<GravatarUrlCreator> urlCreatorFor(User user) {
		try {
			return cache.get(keyOf(user));
		} catch (ExecutionException e) {
			return Optional.absent();
		}
	}

	@VisibleForTesting
	protected void loadIfUnknown(User user) {
		GravatarUser gravatarUser = keyOf(user);
		if(!isKnown(gravatarUser)) {
			try {
				cache.get(gravatarUser);
			} catch (ExecutionException e) {
				LOG.info("Failed to load gravatar for user " + gravatarUser);
			}
		}
	}
	public boolean isKnown(User user) {
		return isKnown(keyOf(user));
	}

	private boolean isKnown(GravatarUser user) {
		return cache.asMap().containsKey(user);
	}

	public boolean hasGravatarCreator(User user) {
		return hasGravatarCreator(keyOf(user));
	}

	private boolean hasGravatarCreator(GravatarUser user) {
		return isKnown(user) && cache.getUnchecked(user).isPresent();
	}

	private GravatarUser keyOf(User user) {
		return gravatarUser(user);
	}
}
