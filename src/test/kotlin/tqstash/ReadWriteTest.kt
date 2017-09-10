package tqstash

import org.junit.Test
import java.io.File
import java.util.Arrays
import java.util.UUID
import kotlin.test.assertTrue

class ReadWriteTest {

    @Test
    fun stashReadWriteTest() {
        val testFileUri = this.javaClass
                .getResource("/transfer-stash-winsys.dxb")
                .toURI()
        val outputFileName = UUID.randomUUID().toString()

        val stashInputFile = File(testFileUri)
        val stashOutputFile = File(outputFileName)

        val stash = readStash(stashInputFile)
        writeStash(stash, stashOutputFile)

        val expected = stashInputFile.readBytes()
        val result = stashOutputFile.readBytes()

        assertTrue(
                actual = Arrays.equals(expected, result),
                message = "files should be identical"
        )

        stashOutputFile.delete()
    }
}
