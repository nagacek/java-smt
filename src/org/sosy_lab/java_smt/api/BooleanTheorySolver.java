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

package org.sosy_lab.java_smt.api;

public abstract class BooleanTheorySolver {

  private Backend backend;

  protected BooleanTheorySolver() {
    backend = null;
  }

  public final void injectBackend(Backend backend) {
    this.backend = backend;
  }

  public abstract void push();
  public abstract void pop(int num);
  public abstract void finish();
  public abstract void equality(BooleanFormula x, BooleanFormula y);
  //currently not supported
  //public abstract BooleanTheorySolver fresh(Backend newBackend);
  public abstract void created(BooleanFormula e);
  public abstract void fixed(BooleanFormula var, BooleanFormula val);

  public final void notifyOnUserFunction() { backend.notifyOnUserFunction(); }
  public final void notifyOnVarAssign() { backend.notifyOnVarAssign(); }
  public final void notifyOnEquality() { backend.notifyOnEquality();}
  public final void notifyOnFullAssign() { backend.notifyOnFullAssign(); }

  public final void addExpressionToWatch(BooleanFormula toAdd) {
    backend.addExpressionToWatch(toAdd);
  }

  public final void addConflict(BooleanFormula[] fixed) {
    backend.addConflict(fixed);
  }
  public final void addConflictEq(BooleanFormula[] fixed, BooleanFormula[] lhs,
                                 BooleanFormula[] rhs) {
    backend.addConflictEq(fixed, lhs, rhs);
  }

  public final void addTheoryLemma(BooleanFormula[] fixed, BooleanFormula conflict) {
    backend.addTheoryLemma(fixed, conflict);
  }

  public final void addLearningClause(BooleanFormula[] fixed, BooleanFormula[] lhs,
        BooleanFormula[] rhs, BooleanFormula constraint) {
    backend.addLearningClause(fixed, lhs, rhs, constraint);
  }


}
