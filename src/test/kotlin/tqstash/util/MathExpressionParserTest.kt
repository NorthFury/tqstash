package tqstash.util

import org.junit.Test
import tqstash.util.MathExpressionParser
import kotlin.test.assertEquals

class MathExpressionParserTest {
    private val parser = MathExpressionParser.PARSER

    @Test
    fun `operator precedence should be respected`() {
        val resultNode = parser.parse("(-1 + 2) * 5 + 3 * 2^2")
        val result = resultNode.eval { 0.0 }
        assertEquals(
                expected = 17.0,
                actual = result
        )
    }

    @Test
    fun `expressions containing symbols should be correctly evaluated`() {
        val resultNode = parser.parse("x + y * z")
        val result = resultNode.eval {
            when (it) {
                "x" -> 1.0
                "y" -> 2.0
                "z" -> 3.0
                else -> throw Exception("Incorrect parsed symbol name")
            }
        }

        assertEquals(
                expected = 7.0,
                actual = result
        )
    }
}
