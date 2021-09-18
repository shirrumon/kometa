package kometa.kotlin.ast

import kometa.kotlin.Visitor

data class FqName(val names: List<String>) {
    override fun toString(): String = names.joinToString(".")
}

data class Locus(
    val fileName: String,
    val startOffset: Int,
    val endOffset: Int
)

sealed class Element {
    abstract val locus: Locus
    var tag: Any? = null

    interface WithAnnotations {
        val anns: List<Annotation>
    }

    interface WithModifiers : WithAnnotations {
        val mods: List<ModifierOrAnnotation>
        override val anns: List<Annotation>
            get() = mods.mapNotNull { it as? Annotation }
    }

    abstract fun accept(v: Visitor)

    abstract fun acceptChildren(v: Visitor)
}

data class File(
    override val locus: Locus,
    val anns: List<Annotation>,
    val pkg: Package?,
    val imports: List<Import>,
    val declarations: List<Declaration>
) : Element() {
    override fun accept(v: Visitor) {
        v.visitFile(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (ann in anns) {
            ann.accept(v)
        }
        pkg?.accept(v)
        for (imp in imports) {
            imp.accept(v)
        }
        for (decl in declarations) {
            decl.accept(v)
        }
    }
}

data class Script(
    override val locus: Locus,
    val anns: List<Annotation>,
    val pkg: Package?,
    val imports: List<Import>,
    val exprs: List<Expression>
) : Element() {
    override fun accept(v: Visitor) {
        v.visitScript(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (ann in anns) {
            ann.accept(v)
        }
        pkg?.accept(v)
        for (imp in imports) {
            imp.accept(v)
        }
        for (expr in exprs) {
            expr.accept(v)
        }
    }
}

data class Package(
    override val locus: Locus,
    val fqName: FqName
) : Element() {
    override fun accept(v: Visitor) {
        v.visitPackage(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class Import(
    override val locus: Locus,
    val fqName: FqName,
    val wildcard: Boolean,
    val alias: String?
) : Element() {
    override fun accept(v: Visitor) {
        v.visitImport(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

sealed class Declaration : Element()

data class ClassDeclaration(
    override val locus: Locus,
    override val mods: List<ModifierOrAnnotation>,
    val kind: Kind,
    val name: String,
    val typeParameters: List<TypeParameter>,
    val primaryConstructor: PrimaryConstructor?,
    val parentAnns: List<Annotation>,
    val parents: List<Parent>,
    val typeConstraints: List<TypeConstraint>,
    // TODO: Can include primary constructor
    val members: List<Declaration>
) : Declaration(), Element.WithModifiers {
    enum class Kind {
        CLASS, ENUM_CLASS, INTERFACE, OBJECT, COMPANION_OBJECT
    }

    sealed class Parent : Element()

    data class SuperClassConstructorCall(
        override val locus: Locus,
        val type: SimpleType,
        val typeArgs: List<Type>,
        val arguments: List<ValueArgument>,
        val lambda: CallExpression.TrailingLambda?
    ) : Parent() {
        override fun accept(v: Visitor) {
            v.visitClassParentConstructorCall(this)
        }

        override fun acceptChildren(v: Visitor) {
            type.accept(v)
            for (typeArg in typeArgs) {
                typeArg.accept(v)
            }
            for (arg in arguments) {
                arg.accept(v)
            }
            lambda?.accept(v)
        }
    }

    data class InterfaceType(
        override val locus: Locus,
        val type: SimpleType,
        val delegated: Expression?
    ) : Parent() {
        override fun accept(v: Visitor) {
            v.visitClassParentInterfaceType(this)
        }

        override fun acceptChildren(v: Visitor) {
            type.accept(v)
            delegated?.accept(v)
        }
    }

    data class PrimaryConstructor(
        override val locus: Locus,
        override val mods: List<ModifierOrAnnotation>,
        val params: List<ValueParameter>
    ) : Element(), WithModifiers {
        override fun accept(v: Visitor) {
            v.visitPrimaryConstructor(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (mod in mods) {
                mod.accept(v)
            }
            for (param in params) {
                param.accept(v)
            }
        }
    }

    data class InitBlock(
        override val locus: Locus,
        val block: Block
    ) : Declaration() {
        override fun accept(v: Visitor) {
            v.visitInitDecl(this)
        }

        override fun acceptChildren(v: Visitor) {
            block.accept(v)
        }
    }

    override fun accept(v: Visitor) {
        v.visitClass(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (mod in mods) {
            mod.accept(v)
        }
        for (typeParam in typeParameters) {
            typeParam.accept(v)
        }
        primaryConstructor?.accept(v)
        for (parentAnn in parentAnns) {
            parentAnn.accept(v)
        }
        for (typeConstraint in typeConstraints) {
            typeConstraint.accept(v)
        }
        for (member in members) {
            member.accept(v)
        }
    }

    data class Constructor(
        override val locus: Locus,
        override val mods: List<ModifierOrAnnotation>,
        val params: List<ValueParameter>,
        val delegationCall: DelegationCall?,
        val block: Block?
    ) : Declaration(), Element.WithModifiers {
        data class DelegationCall(
            override val locus: Locus,
            val target: DelegationTarget,
            val arguments: List<ValueArgument>
        ) : Element() {
            override fun accept(v: Visitor) {
                v.visitConstructorDelegationCall(this)
            }

            override fun acceptChildren(v: Visitor) {
                for (arg in arguments) {
                    arg.accept(v)
                }
            }
        }

        enum class DelegationTarget { THIS, SUPER }

        override fun accept(v: Visitor) {
            v.visitConstructor(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (mod in mods) {
                mod.accept(v)
            }
            for (param in params) {
                param.accept(v)
            }
            delegationCall?.accept(v)
            block?.accept(v)
        }
    }

    data class EnumEntry(
        override val locus: Locus,
        override val mods: List<ModifierOrAnnotation>,
        val name: String,
        val arguments: List<ValueArgument>,
        val members: List<Declaration>
    ) : Declaration(), Element.WithModifiers {
        override fun accept(v: Visitor) {
            v.visitEnumEntry(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (mod in mods) {
                mod.accept(v)
            }
            for (arg in arguments) {
                arg.accept(v)
            }
            for (member in members) {
                member.accept(v)
            }
        }

    }
}

data class FunctionDeclaration(
    override val locus: Locus,
    override val mods: List<ModifierOrAnnotation>,
    val typeParameters: List<TypeParameter>,
    val receiverType: Type?,
    // Name not present on anonymous functions
    val name: String?,
    val params: List<ValueParameter>,
    val type: Type?,
    val typeConstraints: List<TypeConstraint>,
    val body: FunctionBody?
) : Declaration(), Element.WithModifiers {
    override fun accept(v: Visitor) {
        v.visitFunc(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (mod in mods) {
            mod.accept(v)
        }
        for (typeParam in typeParameters) {
            typeParam.accept(v)
        }
        receiverType?.accept(v)
        for (param in params) {
            param.accept(v)
        }
        type?.accept(v)
        for (typeConstraint in typeConstraints) {
            typeConstraint.accept(v)
        }
        body?.accept(v)
    }
}

data class ValueParameter(
    override val locus: Locus,
    override val mods: List<ModifierOrAnnotation>,
    val isVal: Boolean?,
    val name: String,
    // Type can be null for anon functions
    val type: Type?,
    val default: Expression?
) : Element(), Element.WithModifiers {
    override fun accept(v: Visitor) {
        v.visitFunctionParam(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (mod in mods) {
            mod.accept(v)
        }
        type?.accept(v)
        default?.accept(v)
    }
}

sealed class FunctionBody : Element()

data class BlockBody(val block: Block) : FunctionBody() {
    override val locus: Locus
        get() = block.locus

    override fun accept(v: Visitor) {
        v.visitFunctionBlockBody(this)
    }

    override fun acceptChildren(v: Visitor) {
        block.accept(v)
    }
}

data class ExpressionBody(val expr: Expression) : FunctionBody() {
    override val locus: Locus
        get() = expr.locus

    override fun accept(v: Visitor) {
        v.visitFunctionExprBody(this)
    }

    override fun acceptChildren(v: Visitor) {
        expr.accept(v)
    }
}

data class PropertyDeclaration(
    override val locus: Locus,
    override val mods: List<ModifierOrAnnotation>,
    val readOnly: Boolean,
    val typeParameters: List<TypeParameter>,
    val receiverType: Type?,
    // Always at least one, more than one is destructuring, null is underscore in destructure
    val vars: List<DestructuringEntry?>,
    val typeConstraints: List<TypeConstraint>,
    val delegated: Boolean,
    val expr: Expression?,
    val getter: Getter?,
    val setter: Setter?
) : Declaration(), Element.WithModifiers {
    data class DestructuringEntry(
        override val locus: Locus,
        val name: String,
        val type: Type?
    ) : Element() {
        override fun accept(v: Visitor) {
            v.visitPropertyVar(this)
        }

        override fun acceptChildren(v: Visitor) {
            type?.accept(v)
        }
    }

    data class Getter(
        override val locus: Locus,
        override val mods: List<ModifierOrAnnotation>,
        val type: Type?,
        val body: FunctionBody?
    ) : Element(), WithModifiers {
        override fun accept(v: Visitor) {
            v.visitPropertyGetter(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (mod in mods) {
                mod.accept(v)
            }
            type?.accept(v)
            body?.accept(v)
        }
    }

    data class Setter(
        override val locus: Locus,
        override val mods: List<ModifierOrAnnotation>,
        val paramMods: List<ModifierOrAnnotation>,
        val paramName: String?,
        val paramType: Type?,
        val body: FunctionBody?
    ) : Element(), WithModifiers {
        override fun accept(v: Visitor) {
            v.visitPropertySetter(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (mod in mods) {
                mod.accept(v)
            }
            for (paramMod in paramMods) {
                paramMod.accept(v)
            }
            paramType?.accept(v)
            body?.accept(v)
        }
    }

    override fun accept(v: Visitor) {
        v.visitProperty(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (mod in mods) {
            mod.accept(v)
        }
        for (typeParam in typeParameters) {
            typeParam.accept(v)
        }
        receiverType?.accept(v)
        for (vr in vars) {
            vr?.accept(v)
        }
        for (typeConstraint in typeConstraints) {
            typeConstraint.accept(v)
        }
        expr?.accept(v)
        getter?.accept(v)
        setter?.accept(v)
    }
}

data class TypeAliasDeclaration(
    override val locus: Locus,
    override val mods: List<ModifierOrAnnotation>,
    val name: String,
    val typeParameters: List<TypeParameter>,
    val type: Type
) : Declaration(), Element.WithModifiers {
    override fun accept(v: Visitor) {
        v.visitTypeAlias(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (mod in mods) {
            mod.accept(v)
        }
        for (typeParam in typeParameters) {
            typeParam.accept(v)
        }
        type.accept(v)
    }
}

data class TypeParameter(
    override val locus: Locus,
    override val mods: List<ModifierOrAnnotation>,
    val name: String,
    val type: TypeRef?
) : Element(), Element.WithModifiers {
    override fun accept(v: Visitor) {
        v.visitTypeParam(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (mod in mods) {
            mod.accept(v)
        }
        type?.accept(v)
    }
}

data class TypeConstraint(
    override val locus: Locus,
    override val anns: List<Annotation>,
    val name: String,
    val type: Type
) : Element(), Element.WithAnnotations {
    override fun accept(v: Visitor) {
        v.visitTypeConstraint(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (ann in anns) {
            ann.accept(v)
        }
        type.accept(v)
    }
}

sealed class TypeRef : Element()

data class FunctionalType(
    override val locus: Locus,
    val receiverType: Type?,
    val params: List<Param>,
    val type: Type
) : TypeRef() {
    data class Param(
        override val locus: Locus,
        val name: String?,
        val type: Type
    ) : Element() {
        override fun accept(v: Visitor) {
            v.visitFuncTypeParam(this)
        }

        override fun acceptChildren(v: Visitor) {
            type.accept(v)
        }
    }

    override fun accept(v: Visitor) {
        v.visitFuncType(this)
    }

    override fun acceptChildren(v: Visitor) {
        receiverType?.accept(v)
        for (param in params) {
            param.accept(v)
        }
        type.accept(v)
    }
}

data class SimpleType(
    override val locus: Locus,
    val pieces: List<Piece>
) : TypeRef() {
    data class Piece(
        override val locus: Locus,
        val name: String,
        // Null means wildcard
        val typeParams: List<Type?>
    ) : Element() {
        override fun accept(v: Visitor) {
            v.visitSimpleTypePiece(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (typeParam in typeParams) {
                typeParam?.accept(v)
            }
        }
    }

    override fun accept(v: Visitor) {
        v.visitSimpleType(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (piece in pieces) {
            piece.accept(v)
        }
    }
}

data class NullableType(
    override val locus: Locus,
    val type: TypeRef
) : TypeRef() {
    override fun accept(v: Visitor) {
        v.visitNullableType(this)
    }

    override fun acceptChildren(v: Visitor) {
        type.accept(v)
    }
}

data class DynamicType(
    override val locus: Locus
) : TypeRef() {
    override fun accept(v: Visitor) {
        v.visitDynamicType(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class TypeWithModifiers(
    override val locus: Locus,
    override val mods: List<ModifierOrAnnotation>,
    val type: TypeRef
) : TypeRef(), Element.WithModifiers {
    override fun accept(v: Visitor) {
        v.visitParenType(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (mod in mods) {
            mod.accept(v)
        }
        type.accept(v)
    }
}

data class Type(
    override val locus: Locus,
    override val mods: List<ModifierOrAnnotation>,
    val ref: TypeRef
) : Element(), Element.WithModifiers {
    override fun accept(v: Visitor) {
        v.visitType(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (mod in mods) {
            mod.accept(v)
        }
        ref.accept(v)
    }
}

data class ValueArgument(
    override val locus: Locus,
    val name: String?,
    val asterisk: Boolean,
    val expr: Expression
) : Element() {
    override fun accept(v: Visitor) {
        v.visitValueArg(this)
    }

    override fun acceptChildren(v: Visitor) {
        expr.accept(v)
    }
}

sealed class Expression : Element()

data class IfExpression(
    override val locus: Locus,
    val condition: Expression,
    val thenBody: Expression,
    val elseBody: Expression?
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitIfExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        condition.accept(v)
        thenBody.accept(v)
        elseBody?.accept(v)
    }
}

data class TryExpression(
    override val locus: Locus,
    val block: Block,
    val catches: List<Catch>,
    val finallyBlock: Block?
) : Expression() {
    data class Catch(
        override val locus: Locus,
        override val anns: List<Annotation>,
        val varName: String,
        val varType: SimpleType,
        val block: Block
    ) : Element(), WithAnnotations {
        override fun accept(v: Visitor) {
            v.visitCatch(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (ann in anns) {
                ann.accept(v)
            }
            varType.accept(v)
            block.accept(v)
        }
    }

    override fun accept(v: Visitor) {
        v.visitTryExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        block.accept(v)
        for (catch in catches) {
            catch.accept(v)
        }
        finallyBlock?.accept(v)
    }
}

data class ForLoop(
    override val locus: Locus,
    override val anns: List<Annotation>,
    // More than one means destructure, null means underscore
    val vars: List<PropertyDeclaration.DestructuringEntry?>,
    val inExpr: Expression,
    val body: Expression
) : Expression(), Element.WithAnnotations {
    override fun accept(v: Visitor) {
        v.visitForStmt(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (ann in anns) {
            ann.accept(v)
        }
        for (vr in vars) {
            vr?.accept(v)
        }
        inExpr.accept(v)
        body.accept(v)
    }
}

data class WhileLoop(
    override val locus: Locus,
    val condition: Expression,
    val body: Expression
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitWhileStmt(this)
    }

    override fun acceptChildren(v: Visitor) {
        condition.accept(v)
        body.accept(v)
    }
}

data class DoWhileLoop(
    override val locus: Locus,
    val body: Expression,
    val condition: Expression
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitDoWhileStmt(this)
    }

    override fun acceptChildren(v: Visitor) {
        body.accept(v)
        condition.accept(v)
    }
}

data class BinaryExpression(
    override val locus: Locus,
    val lhs: Expression,
    val oper: BinaryOperatorOrInfixCall,
    val rhs: Expression
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitBinaryOp(this)
    }

    override fun acceptChildren(v: Visitor) {
        lhs.accept(v)
        oper.accept(v)
        rhs.accept(v)
    }
}

sealed class BinaryOperatorOrInfixCall : Element()

data class InfixFunctionName(
    override val locus: Locus,
    val str: String
) : BinaryOperatorOrInfixCall() {
    override fun accept(v: Visitor) {
        v.visitInfixBinaryOpOper(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class BinaryOperator(
    override val locus: Locus,
    val token: Token
) : BinaryOperatorOrInfixCall() {
    enum class Token(val str: String) {
        MUL("*"), DIV("/"), MOD("%"), ADD("+"), SUB("-"),
        IN("in"), NOT_IN("!in"),
        GT(">"), GTE(">="), LT("<"), LTE("<="),
        EQ("=="), NEQ("!="),
        ASSN("="), MUL_ASSN("*="), DIV_ASSN("/="), MOD_ASSN("%="), ADD_ASSN("+="), SUB_ASSN("-="),
        OR("||"), AND("&&"), ELVIS("?:"), RANGE(".."),
        DOT("."), DOT_SAFE("?.")
    }

    override fun accept(v: Visitor) {
        v.visitTokenBinaryOpOper(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class UnaryExpression(
    override val locus: Locus,
    val expr: Expression,
    val oper: UnaryOperator,
    val isPrefix: Boolean
) : Expression() {

    override fun accept(v: Visitor) {
        v.visitUnaryOp(this)
    }

    override fun acceptChildren(v: Visitor) {
        expr.accept(v)
        oper.accept(v)
    }
}

data class UnaryOperator(
    override val locus: Locus,
    val token: Token
) : Element() {
    enum class Token(val str: String) {
        NEG("-"), POS("+"), INC("++"), DEC("--"), NOT("!"), NULL_DEREF("!!")
    }

    override fun accept(v: Visitor) {
        v.visitUnaryOpOper(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class TypeExpression(
    override val locus: Locus,
    val lhs: Expression,
    val oper: TypeOperator,
    val rhs: Type
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitTypeOp(this)
    }

    override fun acceptChildren(v: Visitor) {
        lhs.accept(v)
        oper.accept(v)
        rhs.accept(v)
    }
}

data class TypeOperator(
    override val locus: Locus,
    val token: Token
) : Element() {
    enum class Token(val str: String) {
        AS("as"), AS_SAFE("as?"), COL(":"), IS("is"), NOT_IS("!is")
    }

    override fun accept(v: Visitor) {
        v.visitTypeOpOper(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

sealed class ReferenceExpression : Expression() {
    abstract val recv: ReferenceReceiver?
}

data class CallableReferenceExpression(
    override val locus: Locus,
    override val recv: ReferenceReceiver?,
    val name: String
) : ReferenceExpression() {
    override fun accept(v: Visitor) {
        v.visitDoubleColonCallable(this)
    }

    override fun acceptChildren(v: Visitor) {
        recv?.accept(v)
    }
}

data class ClassReferenceExpression(
    override val locus: Locus,
    override val recv: ReferenceReceiver?
) : ReferenceExpression() {
    override fun accept(v: Visitor) {
        v.visitDoubleColonClass(this)
    }

    override fun acceptChildren(v: Visitor) {
        recv?.accept(v)
    }
}

sealed class ReferenceReceiver : Element() {
    data class ExpressionReceiver(val expr: Expression) : ReferenceReceiver() {
        override val locus: Locus
            get() = expr.locus

        override fun accept(v: Visitor) {
            v.visitDoubleColonExprRecv(this)
        }

        override fun acceptChildren(v: Visitor) {
            expr.accept(v)
        }
    }

    data class Type(
        val type: SimpleType,
        val questionMark: Boolean
    ) : ReferenceReceiver() {
        override val locus: Locus
            get() = type.locus

        override fun accept(v: Visitor) {
            v.visitDoubleColonTypeRecv(this)
        }

        override fun acceptChildren(v: Visitor) {
            type.accept(v)
        }
    }
}

// TODO: Replace with sealed class
data class ConstantExpression(
    override val locus: Locus,
    val value: String,
    val form: Form
) : Expression() {
    enum class Form { STRING, BOOLEAN, CHAR, INT, FLOAT, NULL }

    override fun accept(v: Visitor) {
        v.visitConst(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class LambdaLiteral(
    override val locus: Locus,
    val params: List<Param>,
    val block: Block?
) : Expression() {
    data class Param(
        override val locus: Locus,
        // Multiple means destructure, null means underscore
        val vars: List<PropertyDeclaration.DestructuringEntry?>,
        val destructType: Type?
    ) : Expression() {
        override fun accept(v: Visitor) {
            v.visitLambdaParam(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (vr in vars) {
                vr?.accept(v)
            }
            destructType?.accept(v)
        }
    }

    override fun accept(v: Visitor) {
        v.visitLambda(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (param in params) {
            param.accept(v)
        }
        block?.accept(v)
    }
}

data class ThisReference(
    override val locus: Locus,
    val label: String?
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitThis(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class SuperReference(
    override val locus: Locus,
    val typeArg: Type?,
    val label: String?
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitSuper(this)
    }

    override fun acceptChildren(v: Visitor) {
        typeArg?.accept(v)
    }
}

data class WhenExpression(
    override val locus: Locus,
    val subject: Expression?,
    val entries: List<Entry>
) : Expression() {
    data class Entry(
        override val locus: Locus,
        val conds: List<Condition>,
        val body: Expression
    ) : Element() {
        override fun accept(v: Visitor) {
            v.visitWhenEntry(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (cond in conds) {
                cond.accept(v)
            }
            body.accept(v)
        }
    }

    sealed class Condition : Element()
    data class ExpressionCondition(val expr: Expression) : Condition() {
        override val locus: Locus
            get() = expr.locus

        override fun accept(v: Visitor) {
            v.visitWhenExprCond(this)
        }

        override fun acceptChildren(v: Visitor) {
            expr.accept(v)
        }
    }

    data class InCondition(
        override val locus: Locus,
        val expr: Expression,
        val not: Boolean
    ) : Condition() {
        override fun accept(v: Visitor) {
            v.visitWhenInCond(this)
        }

        override fun acceptChildren(v: Visitor) {
            expr.accept(v)
        }
    }

    data class IsCondition(
        override val locus: Locus,
        val type: Type,
        val not: Boolean
    ) : Condition() {
        override fun accept(v: Visitor) {
            v.visitWhenIsCond(this)
        }

        override fun acceptChildren(v: Visitor) {
            type.accept(v)
        }
    }

    override fun accept(v: Visitor) {
        v.visitWhen(this)
    }

    override fun acceptChildren(v: Visitor) {
        subject?.accept(v)
        for (entry in entries) {
            entry.accept(v)
        }
    }
}

data class ObjectDeclaration(
    override val locus: Locus,
    val parents: List<ClassDeclaration.Parent>,
    val members: List<Declaration>
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitObjectExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (parent in parents) {
            parent.accept(v)
        }
        for (member in members) {
            member.accept(v)
        }
    }
}

data class ThrowStatement(
    override val locus: Locus,
    val expr: Expression
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitThrowExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        expr.accept(v)
    }
}

data class ReturnStatement(
    override val locus: Locus,
    val label: String?,
    val expr: Expression?
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitReturnExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        expr?.accept(v)
    }
}

data class ContinueStatement(
    override val locus: Locus,
    val label: String?
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitContinueExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class BreakStatement(
    override val locus: Locus,
    val label: String?
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitBreakExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class CollectionLiteral(
    override val locus: Locus,
    val exprs: List<Expression>
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitCollectionLiteralExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (expr in exprs) {
            expr.accept(v)
        }
    }
}

data class Identifier(
    override val locus: Locus,
    val name: String
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitNameExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}

data class LabelledExpression(
    override val locus: Locus,
    val label: String,
    val expr: Expression
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitLabelledExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        expr.accept(v)
    }
}

data class AnnotatedExpression(
    override val locus: Locus,
    override val anns: List<Annotation>,
    val expr: Expression
) : Expression(), Element.WithAnnotations {
    override fun accept(v: Visitor) {
        v.visitAnnotatedExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (ann in anns) {
            ann.accept(v)
        }
        expr.accept(v)
    }
}

data class CallExpression(
    override val locus: Locus,
    val expr: Expression,
    val typeArgs: List<Type?>,
    val arguments: List<ValueArgument>,
    val lambda: TrailingLambda?
) : Expression() {
    data class TrailingLambda(
        override val locus: Locus,
        override val anns: List<Annotation>,
        val label: String?,
        val func: LambdaLiteral
    ) : Element(), WithAnnotations {
        override fun accept(v: Visitor) {
            v.visitTrailingLambda(this)
        }

        override fun acceptChildren(v: Visitor) {
            for (ann in anns) {
                ann.accept(v)
            }
            func.accept(v)
        }
    }

    override fun accept(v: Visitor) {
        v.visitCallExpr(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (typeArg in typeArgs) {
            typeArg?.accept(v)
        }
        for (arg in arguments) {
            arg.accept(v)
        }
        lambda?.accept(v)
    }
}

data class ArrayAccessExpression(
    override val locus: Locus,
    val expr: Expression,
    val indices: List<Expression>
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitArrayAccess(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (index in indices) {
            index.accept(v)
        }
    }
}

data class AnonymousFunctionExpression(
    override val locus: Locus,
    val function: FunctionDeclaration
) : Expression() {
    override fun accept(v: Visitor) {
        v.visitAnonymousFunction(this)
    }

    override fun acceptChildren(v: Visitor) {
        function.accept(v)
    }
}

// This is only present for when expressions and labeled expressions
data class PropertyExpression(
    val declaration: PropertyDeclaration
) : Expression() {
    override val locus: Locus
        get() = declaration.locus

    override fun accept(v: Visitor) {
        v.visitPropertyExpression(this)
    }

    override fun acceptChildren(v: Visitor) {
        declaration.accept(v)
    }
}

data class Block(
    override val locus: Locus,
    val stmts: List<Expression>
) : Element() {
    override fun accept(v: Visitor) {
        v.visitBlock(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (stmt in stmts) {
            stmt.accept(v)
        }
    }
}

sealed class ModifierOrAnnotation : Element()
data class Annotation(
    override val locus: Locus,
    val name: FqName,
    val target: Target?,
    val typeArgs: List<Type>,
    val arguments: List<ValueArgument>
) : ModifierOrAnnotation() {
    enum class Target {
        FIELD, FILE, PROPERTY, GET, SET, RECEIVER, PARAM, SETPARAM, DELEGATE
    }

    override fun accept(v: Visitor) {
        v.visitAnnotation(this)
    }

    override fun acceptChildren(v: Visitor) {
        for (typeArg in typeArgs) {
            typeArg.accept(v)
        }
        for (arg in arguments) {
            arg.accept(v)
        }
    }
}

data class Modifier(
    override val locus: Locus,
    val keyword: Keyword
) : ModifierOrAnnotation() {
    enum class Keyword {
        ABSTRACT, FINAL, OPEN, ANNOTATION, SEALED, DATA, OVERRIDE, LATEINIT, INNER,
        PRIVATE, PROTECTED, PUBLIC, INTERNAL,
        IN, OUT, NOINLINE, CROSSINLINE, VARARG, REIFIED,
        TAILREC, OPERATOR, INFIX, INLINE, EXTERNAL, SUSPEND, CONST,
        ACTUAL, EXPECT
    }

    override fun accept(v: Visitor) {
        v.visitIdentifierLikeModifier(this)
    }

    override fun acceptChildren(v: Visitor) {
        // no children
    }
}
