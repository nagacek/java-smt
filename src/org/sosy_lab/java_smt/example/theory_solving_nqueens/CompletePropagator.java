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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.sosy_lab.java_smt.api.BooleanFormula;

public class CompletePropagator extends NQueensPropagator {
  protected final Stack<Integer> fixedCount;
  protected final List<BooleanFormula> fixedValues;
  protected final HashMap<BooleanFormula, BooleanFormula> currentModel;
  protected final Set<HashMap<BooleanFormula, BooleanFormula>> modelSet;

  public CompletePropagator() {
    fixedCount = new Stack<>();
    fixedValues = new ArrayList<>();
    currentModel = new HashMap<>();
    modelSet = new HashSet<>();
    initNum();
  }


  @Override
  public void push() {
    fixedCount.push(fixedValues.size());
  }

  @Override
  public void pop(int num) {
    for (int i = 0; i < num; i++) {
      int lastCount = fixedCount.pop();
      for (int size = fixedValues.size(); size > lastCount; size--) {
        currentModel.remove(fixedValues.remove(size - 1));
      }
    }
  }

  @Override
  public void finish() {
    addConflict(fixedValues.toArray(new BooleanFormula[0]));
    if (modelSet.add(currentModel)) {
      incrementNum();
    }
  }

  @Override
  public void equality(BooleanFormula x, BooleanFormula y) {

  }

  @Override
  public void created(BooleanFormula e) {

  }

  @Override
  public void fixed(BooleanFormula var, BooleanFormula val) {
    fixedValues.add(var);
    currentModel.put(var, val);
  }
}
