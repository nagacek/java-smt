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

package org.sosy_lab.java_smt.example.theory_solving_nqueens;

import io.github.cvc5.Pair;
import java.util.HashMap;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;

public class PartialObservantPropagator extends NQueensPropagator{
  private final BooleanFormulaManager bmgr;
  public PartialObservantPropagator(BooleanFormulaManager bmgr) {
    super();
    this.bmgr = bmgr;
  }

  @Override
  public void fixed(BooleanFormula var, BooleanFormula value) {

  }
  @Override
  public void push() {
  }

  @Override
  public void pop(int num) {
  }

  @Override
  public void finish() {
  }

  @Override
  public void equality(BooleanFormula x, BooleanFormula y) {

  }

  @Override
  public void created(BooleanFormula e) {

  }

}
