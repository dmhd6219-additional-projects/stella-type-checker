package org.stella.context;

import org.syntax.stella.Absyn.Decl;
import org.syntax.stella.Absyn.DeclFun;
import org.syntax.stella.Absyn.Type;

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
}
