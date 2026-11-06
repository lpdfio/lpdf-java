#!/bin/sh
# Checks the jars a release-profile build leaves in target/, so that a bad jar fails CI and not the
# release, which builds them first after core's release is out.
# - The sources jar holds only Java sources, folders, its manifest and Maven's pom files. The engine's
#   compiled classes reach the build as a resource folder, which the sources jar would pack too.
# - The main jar holds every file of that folder: the compiled engine, which users run.
# Run from the repo root after `mvn -B -P release verify -Dgpg.skip -DskipTests`.
set -eu

# Where Endive's Maven plugin writes the engine it compiled from wasm/lpdf.wasm.
engine_dir=target/generated-resources/endive-compiler

# Fails unless $2, a list of jars, holds exactly one, named by $1.
require_one_jar() {
    count=$(printf '%s' "$2" | grep -c . || true)
    if [ "$count" -ne 1 ]; then
        echo "::error::Expected one $1 jar in target/, found $count. Build with mvn -B -P release verify -Dgpg.skip -DskipTests first."
        [ -z "$2" ] || printf '%s\n' "$2"
        exit 1
    fi
}

jars=$(ls target/lpdf-*.jar 2>/dev/null || true)
main_jar=$(printf '%s\n' "$jars" | grep -v -e '-sources\.jar$' -e '-javadoc\.jar$' -e '^$' || true)
sources_jar=$(printf '%s\n' "$jars" | grep -e '-sources\.jar$' || true)
javadoc_jar=$(printf '%s\n' "$jars" | grep -e '-javadoc\.jar$' || true)
require_one_jar main "$main_jar"
require_one_jar sources "$sources_jar"
require_one_jar Javadoc "$javadoc_jar"

failed=0

# Listed apart from the filter: in a pipeline the status would be grep's, and a jar that can't be
# read would pass as one with no stray entries.
sources_entries=$(jar tf "$sources_jar") || { echo "::error::Can't read $sources_jar as a jar."; exit 1; }
stray=$(printf '%s\n' "$sources_entries" | grep -v -E '\.java$|/$|^META-INF/MANIFEST\.MF$|^META-INF/maven/' || true)
if [ -n "$stray" ]; then
    echo "::error::$sources_jar holds $(printf '%s\n' "$stray" | wc -l | tr -d ' ') entries that are not Java sources, folders, its manifest or under META-INF/maven/:"
    printf '%s\n' "$stray"
    failed=1
else
    echo "$sources_jar holds only Java sources, folders, its manifest and Maven's pom files."
fi

engine_files=$(find "$engine_dir" -type f 2>/dev/null | sed "s|^$engine_dir/||" | sort || true)
if ! printf '%s\n' "$engine_files" | grep -q '\.class$'; then
    echo "::error::$engine_dir holds no compiled classes: the build compiled no engine."
    failed=1
else
    main_entries=$(jar tf "$main_jar") || { echo "::error::Can't read $main_jar as a jar."; exit 1; }
    missing=$(printf '%s\n' "$engine_files" | while read -r file; do
        printf '%s\n' "$main_entries" | grep -q -x -F "$file" || echo "$file"
    done)
    if [ -n "$missing" ]; then
        echo "::error::$main_jar lacks $(printf '%s\n' "$missing" | wc -l | tr -d ' ') files of the compiled engine, from $engine_dir:"
        printf '%s\n' "$missing"
        failed=1
    else
        echo "$main_jar holds the compiled engine: the $(printf '%s\n' "$engine_files" | wc -l | tr -d ' ') files of $engine_dir."
    fi
fi

exit "$failed"
