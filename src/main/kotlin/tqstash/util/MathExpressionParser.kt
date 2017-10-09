package tqstash.util

import org.jparsec.*
import java.util.function.BiFunction
import java.util.function.Function

object MathExpressionParser {
    private val SYMBOL = Terminals.Identifier.PARSER.map { SymbolicNode(it) }
    private val NUMBER = Terminals.DecimalLiteral.PARSER.map { ValueNode(it.toDouble()) }

    private val TERM = Parsers.or(SYMBOL, NUMBER)

    private val OPERATORS = Terminals.operators("+", "-", "*", "/", "^", "(", ")")

    private val IGNORED = Scanners.WHITESPACES.skipMany()

    private val TOKENIZER = Parsers.or(
            Terminals.DecimalLiteral.TOKENIZER,
            Terminals.Identifier.TOKENIZER,
            OPERATORS.tokenizer()
    )

    val PARSER: Parser<ENode> = calculator(TERM).from(TOKENIZER, IGNORED)

    private fun term(vararg names: String): Parser<*> {
        return OPERATORS.token(*names)
    }

    private fun <T> op(name: String, value: T): Parser<T> {
        return term(name).retn(value)
    }

    private fun calculator(atom: Parser<ENode>): Parser<ENode> {
        val ref: Parser.Reference<ENode> = Parser.newReference()
        val unit = ref.lazy().between(term("("), term(")")).or(atom)
        val parser = OperatorTable<ENode>()
                .infixl(op("+", BiFunction { l, r -> SumNode(l, r) }), 10)
                .infixl(op("-", BiFunction { l, r -> DifNode(l, r) }), 10)
                .infixl(op("*", BiFunction { l, r -> MulNode(l, r) }), 20)
                .infixl(op("/", BiFunction { l, r -> DivNode(l, r) }), 20)
                .infixl(op("^", BiFunction { l, r -> ExpNode(l, r) }), 30)
                .prefix(op("-", Function { v -> NegNode(v) }), 40)
                .build(unit)
        ref.set(parser)
        return parser
    }
}

sealed class ENode {
    fun eval(resolveSymbol: (String) -> Double): Double {
        fun evalNode(node: ENode): Double = when (node) {
            is SymbolicNode -> resolveSymbol(node.name)
            is ValueNode -> node.value
            is SumNode -> evalNode(node.left) + evalNode(node.right)
            is DifNode -> evalNode(node.left) - evalNode(node.right)
            is MulNode -> evalNode(node.left) * evalNode(node.right)
            is DivNode -> evalNode(node.left) / evalNode(node.right)
            is ExpNode -> Math.pow(evalNode(node.left), evalNode(node.right))
            is NegNode -> -evalNode(node.value)
        }

        return evalNode(this)
    }
}

data class SymbolicNode(val name: String) : ENode()
data class ValueNode(val value: Double) : ENode()
data class NegNode(val value: ENode) : ENode()
data class SumNode(val left: ENode, val right: ENode) : ENode()
data class DifNode(val left: ENode, val right: ENode) : ENode()
data class MulNode(val left: ENode, val right: ENode) : ENode()
data class DivNode(val left: ENode, val right: ENode) : ENode()
data class ExpNode(val left: ENode, val right: ENode) : ENode()
