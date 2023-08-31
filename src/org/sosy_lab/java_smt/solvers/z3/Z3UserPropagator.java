/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sosy_lab.java_smt.solvers.z3;


import com.microsoft.z3.Native;
import org.sosy_lab.java_smt.api.Backend;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanTheorySolver;
import org.sosy_lab.java_smt.api.Formula;

public final class Z3UserPropagator extends Native.UserPropagatorBase implements Backend {
  private Z3FormulaCreator creator;
  private Z3FormulaManager manager;
  private BooleanTheorySolver theorySolver;

  // function calls from z3's side
  // (forwarding callbacks to the theory solver)
  @Override
  public void pushWrapper() {
    theorySolver.push();
  }

  @Override
  public void popWrapper(int num) {
    theorySolver.pop(num);
  }

  @Override
  public void finWrapper() {
    theorySolver.finish();
  }

  @Override
  public void eqWrapper(long lx, long ly) {
    theorySolver.equality(creator.encapsulateBoolean(lx), creator.encapsulateBoolean(ly));
  }

  // to function correctly, new prop and context are needed
  // currently, there is no support for delegating subproblems to new user propagators
  @Override
  public Z3UserPropagator freshWrapper(long lctx) {
    return this;
  }

  @Override
  public void createdWrapper(long le) {
    theorySolver.created(creator.encapsulateBoolean(le));
  }

  @Override
  public void fixedWrapper(long lvar, long lvalue) {
    theorySolver.fixed(creator.encapsulateBoolean(lvar), creator.encapsulateBoolean(lvalue));
  }

  public void decideWrapper(long expr, int i, int j) {}


  // function calls from java-smt's side (mostly calls to Backend)
  // (register events on z3's side)

  public Z3UserPropagator(long ctx, long solver, Z3FormulaCreator creator, Z3FormulaManager manager,
                          BooleanTheorySolver theorySolver) {
    super(ctx, solver);
    this.creator = creator;
    this.theorySolver = theorySolver;
    this.manager = manager;
  }

  @Override
  public void addExpressionToWatch(BooleanFormula toWatch) {
    Native.propagateAdd(this, ctx, solver, javainfo, creator.extractInfo(toWatch));
  }

  @Override
  public void addConflict(
      BooleanFormula[] fixed) {
    BooleanFormula conflict = manager.getBooleanFormulaManager().makeFalse();
    BooleanFormula[] empty = new BooleanFormula[0];
    Native.propagateConflict(this, ctx, solver, javainfo, fixed.length, formulaArrayToLong(fixed)
        , 0, formulaArrayToLong(empty), formulaArrayToLong(empty),
        creator.extractInfo(conflict));
  }

  @Override
  public void addConflictEq(
      BooleanFormula[] fixed, BooleanFormula[] lhs,
      BooleanFormula[] rhs) {
    BooleanFormula conflict = manager.getBooleanFormulaManager().makeFalse();
    Native.propagateConflict(this, ctx, solver, javainfo, fixed.length, formulaArrayToLong(fixed)
        , lhs.length, formulaArrayToLong(lhs), formulaArrayToLong(rhs),
        creator.extractInfo(conflict));
  }

  @Override
  public void addTheoryLemma(BooleanFormula[] fixed, BooleanFormula conflict) {
    BooleanFormula[] empty = new BooleanFormula[0];
    Native.propagateConflict(this, ctx, solver, javainfo, fixed.length, formulaArrayToLong(fixed)
        , 0, formulaArrayToLong(empty), formulaArrayToLong(empty), creator.extractInfo(conflict));
  }

  @Override
  public void addLearningClause(BooleanFormula[] fixed, BooleanFormula[] lhs,
                                BooleanFormula[] rhs, BooleanFormula constraint) {
    Native.propagateConflict(this, ctx, solver, javainfo, fixed.length, formulaArrayToLong(fixed)
        , 0, formulaArrayToLong(lhs), formulaArrayToLong(rhs), creator.extractInfo(constraint));
  }
  private long[] formulaArrayToLong(Formula[] formulaArray) {
    if (formulaArray == null)
          return new long[0];
    long[] formulaInfos = new long[formulaArray.length];
    for (int i = 0; i < formulaArray.length; i++) {
      if (formulaArray[i] != null)
        formulaInfos[i] = creator.extractInfo(formulaArray[i]);
    }
    return formulaInfos;
  }

  @Override
  public void notifyOnUserFunction() { registerCreated(); }

  @Override
  public void notifyOnVarAssign() { registerFixed(); }

  @Override
  public void notifyOnEquality() { registerEq(); }

  @Override
  public void notifyOnFullAssign() { registerFinal(); }

  // communication with
}
