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

public interface Backend {

  void addExpressionToWatch(BooleanFormula toAdd);

  void addConflict(BooleanFormula[] fixed);

  void addConflictEq(
      BooleanFormula[] fixed, BooleanFormula[] lhs,
      BooleanFormula[] rhs);

  void addTheoryLemma(BooleanFormula[] fixed, BooleanFormula conflict);

  void addLearningClause(
      BooleanFormula[] fixed, BooleanFormula[] lhs,
      BooleanFormula[] rhs, BooleanFormula constraint);

  void notifyOnUserFunction();
  void notifyOnVarAssign();
  void notifyOnEquality();
  void notifyOnFullAssign();
}
