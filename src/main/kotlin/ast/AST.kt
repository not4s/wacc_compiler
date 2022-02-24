package ast

import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.PositionedError
import utils.SemanticErrorMessageBuilder
import waccType.WAny
import waccType.WBase
import waccType.WPair
import waccType.WPairNull

const val INDENT = "  | "

interface AST {
    /**
     *  Information in the symbol table is the mapping from variable and
     *  function identifiers to its type The symbol table attribute references the table,
     *  corresponding to its nearest scope
     */
    val st: SymbolTable

    /**
     *  Converts the AST node into a string containing the information of that node
     */
    override fun toString(): String
}

class ProgramAST(
    override val st: SymbolTable,
    val functions: List<WACCFunction>,
    val body: Stat,
) : AST {
    override fun toString(): String {
        return "Prog:\n" +
                "{\n" +
                "$functions" +
                "\n$body\n" +
                "}"
    }

}

interface Typed : AST {
    val type: WAny
}

interface RHS : AST, Typed

interface LHS : AST, Typed

interface Expr : AST, Typed, RHS

interface Stat : AST

/**
 * SemanticErrorMessageBuilder smart constructor from Parse Context and Symbol Table
 * @return the incomplete builder which is commonly created.
 * Helps avoid duplication.
 */
fun builderTemplateFromContext(
    parserCtx: ParserRuleContext,
    st: SymbolTable
): SemanticErrorMessageBuilder {
    return SemanticErrorMessageBuilder()
        .provideStart(PositionedError(parserCtx))
        .setLineTextFromSrcFile(st.srcFilePath)
}

/**
 *  The AST Node for Base Type Literals
 **/
class Literal(
    override val st: SymbolTable,
    override val type: WBase,
) : Expr, RHS {

    override fun toString(): String {
        return "Literal\n  (scope:$st)\n${("type: $type").prependIndent(INDENT)}"
    }
}

class WACCType(
    override val st: SymbolTable,
    override val type: WAny
) : Typed {

    override fun toString(): String {
        return "WACCType: $type"
    }
}

/**
 *  The AST Node for Pair Literals
 **/
class PairLiteral(
    override val st: SymbolTable,
    override val type: WPairNull,
) : Expr {

    override fun toString(): String {
        return "PairLiteral(null)\n  (scope:$st)\n${("type: $type").prependIndent(INDENT)}"
    }
}

/**
 * The AST Node for RHS New Pair
 **/
class NewPairRHS(
    override val st: SymbolTable,
    private val left: Expr,
    private val right: Expr,
    override val type: WPair,
) : RHS {
    override fun toString(): String {
        return "NEWPAIR:\n  (scope:$st)\nleft:\n${
            left.toString().prependIndent(INDENT)
        }\nright:\n" + right.toString().prependIndent(INDENT)
    }
}