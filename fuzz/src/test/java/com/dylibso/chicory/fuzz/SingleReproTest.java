package com.dylibso.chicory.fuzz;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.dylibso.chicory.runtime.HostImports;
import com.dylibso.chicory.runtime.Module;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

public class SingleReproTest extends TestModule {
    private static final String CHICORY_FUZZ_SEED_KEY = "CHICORY_FUZZ_SEED";
    private static final String CHICORY_FUZZ_TYPES_KEY = "CHICORY_FUZZ_TYPES";

    WasmSmithWrapper smith = new WasmSmithWrapper();

    boolean enableSingleReproducer() {
        return System.getenv(CHICORY_FUZZ_SEED_KEY) != null
                && System.getenv(CHICORY_FUZZ_TYPES_KEY) != null;
    }

    @Test
    @EnabledIf("enableSingleReproducer")
    void singleReproducer() throws Exception {
        var seed =
                FileUtils.readFileToString(
                        new File(System.getenv(CHICORY_FUZZ_SEED_KEY)), StandardCharsets.UTF_8);
        var types = InstructionTypes.fromString(System.getenv(CHICORY_FUZZ_TYPES_KEY));
        var targetWasm =
                smith.run(seed.substring(0, Math.min(seed.length(), 32)), "test.wasm", types);

        var module = Module.builder(targetWasm).build();
        var instance = module.instantiate(new HostImports(), true, false);

        testModule(targetWasm, module, instance);
        // Sanity check that the starting function doesn't break
        assertDoesNotThrow(() -> module.instantiate());
    }
}
