package ast

interface WAny

interface AbstractSyntaxTree {
//    fun getScope() : SymbolTable
}

class ProgramAST(
    val functions: Array<FunctionAST>,
    val body: StatAST
) : AbstractSyntaxTree {
//    val baseSymbolTable: SymbolTable = initializeSymbolTable()
//    private fun initializeSymbolTable(): SymbolTable {}

}

class FunctionAST(
    val type: WAny,
    val identifier: String,
    val params: Array<ParamAst>,
    val body: StatAST
) : AbstractSyntaxTree

class ParamAst(
    val type: WAny,
    val identifier: String
) : AbstractSyntaxTree

interface StatAST : AbstractSyntaxTree

class SkipAST : StatAST

class ExitAST(
    val expr: ExprAST
) : StatAST

class FreeAST(
    val expr: ExprAST
) : StatAST

class ReturnAST(
    val expr: ExprAST
) : StatAST

/**
 * @property lineBreak refers to println keyword
 */
class PrintAST(
    val lineBreak: Boolean,
    val expr: ExprAST
) : StatAST

class ReadAST(
    val destination: AssignLhsAST
) : StatAST

class IfAST(
    val cond: ExprAST,
    val thenBlock: StatAST,
    val elseBlock: StatAST
)

class WhileAST(
    val cond: ExprAST,
    val body: StatAST
)

/**
 * Combining statement composition into a scope
 */
class ScopeAST(
    val body: Array<StatAST>
) : StatAST

class DeclarationAST(
    val type: WAny,
    val lhs: AssignLhsAST,
    val rhs: AssignRhsAST
)

class AssignmentAST(
    val lhs: AssignLhsAST,
    val rhs: AssignRhsAST
)

interface AssignLhsAST

class IdentifierLhsAST(
    val identifier: String
) : AssignLhsAST

class ArrayElemAST(
    val identifier: String,
    val index: ExprAST
) : AssignLhsAST, ExprAST

/**
 * @property first is used to determine whether the first(true) or second(false)
 * pair element is accessed/assigned
 */
class PairElemAST(
    val first: Boolean,
    val identifier: String
) : AssignLhsAST, AssignRhsAST

interface AssignRhsAST

class ArrayLiterAST(
    val elements: Array<ExprAST>
) : AssignRhsAST

class NewPairAST(
    val first: ExprAST,
    val second: ExprAST
) : AssignRhsAST

class FunctionCallAst(
    val identifier: String,
    val arguments: List<ExprAST>
)

interface ExprAST : AssignRhsAST

class LiteralAST(
    val value: WAny
) : ExprAST

class NullAst : ExprAST

class UnOpAST(
    val operation: UnOperator,
    val operand: ExprAST
) : ExprAST {
    enum class UnOperator {
        NOT, ORD, CHR, LEN, SUB;
    }
}

class BinOpAST(
    val left: ExprAST,
    val operation: BinOperator,
    val right: ExprAST,
) : ExprAST {
    enum class BinOperator {
        MUL, DIV, MOD, ADD, SUB, GT, GEQ, LT, LEQ, EQ, NEQ, AND, OR;
    }
}