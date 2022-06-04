package com.bennyhuo.kotlin.metadata

import org.junit.Test
import java.io.File

/**
 * Created by benny.
 */
class StripMetadataTest {
    @Test
    fun clasFileTest() {
        StripMetadata.stripClass(
            File("testData/StripMetadata.class"),
            File("testData/StripMetadata_stripped.class")
        )
    }

    @Test
    fun jarFileTest() {
        StripMetadata.stripJar(
            File("testData/strip-metadata-1.0.jar"),
            File("testData/strip-metadata-1.0_stripped.jar")
        )
    }
}