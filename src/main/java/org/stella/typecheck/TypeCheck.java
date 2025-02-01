package org.stella.typecheck;

import org.stella.context.Context;
import org.stella.visitors.Visitors;
import org.syntax.stella.Absyn.*;

public class TypeCheck
{
    public static void typecheckProgram(Program program) throws Exception {
        Visitors v = new Visitors();
        program.accept(v.new ProgramVisitor(), null);
    }
}
