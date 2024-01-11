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

package org.sosy_lab.java_smt.example.theory_solving;

import java.util.logging.Level;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.basicimpl.AbstractUserPropagator;

public class InfoTheorySolver extends AbstractUserPropagator {
  LogManager logger;
  BooleanFormulaManager manager;
  BooleanFormula q;

  public InfoTheorySolver(LogManager logger, BooleanFormulaManager manager, BooleanFormula q) {
    this.logger = logger;
    this.manager = manager;
    this.q = q;
  }

  @Override
  public void onPush() {
    logger.log(Level.INFO, "PUSH performed by solver");
  }

  @Override
  public void onPop(int num) {
    logger.log(Level.INFO, "POP performed by solver");
  }

  @Override
  public void onFinalCheck() {
    logger.log(Level.INFO, "Solver is finished");
  }

  @Override
  public void onEquality(BooleanFormula x, BooleanFormula y) {
    logger.log(Level.INFO, "Formulas " + x + " and " + y + " are equal from"
        + " now on");
  }

  /*
  @Override
  public void created(BooleanFormula e) {
    logger.log(Level.WARNING, "There should be no user functions");
  }
   */

  @Override
  public void onKnownValue(BooleanFormula var, BooleanFormula val) {
    logger.log(Level.INFO, "Formula " + var + " is set to be " + val);
    if (val.equals(manager.makeTrue()) && var.equals(q)) {
      logger.log(Level.INFO, "-> Creating conflict on formula " + var);
      BooleanFormula[] fixed = new BooleanFormula[1];
      fixed[0] = var;
      backend.propagateConflict(fixed);
    }
  }

  @Override
  public void initialize() {
    backend.notifyOnEquality();
    backend.notifyOnKnownValue();
    //backend.notifyOnUserFunction();
    backend.notifyOnFinalCheck();
  }

  private Formula[] reverse (BooleanFormula[] eq) {
    BooleanFormula[] ret_val = new BooleanFormula[eq.length];
    for (int i = 0; i < eq.length; i++) {
      ret_val[i] = eq[eq.length - i - 1];
    }
    return ret_val;
  }
}
