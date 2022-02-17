package semantic

import ast.Expr
import ast.IdentifierGet
import symbolTable.ParentRefSymbolTable
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
 * > symbol      :: is the name of the variable or function.
 * > errBuilder  :: is the incomplete SemanticErrorMessageBuilder which is built in case of error
 * > failMessage :: is the optional message for the SemanticException to be thrown
 */
class SemanticChecker {
    companion object {

        /**
         * Prints error and quits if the type of the value is not a WPair
         * @param isFirst is the flag which says whether 'fst' or 'snd' is called on pair.
         * It is used for the error message builder
         */
        fun checkThatTheValueIsPair(valueType: WAny, isFirst: Boolean, errBuilder: SemanticErrorMessageBuilder) {
            if (valueType !is WPair) {
                val pairElemGetter = if (isFirst) "fst" else "snd"
                errBuilder.unOpInvalidType(valueType, pairElemGetter).buildAndPrint()
                throw SemanticException("Cannot obtain $pairElemGetter from type: $valueType")
            }
        }

        /**
         * Prints error and quits if the type of the value is not a WArray
         */
        fun checkThatTheValueIsWArray(valueType: WAny, errBuilder: SemanticErrorMessageBuilder) {
            if (valueType !is WArray) {
                errBuilder.nonArrayTypeElemAccess(valueType).buildAndPrint()
                throw SemanticException("Cannot access index elements of non-array type: $valueType")
            }
        }

        /**
         * Ensuring that declaration variable is not declared already
         * @param prev is the previous type of the symbol table. Must be null in a valid program case.
         */
        fun checkIfRedeclarationHappens(prev: WAny?, symbol: String, errBuilder: SemanticErrorMessageBuilder) {
            if (prev != null) {
                errBuilder.variableRedeclaration(symbol).buildAndPrint()
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
            errBuilder: SemanticErrorMessageBuilder
        ) {
            if (parentTable == null) {
                errBuilder.variableNotInScope(symbol).buildAndPrint()
                throw SemanticException("Attempted to modify undeclared symbol.")
            }
        }

        /**
         * Checks if the value obtained from the table is null or not
         * @param valueGot is the type of the symbol queried earlier
         */
        fun checkIfTheVariableIsInScope(valueGot: WAny?, symbol: String, errBuilder: SemanticErrorMessageBuilder) {
            if (valueGot == null) {
                errBuilder.variableNotInScope(symbol).buildAndPrint()
                throw SemanticException("Attempted to get undeclared variable $symbol")
            }
        }

        /**
         * Compares two types and ensures they are equal in variable Assignment
         * @param firstType and
         * @param secondType are the types to be compared
         */
        fun checkThatAssignmentTypesMatch(
            firstType: WAny,
            secondType: WAny,
            errBuilder: SemanticErrorMessageBuilder,
            failMessage: String = "Assignment Type Mismatch"
        ) {
            if (!typesAreEqual(firstType, secondType)) {
                errBuilder.assignmentTypeMismatch(firstType, secondType).buildAndPrint()
                throw SemanticException(failMessage)
            }
        }

        /**
         * Compares two types of operands and ensures they are equal in expression
         * @param firstType and
         * @param secondType are the types to be compared
         */
        fun checkThatOperandTypesMatch(
            firstType: WAny,
            secondType: WAny,
            errorMessageBuilder: SemanticErrorMessageBuilder,
            extraMessage: String? = null,
            failMessage: String = "Operand Type Mismatch"
        ) {
            if (!typesAreEqual(firstType, secondType)) {
                errorMessageBuilder.operandTypeMismatch(firstType, secondType)
                    .apply {
                        extraMessage?.run { appendCustomErrorMessage(extraMessage) }
                    }
                    .buildAndPrint()
                throw SemanticException(failMessage)
            }
        }

        fun checkThatLhsPairExpressionIsIdentifier(
            expr: Expr,
            errorMessageBuilder: SemanticErrorMessageBuilder,
            extraMessage: String? = null,
            failMessage: String = "Not an identifier"
        ) {
            if (expr is IdentifierGet) {
                return
            }
            errorMessageBuilder.pairElementInvalidType()
                .apply {
                    extraMessage?.run { appendCustomErrorMessage(extraMessage) }
                }
                .buildAndPrint()
            throw SemanticException(failMessage)
        }

        fun takeExprTypeAsWIntWithCheck(expr: Expr, errorMessageBuilder: SemanticErrorMessageBuilder): WInt {
            return expr.type as? WInt ?: run {
                errorMessageBuilder.arrayIndexInvalidType().buildAndPrint()
                throw SemanticException("Non-int index in array ${expr.type}")
            }
        }
    }
}