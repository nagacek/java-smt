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
import java.util.Objects;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;

public class PartialPropagator extends CompletePropagator{
  private final BooleanFormula[][] symbols;
  private final HashMap<BooleanFormula, Pair<Integer, Integer>> symbolToCoordinates;
  private final BooleanFormulaManager bmgr;
  public PartialPropagator(BooleanFormula[][] symbols, BooleanFormulaManager bmgr) {
    super();
    this.symbols = symbols;
    this.bmgr = bmgr;
    symbolToCoordinates = new HashMap<>();
    fillCoordinateMap();
  }

  private void fillCoordinateMap() {
    for (int i = 0; i < symbols[0].length; i++) {
      for (int j = 0; j < symbols[0].length; j++) {
        symbolToCoordinates.put(symbols[i][j], new Pair<>(i, j));
      }
    }
  }

  @Override
  public void onKnownValue(BooleanFormula var, BooleanFormula value) {
    if (bmgr.isTrue(value)) {
      Pair<Integer, Integer> coordinates = symbolToCoordinates.get(var);
      for (BooleanFormula other : fixedValues) {
        BooleanFormula otherValue = currentModel.get(other);
        if (bmgr.isTrue(otherValue)) {
          Pair<Integer, Integer> otherCoordinates = symbolToCoordinates.get(other);
          if (Objects.equals(coordinates.first, otherCoordinates.first) || Objects.equals(
              coordinates.second, otherCoordinates.second)) {
            backend.propagateConflict(new BooleanFormula[]{var, other});
            continue;
          }
          int diffx = Math.abs(coordinates.first - otherCoordinates.first);
          int diffy = Math.abs(coordinates.second - otherCoordinates.second);
          if (diffx == diffy) {
            backend.propagateConflict(new BooleanFormula[]{var, other});
          }
        }
      }
    }

    fixedValues.add(var);
    currentModel.put(var, value);
  }

}
