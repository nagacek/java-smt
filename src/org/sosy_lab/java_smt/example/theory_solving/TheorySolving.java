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

import static com.google.common.base.Verify.verify;

import java.util.logging.Level;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.BooleanTheorySolver;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;

public class TheorySolving {
  private BooleanFormulaManager bfmgr;
  private final ProverEnvironment prover;
  private final SolverContext context;

  public static void main (String[] args) throws InvalidConfigurationException,
                                                 InterruptedException, SolverException {
    Configuration config = Configuration.defaultConfiguration();
    LogManager logger = BasicLogManager.create(config);
    ShutdownNotifier notifier = ShutdownNotifier.createDummy();

    try (SolverContext context = SolverContextFactory.createSolverContext(config, logger, notifier,
        Solvers.Z3);
        ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS,
            ProverOptions.GENERATE_ALL_SAT)) {
        TheorySolving example = new TheorySolving(context, prover);

        prover.push();
        var sat = example.solve(logger);
        logger.log(Level.INFO, "Result is " + sat);
        if (sat) {
          logger.log(Level.INFO, prover.getModel().asList());
        }
    }
    catch (InvalidConfigurationException | UnsatisfiedLinkError e) {
      logger.logUserException(Level.INFO, e, "Z3 is not available.");
    } catch (UnsupportedOperationException e) {
      logger.logUserException(Level.INFO, e, e.getMessage());
    }

  }

  private boolean solve(LogManager logger) throws InterruptedException, SolverException {
    bfmgr = context.getFormulaManager().getBooleanFormulaManager();
    BooleanFormula p = bfmgr.makeVariable("p");
    prover.addConstraint(p);

    BooleanTheorySolver theory = new InfoTheorySolver(logger, bfmgr);
    verify(prover.registerTheorySolver(theory));

    theory.addExpressionToWatch(p);
    theory.notifyOnVarAssign();
    theory.notifyOnEquality();
    theory.notifyOnFullAssign();
    theory.notifyOnUserFunction();


    return !prover.isUnsat();
  }

  public TheorySolving(SolverContext ctx, ProverEnvironment prov) {
    context = ctx;
    prover = prov;
  }
}
