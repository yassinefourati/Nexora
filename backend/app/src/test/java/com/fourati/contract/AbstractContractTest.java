package com.fourati.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Shared plumbing for web-slice contract tests. Slices boot only the MVC
 * layer, so they run without Docker and are safe in {@code make test-unit}.
 */
abstract class AbstractContractTest {

    @Autowired
    protected MockMvc mvc;
}
