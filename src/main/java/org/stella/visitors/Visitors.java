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
        public Context visit(AProgram p, Context context) {
            Context ctx = new Context();

            for (Decl decl : p.listdecl_) {
                decl.accept(new DeclVisitor(), ctx);
            }

            if (!ctx.functions.containsKey("main")) {
                throw new StellaException("ERROR_MISSING_MAIN", "Main function not found");
            }

            return ctx;
        }
    }

    public class DeclVisitor implements Decl.Visitor<Object, Context> {
        @Override
        public Object visit(DeclFun p, Context arg) {
            arg.functions.put(p.stellaident_, p);

            Context ctx = arg.copy();

            if (p.listparamdecl_.size() != arg.functions.get(p.stellaident_).listparamdecl_.size()) {
                throw new StellaException("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS", "Parameter count mismatch");
            }

            for (ParamDecl param : p.listparamdecl_) {
                ctx.vars.putAll(param.accept(new ParamDeclVisitor(), ctx));
            }

            // TODO: check extension enabled
            for (Decl fun : p.listdecl_) {
                fun.accept(new DeclVisitor(), ctx);
            }

            checkType(
                    p.returntype_.accept(new ReturnTypeVisitor(), arg),
                    p.expr_.accept(new ExprVisitor(), ctx)
            );

            return null;
        }

        @Override
        public Object visit(DeclFunGeneric p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "DeclFunGeneric not implemented");
        }

        @Override
        public Object visit(DeclTypeAlias p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "DeclTypeAlias not implemented");
        }

        @Override
        public Object visit(DeclExceptionType p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "DeclExceptionType not implemented");
        }

        @Override
        public Object visit(DeclExceptionVariant p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "DeclExceptionVariant not implemented");
        }
    }

    // returns context copy with new declared vars
    public static class ParamDeclVisitor implements ParamDecl.Visitor<Map<String, Type>, Context> {
        @Override
        public Map<String, Type> visit(AParamDecl p, Context arg) {
            Map<String, Type> map = new HashMap<>();
            map.put(p.stellaident_, p.type_);
            return map;
        }
    }

    public class ExprVisitor implements Expr.Visitor<Type, Context> {
        @Override
        public Type visit(Sequence p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Sequence not implemented");
        }

        @Override
        public Type visit(Assign p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Assign not implemented");
        }

        @Override
        public Type visit(If p, Context arg) {
            checkType(new TypeBool(), p.expr_1.accept(this, arg));
            checkType(p.expr_2.accept(this, arg), p.expr_3.accept(this, arg));

            return p.expr_2.accept(this, arg);
        }

        @Override
        public Type visit(Let p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Let not implemented");
        }

        @Override
        public Type visit(LetRec p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "LetRec not implemented");
        }

        @Override
        public Type visit(TypeAbstraction p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "TypeAbstraction not implemented");
        }

        @Override
        public Type visit(LessThan p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "LessThan not implemented");
        }

        @Override
        public Type visit(LessThanOrEqual p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "LessThanOrEqual not implemented");
        }

        @Override
        public Type visit(GreaterThan p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "GreaterThan not implemented");
        }

        @Override
        public Type visit(GreaterThanOrEqual p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "GreaterThanOrEqual not implemented");
        }

        @Override
        public Type visit(Equal p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Equal not implemented");
        }

        @Override
        public Type visit(NotEqual p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "NotEqual not implemented");
        }

        @Override
        public Type visit(TypeAsc p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "TypeAsc not implemented");
        }

        @Override
        public Type visit(TypeCast p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "TypeCast not implemented");
        }

        @Override
        public Type visit(Abstraction p, Context arg) {
            Context ctx = arg.withFunction(p);
            Type exprType = p.expr_.accept(this, ctx);

            ListType paramTypes = new ListType();
            for (ParamDecl param : p.listparamdecl_) {
                ctx.vars.putAll(param.accept(new ParamDeclVisitor(), ctx));
                for (Map.Entry<String, Type> entry : param.accept(new ParamDeclVisitor(), ctx).entrySet()) {
                    paramTypes.add(entry.getValue());
                }
            }
            return new TypeFun(paramTypes, exprType);
        }

        @Override
        public Type visit(Variant p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Variant not implemented");
        }

        @Override
        public Type visit(Match p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Match not implemented");
        }

        @Override
        public Type visit(List p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "List not implemented");
        }

        @Override
        public Type visit(Add p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Add not implemented");
        }

        @Override
        public Type visit(Subtract p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Subtract not implemented");
        }

        @Override
        public Type visit(LogicOr p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "LogicOr not implemented");
        }

        @Override
        public Type visit(Multiply p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Multiply not implemented");
        }

        @Override
        public Type visit(Divide p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Divide not implemented");
        }

        @Override
        public Type visit(LogicAnd p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "LogicAnd not implemented");
        }

        @Override
        public Type visit(Ref p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Ref not implemented");
        }

        @Override
        public Type visit(Deref p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Deref not implemented");
        }

        @Override
        public Type visit(Application p, Context arg) {
            Type type = p.expr_.accept(this, arg);
            if (!(type instanceof TypeFun typeFun)) {
                throw new StellaException("ERROR_NOT_A_FUNCTION", PrettyPrinter.print(p.expr_));
            }

            if (p.listexpr_.size() != typeFun.listtype_.size()) {
                throw new StellaException("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS", PrettyPrinter.print(p.expr_));
            }

            for (int i = 0; i < p.listexpr_.size(); i++) {
                checkType(typeFun.listtype_.get(i), p.listexpr_.get(i).accept(this, arg));
            }

            return typeFun.type_;
        }

        @Override
        public Type visit(TypeApplication p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "TypeApplication not implemented");
        }

        @Override
        public Type visit(DotRecord p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "DotRecord not implemented");
        }

        @Override
        public Type visit(DotTuple p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "DotTuple not implemented");
        }

        @Override
        public Type visit(Tuple p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Tuple not implemented");
        }

        @Override
        public Type visit(Record p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Record not implemented");
        }

        @Override
        public Type visit(ConsList p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "ConsList not implemented");
        }

        @Override
        public Type visit(Head p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Head not implemented");
        }

        @Override
        public Type visit(IsEmpty p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "IsEmpty not implemented");
        }

        @Override
        public Type visit(Tail p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Tail not implemented");
        }

        @Override
        public Type visit(Panic p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Panic not implemented");
        }

        @Override
        public Type visit(Throw p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Throw not implemented");
        }

        @Override
        public Type visit(TryCatch p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "TryCatch not implemented");
        }

        @Override
        public Type visit(TryWith p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "TryWith not implemented");
        }

        @Override
        public Type visit(TryCastAs p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "TryCastAs not implemented");
        }

        @Override
        public Type visit(Inl p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Inl not implemented");
        }

        @Override
        public Type visit(Inr p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Inr not implemented");
        }

        @Override
        public Type visit(Succ p, Context arg) {
            checkType(new TypeNat(), p.expr_.accept(this, arg));
            return new TypeNat();
        }

        @Override
        public Type visit(LogicNot p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "LogicNot not implemented");
        }

        @Override
        public Type visit(Pred p, Context arg) {
            checkType(new TypeNat(), p.expr_.accept(this, arg));
            return new TypeNat();
        }

        @Override
        public Type visit(IsZero p, Context arg) {
            checkType(new TypeNat(), p.expr_.accept(this, arg));
            return new TypeBool();
        }

        @Override
        public Type visit(Fix p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Fix not implemented");
        }

        @Override
        public Type visit(NatRec p, Context arg) {
            checkType(new TypeNat(), p.expr_1.accept(this, arg));

            Type type2 = p.expr_2.accept(this, arg);

            ListType listType1 = new ListType();
            listType1.add(new TypeNat());

            ListType listType2 = new ListType();
            listType2.add(type2);

            Type type1 = new TypeFun(listType1, new TypeFun(listType2, type2));
            Type type3 = p.expr_3.accept(this, arg);

            checkType(type1, type3);

            return type2;
        }

        @Override
        public Type visit(Fold p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Fold not implemented");
        }

        @Override
        public Type visit(Unfold p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "Unfold not implemented");
        }

        @Override
        public Type visit(ConstTrue p, Context arg) {
            return new TypeBool();
        }

        @Override
        public Type visit(ConstFalse p, Context arg) {
            return new TypeBool();
        }

        @Override
        public Type visit(ConstUnit p, Context arg) {
            return new TypeUnit();
        }

        @Override
        public Type visit(ConstInt p, Context arg) {
            // TODO: check extension
            if (p.integer_ < 0) {
                throw new StellaException("ERROR_ILLEGAL_NEGATIVE_LITERAL", "");
            }
            return new TypeNat();
        }

        @Override
        public Type visit(ConstMemory p, Context arg) {
            throw new StellaException("NOT IMPLEMENTED", "ConstMemory not implemented");
        }

        @Override
        public Type visit(Var p, Context arg) {
            if (arg.vars.containsKey(p.stellaident_)) {
                return arg.vars.get(p.stellaident_);
            }

            if (arg.functions.containsKey(p.stellaident_)) {
                DeclFun declFun = arg.functions.get(p.stellaident_);
                ListType listType = new ListType();
                for (ParamDecl param : declFun.listparamdecl_) {
                    Map<String, Type> map = param.accept(new ParamDeclVisitor(), new Context());
                    listType.addAll(map.values());
                }
                return new TypeFun(listType, declFun.returntype_.accept(new ReturnTypeVisitor(), arg));
            }

            throw new StellaException("ERROR_UNDEFINED_VARIABLE", p.stellaident_);
        }
    }

    public class ReturnTypeVisitor implements ReturnType.Visitor<Type, Context> {
        @Override
        public Type visit(NoReturnType p, Context arg) {
            return arg.currentFun.expr_.accept(new ExprVisitor(), arg);
        }

        @Override
        public Type visit(SomeReturnType p, Context arg) {
            return p.type_;
        }
    }

    public void checkType(Type expected, Type type) {
        if (!expected.equals(type)) {
            throw new StellaException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION",
                    PrettyPrinter.print(expected) + " | " + PrettyPrinter.print(type));
        }
    }
}