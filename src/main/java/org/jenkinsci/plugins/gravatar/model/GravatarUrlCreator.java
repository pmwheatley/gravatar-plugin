package org.jenkinsci.plugins.gravatar.model;

import de.bripkens.gravatar.DefaultImage;
import de.bripkens.gravatar.Gravatar;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.gravatar.factory.GravatarFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class GravatarUrlCreator {

	private final String email;

	private GravatarUrlCreator(String email) {
		this.email = email;
	}

	public static GravatarUrlCreator of(GravatarUser user) {
		checkNotNull(user);
		checkArgument(user.emailAddress().isPresent(), "Only users with e-mail address are supported");
		return new GravatarUrlCreator(user.emailAddress().get());
	}

	public String buildUrlForSize(int size) {
		return gravatar().setSize(size).getUrl(email);
	}

	private Gravatar gravatar() {
		return new GravatarFactory().userGravatar();
	}

}
