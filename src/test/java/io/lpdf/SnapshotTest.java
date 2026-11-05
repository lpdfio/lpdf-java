package io.lpdf;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Each fixture rendered to a PDF whose SHA-256 is its snapshot, as the .NET SDK's snapshot test renders them. After an
 * intended change to the engine's output, the snapshots are written again in core, not here.
 */
class SnapshotTest {
    @ParameterizedTest
    @ValueSource(strings = {
        "example1", "example2", "example3", "example4", "example5", "example6", "example7", "example8", "example9",
        "example10", "example11", "showcase-row", "showcase-row-fill", "showcase-frame", "showcase-grid",
        "showcase-row-between", "showcase-column", "showcase-table", "showcase-barcode", "showcase-encryption",
        "showcase-forms", "bench_xs", "bench_s", "bench_m", "bench_l", "bench_xl",
    })
    void a_fixture_renders_to_its_snapshot(String name) {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        // showcase-encryption asks for encryption in its output: its snapshot is the file unencrypted.
        Snapshots.assertMatches(name, Snapshots.render(name, new EngineOptions()));
    }
}
