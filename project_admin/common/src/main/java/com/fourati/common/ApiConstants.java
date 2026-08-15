package com.fourati.common;

/**
 * Centralised API path constants.
 *
 * Use these in @RequestMapping to ensure all controllers share the same prefix.
 * Changing the version here updates every endpoint at once.
 *
 * Usage:
 *   @RequestMapping(ApiConstants.VERSION + "/items")
 */
public final class ApiConstants {

    public static final String VERSION = "/api/v1";

    private ApiConstants() {
    	
    }

}
