package org.stella.context;

import org.stella.visitors.Visitors;
import org.syntax.stella.Absyn.*;

import java.util.HashMap;
import java.util.Map;

public class Context {
    public final Map<String, DeclFun> functions = new HashMap<>();
    public final Map<String, Type> vars = new HashMap<>();

    public DeclFun currentFun = null;

    public Context copy() {
        Context context = new Context();
        context.functions.putAll(functions);
        context.vars.putAll(vars);
        context.currentFun = currentFun;
        return context;
    }

    public Context withFunction(Abstraction function) {
        Context context = copy();
        for (ParamDecl paramDecl : function.listparamdecl_) {
            context.vars.putAll(paramDecl.accept(new Visitors.ParamDeclVisitor(), context));
        }
        return context;
    }
}
