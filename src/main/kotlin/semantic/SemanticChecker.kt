package semantic

import ast.*
import ast.statement.IfThenStat
import ast.statement.JoinStat
import ast.statement.ReturnStat
import ast.statement.WhileStat
import symbolTable.ParentRefSymbolTable
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*

/**
 * Collection of static methods which perform semantic error checks of all sorts
 * The class was created in order to keep all the semantic error identification
 * one place rather than across a variety of classes
 * @exception SemanticException is thrown in every method if the check is not passed
 *
 * Common params for many methods are the following:
 * > symbol                :: is the name of the variable or function.
 * > errorMessageBuilder   :: is the incomplete SemanticErrorMessageBuilder which is built in case of error
 * > extraMessage          :: Some additional information which is added using appendCustomErrorMessage() method
 * > failMessage           :: is the optional message for the SemanticException to be thrown
 * > firstType, secondType :: are the types to be compared in type matching functions
 */
class SemanticChecker {
    companion object {

        /**
         * Generalization of a common pattern then the condition is checked,
         * builder is constructed if the condition holds and exception is thrown
         * @param condition if true, then the error is raised
         * @param building is a method on the builder which should call method which appends specific error message
         *
         *        Example building = { builder -> builder.pairElementInvalidType() }
         */
        private fun perform(
            condition: Boolean,
            errorMessageBuilder: SemanticErrorMessageBuilder,
            extraMessage: String? = null,
            failMessage: String = "Semantic Error!",
            building: (SemanticErrorMessageBuilder) -> SemanticErrorMessageBuilder
        ) {
            if (!condition) {
                return
            }
            errorMessageBuilder.apply {
                building.invoke(this)
                extraMessage?.run { appendCustomErrorMessage(extraMessage) }
            }.buildAndPrint()
            throw SemanticException(failMessage)
        }

        /**
         * Prints error and quits if the type of the value is not a WPair
         * @param isFirst is the flag which says whether 'fst' or 'snd' is called on pair.
         * It is used for the error message builder
         */
        fun checkThatTheValueIsPair(
            valueType: WAny,
            isFirst: Boolean,
            errorMessageBuilder: SemanticErrorMessageBuilder
        ) {
            perform(valueType !is WPair, errorMessageBuilder) {
                val pairElemGetter = if (isFirst) "fst" else "snd"
                it.unOpInvalidType(valueType, pairElemGetter)
            }
        }

        /**
         * Prints error and quits if the type of the value is not a WArray
         */
        fun checkThatTheValueIsWArray(valueType: WAny, errorMessageBuilder: SemanticErrorMessageBuilder) {
            if (valueType !is WArray) {
                errorMessageBuilder.nonArrayTypeElemAccess(valueType).buildAndPrint()
                throw SemanticException("Cannot access index elements of non-array type: $valueType")
            }
        }

        /**
         * Ensuring that declaration variable is not declared already
         * @param prev is the previous type of the symbol table. Must be null in a valid program case.
         */
        fun checkIfRedeclarationHappens(prev: WAny?, symbol: String, errorMessageBuilder: SemanticErrorMessageBuilder) {
            if (prev != null) {
                errorMessageBuilder.variableRedeclaration(symbol).buildAndPrint()
                throw SemanticException("Attempted to redeclare variable $symbol")
            }
        }

        /**
         * Prints error message if the parent table is not present, which means that symbol will not be found
         * @param parentTable is the parent table in a tree of ParentRefSymbolTable's.
         */
        fun checkParentTableIsNotNull(
            parentTable: ParentRefSymbolTable?,
            symbol: String,
            errorMessageBuilder: SemanticErrorMessageBuilder
        ) {
            if (parentTable == null) {
                errorMessageBuilder.variableNotInScope(symbol).buildAndPrint()
                throw SemanticException("Attempted to modify undeclared symbol.")
            }
        }

        /**
         * Checks if the value obtained from the table is null or not
         * @param valueGot is the type of the symbol queried earlier
         */
        fun checkIfTheVariableIsInScope(valueGot: WAny?, symbol: String, errorMessageBuilder: SemanticErrorMessageBuilder) {
            if (valueGot == null) {
                errorMessageBuilder.variableNotInScope(symbol).buildAndPrint()
                throw SemanticException("Attempted to get undeclared variable $symbol")
            }
        }

        /**
         * Checks that the type validity for variety of types
         * @param type is the type of expression
         * @param expectedType is a Base type or a Pair of do-not-cares
         * @param building is a specific action on SemanticErrorMessageBuilder
         */
        private fun checkExprTypeIs(
            type: WAny,
            expectedType: WAny,
            errorMessageBuilder: SemanticErrorMessageBuilder,
            failMessage: String,
            building: (SemanticErrorMessageBuilder) -> SemanticErrorMessageBuilder
        ) {
            perform(
                condition = expectedType::class != type::class,
                errorMessageBuilder = errorMessageBuilder,
                failMessage = failMessage,
                building = building
            )
        }

        /**
         * Checks that the type of the expression is WInt
         */
        fun checkExprTypeIsWInt(type: WAny, errorMessageBuilder: SemanticErrorMessageBuilder, failMessage: String) {
            checkExprTypeIs(type, WInt(), errorMessageBuilder, failMessage) { it.nonIntExpressionExit(type) }
        }

        /**
         * Checks that the type of the expression is WPair (of whatever, does not matter)
         */
        fun checkExprTypeIsWPair(type: WAny, errorMessageBuilder: SemanticErrorMessageBuilder, failMessage: String) {
            checkExprTypeIs(type, WPair.ofWUnknowns(), errorMessageBuilder, failMessage) { it.freeNonPair(type) }
        }

        /**
         * Checks that the type of the 'if' statement condition expression is WBool
         */
        fun checkIfCondIsWBool(type: WAny, errorMessageBuilder: SemanticErrorMessageBuilder, failMessage: String) {
            checkExprTypeIs(type, WBool(), errorMessageBuilder, failMessage) {
                it.ifStatConditionHasNonBooleanType(type)
            }
        }

        /**
         * Checks that the type of the 'while' statement condition expression is WBool
         */
        fun checkWhileCondIsWBool(type: WAny, errorMessageBuilder: SemanticErrorMessageBuilder, failMessage: String) {
            checkExprTypeIs(type, WBool(), errorMessageBuilder, failMessage) {
                it.whileStatConditionHasNonBooleanType(type)
            }
        }

        /**
         * Checks that the type of the read expression is either WChar or WInt
         */
        fun checkReadType(type: WAny, errorMessageBuilder: SemanticErrorMessageBuilder, failMessage: String) {
            if (type !is WChar && type !is WInt) {
                errorMessageBuilder.readTypeIsIncorrect(type).buildAndPrint()
                throw SemanticException(failMessage)
            }
        }

        /**
         * Checks that the SymbolTable provided is the root table, i.e. it is the global scope
         * It is important for the 'return' statement outside a function
         * @param st is the symbol table which is expected to be global.
         */
        fun checkReturnFromGlobalScope(st: SymbolTable, errorMessageBuilder: SemanticErrorMessageBuilder) {
            if (st.isGlobal) {
                errorMessageBuilder.returnFromGlobalScope().buildAndPrint()
                throw SemanticException("Cannot return out of global scope.")
            }
        }

        /**
         * Compares two types and ensures they are equal in variable Assignment
         */
        fun checkThatAssignmentTypesMatch(
            firstType: WAny,
            secondType: WAny,
            errorMessageBuilder: SemanticErrorMessageBuilder,
            failMessage: String = "Assignment Type Mismatch"
        ) {
            if (!typesAreEqual(firstType, secondType)) {
                errorMessageBuilder.assignmentTypeMismatch(firstType, secondType).buildAndPrint()
                throw SemanticException(failMessage)
            }
        }

        /**
         * Compares two types of operands and ensures they are equal in expression
         */
        fun checkThatOperandTypesMatch(
            firstType: WAny,
            secondType: WAny,
            errorMessageBuilder: SemanticErrorMessageBuilder,
            extraMessage: String? = null,
            failMessage: String = "Operand Type Mismatch"
        ) {
            perform(!typesAreEqual(firstType, secondType), errorMessageBuilder, extraMessage, failMessage) {
                it.operandTypeMismatch(firstType, secondType)
            }
        }

        /**
         * Checking that the provided BINARY operation and operand types are the same
         * The precondition is that both operands have the same type
         * @param operandType is the type of both operands in binary operation
         * @param operation is the type of BINARY operation
         */
        fun checkThatOperationTypeIsValid(
            operandType: WAny,
            errorMessageBuilder: SemanticErrorMessageBuilder,
            operation: BinOperator,
        ) {
            val operationTypeNotValid: Boolean = when {
                BinOperator.isForInt(operation) -> !typesAreEqual(operandType, WInt())
                BinOperator.isOrdering(operation) ->
                    !typesAreEqual(operandType, WInt()) && !typesAreEqual(operandType, WChar())
                BinOperator.isForAnyType(operation) -> false
                BinOperator.isForBool(operation) -> !typesAreEqual(operandType, WBool())
                else -> throw Exception("Unknown BinOperator value!")
            }
            if (operationTypeNotValid) {
                errorMessageBuilder.binOpInvalidType(operandType, operation.toString()).buildAndPrint()
                throw SemanticException(
                    "Attempted to call binary operation $operation on operands of invalid type: $operandType")
            }
        }

        /**
         * Overloaded version of the same function, but for UNARY operations
         */
        fun checkThatOperationTypeIsValid(
            operandType: WAny,
            errorMessageBuilder: SemanticErrorMessageBuilder,
            operation: UnOperator,
        ) {
            val typeIsIncorrect: Boolean = when (operation) {
                UnOperator.NOT -> operandType !is WBool
                UnOperator.ORD -> operandType !is WChar
                UnOperator.LEN -> operandType !is WArray
                UnOperator.CHR, UnOperator.SUB -> operandType !is WInt
            }
            if (typeIsIncorrect) {
                errorMessageBuilder.unOpInvalidType(operandType, operation.toString()).buildAndPrint()
                throw SemanticException("Attempted to call $operation operation on invalid type: $operandType")
            }
        }

        /**
         * Ensuring that the 'fst' or 'snd' not applied to null pairs
         */
        fun checkNullDereference(expr: Expr, errorMessageBuilder: SemanticErrorMessageBuilder) {
            if (expr is PairLiteral || expr.type is WPairNull) {
                errorMessageBuilder.pairElementInvalidType().buildAndPrint()
                throw SemanticException("NULL POINTER EXCEPTION! Can't dereference null.")
            }
        }

        /**
         * Ensures that all the indices in arbitrary dimension array ate ints.
         * @param indices is a collection of N indices o N-dimensional array
         */
        fun checkThatAllIndicesAreWInts(indices: Array<Expr>, errorMessageBuilder: SemanticErrorMessageBuilder) {
            indices.map { expr -> takeExprTypeAsWIntWithCheck(expr, errorMessageBuilder) }
        }

        /**
         * Ensures that all the elements in the array are the same.
         * @param elements is the collection of the elements of the array
         */
        fun checkThatAllArrayElementsHaveTheSameType(
            elements: Array<Expr>,
            errorMessageBuilder: SemanticErrorMessageBuilder
        ) {
            if (elements.isEmpty()) {
                return
            }
            val expType: WAny = elements.first().type
            elements.forEach {
                if (!typesAreEqual(it.type, expType)) {
                    errorMessageBuilder.arrayEntriesTypeClash().buildAndPrint()
                    throw SemanticException("Types in array are not equal: ${it.type}, $expType")
                }
            }
        }

        fun checkTheExprIsPairAndNoNullDereference(
            expr: Expr,
            first: Boolean,
            errorMessageBuilder: SemanticErrorMessageBuilder
        ) {
            if (expr.type is WPairKW) {
                return
            }
            checkThatTheValueIsPair(expr.type, first, errorMessageBuilder)
            checkNullDereference(expr, errorMessageBuilder)
        }

        /**
         * Compares the number of arguments in function definition and function call.
         * Checks type validity for function arguments, in function definition and function call
         * @param func is the function definition
         * @param params are the parameters of the function call
         */
        fun checkFunctionCall(
            func: WACCFunction,
            params: Array<Expr>,
            errorMessageBuilder: SemanticErrorMessageBuilder,
            identifier: String
        ) {
            // Checking argument count in function call and in the definition
            if (func.params.size != params.size) {
                errorMessageBuilder.functionArgumentCountMismatch(func.params.size, params.size).buildAndPrint()
                throw SemanticException(
                    "Argument count does not match up with expected count for function $identifier")
            }
            // Checking type validity for each parameter
            func.params.onEachIndexed { index, (_, expectedType) ->
                val actualType = params[index].type
                if (!typesAreEqual(expectedType, actualType)) {
                    errorMessageBuilder.functionArgumentTypeMismatch(expectedType, actualType).buildAndPrint()
                    throw SemanticException(
                        "Mismatching types for function $identifier call: expected $expectedType, got $actualType")
                }
            }
        }

        /**
         * Returns the type of Expression, cast as WInt
         * @param expr is the expression which is expected to be WInt
         * @throws SemanticException if safe casting did not succeed
         */
        private fun takeExprTypeAsWIntWithCheck(expr: Expr, errorMessageBuilder: SemanticErrorMessageBuilder): WInt {
            return expr.type as? WInt ?: run {
                errorMessageBuilder.arrayIndexInvalidType().buildAndPrint()
                throw SemanticException("Cannot use non-int index for array, actual: ${expr.type}")
            }
        }

        fun checkAssignment(lhs: LHS, rhs: RHS, st: SymbolTable, errorMessageBuilder: SemanticErrorMessageBuilder) {
            checkThatOperandTypesMatch(lhs.type, rhs.type, errorMessageBuilder,
                failMessage = "Cannot assign ${rhs.type} to ${lhs.type}"
            )
            // Checking AssignLhs Depending on its type
            when (lhs) {
                is IdentifierSet -> st.reassign(lhs.identifier, rhs.type, errorMessageBuilder)
                is ArrayElement -> {
                    val indices: Array<WInt> = lhs.indices.map {
                        takeExprTypeAsWIntWithCheck(it, errorMessageBuilder)
                    }.toTypedArray()
                    st.reassign(lhs.identifier, indices, rhs.type, errorMessageBuilder)
                }
                is PairElement -> {
                    val msg = "Cannot refer to ${lhs.type} with fst/snd"
                    perform(lhs.expr !is IdentifierGet, errorMessageBuilder, msg, msg) { it.pairElementInvalidType() }

                    val identifier = (lhs.expr as IdentifierGet).identifier
                    st.reassign(identifier, lhs.first, rhs.type, errorMessageBuilder)
                }
            }
        }

        /**
         * Checks the return type of statement by matching patterns recursively and
         * throws a semantic exception if the type does not match the expected
         * @param stat : return type to be checked
         * @param expected : expected type to be matched
         * @param errorMessageBuilder : incomplete semantic error message builder which is built in error case
         **/
        fun checkReturnType(stat: Stat, expected: WAny, errorMessageBuilder: SemanticErrorMessageBuilder) {
            when (stat) {
                is IfThenStat -> {
                    checkReturnType(stat.thenStat, expected, errorMessageBuilder)
                    checkReturnType(stat.elseStat, expected, errorMessageBuilder)
                }
                is WhileStat -> checkReturnType(stat.doBlock, expected, errorMessageBuilder)
                is JoinStat -> {
                    checkReturnType(stat.first, expected, errorMessageBuilder)
                    checkReturnType(stat.second, expected, errorMessageBuilder)
                }
                is ReturnStat -> {
                    if (typesAreEqual(expected, stat.type)) {
                        return
                    }
                    errorMessageBuilder.functionReturnStatTypeMismatch(expected, stat.type).buildAndPrint()
                    throw SemanticException(
                        "Mismatching return type for function, expected: $expected, got: ${stat.type}")
                }
            }
        }
    }
}