package com.test.POJO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InboxEmail {
	@JsonProperty("uid")
	private String messageId;

	@JsonProperty("r")
	private Long id;

	@JsonProperty("f")
	private String from;

	@JsonProperty("s")
	private String subject;

	@JsonProperty("ru")
	private String unRead;

	public String getMessageId() {
		return messageId;
	}

	public String getFrom() {
		return from;
	}

	public String getSubject() {
		return subject;
	}

	public boolean getUnreadMail() {
		if (unRead == null || unRead.isEmpty())
			return true;
		return false;
	}

	public Long getId() {
		return id;
	}
}
