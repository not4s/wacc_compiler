package semantic

import symbolTable.ParentRefSymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WAny

/**
 * Collection of static methods which perform semantic error checks of all sorts
 * The class was created in order to keep all the semantic error identification
 * one place rather than across a variety of classes
 * @exception SemanticException is thrown in every method if the check is not passed
 *
 * Common params for many methods are the following:
 * @param symbol is the name of the variable or function.
 * @param errBuilder is the incomplete SemanticErrorMessageBuilder which is built in case of error
 */
class SemanticChecker {
    companion object {

        /**
         * Ensuring that declaration variable is not declared already
         * @param prev is the previous type of the symbol table. Must be null in a valid program case.
         * @throws SemanticException if the variable was declared, i.e. the symbol already a key in symbol table
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
         * @throws SemanticException if there's no parent table which may contain the desired symbol
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
         * @throws SemanticException if the value is null which means that no such symbol in the table
         */
        fun checkIfTheVariableIsInScope(valueGot: WAny?, symbol: String, errBuilder: SemanticErrorMessageBuilder) {
            if (valueGot == null) {
                errBuilder.variableNotInScope(symbol).buildAndPrint()
                throw SemanticException("Attempted to get undeclared variable $symbol")
            }
        }
    }
}