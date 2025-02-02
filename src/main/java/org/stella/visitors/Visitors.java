package org.stella.visitors;

import org.stella.context.Context;
import org.stella.typecheck.StellaException;
import org.syntax.stella.Absyn.*;
import org.syntax.stella.Absyn.Record;
import org.syntax.stella.PrettyPrinter;

import java.util.HashMap;
import java.util.Map;

public class Visitors {
    public class ProgramVisitor implements Program.Visitor<Context, Context> {
        @Override
        public Context visit(AProgram p, Context ctx) {
            Context newContext = new Context();

            for (Extension extension : p.listextension_) {
                newContext.extensions.addAll(extension.accept(new ExtensionVisitor(), newContext));
            }

            for (Decl decl : p.listdecl_) {
                decl.accept(new DeclVisitor(), newContext);
            }

            if (!newContext.functions.containsKey("main")) {
                throw new StellaException("ERROR_MISSING_MAIN", "Main function not found");
            }
            if (newContext.functions.get("main").listparamdecl_.size() != 1) {
                throw new StellaException("ERROR_INCORRECT_ARITY_OF_MAIN", "Main function contains" + newContext.functions.get("main").listparamdecl_.size() + "but needed only 1");
            }

            return newContext;
        }
    }

    public class DeclVisitor implements Decl.Visitor<Object, Context> {
        @Override
        public Object visit(DeclFun p, Context ctx) {
            ctx.functions.put(p.stellaident_, p);

            Context newContext = ctx.copy();

            if (p.listparamdecl_.size() != ctx.functions.get(p.stellaident_).listparamdecl_.size()) {
                throw new StellaException("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS", "Parameter count mismatch");
            }

            if (p.listparamdecl_.isEmpty()) {
                checkExtension("#nullary-functions", newContext);
            }

            if (p.listparamdecl_.size() > 1) {
                checkExtension("#multiparameter-functions", newContext);
            }

            for (ParamDecl param : p.listparamdecl_) {
                newContext.vars.putAll(param.accept(new ParamDeclVisitor(), newContext));
            }

            for (Decl fun : p.listdecl_) {
                checkExtension("#nested-function-declarations", newContext);
                fun.accept(new DeclVisitor(), newContext);
            }

            Type expected = p.returntype_.accept(new ReturnTypeVisitor(), ctx);
            Type type = p.expr_.accept(new ExprVisitor(), newContext);

            if (!(expected instanceof TypeFun) && (p.expr_ instanceof Abstraction)) {
                throw new StellaException("ERROR_UNEXPECTED_LAMBDA",
                        String.format("Expected: %s, Got: %s", PrettyPrinter.print(expected), PrettyPrinter.print(type))
                );

            }

            if (expected instanceof TypeFun expectedFun){
                if (type instanceof TypeFun returnTypeFun){
                    if (expectedFun.listtype_.size() != returnTypeFun.listtype_.size()) {
                        throw new StellaException("ERROR_UNEXPECTED_NUMBER_OF_PARAMETERS_IN_LAMBDA",
                                String.format("Expected: %s, Got: %s", expectedFun.listtype_.size(), returnTypeFun.listtype_.size())
                        );
                    }

                    if (!expectedFun.listtype_.equals(returnTypeFun.listtype_)) {
                        throw new StellaException("ERROR_UNEXPECTED_TYPE_FOR_PARAMETER",
                                String.format("Expected: %s, Got: %s", PrettyPrinter.print(expectedFun.listtype_), PrettyPrinter.print(returnTypeFun.listtype_))
                        );
                    }

                }
            }

            checkType(
                    expected,
                    type,
                    newContext
            );

            return null;
        }

        @Override
        public Object visit(DeclFunGeneric p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "DeclFunGeneric not implemented");
        }

        @Override
        public Object visit(DeclTypeAlias p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "DeclTypeAlias not implemented");
        }

        @Override
        public Object visit(DeclExceptionType p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "DeclExceptionType not implemented");
        }

        @Override
        public Object visit(DeclExceptionVariant p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "DeclExceptionVariant not implemented");
        }
    }

    // returns context copy with new declared vars
    public static class ParamDeclVisitor implements ParamDecl.Visitor<Map<String, Type>, Context> {
        @Override
        public Map<String, Type> visit(AParamDecl p, Context arg) {
            Map<String, Type> map = new HashMap<>();
            map.put(p.stellaident_, p.type_);
            if (p.type_ instanceof TypeTuple typeTuple) {
                if (typeTuple.listtype_.size() == 2) {
                    checkExtension("#pairs", arg);
                } else {
                    checkExtension("#tuples", arg);
                }
            }

            return map;
        }
    }

    public class ExprVisitor implements Expr.Visitor<Type, Context> {
        @Override
        public Type visit(Sequence p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Sequence not implemented");
        }

        @Override
        public Type visit(Assign p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Assign not implemented");
        }

        @Override
        public Type visit(If p, Context ctx) {
            checkType(new TypeBool(), p.expr_1.accept(this, ctx), ctx);
            checkType(p.expr_2.accept(this, ctx), p.expr_3.accept(this, ctx), ctx);

            return p.expr_2.accept(this, ctx);
        }

        @Override
        public Type visit(Let p, Context ctx) {
            checkExtension("#let-bindings", ctx);
            Map<String, Type> lhs = new HashMap<>();
            for (PatternBinding patternBinding : p.listpatternbinding_) {
                lhs.putAll(patternBinding.accept(new PatternBindingVisitor(), ctx));
            }

            return p.expr_.accept(this, ctx.withVariables(lhs));
        }

        @Override
        public Type visit(LetRec p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "LetRec not implemented");
        }

        @Override
        public Type visit(TypeAbstraction p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "TypeAbstraction not implemented");
        }

        @Override
        public Type visit(LessThan p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "LessThan not implemented");
        }

        @Override
        public Type visit(LessThanOrEqual p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "LessThanOrEqual not implemented");
        }

        @Override
        public Type visit(GreaterThan p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "GreaterThan not implemented");
        }

        @Override
        public Type visit(GreaterThanOrEqual p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "GreaterThanOrEqual not implemented");
        }

        @Override
        public Type visit(Equal p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Equal not implemented");
        }

        @Override
        public Type visit(NotEqual p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "NotEqual not implemented");
        }

        @Override
        public Type visit(TypeAsc p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "TypeAsc not implemented");
        }

        @Override
        public Type visit(TypeCast p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "TypeCast not implemented");
        }

        @Override
        public Type visit(Abstraction p, Context ctx) {
            Context newContext = ctx.withFunction(p);
            Type exprType = p.expr_.accept(this, newContext);

            ListType paramTypes = new ListType();
            for (ParamDecl param : p.listparamdecl_) {
                newContext.vars.putAll(param.accept(new ParamDeclVisitor(), newContext));
                for (Map.Entry<String, Type> entry : param.accept(new ParamDeclVisitor(), newContext).entrySet()) {
                    paramTypes.add(entry.getValue());
                }
            }
            return new TypeFun(paramTypes, exprType);
        }

        @Override
        public Type visit(Variant p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Variant not implemented");
        }

        @Override
        public Type visit(Match p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Match not implemented");
        }

        @Override
        public Type visit(List p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "List not implemented");
        }

        @Override
        public Type visit(Add p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Add not implemented");
        }

        @Override
        public Type visit(Subtract p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Subtract not implemented");
        }

        @Override
        public Type visit(LogicOr p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "LogicOr not implemented");
        }

        @Override
        public Type visit(Multiply p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Multiply not implemented");
        }

        @Override
        public Type visit(Divide p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Divide not implemented");
        }

        @Override
        public Type visit(LogicAnd p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "LogicAnd not implemented");
        }

        @Override
        public Type visit(Ref p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Ref not implemented");
        }

        @Override
        public Type visit(Deref p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Deref not implemented");
        }

        @Override
        public Type visit(Application p, Context ctx) {
            Type type = p.expr_.accept(this, ctx);
            if (!(type instanceof TypeFun typeFun)) {
                throw new StellaException("ERROR_NOT_A_FUNCTION", PrettyPrinter.print(p.expr_));
            }

            if (p.listexpr_.size() != typeFun.listtype_.size()) {
                throw new StellaException("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS", PrettyPrinter.print(p.expr_));
            }

            for (int i = 0; i < p.listexpr_.size(); i++) {
                checkType(typeFun.listtype_.get(i), p.listexpr_.get(i).accept(this, ctx), ctx);
            }

            return typeFun.type_;
        }

        @Override
        public Type visit(TypeApplication p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "TypeApplication not implemented");
        }

        @Override
        public Type visit(DotRecord p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "DotRecord not implemented");
        }

        @Override
        public Type visit(DotTuple p, Context ctx) {
            Type type = p.expr_.accept(this, ctx);
            if (!(type instanceof TypeTuple typeTuple)) {
                throw new StellaException("ERROR_NOT_A_TUPLE", "");
            }

            Integer index = p.integer_;
            if (index > typeTuple.listtype_.size()) {
                throw new StellaException("ERROR_TUPLE_INDEX_OUT_OF_BOUNDS", "");
            }

            return typeTuple.listtype_.get(index - 1);
        }

        @Override
        public Type visit(Tuple p, Context ctx) {
            if (p.listexpr_.size() == 2) {
                checkExtension("#pairs", ctx);
            } else {
                checkExtension("#tuples", ctx);
            }

            ListType listType = new ListType();
            for (Expr listExpr : p.listexpr_) {
                listType.add(listExpr.accept(this, ctx));
            }
            return new TypeTuple(listType);
        }

        @Override
        public Type visit(Record p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Record not implemented");
        }

        @Override
        public Type visit(ConsList p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "ConsList not implemented");
        }

        @Override
        public Type visit(Head p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Head not implemented");
        }

        @Override
        public Type visit(IsEmpty p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "IsEmpty not implemented");
        }

        @Override
        public Type visit(Tail p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Tail not implemented");
        }

        @Override
        public Type visit(Panic p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Panic not implemented");
        }

        @Override
        public Type visit(Throw p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Throw not implemented");
        }

        @Override
        public Type visit(TryCatch p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "TryCatch not implemented");
        }

        @Override
        public Type visit(TryWith p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "TryWith not implemented");
        }

        @Override
        public Type visit(TryCastAs p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "TryCastAs not implemented");
        }

        @Override
        public Type visit(Inl p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Inl not implemented");
        }

        @Override
        public Type visit(Inr p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Inr not implemented");
        }

        @Override
        public Type visit(Succ p, Context ctx) {
            checkType(new TypeNat(), p.expr_.accept(this, ctx), ctx);
            return new TypeNat();
        }

        @Override
        public Type visit(LogicNot p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "LogicNot not implemented");
        }

        @Override
        public Type visit(Pred p, Context ctx) {
            checkType(new TypeNat(), p.expr_.accept(this, ctx), ctx);
            return new TypeNat();
        }

        @Override
        public Type visit(IsZero p, Context ctx) {
            checkType(new TypeNat(), p.expr_.accept(this, ctx), ctx);
            return new TypeBool();
        }

        @Override
        public Type visit(Fix p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Fix not implemented");
        }

        @Override
        public Type visit(NatRec p, Context ctx) {
            checkType(new TypeNat(), p.expr_1.accept(this, ctx), ctx);

            Type type2 = p.expr_2.accept(this, ctx);

            ListType listType1 = new ListType();
            listType1.add(new TypeNat());

            ListType listType2 = new ListType();
            listType2.add(type2);

            Type type1 = new TypeFun(listType1, new TypeFun(listType2, type2));
            Type type3 = p.expr_3.accept(this, ctx);

            checkType(type1, type3, ctx);

            return type2;
        }

        @Override
        public Type visit(Fold p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Fold not implemented");
        }

        @Override
        public Type visit(Unfold p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "Unfold not implemented");
        }

        @Override
        public Type visit(ConstTrue p, Context ctx) {
            return new TypeBool();
        }

        @Override
        public Type visit(ConstFalse p, Context ctx) {
            return new TypeBool();
        }

        @Override
        public Type visit(ConstUnit p, Context ctx) {
            checkExtension("#unit-type", ctx);
            return new TypeUnit();
        }

        @Override
        public Type visit(ConstInt p, Context ctx) {
            if (p.integer_ != 0) {
                checkExtension("#natural-literals", ctx);
            }

            if (p.integer_ < 0) {
                throw new StellaException("ERROR_ILLEGAL_NEGATIVE_LITERAL", "");
            }

            return new TypeNat();
        }

        @Override
        public Type visit(ConstMemory p, Context ctx) {
            throw new StellaException("ERROR_NOT_IMPLEMENTED", "ConstMemory not implemented");
        }

        @Override
        public Type visit(Var p, Context ctx) {
            if (ctx.vars.containsKey(p.stellaident_)) {
                return ctx.vars.get(p.stellaident_);
            }

            if (ctx.functions.containsKey(p.stellaident_)) {
                DeclFun declFun = ctx.functions.get(p.stellaident_);
                ListType listType = new ListType();
                for (ParamDecl param : declFun.listparamdecl_) {
                    Map<String, Type> map = param.accept(new ParamDeclVisitor(), ctx);
                    listType.addAll(map.values());
                }
                return new TypeFun(listType, declFun.returntype_.accept(new ReturnTypeVisitor(), ctx));
            }

            throw new StellaException("ERROR_UNDEFINED_VARIABLE", p.stellaident_);
        }
    }

    public class ReturnTypeVisitor implements ReturnType.Visitor<Type, Context> {
        @Override
        public Type visit(NoReturnType p, Context ctx) {
            return ctx.currentFun.expr_.accept(new ExprVisitor(), ctx);
        }

        @Override
        public Type visit(SomeReturnType p, Context ctx) {
            return p.type_;
        }
    }

    public class PatternBindingVisitor implements PatternBinding.Visitor<Map<String, Type>, Context> {
        @Override
        public Map<String, Type> visit(APatternBinding p, Context ctx) {
            Map<String, Type> map = new HashMap<>();
            PatternVar pattern = (PatternVar) p.pattern_;
            map.put(pattern.stellaident_, p.expr_.accept(new ExprVisitor(), ctx));
            return map;
        }
    }

    public static class ExtensionVisitor implements Extension.Visitor<ListExtensionName, Context> {
        @Override
        public ListExtensionName visit(AnExtension p, Context ctx) {
            return p.listextensionname_;
        }
    }

    public void checkType(Type expected, Type type, Context ctx) {
        if (!expected.equals(type)) {
            if (expected instanceof TypeTuple tupleExpected && type instanceof TypeTuple typeTuple) {
                if (tupleExpected.listtype_.size() != typeTuple.listtype_.size()) {
                    throw new StellaException("ERROR_UNEXPECTED_TUPLE_LENGTH",
                            String.format("Expected: %s, Got: %s", tupleExpected.listtype_.size(), typeTuple.listtype_.size()));
                }
            }

            throw new StellaException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION",
                    PrettyPrinter.print(expected) + " | " + PrettyPrinter.print(type));
        }

        if (expected instanceof TypeUnit || type instanceof TypeUnit) {
            checkExtension("#unit-type", ctx);
        }

        if (expected instanceof TypeTuple expectedTuple) {
            if (expectedTuple.listtype_.size() == 2) {
                checkExtension("#pairs", ctx);
            } else {
                checkExtension("#tuples", ctx);
            }
        }
    }

    public static void checkExtension(String extension, Context context) {
        System.out.println(context);
        if (!context.extensions.contains(extension)) {
            if (extension.equals("#pairs")) {
                checkExtension("#tuples", context);
                return;
            }

            throw new StellaException("ERROR_EXTENSION_IS_NOT_ENABLED", extension);
        }
    }
}