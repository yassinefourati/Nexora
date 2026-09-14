package com.fourati.platform.error;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 2715789302102532184L;

	public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id);
    }
}
