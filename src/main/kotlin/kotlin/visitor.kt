package kometa.kotlin

import kometa.kotlin.ast.*
import kometa.kotlin.ast.Annotation

abstract class Visitor {
    abstract fun visitElement(element: Element)
    open fun visitFile(file: File) = visitElement(file)
    open fun visitScript(script: Script) = visitElement(script)
    open fun visitPackage(pkg: Package) = visitElement(pkg)
    open fun visitImport(imp: Import) = visitElement(imp)

    open fun visitDeclaration(decl: Declaration) = visitElement(decl)

    open fun visitClassDeclaration(clazz: ClassDeclaration) = visitDeclaration(clazz)
    open fun visitClassParent(classParent: ClassDeclaration.Parent) = visitElement(classParent)
    open fun visitSuperClassConstructorCall(call: ClassDeclaration.SuperClassConstructorCall) = visitClassParent(call)
    open fun visitSuperInterfaceType(type: ClassDeclaration.SuperInterface) = visitClassParent(type)
    open fun visitPrimaryConstructor(constructor: ClassDeclaration.PrimaryConstructor) = visitElement(constructor)
    open fun visitInitBlock(init: ClassDeclaration.InitBlock) = visitDeclaration(init)

    open fun visitFunction(func: FunctionDeclaration) = visitDeclaration(func)
    open fun visitValueParameter(param: ValueParameter) = visitElement(param)
    open fun visitFunctionBody(body: FunctionBody) = visitElement(body)
    open fun visitBlockBody(body: BlockBody) = visitFunctionBody(body)
    open fun visitExpressionBody(body: ExpressionBody) = visitFunctionBody(body)

    open fun visitPropertyDeclaration(property: PropertyDeclaration) = visitDeclaration(property)
    open fun visitDestructuringEntry(propVar: PropertyDeclaration.DestructuringEntry) = visitElement(propVar)
    open fun visitPropertyGetter(getter: PropertyDeclaration.Getter) = visitElement(getter)
    open fun visitPropertySetter(setter: PropertyDeclaration.Setter) = visitElement(setter)

    open fun visitTypeAliasDeclaration(typeAlias: TypeAliasDeclaration) = visitDeclaration(typeAlias)

    open fun visitConstructor(constructor: ClassDeclaration.Constructor) = visitDeclaration(constructor)
    open fun visitConstructorDelegationCall(call: ClassDeclaration.Constructor.DelegationCall) = visitElement(call)

    open fun visitEnumEntry(entry: ClassDeclaration.EnumEntry) = visitDeclaration(entry)

    open fun visitTypeParameter(param: TypeParameter) = visitElement(param)
    open fun visitTypeConstraint(constraint: TypeConstraint) = visitElement(constraint)

    open fun visitTypeRef(ref: TypeRef) = visitElement(ref)
    open fun visitFunctionalType(type: FunctionalType) = visitTypeRef(type)
    open fun visitFunctionalTypeParameter(param: FunctionalType.Parameter) = visitElement(param)
    open fun visitSimpleType(type: SimpleType) = visitTypeRef(type)
    open fun visitSimpleTypePiece(type: SimpleType.Piece) = visitElement(type)
    open fun visitNullableType(type: NullableType) = visitTypeRef(type)
    open fun visitDynamicType(type: DynamicType) = visitTypeRef(type)

    open fun visitTypeOrWildcard(type: TypeOrWildcard) = visitElement(type)
    open fun visitType(type: Type) = visitTypeOrWildcard(type)
    open fun visitWildcard(wildcard: Wildcard) = visitTypeOrWildcard(wildcard)

    open fun visitValueArgument(arg: ValueArgument) = visitElement(arg)

    open fun visitExpression(expr: Expression) = visitElement(expr)
    open fun visitIfExpression(expr: IfExpression) = visitExpression(expr)
    open fun visitTryExpression(expr: TryExpression) = visitExpression(expr)
    open fun visitCatch(catch: TryExpression.Catch) = visitElement(catch)
    open fun visitForLoop(stmt: ForLoop) = visitExpression(stmt)
    open fun visitWhileLoop(stmt: WhileLoop) = visitExpression(stmt)
    open fun visitDoWhileLoop(stmt: DoWhileLoop) = visitExpression(stmt)
    open fun visitBinaryExpression(op: BinaryExpression) = visitExpression(op)
    open fun visitBinaryOperatorOrInfixCall(oper: BinaryOperatorOrInfixCall) = visitElement(oper)
    open fun visitInfixFunctionName(oper: InfixFunctionName) = visitBinaryOperatorOrInfixCall(oper)
    open fun visitBinaryOperator(oper: BinaryOperator) = visitBinaryOperatorOrInfixCall(oper)
    open fun visitUnaryExpression(expr: UnaryExpression) = visitExpression(expr)
    open fun visitUnaryOperator(oper: UnaryOperator) = visitElement(oper)
    open fun visitTypeExpression(expr: TypeExpression) = visitExpression(expr)
    open fun visitTypeOperator(oper: TypeOperator) = visitElement(oper)
    open fun visitReferenceExpression(ref: ReferenceExpression) = visitExpression(ref)
    open fun visitCallableReferenceExpression(callable: CallableReferenceExpression) = visitReferenceExpression(callable)
    open fun visitClassReferenceExpression(ref: ClassReferenceExpression) = visitReferenceExpression(ref)
    open fun visitReferenceReceiver(recv: ReferenceReceiver) = visitElement(recv)
    open fun visitReferenceExpressionReceiver(recv: ReferenceReceiver.ExpressionReceiver) = visitReferenceReceiver(recv)
    open fun visitReferenceTypeExpression(recv: ReferenceReceiver.Type) = visitReferenceReceiver(recv)
    open fun visitConstantExpression(const: ConstantExpression) = visitExpression(const)
    open fun visitLambdaLiteral(lambda: LambdaLiteral) = visitExpression(lambda)
    open fun visitLambdaParameter(param: LambdaLiteral.Parameter) = visitExpression(param)
    open fun visitThisReference(expr: ThisReference) = visitExpression(expr)
    open fun visitSuperReference(expr: SuperReference) = visitExpression(expr)
    open fun visitWhenExpression(expr: WhenExpression) = visitExpression(expr)
    open fun visitWhenEntry(entry: WhenExpression.Entry) = visitElement(entry)
    open fun visitWhenCondition(cond: WhenExpression.Condition) = visitElement(cond)
    open fun visitWhenExpressionCondition(cond: WhenExpression.ExpressionCondition) = visitWhenCondition(cond)
    open fun visitWhenInCondition(cond: WhenExpression.InCondition) = visitWhenCondition(cond)
    open fun visitWhenIsCondition(cond: WhenExpression.IsCondition) = visitWhenCondition(cond)
    open fun visitAnonymousObjectExpression(obj: AnonymousObjectExpression) = visitExpression(obj)
    open fun visitThrowStatement(expr: ThrowStatement) = visitExpression(expr)
    open fun visitReturnStatement(expr: ReturnStatement) = visitExpression(expr)
    open fun visitContinueStatement(expr: ContinueStatement) = visitExpression(expr)
    open fun visitBreakStatement(expr: BreakStatement) = visitExpression(expr)
    open fun visitCollectionLiteral(lit: CollectionLiteral) = visitExpression(lit)
    open fun visitIdentifier(name: Identifier) = visitExpression(name)
    open fun visitLabelledExpression(expr: LabelledExpression) = visitExpression(expr)
    open fun visitAnnotatedExpression(expr: AnnotatedExpression) = visitExpression(expr)
    open fun visitCallExpression(call: CallExpression) = visitExpression(call)
    open fun visitTrailingLambda(lambda: CallExpression.TrailingLambda) = visitElement(lambda)
    open fun visitArrayAccessExpression(expr: ArrayAccessExpression) = visitExpression(expr)
    open fun visitAnonymousFunctionExpression(func: AnonymousFunctionExpression) = visitExpression(func)
    open fun visitPropertyExpression(expr: PropertyExpression) = visitExpression(expr)

    open fun visitBlock(block: Block) = visitElement(block)
    open fun visitModifierOrAnnotation(mod: ModifierOrAnnotation) = visitElement(mod)
    open fun visitAnnotation(ann: Annotation) = visitModifierOrAnnotation(ann)
    open fun visitModifier(mod: Modifier) = visitModifierOrAnnotation(mod)
}