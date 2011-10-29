package org.apache.openejb.arquillian.common;

public class DownloadException extends Exception {

	public DownloadException() {
	}

	public DownloadException(String message) {
		super(message);
	}

	public DownloadException(Throwable throwable) {
		super(throwable);
	}

	public DownloadException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
