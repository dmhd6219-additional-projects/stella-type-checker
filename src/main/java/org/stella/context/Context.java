package org.stella.context;

import org.stella.visitors.Visitors;
import org.syntax.stella.Absyn.Abstraction;
import org.syntax.stella.Absyn.DeclFun;
import org.syntax.stella.Absyn.ParamDecl;
import org.syntax.stella.Absyn.Type;

import java.util.*;

public class Context {
    public final Map<String, DeclFun> functions = new HashMap<>();
    public final Map<String, Type> vars = new HashMap<>();
    public final Set<String> extensions = new HashSet<>();

    public DeclFun currentFun = null;

    public Context copy() {
        Context context = new Context();
        context.functions.putAll(functions);
        context.vars.putAll(vars);
        context.currentFun = currentFun;
        context.extensions.addAll(extensions);
        return context;
    }

    public Context withFunction(Abstraction function) {
        Context context = copy();
        for (ParamDecl paramDecl : function.listparamdecl_) {
            context.vars.putAll(paramDecl.accept(new Visitors.ParamDeclVisitor(), context));
        }
        return context;
    }

    public Context withVariables(Map<String, Type> variables) {
        Context context = copy();
        context.vars.putAll(variables);
        return context;
    }
}
