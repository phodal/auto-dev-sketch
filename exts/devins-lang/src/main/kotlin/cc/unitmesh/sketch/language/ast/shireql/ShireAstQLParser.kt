package cc.unitmesh.sketch.language.ast.shireql

import cc.unitmesh.sketch.language.ast.FrontMatterType
import cc.unitmesh.sketch.language.ast.ShirePsiQueryStatement
import cc.unitmesh.sketch.language.ast.Statement
import cc.unitmesh.sketch.language.ast.VariableElement
import cc.unitmesh.sketch.language.compiler.HobbitHoleParser
import cc.unitmesh.sketch.language.psi.DevInFromClause
import cc.unitmesh.sketch.language.psi.DevInQueryStatement
import cc.unitmesh.sketch.language.psi.DevInSelectClause
import cc.unitmesh.sketch.language.psi.DevInWhereClause


object ShireAstQLParser {
    fun parse(statement: DevInQueryStatement): FrontMatterType {
        val value = ShirePsiQueryStatement(
            parseFrom(statement.fromClause),
            parseWhere(statement.whereClause)!!,
            parseSelect(statement.selectClause)
        )

        return FrontMatterType.QUERY_STATEMENT(value)
    }

    private fun parseFrom(fromClause: DevInFromClause): List<VariableElement> {
        return fromClause.psiElementDecl.psiVarDeclList.map {
            VariableElement(it.psiType.identifier.text, it.identifier.text)
        }
    }

    private fun parseWhere(whereClause: DevInWhereClause): Statement? {
        return HobbitHoleParser.parseExpr(whereClause.expr)
    }

    private fun parseSelect(selectClause: DevInSelectClause): List<Statement> {
        return selectClause.exprList.mapNotNull {
            HobbitHoleParser.parseExpr(it)
        }
    }
}
